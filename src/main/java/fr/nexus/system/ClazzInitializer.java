package fr.nexus.system;

import com.google.common.reflect.ClassPath;
import fr.nexus.Core;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ClazzInitializer{
    //VARIABLES (STATICS)
    private static final Logger logger=new Logger(Core.getInstance(),ClazzInitializer.class);

    //METHODS (STATICS)
    public static void initialize(){
        getAllClassesInPackage().forEach(ClazzInitializer::initializeClass);
    }

    //INITIALIZER
    private static void initializeClass(@NotNull Class<?> clazz) {
        try {
            if (clazz.getName().contains("net/minecraft/server")) return;

            Class.forName(clazz.getName());
        } catch (Throwable e) {
            System.err.println("[ClazzInitializer] Error loading class: " + clazz.getName() + " error: "+e.getMessage());
        }
    }
    private static Set<Class<?>> getAllClassesInPackage(){
        try {
            return ClassPath.from(Core.getInstance().getClazzLoader()).getAllClasses().stream()
                    .filter(clazz -> clazz.getPackageName().contains("fr."))
                    .map(ClassPath.ClassInfo::load)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }
    private static Set<String>getPackagesFromJar() {
        Set<String> packages = new HashSet<>();
        try {
            String path =Core.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            JarFile jarFile = new JarFile(path);

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    int lastDot = className.lastIndexOf('.');
                    if (lastDot > 0) {
                        packages.add(className.substring(0, lastDot));
                    }
                }
            }

            jarFile.close();
        }catch(IOException e){
            logger.severe("{}",e.getMessage());
        }return packages;
    }
}