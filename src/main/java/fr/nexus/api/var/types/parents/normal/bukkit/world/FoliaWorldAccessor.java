//package fr.nexus.api.var.types.parents.normal.bukkit.world;
//
//import org.bukkit.Bukkit;
//import org.bukkit.World;
//
//import java.lang.reflect.Method;
//import java.util.concurrent.Callable;
//import java.util.concurrent.Future;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class FoliaWorldAccessor implements WorldAccessor {
//    private final Object scheduler;
//    private final Method executeSyncMethod;
//
//    public FoliaWorldAccessor() {
//        try {
//            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.Scheduler");
//            // récupérer le scheduler via Bukkit
//            Method getScheduler = Bukkit.getServer().getClass().getMethod("getScheduler");
//            this.scheduler = getScheduler.invoke(Bukkit.getServer());
//
//            // méthode executeSync
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
//    public World getWorld(String name) {
//        return runSync(() -> Bukkit.getWorld(name));
//    }
//
//    @Override
//    public String getName(World world) {
//        return runSync(world::getName);
//    }
//}
