//package fr.nexus.api.gui.customTypes;
//
//import fr.nexus.api.gui.Gui;
//import net.kyori.adventure.text.Component;
//import org.bukkit.event.inventory.InventoryType;
//import org.bukkit.event.inventory.PrepareAnvilEvent;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.function.Consumer;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class AnvilGui extends Gui{
//    //VARIABLES(INSTANCES)
//    private@Nullable Consumer<@NotNull PrepareAnvilEvent>prepareAnvilEvent;
//
//    //CONSTRUCTOR
//    public AnvilGui(@Nullable Component title){
//        super(InventoryType.ANVIL,title);
//    }
//
//
//    //METHODS(INSTANCES)
//    public void setPrepareAnvilEvent(@Nullable Consumer<@NotNull PrepareAnvilEvent>prepareAnvilEvent){
//        this.prepareAnvilEvent=prepareAnvilEvent;
//    }
//    public@Nullable Consumer<@NotNull PrepareAnvilEvent>getPrepareAnvilEvent(){
//        return this.prepareAnvilEvent;
//    }
//}