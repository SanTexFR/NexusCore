package fr.nexus.api.var.types.parents.map.types;

import fr.nexus.api.var.types.parents.map.MapType;

import java.util.IdentityHashMap;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class IdentityHashMapType<T,T2>extends MapType<IdentityHashMap<T,T2>>{
    //CONSTRUCTOR
    public IdentityHashMapType(){
        super((Class<IdentityHashMap<T,T2>>)(Class<?>)IdentityHashMap.class,IdentityHashMap::new);
    }
}