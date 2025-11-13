package fr.nexus.api.var.types.parents.map.types;

import fr.nexus.api.var.types.parents.map.MapType;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class ConcurrentHashMapType<T,T2>extends MapType<ConcurrentHashMap<T,T2>>{
    //CONSTRUCTOR
    public ConcurrentHashMapType(){
        super((Class<ConcurrentHashMap<T,T2>>)(Class<?>)ConcurrentHashMap.class,ConcurrentHashMap::new);
    }
}