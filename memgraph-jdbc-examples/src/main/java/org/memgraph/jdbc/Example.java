package org.memgraph.jdbc;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Example {
    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost", "user", "")) {
            executeQuery(con, "MATCH (n) DETACH DELETE n");
            executeQuery(con, "CREATE (n:Node {id:1});");
            executeQuery(con, "CREATE (n:Node {id:2});");
            executeQuery(con, "CREATE (n:Node {id:3});");
            executeQuery(con, "MATCH (n) RETURN n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void executeQuery(Connection con, String query) throws SQLException{
        try (PreparedStatement stmt = con.prepareStatement(query)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("And another one...");
                }
            }
        }
    }
}