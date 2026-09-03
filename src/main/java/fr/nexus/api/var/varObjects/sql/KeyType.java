package fr.nexus.api.var.varObjects.sql;

public enum KeyType {
    STRING(SqlKeyType.STRING),
    UUID(SqlKeyType.UUID),
    INT(SqlKeyType.INT),
    LONG(SqlKeyType.LONG);

    private final SqlKeyType<?> sqlKeyType;

    KeyType(SqlKeyType<?> sqlKeyType) {
        this.sqlKeyType = sqlKeyType;
    }

    public SqlKeyType<?> getSqlKeyType() {
        return sqlKeyType;
    }
}