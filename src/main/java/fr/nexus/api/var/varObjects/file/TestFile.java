//package fr.nexus.api.var.varObjects.file;
//import fr.nexus.Core;
//import fr.nexus.utils.Utils;
//import fr.nexus.api.var.VarFile;
//import org.bukkit.plugin.Plugin;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class TestFile extends VarObjectFile<UUID>{
//    //CONSTRUCTOR
//    private TestFile(@NotNull Class<TestFile>clazz, @NotNull UUID uuid, @NotNull Plugin plugin, @NotNull String varPath, @NotNull VarFile var){
//        super(clazz,uuid,plugin,varPath,var);
//    }
//
//    //METHODS (STATICS)
//    public static@NotNull TestFile getSync(@NotNull UUID uuid){
//        return getVarObjectSync(TestFile.class,uuid, TestFile::new,Core.getInstance(),"playerdata/"+uuid, var->
//                CompletableFuture.completedFuture(Utils.isOnline(uuid)));
//    }
//    public static@NotNull CompletableFuture<@NotNull TestFile>getAsync(@NotNull UUID uuid){
//        return getVarObjectAsync(TestFile.class,uuid, TestFile::new,Core.getInstance(),"playerdata/"+uuid, var->
//                CompletableFuture.completedFuture(Utils.isOnline(uuid)));
//    }
//}