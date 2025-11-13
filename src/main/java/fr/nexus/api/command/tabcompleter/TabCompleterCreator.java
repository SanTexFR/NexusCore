package fr.nexus.api.command.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class TabCompleterCreator implements org.bukkit.command.TabCompleter{
    @Override
    public@NotNull List<@NotNull String>onTabComplete(@NotNull CommandSender sender,@NotNull Command command,@NotNull String alias,@NotNull String@NotNull[]args){
        final List<TabCompleterHandler>tabCompleters=TabCompleterHandler.commandCompleter.get(command.getName().toLowerCase());
        if(tabCompleters==null)return new ArrayList<>();

        final List<String>completions=new ArrayList<>();

        for(final TabCompleterHandler completer:tabCompleters){
            if(completer.args.size()+1!=args.length)continue;

            boolean check=false;
            int index=0;
            for(final Function<CommandSender,Supplier<String>>function:completer.args){
                final String arg=function.apply(sender).get();

                if(arg==null||arg.isEmpty()){
                    index++;
                    continue;
                }

                if(!arg.equals(args[index++])){
                    check=true;
                    break;
                }
            }

            if(check)continue;

            for(Function<CommandSender,Supplier<Set<String>>>function:completer.displays){
                final Set<String>values=function.apply(sender).get();

                final String lastArg=args[args.length-1].toLowerCase();
                for(final String value:values){
                    if(value.toLowerCase().startsWith(lastArg)){
                        completions.add(value);
                    }
                }
            }
        }

        return completions;
    }
}
