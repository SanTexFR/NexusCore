package fr.nexus.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import fr.nexus.Core;
import fr.nexus.system.ThreadPool;
import fr.nexus.api.listeners.core.CoreDisableEvent;
import fr.nexus.api.listeners.core.CoreInitializeEvent;
import fr.nexus.api.listeners.Listeners;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class Utils{
    //VARIABLES (STATICS)
    public static ThreadPool THREADPOOL;

    public static final @NotNull Object2ObjectOpenHashMap<@NotNull UUID,Player>onlinePlayerUUIDCache=new Object2ObjectOpenHashMap<>();
    public static final @NotNull Object2ObjectOpenHashMap<@NotNull String,Player>onlinePlayerNameCache=new Object2ObjectOpenHashMap<>();

    private static int MAX_OFFLINE_CACHE;
    private static final@NotNull Object OFFLINE_LOCK=new Object();
    public static LinkedHashMap<@NotNull UUID,@NotNull OfflinePlayer>offlinePlayerUUIDCache;
    public static LinkedHashMap<@NotNull String,@NotNull OfflinePlayer>offlinePlayerNameCache;
    static{
        Listeners.register(CoreInitializeEvent.class,Utils::onCoreInitialize);
        Listeners.register(CoreDisableEvent.class,Utils::onCoreDisable);
        Listeners.register(PlayerJoinEvent.class,Utils::onPlayerJoin);
        Listeners.register(PlayerQuitEvent.class,Utils::onPlayerQuit);
    }

    //METHODS (STATICS)

    //MAIN LISTENERS
    private static void onCoreInitialize(CoreInitializeEvent e){
        THREADPOOL=new ThreadPool(
                Core.getInstance().getConfig().getInt("thread.utils.amount",1),
                Core.getInstance().getConfig().getInt("thread.utils.queue-size",128),
                "Utils Async",
                Thread.NORM_PRIORITY-1
        );

        MAX_OFFLINE_CACHE=Core.getInstance().getConfig().getInt("cache.maxOfflineCache",1);
        offlinePlayerUUIDCache=new LinkedHashMap<>(MAX_OFFLINE_CACHE,0.75f,true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID,OfflinePlayer>eldest){
                return size()>MAX_OFFLINE_CACHE;
            }
        };

        offlinePlayerNameCache=new LinkedHashMap<>(MAX_OFFLINE_CACHE,0.75f,true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<String,OfflinePlayer>eldest){
                return size()>MAX_OFFLINE_CACHE;
            }
        };
    }
    private static void onCoreDisable(CoreDisableEvent e){
        synchronized(onlinePlayerUUIDCache){
            synchronized(onlinePlayerNameCache){
                onlinePlayerUUIDCache.clear();
                onlinePlayerNameCache.clear();
            }
        }
        offlinePlayerUUIDCache.clear();
        offlinePlayerNameCache.clear();

        Core.shutdownExecutor(THREADPOOL);
    }

    //PLAYER
    public static boolean isOnline(@NotNull UUID uuid){
        return onlinePlayerUUIDCache.containsKey(uuid);
    }
    public static boolean isOnline(@NotNull String name){
        return onlinePlayerNameCache.containsKey(name.toLowerCase());
    }

    public static@Nullable Player getPlayer(@NotNull UUID uuid){
        Player p;
        synchronized(onlinePlayerUUIDCache){
            p=onlinePlayerUUIDCache.get(uuid);
        }
        if(p==null)p=Bukkit.getPlayer(uuid);
        return p;
    }
    public static@Nullable Player getPlayer(@NotNull String name){
        name=name.toLowerCase();
        Player p;
        synchronized(onlinePlayerNameCache){
            p=onlinePlayerNameCache.get(name);
        }
        if(p==null)p=Bukkit.getPlayer(name);
        return p;
    }

    public static@NotNull CompletableFuture<@NotNull OfflinePlayer>getOfflinePlayer(@NotNull UUID uuid){
        OfflinePlayer offlinePlayer;
        synchronized(OFFLINE_LOCK){
            offlinePlayer=offlinePlayerUUIDCache.get(uuid);
        }
        if(offlinePlayer!=null)return CompletableFuture.completedFuture(offlinePlayer);

        offlinePlayer=onlinePlayerUUIDCache.get(uuid);
        if(offlinePlayer!=null)return CompletableFuture.completedFuture(offlinePlayer);

        return CompletableFuture.supplyAsync(()->{
            final OfflinePlayer player=Bukkit.getOfflinePlayer(uuid);
            putOfflinePlayerInCache(player);

            return player;
        },THREADPOOL);
    }
    public static@NotNull CompletableFuture<@NotNull OfflinePlayer>getOfflinePlayer(@NotNull String name){
        name=name.toLowerCase();
        OfflinePlayer offlinePlayer;
        synchronized(OFFLINE_LOCK){
            offlinePlayer=offlinePlayerNameCache.get(name);
        }
        if(offlinePlayer!=null)return CompletableFuture.completedFuture(offlinePlayer);

        offlinePlayer=onlinePlayerNameCache.get(name);
        if(offlinePlayer!=null)return CompletableFuture.completedFuture(offlinePlayer);

        offlinePlayer=Bukkit.getOfflinePlayerIfCached(name);
        if(offlinePlayer!=null){
            putOfflinePlayerInCache(offlinePlayer);
            return CompletableFuture.completedFuture(offlinePlayer);
        }

        final String finalName=name;
        return CompletableFuture.supplyAsync(()->{
            final OfflinePlayer player=Bukkit.getOfflinePlayer(finalName);
            putOfflinePlayerInCache(player);

            return player;
        },THREADPOOL);
    }
    private static void putOfflinePlayerInCache(@NotNull OfflinePlayer offlinePlayer){
        final UUID uuid=offlinePlayer.getUniqueId();
        final String name=offlinePlayer.getName();

        synchronized(OFFLINE_LOCK){
            offlinePlayerUUIDCache.put(uuid,offlinePlayer);
            if(name!=null)offlinePlayerNameCache.put(name,offlinePlayer);
        }
    }

    //HEAD
    private static final@NotNull UUID HEAD_UUID=UUID.randomUUID();
    public static@NotNull ItemStack getHeadByTexture(@NotNull String textures){
        final ItemStack item=new ItemStack(Material.PLAYER_HEAD);
        return applyHeadTexture(item,textures);
    }
    public static@NotNull ItemStack applyHeadTexture(@NotNull ItemStack item,@NotNull String textures){
        final SkullMeta itemMeta=(SkullMeta)item.getItemMeta();

        final PlayerProfile profile=Bukkit.createProfileExact(HEAD_UUID,"noname");
        final Set<ProfileProperty>properties=profile.getProperties();
        properties.add(new ProfileProperty("textures",textures));
        itemMeta.setPlayerProfile(profile);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static@NotNull CompletableFuture<@NotNull ItemStack>getPlayerHead(@NotNull UUID uuid){
        return buildPlayerHead(getOfflinePlayer(uuid));
    }
    public static@NotNull CompletableFuture<@NotNull ItemStack>getPlayerHead(@NotNull String name){
        return buildPlayerHead(getOfflinePlayer(name));
    }
    private static@NotNull CompletableFuture<@NotNull ItemStack>buildPlayerHead(@NotNull CompletableFuture<OfflinePlayer>futurePlayer){
        return futurePlayer.thenApply(player->{
            final ItemStack skull=new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta skullMeta=(SkullMeta)skull.getItemMeta();
            skullMeta.setOwningPlayer(player);
            skull.setItemMeta(skullMeta);
            return skull;
        });
    }

    //LISTENERS
    private static void onPlayerJoin(PlayerJoinEvent e){
        final Player p=e.getPlayer();
        synchronized(onlinePlayerUUIDCache){
            onlinePlayerUUIDCache.put(p.getUniqueId(),p);
        }
        synchronized(onlinePlayerNameCache){
            onlinePlayerNameCache.put(p.getName(),p);
        }
    }
    private static void onPlayerQuit(PlayerQuitEvent e){
        final Player p=e.getPlayer();
        synchronized(onlinePlayerUUIDCache){
            onlinePlayerUUIDCache.remove(p.getUniqueId());
        }
        synchronized(onlinePlayerNameCache){
            onlinePlayerNameCache.remove(p.getName());
        }
    }

    //THRESHOLD
    private static final @NotNull Set<@NotNull Object>thresholds=new HashSet<>();
    public static boolean containThreshold(@NotNull Object obj){
        return thresholds.contains(obj);
    }

    public static void addThreshold(@NotNull Object obj){
        thresholds.add(obj);
    }
    public static void removeThreshold(@NotNull Object obj){
        thresholds.remove(obj);
    }
}