package fr.nexus.lang;

import fr.nexus.Core;
import fr.nexus.system.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class Language{
    //VARIABLES (STATICS)
    private static final@NotNull Logger logger=new Logger(Core.getInstance(),Language.class);
    private static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull Language>languages=new ConcurrentHashMap<>();

    //VARIABLES (INSTANCES)
    private final@NotNull Path path;
    private final@NotNull ConcurrentHashMap<@NotNull String,@NotNull String>messages=new ConcurrentHashMap<>();

    //CONSTRUCTOR
    private Language(@NotNull Path path){
        this.path=path;
        languages.put(path.toString(),this);
        reload();
    }


    //METHODS (STATICS)
    public static @NotNull Language get(@NotNull Plugin plugin,@NotNull String lang){
        final Path path=getLangPath(plugin,lang);
        final Language language=languages.get(path.toString());
        return language!=null?language:new Language(path);
    }
    public static boolean exist(@NotNull Plugin plugin,@NotNull String lang){
        return Files.exists(getLangPath(plugin,lang));
    }
    static@NotNull Path getLangPath(@NotNull Plugin plugin,@NotNull String lang){
        return plugin.getDataFolder().toPath().resolve("data").resolve("langs").resolve(lang+".lang");
    }

    //METHODS (INSTANCES)
    public@NotNull Component getText(@Nullable String key,@Nullable PlaceholderContext context){
        if(key==null)return Component.text("");

        final String[]message=new String[1];
        message[0]=messages.getOrDefault(key,"");

        //PLACEHOLDER
        if(context!=null)context.components.forEach((pKey,placeholder)->
            message[0]=message[0].replace("%"+pKey+"%",MiniMessage.miniMessage().serialize(placeholder)));

        return MiniMessage.miniMessage().deserialize(message[0]);
    }

    //PRIVATE (INSTANCES)
    private boolean createFileDirectory(){
        try{
            Files.createDirectories(this.path.getParent());
            if(!Files.exists(this.path))Files.createFile(this.path);
            return true;
        }catch(IOException e){
            logger.severe("Error when creating the path from the file: '{}' error: '{}'",this.path.toString(),e.getMessage());
            return false;
        }
    }
    public void reload(){
        if(!createFileDirectory())return;
        try{
            for(final String line:Files.readAllLines(this.path)){
                final String[]split=line.split(": ",2);
                if(split.length>=2)messages.put(split[0],split[1]);
            }
        }catch(IOException e){
            logger.severe("Error when loading the lang: '{}'",this.path.toString());
        }
    }
}