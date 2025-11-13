package fr.nexus.api.var.types.parents.map.types;

import fr.nexus.api.var.types.parents.map.MapType;

import java.util.TreeMap;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class TreeMapType<T,T2>extends MapType<TreeMap<T,T2>>{
    //CONSTRUCTOR
    public TreeMapType(){
        super((Class<TreeMap<T,T2>>)(Class<?>)TreeMap.class,TreeMap::new);
    }
}