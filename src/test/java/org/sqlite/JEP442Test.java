package org.sqlite;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JEP442Test {

    @Test
    void connection() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:test")) {

        }
    }
}
