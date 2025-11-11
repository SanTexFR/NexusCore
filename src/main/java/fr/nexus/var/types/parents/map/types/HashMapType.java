package fr.nexus.var.types.parents.map.types;

import fr.nexus.var.types.parents.map.MapType;

import java.util.HashMap;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class HashMapType<T,T2>extends MapType<HashMap<T,T2>>{
    //CONSTRUCTOR
    public HashMapType(){
        super((Class<HashMap<T,T2>>)(Class<?>)HashMap.class,HashMap::new);
    }
}