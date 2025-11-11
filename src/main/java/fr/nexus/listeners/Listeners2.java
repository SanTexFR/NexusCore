//package fr.nexus.listeners;
//
//import fr.nexus.Core;
//import fr.nexus.listeners.events.CoreEvent;
//import org.bukkit.Bukkit;
//import org.bukkit.event.Cancellable;
//import org.bukkit.event.Event;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.plugin.PluginManager;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//@SuppressWarnings({"unchecked", "rawtypes"})
//public class Listeners implements Listener {
//    private static final ConcurrentHashMap<String, List<ConsumerWrapper<?, ?>>> eventsRegistered = new ConcurrentHashMap<>();
//    private static final ConcurrentHashMap<Class<?>, AsyncEventProcessor> processors = new ConcurrentHashMap<>();
//
//    private record ConsumerWrapper<E, R>(Function<E, R> asyncConsumer,BiConsumer<E, R> syncConsumer,BiConsumer<E, R> postResultSync,Function<E, Object> keyExtractor) {
//        void execute(E event) {
//            Object key = keyExtractor != null ? keyExtractor.apply(event) : null;
//            AsyncEventProcessor processor = getProcessorForEvent(event.getClass());
//
//            if (key != null) processor.submitWithKey(key, this, event);
//            else processor.submit(this, event);
//        }
//    }
//
//    private static class AsyncEventProcessor<E, R> {
//
//        private final Map<Object, Queue<Pair<ConsumerWrapper<E, R>, E>>> queues = new ConcurrentHashMap<>();
//        private final Map<Object, Boolean> runningFlags = new ConcurrentHashMap<>();
//
//        void submitWithKey(Object key, ConsumerWrapper<E, R> wrapper, E event) {
//            if (key == null) {
//                Core.IO_EXECUTOR.submit(() -> execute(wrapper, event));
//                return;
//            }
//
//            queues.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(new Pair<>(wrapper, event));
//            processNext(key);
//        }
//
//        void submit(ConsumerWrapper<E, R> wrapper, E event) {
//            Core.IO_EXECUTOR.submit(() -> execute(wrapper, event));
//        }
//
//        private void processNext(Object key) {
//            // Vérifie si la clé est déjà en cours
//            if (runningFlags.putIfAbsent(key, true) != null) return;
//
//            Core.IO_EXECUTOR.submit(() -> {
//                Queue<Pair<ConsumerWrapper<E, R>, E>> queue = queues.get(key);
//                if (queue == null) {
//                    runningFlags.remove(key);
//                    return;
//                }
//
//                Pair<ConsumerWrapper<E, R>, E> pair = queue.poll();
//                if (pair == null) {
//                    runningFlags.remove(key);
//                    queues.remove(key);
//                    return;
//                }
//
//                R result = null;
//                if (pair.first.asyncConsumer != null) result = pair.first.asyncConsumer.apply(pair.second);
//                R finalResult = result;
//
//                runSync(() -> {
//                    if (pair.first.syncConsumer != null)
//                        pair.first.syncConsumer.accept(pair.second, finalResult);
//                });
//
//                // On supprime le drapeau "en cours" après traitement et on relance si queue non vide
//                Core.IO_EXECUTOR.submit(() -> {
//                    if (!queue.isEmpty()) processNext(key);
//                    else runningFlags.remove(key);
//                });
//            });
//        }
//
//        private void execute(ConsumerWrapper<E, R> wrapper, E event) {
//            R result = null;
//            if (wrapper.asyncConsumer != null) result = wrapper.asyncConsumer.apply(event);
//
//            R finalResult = result;
//            runSync(() -> {
//                if (wrapper.syncConsumer != null) wrapper.syncConsumer.accept(event, finalResult);
//            });
//        }
//    }
//
//    private static AsyncEventProcessor getProcessorForEvent(Class<?> clazz) {
//        return processors.computeIfAbsent(clazz, k -> new AsyncEventProcessor<>());
//    }
//
//    public static<E extends@NotNull Event>void register(@NotNull Class<E> eventClass,@NotNull Consumer<E> consumer) {
//        register(eventClass,consumer,EventPriority.NORMAL);
//    }
//    public static<E extends@NotNull Event>void register(@NotNull Class<E>eventClass,@NotNull Consumer<E>consumer,@NotNull EventPriority eventPriority) {
//        String key = buildKey(eventClass, eventPriority);
//
//        ConsumerWrapper<E, Object> wrapper = new ConsumerWrapper<>(null,null, (e, r) -> consumer.accept(e),null);
//        eventsRegistered.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
//                .add(wrapper);
//
//        if (!processors.containsKey(eventClass)) {
//            processors.put(eventClass, new AsyncEventProcessor<>());
//
//            PluginManager pm = Core.getInstance().getServer().getPluginManager();
//            pm.registerEvent(eventClass, Core.getListeners(), eventPriority, (listener, event) -> {
//                if (!eventClass.isInstance(event)) return;
//
//                List<ConsumerWrapper<?, ?>> consumers = eventsRegistered.get(key);
//                if (consumers == null) return;
//
//                for (ConsumerWrapper<?, ?> c : consumers) {
//                    ((ConsumerWrapper<E, Object>) c).execute(eventClass.cast(event));
//                }
//            }, Core.getInstance(), false);
//        }
//    }
//
//    private static final Set<Class<?>>registeredCoreBridges=ConcurrentHashMap.newKeySet();
//    public static <B extends Event, C extends CoreEvent<B>> void registerCoreBridge(@NotNull Class<B> bukkitEventClass,@NotNull Function<B, C> coreEventConstructor) {
//        if (registeredCoreBridges.contains(bukkitEventClass)) return; // évite double enregistrement Bukkit
//        registeredCoreBridges.add(bukkitEventClass);
//
//        PluginManager pm = Core.getInstance().getServer().getPluginManager();
//        pm.registerEvent(bukkitEventClass, Core.getListeners(), EventPriority.HIGHEST, (listener, event) -> {
//            if (!bukkitEventClass.isInstance(event)) return;
//            B bukkitEvent = bukkitEventClass.cast(event);
//
//            if (bukkitEvent instanceof Cancellable cancellable) {
//                cancellable.setCancelled(true);
//            }
//
//            // Appelle tous les CoreEvent associés
//            callCoreEvent(coreEventConstructor.apply(bukkitEvent));
//        }, Core.getInstance(), false);
//    }
//    public static <E extends CoreEvent<?>> void registerCoreEvent(@NotNull Class<E> coreEventClass, @NotNull Function<E, ?> asyncConsumer, @Nullable BiConsumer<E, ?> syncConsumer,@Nullable BiConsumer<E, ?> postResultSync) {
//        String key = coreEventClass.getName();
//        ConsumerWrapper<E, Object> wrapper = new ConsumerWrapper(asyncConsumer, syncConsumer, postResultSync, null);
//        eventsRegistered.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>())).add(wrapper);
//    }
//
//    public static void callCoreEvent(CoreEvent<?> coreEvent) {
//        List<ConsumerWrapper<?, ?>> consumers = eventsRegistered.get(coreEvent.getClass().getName());
//        if (consumers == null || consumers.isEmpty()) return;
//
//        Object key = coreEvent.getKey();
//        AsyncEventProcessor<CoreEvent<?>, Object> processor = getProcessorForEvent(coreEvent.getClass());
//
//        ConsumerWrapper<CoreEvent<?>, Object> wrapper = new ConsumerWrapper<>(
//                event -> {
//                    Object result = null;
//                    for (ConsumerWrapper<?, ?> rawWrapper : consumers) {
//                        ConsumerWrapper<CoreEvent<?>, Object> w = (ConsumerWrapper<CoreEvent<?>, Object>) rawWrapper;
//                        if (w.asyncConsumer != null) {
//                            result = w.asyncConsumer.apply(coreEvent);
//                        }
//                        Object finalResult = result;
//                        runSync(() -> {
//                            if (w.syncConsumer != null) w.syncConsumer.accept(coreEvent, finalResult);
//                        });
//
//                        if (coreEvent.isAsyncCancelled()) break;
//                    }
//                    return result;
//                },
//                (event, result) -> {
//                    if (coreEvent.isAsyncCancelled()) return;
//
//                    runSync(() -> {
//                        boolean applied = coreEvent.applyResult();
//
//                        for (ConsumerWrapper<?, ?> rawWrapper : consumers) {
//                            ConsumerWrapper<CoreEvent<?>, Object> w = (ConsumerWrapper<CoreEvent<?>, Object>) rawWrapper;
//                            if (applied && w.postResultSync != null) {
//                                w.postResultSync.accept(coreEvent, result);
//                            }
//                        }
//                    });
//                },
//                null,
//                e -> key
//        );
//
//
//        processor.submitWithKey(key, wrapper, coreEvent);
//    }
//
//
//
//
//    // ===========================
//    // Autres méthodes
//    // ===========================
//    private static @NotNull String buildKey(@NotNull Class<?> eventClass, EventPriority priority) {
//        return eventClass.getName() + "/" + priority.name();
//    }
//
//    private static void runSync(Runnable runnable) {
//        if (Bukkit.isPrimaryThread()) runnable.run();
//        else Bukkit.getScheduler().runTask(Core.getInstance(), runnable);
//    }
//
//    private static class Pair<F, S> {
//        final F first;
//        final S second;
//        Pair(F f, S s) { first = f; second = s; }
//    }
//}