package fr.nexus.system;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class Logger{
    //VARIABLES (INSTANCES)
    private final @NotNull String prefix;
    private final @NotNull java.util.logging.Logger bukkitLogger;

    //CONSTRUCTOR
    public Logger(@NotNull Plugin plugin,@NotNull Class<?>clazz){
        this.prefix="["+clazz.getSimpleName()+"] ";
        this.bukkitLogger=plugin.getLogger();
    }

    //METHODS (INSTANCES)
    public void log(@NotNull Level level,@NotNull String msg,@NotNull String... args){
        for(final String arg:args)
            msg=msg.replaceFirst("\\{}",arg);
        this.bukkitLogger.log(level,this.prefix+msg);
    }

    public void info(@NotNull String msg,@NotNull String...args){
        log(Level.INFO,msg,args);
    }

    public void warning(@NotNull String msg,@NotNull String...args){
        log(Level.WARNING,msg,args);
    }

    public void severe(@NotNull String msg,@NotNull String...args){
        log(Level.SEVERE,msg,args);
    }

    public void debug(@NotNull String msg,@NotNull String...args){
        log(Level.FINE,msg,args);
    }
}