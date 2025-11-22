//package fr.nexus.api.var.types.parents.normal.bukkit.itemstack;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//
//import java.lang.reflect.Method;
//import java.util.concurrent.Callable;
//import java.util.concurrent.Future;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class FoliaItemStackAccessor implements ItemStackAccessor {
//    private static final @NotNull ItemStack AIR_ITEM_STACK=new ItemStack(Material.AIR);
//    private final Object scheduler;
//    private final Method executeSyncMethod;
//
//    public FoliaItemStackAccessor() {
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
//    public byte[] serializeSync(ItemStack item) {
//        return runSync(() -> {
//            if(item == null || item.getType().isAir()) return new byte[]{(byte)0xff};
//            return item.serializeAsBytes();
//        });
//    }
//
//    @Override
//    public ItemStack deserializeSync(byte[] bytes) {
//        return runSync(() -> {
//            if(bytes.length == 0 || bytes[0] == (byte)0xff) return AIR_ITEM_STACK;
//            return ItemStack.deserializeBytes(bytes);
//        });
//    }
//}