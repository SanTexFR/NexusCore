package fr.nexus.api.actionBar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

public class ActionBarEntry {
    private final Supplier<Component> textSupplier;
    private final int priority;
    private final int totalDurationTicks;
    private final int updateRateTicks;

    // Utilisé pour reprendre là où on s'était arrêté si un message plus prioritaire coupe celui-ci
    private int remainingTicks;

    private ActionBarEntry(Builder builder) {
        this.textSupplier = builder.textSupplier;
        this.priority = builder.priority;
        this.totalDurationTicks = builder.durationTicks;
        this.updateRateTicks = builder.updateRateTicks;
        this.remainingTicks = builder.durationTicks;
    }

    public Supplier<Component> getTextSupplier() { return textSupplier; }
    public int getPriority() { return priority; }
    public int getUpdateRateTicks() { return updateRateTicks; }
    public int getRemainingTicks() { return remainingTicks; }
    public void decrementTicks() { this.remainingTicks--; }

    public static Builder builder() {
        return new Builder();
    }

    // --- PATTERN BUILDER ---
    public static class Builder {
        private Supplier<Component> textSupplier = Component::empty;
        private int priority = ActionBarPriority.NORMAL.level();
        private int durationTicks = 60; // 3 secondes par défaut
        private int updateRateTicks = 20; // 1 update par seconde par défaut

        public Builder text(@NotNull Component text) {
            this.textSupplier = () -> text;
            return this;
        }

        public Builder textAnimated(@NotNull Supplier<Component> textSupplier) {
            this.textSupplier = textSupplier;
            return this;
        }

        public Builder priority(@NotNull ActionBarPriority priority) {
            this.priority = priority.level();
            return this;
        }
        public Builder priority(int priority) {
            this.priority = Math.max(0, priority);
            return this;
        }

        public Builder duration(int durationTicks) {
            this.durationTicks = Math.max(1, durationTicks);
            return this;
        }

        public Builder updateRate(int updateRateTicks) {
            this.updateRateTicks = Math.max(1, updateRateTicks);
            return this;
        }

        public ActionBarEntry build() {
            return new ActionBarEntry(this);
        }
    }
}