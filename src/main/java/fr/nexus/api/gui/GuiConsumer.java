package fr.nexus.api.gui;

import com.cjcrafter.foliascheduler.TaskImplementation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
class GuiConsumer{
    //VARIABLES (INSTANCES)
    private final@NotNull WeakReference<@NotNull Gui> weakReference;
    private final int tick;
    private final@NotNull Consumer<@NotNull Gui> consumer;
    private@Nullable TaskImplementation<?> task;

    //CONSTRUCTOR
    public GuiConsumer(@NotNull WeakReference<@NotNull Gui>weakReference, int tick, @NotNull Consumer<@NotNull Gui>consumer, @Nullable TaskImplementation<?>task){
        this.weakReference=weakReference;
        this.tick=tick;
        this.consumer=consumer;
        this.task=task;
    }

    //METHODS (INSTANCES)
    public@NotNull WeakReference<@NotNull Gui>getWeakReference(){
        return this.weakReference;
    }

    public int getTick(){
        return this.tick;
    }
    public@NotNull Consumer<@NotNull Gui>getConsumer(){
        return this.consumer;
    }

    public void setTask(@Nullable TaskImplementation<?>task){
        this.task=task;
    }
    public@Nullable TaskImplementation<?>getTask(){
        return this.task;
    }
}