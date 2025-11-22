//package fr.nexus.api.var.types.parents.normal.bukkit.inventory;
//
//import fr.nexus.api.var.types.VarTypes;
//import org.bukkit.Bukkit;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//
//import java.lang.reflect.Method;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//import java.util.concurrent.Callable;
//import java.util.concurrent.Future;
//
//@SuppressWarnings({"unused", "UnusedReturnValue"})
//public class FoliaInventoryAccessor implements InventoryAccessor {
//    private final Object scheduler;
//    private final Method executeSyncMethod;
//    private static final ItemStack AIR_ITEM_STACK = new ItemStack(org.bukkit.Material.AIR);
//
//    public FoliaInventoryAccessor() {
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
//    public byte[] serializeSync(Inventory inv) {
//        return runSync(() -> {
//            ItemStack[] contents = inv.getContents();
//            Arrays.setAll(contents, i -> contents[i] != null ? contents[i] : AIR_ITEM_STACK);
//            byte[] itemBytes = VarTypes.ITEMSTACK_ARRAY.serializeSync(contents);
//            byte[] typeBytes = inv.getType().name().getBytes();
//            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + typeBytes.length + itemBytes.length);
//            buffer.putInt(typeBytes.length);
//            buffer.put(typeBytes);
//            buffer.put(itemBytes);
//            return buffer.array();
//        });
//    }
//
//    @Override
//    public Inventory deserializeSync(byte[] bytes) {
//        return runSync(() -> {
//            ByteBuffer buffer = ByteBuffer.wrap(bytes);
//            byte[] typeBytes = new byte[buffer.getInt()];
//            buffer.get(typeBytes);
//            String typeName = new String(typeBytes);
//            byte[] itemBytes = new byte[buffer.remaining()];
//            buffer.get(itemBytes);
//
//            Inventory inv = Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.valueOf(typeName));
//            ItemStack[] contents = VarTypes.ITEMSTACK_ARRAY.deserializeSync(itemBytes);
//            if (contents != null) inv.setContents(contents);
//            return inv;
//        });
//    }
//}