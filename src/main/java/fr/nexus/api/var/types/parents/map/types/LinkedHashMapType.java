package fr.nexus.api.var.types.parents.map.types;

import fr.nexus.api.var.types.parents.map.MapType;

import java.util.LinkedHashMap;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class LinkedHashMapType<T,T2>extends MapType<LinkedHashMap<T,T2>>{
    //CONSTRUCTOR
    public LinkedHashMapType(){
        super((Class<LinkedHashMap<T,T2>>)(Class<?>)LinkedHashMap.class,LinkedHashMap::new);
    }
}