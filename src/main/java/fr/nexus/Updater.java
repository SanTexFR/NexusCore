package fr.nexus;

import fr.nexus.logger.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Updater{
    //VARIABLES (STATICS)
    private static final@NotNull Logger logger=new Logger(Core.getInstance(),Updater.class);
    public static final@NotNull String USER="SanTexFR";
    public static final@NotNull String REPO="NexusCore";
    
    //METHODS (STATICSà
    public static void checkForUpdates(){
        Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(),()->{
            try{
                final String latestTag=getLatestTag();
                final String currentVersion=Core.getInstance().getPluginMeta().getVersion();
                if (isNewerVersion(latestTag,currentVersion)){
                    Bukkit.getScheduler().runTask(Core.getInstance(),()->{
                        logger.warning("=================================================");
                        logger.warning(" Une nouvelle version de NexusCore est disponible !");
                        logger.warning(" Version installée: v{}",currentVersion);
                        logger.warning(" Nouvelle version: {}",latestTag);
                        logger.warning(" Téléchargement: https://github.com/{}/{}/releases/latest",USER,REPO);
                        logger.warning("=================================================");
                    });
                }
            }catch(Exception e){
                logger.warning("Impossible de vérifier les mises à jour: {}"+e.getMessage());
            }
        });
    }

    public static@NotNull String getLatestTag()throws Exception{
        final URL url=java.net.URI.create("https://api.github.com/repos/"+USER+"/"+REPO+"/releases/latest").toURL();
        final HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Accept","application/vnd.github+json");
        connection.setRequestProperty("User-Agent",REPO+"-UpdateChecker");

        final BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final StringBuilder json=new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
            json.append(line);
        }reader.close();

        return json.toString().split("\"tag_name\":\"")[1].split("\"")[0];
    }
    public static boolean isNewerVersion(@NotNull String latest,@NotNull String current){
        try{
            final float newV=Float.parseFloat(latest.replace("v",""));
            final float curV=Float.parseFloat(current);
            return newV>curV;
        }catch(NumberFormatException e){
            return false;
        }
    }
}