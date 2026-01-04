package fr.nexus.api.command;

import fr.nexus.api.command.tabcompleter.TabCompleterCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
class CommandHandler extends Command{
    //VARIABLES
    private static final@NotNull TabCompleterCreator tabCompleterInstance=new TabCompleterCreator();
    private final@Nullable TabCompleterCreator tabCompleter;
    private@Nullable BiConsumer<@NotNull CommandSenderHandler,@NotNull String@NotNull[]>action;

    //CONSTRUCTOR
    public CommandHandler(@NotNull CommandCreator customCommand){
        super(customCommand.command);

        if(customCommand.action!=null)
            this.action=customCommand.action;

        if(customCommand.description!=null)
            this.setDescription(customCommand.description);

        if(customCommand.usage!=null)
            this.setUsage(customCommand.usage);

        if(customCommand.usageMessage!=null)
            this.usageMessage=customCommand.usageMessage;

        if(customCommand.aliases!=null){
            final List<String>safeAliases=Arrays.stream(customCommand.aliases)
                    .filter(Objects::nonNull)
                    .toList();
            this.setAliases(safeAliases);
        }

        if(customCommand.permission!=null)
            this.setPermission(String.join(";",customCommand.permission));

        this.tabCompleter=tabCompleterInstance;
    }


    //METHODS
    @Override
    public boolean execute(@NotNull CommandSender sender,@NotNull String label,@NotNull String@NotNull[]args){
        if(this.action!=null)this.action.accept(CommandSenderHandler.handle(sender),args);
        return true;
    }
    @Override
    public@NotNull List<@NotNull String>tabComplete(@NotNull CommandSender sender,@NotNull String alias,@NotNull String@NotNull[]args){
        if(this.tabCompleter==null)return Collections.emptyList();

        return this.tabCompleter.onTabComplete(sender,this,alias,args);
    }
}