package fr.nexus.api.var.varObjects.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public interface SqlKeyType<K> {
    // Les "Enums" disponibles
    SqlKeyType<String> STRING = new Impl<>("VARCHAR(255)", new String[]{"VARCHAR", "CHAR"}, PreparedStatement::setString);
    SqlKeyType<UUID> UUID = new Impl<>("UUID", new String[]{"UUID"}, PreparedStatement::setObject);
    SqlKeyType<Integer> INT = new Impl<>("INTEGER", new String[]{"INT", "INTEGER"}, PreparedStatement::setInt);
    SqlKeyType<Long> LONG = new Impl<>("BIGINT", new String[]{"BIGINT", "INT8"}, PreparedStatement::setLong);

    // Contrat de l'interface
    String getSqlDeclaration();
    boolean isValidType(String dbColumnType);
    void setParameter(PreparedStatement stmt, int index, K value) throws SQLException;

    // L'implémentation interne (Record Java 16+)
    record Impl<K>(String sqlDeclaration, String[] validColumnTypes, SqlSetter<K> setter) implements SqlKeyType<K> {
        @Override
        public String getSqlDeclaration() { return sqlDeclaration; }

        @Override
        public boolean isValidType(String dbColumnType) {
            for (String type : validColumnTypes) {
                if (dbColumnType.contains(type)) return true;
            }
            return false;
        }

        @Override
        public void setParameter(PreparedStatement stmt, int index, K value) throws SQLException {
            setter.set(stmt, index, value);
        }
    }

    @FunctionalInterface
    interface SqlSetter<K> {
        void set(PreparedStatement stmt, int index, K value) throws SQLException;
    }
}