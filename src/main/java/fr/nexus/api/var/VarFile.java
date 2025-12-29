package fr.nexus.api.var;

import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class VarFile extends Var{
    //CONSTRUCTOR
    private VarFile(@NotNull Path path,@NotNull Runnable closeRunnable,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>> shouldStayLoaded){
        super(path,closeRunnable,shouldStayLoaded);
    }


    //METHODS (STATICS)
    @Deprecated
    public static@NotNull VarFile getVarSync(@NotNull Plugin plugin,@NotNull String key){
        return getVarSync(plugin,key,null,null,null);
    }
    public static@NotNull VarFile getVarSync(@NotNull Plugin plugin,@NotNull String key,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>>shouldStayLoaded,@Nullable Consumer<@NotNull Var>notCachedConsumer,@Nullable Runnable unloadRunnable){
        final long nanoTime=System.nanoTime();

        final Path path=getVarPath(plugin,key);
        final String completePath=String.join("/","file",path.toString());

        final VarFile cached=getIfCached(completePath);
        if(cached!=null){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
            return cached;
        }

        final CompletableFuture<Var>async;
        synchronized(asyncLoads){
            async=asyncLoads.get(completePath);
        }

        if(async!=null){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
            return(VarFile)async.join();
        }

        final VarFile var=new VarFile(path,new Unload(key,unloadRunnable),shouldStayLoaded);

        if(Files.exists(path)){
            try{
                synchronized(var.data){
                    VarSerializer.deserializeDataSync(Files.readAllBytes(path),var.data);
                }
            }catch(IOException e){
                PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
                throw new RuntimeException("Failed to load sync var: "+key,e);
            }
        }

        synchronized(vars){
            vars.put(completePath,new WeakReference<>(var));
            if(notCachedConsumer!=null)notCachedConsumer.accept(var);
        }

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);

        return var;
    }
    public static @NotNull CompletableFuture<@NotNull VarFile>getVarAsync(@NotNull Plugin plugin,@NotNull String key){
        return getVarAsync(plugin,key,null,null,null);
    }
    public static @NotNull CompletableFuture<@NotNull VarFile>getVarAsync(@NotNull Plugin plugin,@NotNull String key,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>>shouldStayLoaded,@Nullable Consumer<@NotNull Var> notCachedConsumer,@Nullable Runnable unloadRunnable){
        final Path path=getVarPath(plugin,key);
        final String completePath=String.join("/","file",path.toString());

        final VarFile cached=getIfCached(completePath);
        if(cached!=null)return CompletableFuture.completedFuture(cached);

        final CompletableFuture<Var>existing;
        synchronized(asyncLoads){
            existing=asyncLoads.get(completePath);
        }
        if(existing!=null)return existing.thenApply(var->(VarFile)var);

        final VarFile var=new VarFile(path,new Unload(key,unloadRunnable),shouldStayLoaded);
        final CompletableFuture<VarFile>future;

        if(Files.exists(path)){
            future = CompletableFuture.supplyAsync(()->{
                try{
                    return Files.readAllBytes(path);
                } catch(IOException e){
                    throw new CompletionException(e);
                }
            },Var.THREADPOOL).thenCompose(bytes->{
                synchronized(var.data){
                    return VarSerializer.deserializeDataAsync(bytes, var.data)
                            .thenApply(unused->var);
                }
            });

            synchronized(asyncLoads){
                asyncLoads.put(completePath,future.thenApply(v->v));
            }

            future.whenComplete((res,ex)->{
                synchronized(asyncLoads){
                    asyncLoads.remove(completePath);
                }
                if(ex==null){
                    synchronized(vars){
                        vars.put(completePath,new WeakReference<>(var));
                        if(notCachedConsumer!=null)notCachedConsumer.accept(var);
                    }
                }
            });
        }else{
            future=CompletableFuture.completedFuture(var);
            synchronized(vars){
                vars.put(completePath,new WeakReference<>(var));
                if(notCachedConsumer!=null)notCachedConsumer.accept(var);
            }
        }

        return future;
    }
    private static@Nullable VarFile getIfCached(@NotNull String completePath){
        final WeakReference<?>weak;
        synchronized(vars){
            weak=vars.get(completePath);
        }
        if(weak==null)return null;

        final Object mesh=weak.get();
        if(mesh!=null)return(VarFile)mesh;

        return null;
    }
    static@NotNull Path getVarPath(@NotNull Plugin plugin,@NotNull String path){
        return plugin.getDataFolder().toPath().resolve("data").resolve(path+".var");
    }

    //ABSTRACT
    @Deprecated
    public void saveSync(){
        if(!isDirty())return;
        final long nanoTime=System.nanoTime();

        final Path path=super.getPath();
        try{
            final byte[]bytes;
            synchronized(super.data){
                bytes=VarSerializer.serializeDataSync(super.data);
            }
            if(bytes==null||bytes.length==0){
                Files.deleteIfExists(path);

                PerformanceTracker.increment(PerformanceTracker.Types.VAR,"saveSync",System.nanoTime()-nanoTime);
                return;
            }

            Files.createDirectories(path.getParent());
            Files.write(path,bytes);
        }catch(IOException e){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"saveSync",System.nanoTime()-nanoTime);
            throw new RuntimeException("Failed to save data synchronously: "+path,e);
        }

        setDirty(false);

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"saveSync",System.nanoTime()-nanoTime);
    }
    public @NotNull CompletableFuture<@Nullable Void>saveAsync() {
        if(!isDirty())return CompletableFuture.completedFuture(null);

        final Path path=super.getPath();

        final Object2ObjectOpenHashMap<String,Object[]>snapshot;
        synchronized(super.data){
            snapshot=new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>(super.data);
        }

        return VarSerializer.serializeDataAsync(snapshot).thenAcceptAsync(bytes->{
            try{
                if(bytes==null || bytes.length==0){
                    Files.deleteIfExists(path);
                }else{
                    Files.createDirectories(path.getParent());
                    Files.write(path,bytes);
                }

                setDirty(false);

            }catch(Throwable t){
                throw new CompletionException(t);
            }
        },Var.THREADPOOL)
                .exceptionally(ex -> {
                    // ❗ Sauvegarde échouée → on garde dirty = true
                    // ❗ Le fichier existant n'est PAS touché

                    // Optionnel mais fortement recommandé :
                    ex.printStackTrace();

                    return null;
                });
    }

    //INNER CLASS
    private record Unload(@NotNull String path,@Nullable Runnable unloadRunnable)implements Runnable{
        @Override
        public void run(){
            synchronized(vars){
                vars.remove(path);
            }

            if(unloadRunnable!=null)unloadRunnable.run();
        }
    }
}