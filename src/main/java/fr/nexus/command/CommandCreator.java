package fr.nexus.command;

import fr.nexus.Core;
import fr.nexus.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class CommandCreator{
    //VARIABLES(STATICS)
    private static final@NotNull Logger logger=new Logger(Core.getInstance(),CommandCreator.class);

    //VARIABLES(INSTANCES)
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
        }catch(Exception e){
            logger.severe("error when performing the command: {}, error: {}",this.command,e.getMessage());
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