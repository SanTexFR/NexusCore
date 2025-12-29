package fr.nexus.api.var.types.parents.normal;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarVersion;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.utils.CollectionUtils;
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
    private final@NotNull Class<@NotNull T>typeClazz;

    //CONSTRUCTOR
    protected VarType(){
        super(1);
        this.typeClazz=(Class<T>)((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];

        init();
    }
    protected VarType(int version){
        super(version);
        this.typeClazz=(Class<T>)((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];

        init();
    }
    protected VarType(@NotNull Class<@NotNull T>typeClazz,int version){
        super(version);
        this.typeClazz=typeClazz;

        init();
    }
    private void init(){
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
        return"Normal<"+this.typeClazz.getName()+">";
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




    //INNER CONCURRENT-SKIP-LIST-SET
    public@NotNull ConcurrentSkipListSetType concurrent_skip_list_sets(){
        return new ConcurrentSkipListSetType();
    }
    public final class ConcurrentSkipListSetType implements VarSubType<ConcurrentSkipListSet<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"ConcurrentSkipListSet<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull ConcurrentSkipListSet<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable ConcurrentSkipListSet<T> deserializeSync(byte@NotNull[]bytes){
            return(ConcurrentSkipListSet<T>)deserializeCollection(bytes,new ConcurrentSkipListSet<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull ConcurrentSkipListSet<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable ConcurrentSkipListSet<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new ConcurrentSkipListSet<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(ConcurrentSkipListSet<T>)result);
        }
    }

    //INNER CONCURRENT-SET
    public@NotNull ConcurrentSetType concurrent_sets(){
        return new ConcurrentSetType();
    }
    public final class ConcurrentSetType implements VarSubType<ConcurrentHashMap.KeySetView<T,Boolean>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"ConcurrentSet<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull ConcurrentHashMap.KeySetView<@NotNull T,Boolean>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable ConcurrentHashMap.KeySetView<@NotNull T,Boolean>deserializeSync(byte@NotNull[]bytes){
            return(ConcurrentHashMap.KeySetView<@NotNull T,Boolean>)deserializeCollection(bytes,ConcurrentHashMap.newKeySet(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull ConcurrentHashMap.KeySetView<@NotNull T,Boolean>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<ConcurrentHashMap.KeySetView<@Nullable T,Boolean>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,ConcurrentHashMap.newKeySet(),VarType.this::deserializeAsync)
                    .thenApply(result->(ConcurrentHashMap.KeySetView<T,Boolean>)result);
        }
    }

    //INNER LINKED-SET
    public@NotNull LinkedSetType linked_sets(){
        return new LinkedSetType();
    }
    public final class LinkedSetType implements VarSubType<LinkedHashSet<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"LinkedSet<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull LinkedHashSet<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable LinkedHashSet<T> deserializeSync(byte@NotNull[]bytes){
            return(LinkedHashSet<T>)deserializeCollection(bytes,new LinkedHashSet<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull LinkedHashSet<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable LinkedHashSet<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new HashSet<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(LinkedHashSet<T>)result);
        }
    }

    //INNER TREE-SET
    public@NotNull TreeSetType tree_sets(){
        return new TreeSetType();
    }
    public final class TreeSetType implements VarSubType<TreeSet<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"TreeSet<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull TreeSet<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable TreeSet<T>deserializeSync(byte@NotNull[]bytes){
            return(TreeSet<T>)deserializeCollection(bytes,new TreeSet<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull TreeSet<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable TreeSet<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new TreeSet<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(TreeSet<T>)result);
        }
    }


    //INNER SET
    public@NotNull SetType sets(){
        return new SetType();
    }
    public final class SetType implements VarSubType<Set<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"Set<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull Set<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable Set<T> deserializeSync(byte@NotNull[]bytes){
            return(Set<T>)deserializeCollection(bytes,new HashSet<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull Set<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable Set<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new HashSet<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(Set<T>)result);
        }
    }

    //INNER LIST
    public@NotNull ListType lists(){
        return new ListType();
    }
    public final class ListType implements VarSubType<List<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"List<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull List<T>list){
            return serializeCollection(list,VarType.this::serializeSync);
        }
        public@Nullable List<T> deserializeSync(byte@NotNull[]bytes){
            return(List<T>)deserializeCollection(bytes,new ArrayList<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull List<T>list){
            return serializeCollectionAsync(list,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable List<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new ArrayList<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(List<T>)result);
        }
    }

    //INNER ARRAY
    public@NotNull ArrayType arrays(){
        return new ArrayType();
    }
    public final class ArrayType implements VarSubType<T[]>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"Array<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull T@NotNull[]array){
            return VarType.this.lists().serializeSync(Arrays.asList(array));
        }
        public@Nullable T[] deserializeSync(byte@NotNull[]bytes){
            final List<T>deserializedList=VarType.this.lists().deserializeSync(bytes);
            return deserializedList==null?null:deserializedList.toArray((T[])Array.newInstance(getTypeClazz(),deserializedList.size()));
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte @NotNull[]>serializeAsync(@NotNull T @NotNull[]array){
            return VarType.this.lists().serializeAsync(Arrays.asList(array));
        }
        public@NotNull CompletableFuture<@Nullable T[]>deserializeAsync(byte @NotNull[]bytes){
            return VarType.this.lists().deserializeAsync(bytes).thenApply(deserializedList->{
                if(deserializedList==null)return null;
                return deserializedList.toArray((T[]) Array.newInstance(getTypeClazz(),deserializedList.size()));
            });
        }
    }



    //INNER PRIORITY-BLOCKING-QUEUE
    public@NotNull PriorityBlockingQueueType priority_blocking_queues(){
        return new PriorityBlockingQueueType();
    }
    public final class PriorityBlockingQueueType implements VarSubType<PriorityBlockingQueue<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"PriorityBlockingQueue<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull PriorityBlockingQueue<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable PriorityBlockingQueue<T>deserializeSync(byte@NotNull[]bytes){
            return(PriorityBlockingQueue<T>)deserializeCollection(bytes,new PriorityBlockingQueue<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull PriorityBlockingQueue<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable PriorityBlockingQueue<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new PriorityBlockingQueue<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(PriorityBlockingQueue<T>)result);
        }
    }

    //INNER PRIORITY-QUEUE
    public@NotNull PriorityQueueType priority_queues(){
        return new PriorityQueueType();
    }
    public final class PriorityQueueType implements VarSubType<PriorityQueue<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"PriorityQueue<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull PriorityQueue<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable PriorityQueue<T>deserializeSync(byte@NotNull[]bytes){
            return(PriorityQueue<T>)deserializeCollection(bytes,new PriorityQueue<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull PriorityQueue<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable PriorityQueue<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new PriorityQueue<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(PriorityQueue<T>)result);
        }
    }

    //INNER QUEUE
    public@NotNull ArrayDequeType array_deques(){
        return new ArrayDequeType();
    }
    public final class ArrayDequeType implements VarSubType<ArrayDeque<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"ArrayDeque<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull ArrayDeque<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable ArrayDeque<T>deserializeSync(byte@NotNull[]bytes){
            return(ArrayDeque<T>)deserializeCollection(bytes,new ArrayDeque<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull ArrayDeque<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable ArrayDeque<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new ArrayDeque<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(ArrayDeque<T>)result);
        }
    }

    //INNER QUEUE
    public@NotNull StackType stacks(){
        return new StackType();
    }
    public final class StackType implements VarSubType<Stack<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"Stack<"+VarType.this.typeClazz.getName()+">";
        }

        //SERIALIZATION-SYNC
        public byte@NotNull[] serializeSync(@NotNull Stack<T>set){
            return serializeCollection(set,VarType.this::serializeSync);
        }
        public@Nullable Stack<T>deserializeSync(byte@NotNull[]bytes){
            return(Stack<T>)deserializeCollection(bytes,new Stack<>(),VarType.this::deserializeSync);
        }

        //SERIALIZATION-ASYNC
        public@NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull Stack<T>set){
            return serializeCollectionAsync(set,VarType.this::serializeAsync);
        }
        public@NotNull CompletableFuture<@Nullable Stack<T>>deserializeAsync(byte @NotNull[]bytes){
            return deserializeCollectionAsync(bytes,new Stack<>(),VarType.this::deserializeAsync)
                    .thenApply(result->(Stack<T>)result);
        }
    }
}