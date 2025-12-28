package fr.nexus.api.var.types.parents.map;

import fr.nexus.api.var.types.parents.map.types.*;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class VarMapTypes {
    private static final ConcurrentHashMapType<?,?>CONCURRENT_HASH_MAP=new ConcurrentHashMapType<>();
    private static final ConcurrentSkipListMapType<?,?>CONCURRENT_SKIP_LIST_MAP=new ConcurrentSkipListMapType<>();
    private static final HashMapType<?,?>HASH_MAP=new HashMapType<>();
    private static final IdentityHashMapType<?,?>IDENTITY_HASH_MAP=new IdentityHashMapType<>();
    private static final LinkedHashMapType<?,?>LINKED_HASH_MAP=new LinkedHashMapType<>();
    private static final TreeMapType<?,?>TREE_MAP=new TreeMapType<>();
    private static final WeakHashMapType<?,?>WEAK_HASH_MAP=new WeakHashMapType<>();

    public static<T,T2>ConcurrentHashMapType<T,T2>CONCURRENT_HASH_MAP(){
        return(ConcurrentHashMapType<T,T2>)CONCURRENT_HASH_MAP;
    }
    public static<T,T2>ConcurrentSkipListMapType<T,T2>CONCURRENT_SKIP_LIST_MAP(){
        return(ConcurrentSkipListMapType<T,T2>)CONCURRENT_SKIP_LIST_MAP;
    }
    public static<T,T2>HashMapType<T,T2>HASH_MAP(){
        return(HashMapType<T,T2>)HASH_MAP;
    }
    public static<T,T2>IdentityHashMapType<T,T2>IDENTITY_HASH_MAP(){
        return(IdentityHashMapType<T,T2>)IDENTITY_HASH_MAP;
    }
    public static<T,T2>LinkedHashMapType<T,T2>LINKED_HASH_MAP(){
        return(LinkedHashMapType<T,T2>)LINKED_HASH_MAP;
    }
    public static<T,T2>TreeMapType<T,T2>TREE_MAP(){
        return(TreeMapType<T,T2>)TREE_MAP;
    }
    public static<T,T2>WeakHashMapType<T,T2>WEAK_HASH_MAP(){
        return(WeakHashMapType<T,T2>)WEAK_HASH_MAP;
    }
}
