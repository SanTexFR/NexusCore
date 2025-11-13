package fr.nexus.api.var.types.parents.normal;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarVersion;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public abstract class VarType<T>extends VarVersion implements VarSubType<T>,Vars,CollectionUtils{
    //VARIABLES (STATICS)
    private static final@NotNull Map<@NotNull String,@NotNull VarType<?>>types=new HashMap<>();

    //VARIABLES (INSTANCES)
    private final@NotNull Class<@NotNull T>typeClazz;

    //CONSTRUCTOR
    protected VarType(@NotNull Class<@NotNull T>typeClazz,int version){
        super(version);
        this.typeClazz=typeClazz;
        types.put(getStringType(),this);
    }


    //METHODS (STATICS)
    public static@NotNull Map<@NotNull String,@NotNull VarType<?>>getTypes(){
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
    public abstract@Nullable T deserializeSync(byte@NotNull[]bytes);

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

    //INNER SET
    public@NotNull SetType sets(){
        return new SetType();
    }
    public final class SetType implements VarSubType<Set<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"Set<"+VarType.this.typeClazz.getName()+">";
        }
        public boolean isWrapper(){
            return true;
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

    //INNER LINKED-SET
    public@NotNull LinkedSetType linked_sets(){
        return new LinkedSetType();
    }
    public final class LinkedSetType implements VarSubType<LinkedHashSet<T>>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"LinkedSet<"+VarType.this.typeClazz.getName()+">";
        }
        public boolean isWrapper(){
            return true;
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

    //INNER ARRAY
    public@NotNull ArrayType arrays(){
        return new ArrayType();
    }
    public final class ArrayType implements VarSubType<T[]>{
        //RAW TYPE
        public@NotNull String getStringType(){
            return"Array<"+VarType.this.typeClazz.getName()+">";
        }
        public boolean isWrapper(){
            return true;
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
}