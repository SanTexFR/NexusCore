package fr.nexus.api.var.types.parents;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarVersion;
import fr.nexus.utils.CollectionUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.PriorityBlockingQueue;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked",})
public abstract class VarType<T>extends VarVersion implements VarSubType<T>,Vars,CollectionUtils{
    //VARIABLES (STATICS)
    private static final@NotNull Map<@NotNull String,@NotNull VarSubType<?>>types=new HashMap<>();

    //VARIABLES (INSTANCES)
    private@NotNull Plugin handler;
    private@NotNull Class<@NotNull T>typeClazz;

    //CONSTRUCTOR
    protected VarType(@NotNull Plugin handler){
        super(1);
        init(handler,(Class<T>)((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0]);
    }
    protected VarType(@NotNull Plugin handler,int version){
        super(version);
        init(handler,(Class<T>)((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0]);
    }
    protected VarType(@NotNull Plugin handler,@NotNull Class<@NotNull T>typeClazz,int version){
        super(version);
        init(handler,typeClazz);
    }
    private void init(@NotNull Plugin handler,@NotNull Class<@NotNull T>typeClazz){
        this.handler=handler;
        this.typeClazz=typeClazz;

        types.put(getStringType(),this);

        final VarSubType<?>list=lists();
        types.put(list.getStringType(),list);

        final VarSubType<?>arrays=arrays();
        types.put(arrays.getStringType(),arrays);

        final VarSubType<?>sets=sets();
        types.put(sets.getStringType(),sets);

        final VarSubType<?>linked_sets=linked_sets();
        types.put(linked_sets.getStringType(),linked_sets);
    }


    //METHODS (STATICS)
    public static@NotNull Map<@NotNull String,@NotNull VarSubType<?>>getTypes(){
        return types;
    }

    //METHODS (INSTANCES)

    //HANDLER
    public@NotNull Plugin getHandler(){
        return this.handler;
    }

    //CLAZZ
    public@NotNull Class<@NotNull T>getTypeClazz(){
        return this.typeClazz;
    }

    //TYPE
    public boolean isWrapper(){
        return true;
    }

    //RAW TYPE
    public@NotNull String getStringType(){
        return"Normal<"+getRawStringType()+">";
    }

    private@NotNull String getRawStringType(){
        return this.handler.getName()+"¦"+this.typeClazz.getSimpleName();
    }

    //SERIALIZATION-SYNC
    public abstract byte@NotNull[]serializeSync(@NotNull T obj);
    public@NotNull T deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder value=readVersionAndRemainder(bytes);
        return deserializeSync(value.version(),value.remainder());
    }
    public abstract@NotNull T deserializeSync(int version,byte[]bytes);

    //SERIALIZATION-ASYNC
    public@NotNull CompletableFuture<byte[]>serializeAsync(@NotNull T obj){
        try {
            byte[] result = serializeSync(obj);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<byte[]> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
    public@NotNull CompletableFuture<@Nullable T>deserializeAsync(byte@NotNull[]bytes){
        try {
            T result = deserializeSync(bytes);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }



    //INNER
    private final class CollectionVarType<C extends Collection<T>> implements VarSubType<C> {
        private final@NotNull String prefix;
        private final@NotNull java.util.function.Supplier<@NotNull C>supplier;

        public CollectionVarType(@NotNull String prefix,@NotNull java.util.function.Supplier<@NotNull C>supplier){
            this.prefix=prefix;
            this.supplier=supplier;
        }

        public@NotNull String getStringType(){
            return this.prefix+"<"+getRawStringType()+">";
        }
        public byte@NotNull[]serializeSync(@NotNull C coll){
            return serializeCollection(coll,VarType.this::serializeSync);
        }
        public C deserializeSync(byte@NotNull[]bytes){
            return(C)deserializeCollection(bytes,this.supplier.get(),VarType.this::deserializeSync);
        }
        public@NotNull CompletableFuture<byte[]>serializeAsync(@NotNull C coll) {
            return serializeCollectionAsync(coll,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<C>deserializeAsync(byte@NotNull[]bytes){
            return deserializeCollectionAsync(bytes,this.supplier.get(),VarType.this::deserializeAsync)
                    .thenApply(res->(C)res);
        }
    }

    //COLLECTIONS
    public @NotNull VarSubType<List<T>>lists(){
        return new CollectionVarType<>("List",ArrayList::new);
    }
    public @NotNull VarSubType<Set<T>>sets(){
        return new CollectionVarType<>("Set",HashSet::new);
    }
    public @NotNull VarSubType<LinkedHashSet<T>>linked_sets() {
        return new CollectionVarType<>("LinkedSet",LinkedHashSet::new);
    }
    public @NotNull VarSubType<TreeSet<T>>tree_sets(){
        return new CollectionVarType<>("TreeSet",TreeSet::new);
    }
    public @NotNull VarSubType<ConcurrentSkipListSet<T>>concurrent_skip_list_sets(){
        return new CollectionVarType<>("ConcurrentSkipListSet",ConcurrentSkipListSet::new);
    }
    public @NotNull VarSubType<ConcurrentHashMap.KeySetView<T,Boolean>>concurrent_sets() {
        return new CollectionVarType<>("ConcurrentSet",ConcurrentHashMap::newKeySet);
    }
    public @NotNull VarSubType<PriorityQueue<T>>priority_queues(){
        return new CollectionVarType<>("PriorityQueue",PriorityQueue::new);
    }
    public @NotNull VarSubType<PriorityBlockingQueue<T>>priority_blocking_queues(){
        return new CollectionVarType<>("PriorityBlockingQueue",PriorityBlockingQueue::new);
    }
    public @NotNull VarSubType<ArrayDeque<T>>array_deques(){
        return new CollectionVarType<>("ArrayDeque",ArrayDeque::new);
    }
    public @NotNull VarSubType<Stack<T>>stacks(){
        return new CollectionVarType<>("Stack",Stack::new);
    }

    //ARRAY
    public @NotNull ArrayType arrays() {
        return new ArrayType();
    }

    public final class ArrayType implements VarSubType<T[]>{
        public @NotNull String getStringType(){
            return "Array<"+getRawStringType()+">";
        }

        public byte@NotNull[]serializeSync(@NotNull T@NotNull[]array) {
            return VarType.this.lists().serializeSync(Arrays.asList(array));
        }

        public@NotNull T[]deserializeSync(byte @NotNull[]bytes){
            final List<T>list=VarType.this.lists().deserializeSync(bytes);
            return list==null?null:list.toArray((T[])Array.newInstance(getTypeClazz(),list.size()));
        }

        public@NotNull CompletableFuture<byte[]>serializeAsync(@NotNull T@NotNull[]array){
            return VarType.this.lists().serializeAsync(Arrays.asList(array));
        }

        public@NotNull CompletableFuture<T[]>deserializeAsync(byte@NotNull[]bytes){
            return VarType.this.lists().deserializeAsync(bytes).thenApply(list->
                    list==null?null:list.toArray((T[])Array.newInstance(getTypeClazz(),list.size()))
            );
        }
    }
}