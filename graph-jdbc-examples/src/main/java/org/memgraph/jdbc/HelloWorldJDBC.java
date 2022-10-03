package org.memgraph.jdbc;

import java.sql.*;

public class HelloWorldJDBC {

    public static void main(String[] args) {
        // Connecting
        try (Connection con = DriverManager.getConnection("jdbc:graph:bolt://localhost:7687", "user", "")) {

            // Querying
            String query = "CREATE (p:Person {id: \"HelloWorldJDBC123 example\"})";
            try (PreparedStatement stmt = con.prepareStatement(query)) {

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Query result....");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
