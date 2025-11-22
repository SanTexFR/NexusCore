//package fr.nexus.api.var.types.parents.normal.bukkit.location;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//
//import java.lang.reflect.Method;
//import java.util.concurrent.Callable;
//import java.util.concurrent.Future;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class FoliaLocationAccessor implements LocationAccessor {
//    private final Object scheduler;
//    private final Method executeSyncMethod;
//
//    public FoliaLocationAccessor() {
//        try {
//            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.Scheduler");
//            Method getScheduler = Bukkit.getServer().getClass().getMethod("getScheduler");
//            this.scheduler = getScheduler.invoke(Bukkit.getServer());
//            this.executeSyncMethod = schedulerClass.getMethod("executeSync", Callable.class);
//        } catch (Exception e) {
//            throw new RuntimeException("Folia classes not found", e);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private <T> T runSync(Callable<T> task) {
//        try {
//            Future<T> future = (Future<T>) executeSyncMethod.invoke(scheduler, task);
//            return future.get();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public Location safeCopy(Location loc) {
//        return runSync(loc::clone);
//    }
//}