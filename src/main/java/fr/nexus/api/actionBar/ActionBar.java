package fr.nexus.api.actionBar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ActionBar{
    //VARIABLES (INSTANCES)
    private final@NotNull ConcurrentHashMap<@NotNull Integer,@NotNull Message>messages=new ConcurrentHashMap<>();

    //CONSTRUCTOR
    ActionBar(@NotNull UUID uuid){
        ActionBarManager.addActionBar(uuid,this);
    }


    //METHODS (INSTANCES)
    public void add(int priority,@NotNull Component component){
        final Message message=this.messages.get(priority);
        if(message!=null){

        }

//        this.component=()->component;
    }
    public void add(int priority,@NotNull Supplier<@NotNull Component>component){
//        this.component=component;
    }

    public void remove(int priority){

    }

    public void skip(){

    }

    public void clear(){

    }

    //INNER CLASS
    static class Message{
        //VARIABLES (INSTANCES)
        private@NotNull Supplier<@NotNull Component>text;
        private int updateRate;
        private int duration;

        //CONSTRUCTOR
        public Message(@NotNull Supplier<@NotNull Component>text,int updateRate,int duration){
            this.text=text;
        }

        //METHODS (INSTANCES)

        //TEXT
        public@NotNull Supplier<@NotNull Component>getText(){
            return this.text;
        }
        public void setText(@NotNull Supplier<@NotNull Component>text){
            this.text=text;
        }

        //UPDATE-RATE
        public int getUpdateRate(){
            return this.updateRate;
        }
        public void setUpdateRate(int updateRate){
            this.updateRate=updateRate;
        }

        //DURATION
        public int getDuration(){
            return this.duration;
        }
        public void setDuration(int duration){
            this.duration=duration;
        }
    }
}