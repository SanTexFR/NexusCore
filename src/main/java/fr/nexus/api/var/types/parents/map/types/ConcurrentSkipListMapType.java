package fr.nexus.api.var.types.parents.map.types;

import fr.nexus.api.var.types.parents.map.MapType;

import java.util.concurrent.ConcurrentSkipListMap;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class ConcurrentSkipListMapType<T,T2>extends MapType<ConcurrentSkipListMap<T,T2>>{
    //CONSTRUCTOR
    public ConcurrentSkipListMapType(){
        super((Class<ConcurrentSkipListMap<T,T2>>)(Class<?>)ConcurrentSkipListMap.class,ConcurrentSkipListMap::new);
    }
}