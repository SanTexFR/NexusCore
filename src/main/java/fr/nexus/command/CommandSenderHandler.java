package fr.nexus.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class CommandSenderHandler{
    //VARIABLES(INSTANCES)
    private final@NotNull CommandSender sender;

    //CONSTRUCTOR
    private CommandSenderHandler(@NotNull CommandSender sender){
        this.sender=sender;
    }


    //METHODS(STATICS)
    public static @NotNull CommandSenderHandler handle(@NotNull CommandSender sender){
        return new CommandSenderHandler(sender);
    }

    //METHODS(INSTANCES)
    public @NotNull CommandSenderHandler ifPlayer(@NotNull Consumer<Player>playerConsumer){
        if(this.sender instanceof Player p)
            playerConsumer.accept(p);
        return this;
    }

    public @NotNull CommandSenderHandler ifNotPlayer(@NotNull Consumer<@NotNull CommandSender>consoleAction){
        if(!(this.sender instanceof Player))
            consoleAction.accept(this.sender);
        return this;
    }

    public void always(@NotNull Consumer<@NotNull CommandSender>action){
        action.accept(this.sender);
    }
}