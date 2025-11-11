package fr.nexus;

import fr.nexus.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class ThreadPool implements Executor {
    private static final Logger logger=new Logger(Core.getInstance(),ThreadPool.class);
    private static final long PARK_NANOS = 200_000L;

    private volatile boolean shutdown = false;

    private final Thread[] threads;
    private final MpmcQueue<Runnable> queue;
    private final ConcurrentQueue parkQueue = new ConcurrentQueue();
    private final String prefix;

    public ThreadPool(int workerThreads, int queueCapacity, String prefix, int priority) {
        this.prefix=prefix;
        this.threads = new Thread[workerThreads + 1]; // +1 = dispatcher
        this.queue = new MpmcQueue<>(queueCapacity);

        // Dispatcher
        Thread dispatcher = new Thread(new Dispatcher(this), prefix + " Dispatcher");
        dispatcher.setDaemon(false);
        dispatcher.setPriority(Math.min(Thread.MAX_PRIORITY, priority + 1));
        dispatcher.start();
        this.threads[0] = dispatcher;

        // Workers
        for (int i = 1; i < threads.length; i++) {
            Thread t = new Thread(new Worker(this), prefix + " Worker - " + i);
            t.setDaemon(false);
            t.setPriority(Math.max(Thread.MIN_PRIORITY, priority));
            t.start();
            threads[i] = t;
        }
    }

    public String getPrefix(){
        return this.prefix;
    }

    @Override
    public void execute(@NotNull Runnable task) {
        if (shutdown || !queue.offer(task)) {
            // fallback direct run
            try {
                task.run();
            } catch (Throwable t) {
                logger.severe("execute issue ("+getPrefix()+"): {}",t.getMessage());
            }
            return;
        }
        Thread parker = parkQueue.poll();
        if (parker != null) LockSupport.unpark(parker);
    }

    public <V> Future<V> submit(Callable<V> callable) {
        FutureTask<V> ft = new FutureTask<>(callable);
        execute(ft);
        return ft;
    }

    public <V> Future<V> submit(Runnable runnable, V result) {
        FutureTask<V> ft = new FutureTask<>(runnable, result);
        execute(ft);
        return ft;
    }

    public void shutdown() {
        shutdown = true;
        for (Thread t : threads) {
            if (t != null) LockSupport.unpark(t);
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        final long nanos = unit.toNanos(timeout);
        final long start = System.nanoTime();
        boolean allJoined = true;
        for (Thread t : threads) {
            if (t == null) continue;
            long elapsed = System.nanoTime() - start;
            long rem = nanos - elapsed;
            if (rem <= 0) {
                t.join();
            } else {
                long millis = TimeUnit.NANOSECONDS.toMillis(rem);
                int nanosPart = (int) (rem - TimeUnit.MILLISECONDS.toNanos(millis));
                t.join(millis, nanosPart);
            }
            if (t.isAlive()) {
                allJoined = false;
                break;
            }
        }

        Runnable r;
        while ((r = queue.poll()) != null) {
            try {
                r.run();
            } catch (Throwable e) {
                logger.severe("awaitTermination issue ("+getPrefix()+"): {}",e.getMessage());
            }
        }
        return allJoined;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    // ---------------- Worker ----------------
    private record Worker(ThreadPool pool) implements Runnable {
        @Override
        public void run() {
            final Thread current = Thread.currentThread();
            while (true) {
                Runnable task = pool.queue.poll();
                if (task != null) {
                    try {
                        task.run();
                    } catch (Throwable t) {
                        logger.severe("worker issue ("+pool.getPrefix()+"): {}",t.getMessage());
                    }
                    continue;
                }

                if (pool.shutdown) break;
                pool.parkQueue.offer(current);
                LockSupport.park();
                if (Thread.interrupted() && pool.shutdown) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // ---------------- Dispatcher ----------------
    private record Dispatcher(ThreadPool pool) implements Runnable {
        @Override
        public void run() {
            int backoff = 0;
            while (true) {
                int qSize = pool.queue.size();
                int parked = pool.parkQueue.size();

                if (qSize > 0 && parked > 0) {
                    backoff = 0;
                    Thread toWake = pool.parkQueue.poll();
                    if (toWake != null) LockSupport.unpark(toWake);
                } else if (pool.shutdown) {
                    break;
                } else if (backoff < 8) {
                    backoff++;
                    Thread.yield();
                } else {
                    LockSupport.parkNanos(PARK_NANOS);
                    if (Thread.interrupted() && pool.shutdown) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            Thread leftover;
            while ((leftover = pool.parkQueue.poll()) != null) {
                LockSupport.unpark(leftover);
            }
        }
    }

    // ---------------- Lock-free MPMC Queue ----------------
    private static final class MpmcQueue<E> {
        private final AtomicReferenceArray<E> buffer;
        private final int mask;
        private final AtomicLong head = new AtomicLong();
        private final AtomicLong tail = new AtomicLong();

        MpmcQueue(int capacity) {
            int size = 1;
            while (size < capacity) size <<= 1;
            this.buffer = new AtomicReferenceArray<>(size);
            this.mask = size - 1;
        }

        boolean offer(E e) {
            if (e == null) throw new NullPointerException();
            while (true) {
                long t = tail.get();
                long h = head.get();
                if (t - h >= buffer.length()) return false; // full
                int index = (int) (t & mask);
                if (buffer.get(index) != null) continue;
                if (tail.compareAndSet(t, t + 1)) {
                    buffer.lazySet(index, e);
                    return true;
                }
            }
        }

        E poll() {
            while (true) {
                long h = head.get();
                int index = (int) (h & mask);
                E e = buffer.get(index);
                if (e == null) return null;
                if (head.compareAndSet(h, h + 1)) {
                    buffer.lazySet(index, null);
                    return e;
                }
            }
        }

        int size() {
            long h = head.get();
            long t = tail.get();
            return (int) (t - h);
        }
    }

    // ---------------- Minimal concurrent queue (for parked threads) ----------------
    private static final class ConcurrentQueue {
        private static final class Node {
            final Thread thread;
            Node next;
            Node(Thread thread) { this.thread = thread; }
        }

        private volatile Node head;
        private volatile Node tail;

        ConcurrentQueue() {
            head = tail = new Node(null);
        }

        void offer(Thread t) {
            Node node = new Node(t);
            Node prev;
            synchronized (this) {
                prev = tail;
                tail = node;
                prev.next = node;
            }
        }

        Thread poll() {
            synchronized (this) {
                Node first = head.next;
                if (first == null) return null;
                head = first;
                return first.thread;
            }
        }

        int size() {
            int count = 0;
            Node n = head.next;
            while (n != null) {
                count++;
                n = n.next;
            }
            return count;
        }
    }
}