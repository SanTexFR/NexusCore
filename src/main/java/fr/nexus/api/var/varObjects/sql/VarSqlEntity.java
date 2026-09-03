package fr.nexus.api.var.varObjects.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VarSqlEntity {
    String db();
    String table();
    SqlKeyType keyType() default SqlKeyType.STRING;
}