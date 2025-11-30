package fr.nexus.api.command.tabcompleter;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class TabCompleterHandler{
    //VARIABLES
    public static final@NotNull Map<@NotNull String,@NotNull List<@NotNull TabCompleterHandler>>commandCompleter=new HashMap<>();

    private final@NotNull String command;
    protected@NotNull List<@Nullable Function<@NotNull CommandSender,@NotNull Supplier<@NotNull String>>>args=new ArrayList<>();
    protected@NotNull Set<@NotNull Function<@NotNull CommandSender,@NotNull Supplier<@NotNull Set<@NotNull String>>>>displays=new HashSet<>();

    //CONSTRUCTOR
    private TabCompleterHandler(@NotNull String command){
        this.command=command;
    }


    //METHODS
    public static@NotNull TabCompleterHandler create(@NotNull String command){
        return new TabCompleterHandler(command);
    }

    public@NotNull TabCompleterHandler addArg(@Nullable Function<@NotNull CommandSender,@NotNull Supplier<@NotNull String>>arg){
        this.args.add(arg);
        return this;
    }
    public@NotNull TabCompleterHandler addDisplay(@NotNull Function<@NotNull CommandSender,@NotNull Supplier<@NotNull Set<@NotNull String>>>display){
        this.displays.add(display);
        return this;
    }

    public void perform(){
        commandCompleter.computeIfAbsent(this.command,key->new ArrayList<>()).add(this);
    }
}