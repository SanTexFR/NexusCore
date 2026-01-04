package fr.nexus.api.var.types.parents;

import fr.nexus.Core;

@SuppressWarnings({"unused","UnusedReturnValue",})
public abstract class InternalVarType<T>extends VarType<T>{
    public InternalVarType(){
        super(Core.getInstance());
    }
}