package org.example.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DaoUtils {
    public static void validateAffectedRows(final int affectedRows) throws SQLException {
        if (affectedRows == 0) {
            throw new SQLException("Bank operation failed, no rows affected.");
        }
    }

    public static void validateGeneratedKeys(final ResultSet generatedKeys) throws SQLException {
        if (!generatedKeys.next()) {
            throw new SQLException("Creating Bank failed, no ID obtained");
        }
    }
}
