package fr.nexus.command;

import fr.nexus.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class CommandCreator{
//    private static final@NotNull Map<@NotNull String,@NotNull CommandCreator>commandsWithCreator=new HashMap<>();
//    private static final@NotNull Set<@NotNull String>commands=new HashSet<>();

    //VARIABLES
    protected@NotNull String command;
    protected@Nullable BiConsumer<@NotNull CommandSenderHandler,@NotNull String@NotNull[]>action;

    protected@Nullable String description;
    protected@Nullable String usage;
    protected@Nullable String usageMessage;
    protected@Nullable String@Nullable[]aliases;
    protected@Nullable String permission;

    //CONSTRUCTOR
    private CommandCreator(@NotNull String command){
        this.command=command;
    }


    //METHODS

    //GENERALS
    public static CommandCreator create(@NotNull String command){
        return new CommandCreator(command);
    }

    public void perform(){
        try{
            Field commandMapField= Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap=(CommandMap) commandMapField.get(Bukkit.getServer());

            commandMap.register(Core.getInstance().getName(),new CommandHandler(this));
//            commands.add(this.command);
//            commandsWithCreator.put(this.command,this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //OTHERS
    public@NotNull CommandCreator setAction(@Nullable BiConsumer<@NotNull CommandSenderHandler,@NotNull String@NotNull[]>action){
        this.action=action;
        return this;
    }

    public@NotNull CommandCreator setDescription(@Nullable String description){
        this.description=description;
        return this;
    }
    public@NotNull CommandCreator setUsage(@Nullable String usage){
        this.usage=usage;
        return this;
    }
    public@NotNull CommandCreator setUsageMessage(@Nullable String message){
        this.usageMessage=message;
        return this;
    }
    public@NotNull CommandCreator setAliases(@NotNull String...aliases){
        this.aliases=aliases;
        return this;
    }
    public@NotNull CommandCreator setPermission(@Nullable String permission){
        this.permission=permission;
        return this;
    }
}