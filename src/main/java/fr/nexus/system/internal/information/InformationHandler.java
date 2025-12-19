package fr.nexus.system.internal.information;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class InformationHandler{
    static final@NotNull Set<@NotNull Information>informations=new HashSet<>();

    public static void register(@NotNull Information information){
        informations.add(information);
    }
}