package fr.nexus.api.var.varObjects.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public enum SqlKeyType {
    STRING("VARCHAR(255)", new String[]{"VARCHAR", "CHAR"}, PreparedStatement::setString),
    UUID("UUID", new String[]{"UUID"}, PreparedStatement::setObject),
    INT("INTEGER", new String[]{"INT", "INTEGER"}, PreparedStatement::setInt),
    LONG("BIGINT", new String[]{"BIGINT", "INT8"}, PreparedStatement::setLong);

    private final String sqlDeclaration;
    private final String[] validColumnTypes;
    private final SqlSetter<?> setter;

    <K> SqlKeyType(String sqlDeclaration, String[] validColumnTypes, SqlSetter<K> setter) {
        this.sqlDeclaration = sqlDeclaration;
        this.validColumnTypes = validColumnTypes;
        this.setter = setter;
    }

    public String getSqlDeclaration() {
        return sqlDeclaration;
    }

    public boolean isValidType(String dbColumnType) {
        for (String type : validColumnTypes) {
            if (dbColumnType.contains(type)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <K> void setParameter(PreparedStatement stmt, int index, K value) throws SQLException {
        ((SqlSetter<K>) setter).set(stmt, index, value);
    }

    @FunctionalInterface
    public interface SqlSetter<K> {
        void set(PreparedStatement stmt, int index, K value) throws SQLException;
    }
}