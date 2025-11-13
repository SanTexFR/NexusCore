package fr.nexus.api.var.types.parents.map.types;

import fr.nexus.api.var.types.parents.map.MapType;

import java.util.WeakHashMap;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class WeakHashMapType<T,T2>extends MapType<WeakHashMap<T,T2>>{
    //CONSTRUCTOR
    public WeakHashMapType(){
        super((Class<WeakHashMap<T,T2>>)(Class<?>)WeakHashMap.class,WeakHashMap::new);
    }
}