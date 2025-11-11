package fr.nexus.gui;

import java.lang.annotation.*;

/**
 * ⚠️ Ne pas stocker de référence directe au GUI ici,
 * sinon il ne pourra pas être collecté par le garbage collector.
 */
@Documented
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@SuppressWarnings({"unused","UnusedReturnValue"})
public @interface DoNotStoreGui{}