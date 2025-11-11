package fr.nexus.utils;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class EventMesh extends Event{
    private static final HandlerList handlers=new HandlerList();
    public@NotNull HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}