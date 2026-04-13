package fr.nexus.api.actionBar;

public record ActionBarPriority(int level){
    // Constantes prédéfinies
    public static final ActionBarPriority CRITICAL = new ActionBarPriority(0);   // Alertes système, Urgences
    public static final ActionBarPriority HIGHEST = new ActionBarPriority(10);
    public static final ActionBarPriority HIGH = new ActionBarPriority(25);
    public static final ActionBarPriority MEDIUM = new ActionBarPriority(50);    // Défaut pour les actions importantes
    public static final ActionBarPriority NORMAL = new ActionBarPriority(100);   // Défaut général
    public static final ActionBarPriority LOW = new ActionBarPriority(200);
    public static final ActionBarPriority LOWEST = new ActionBarPriority(500);
    public static final ActionBarPriority COSMETIC = new ActionBarPriority(1000); // Infos de boussole, etc.

    // Méthode utilitaire pour créer une priorité personnalisée facilement
    public static ActionBarPriority of(int level) {
        return new ActionBarPriority(level);
    }
}
