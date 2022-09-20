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
        try (Connection con = DriverManager.getConnection("jdbc:graph:bolt://localhost", "user", "")) {
            executeQuery(con, "MATCH (n) DETACH DELETE n");

            executeQuery(con, "CREATE (:User{name: \"John\"})-[:FRIEND]->(:User{name:\"Mate\", age:15})");

            PreparedStatement stmt = con.prepareStatement("MATCH (u:User)-[:FRIEND]-(f:User) WHERE u.name = $0 RETURN f.name, f.age");

            stmt.setString(0, "John");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Friend: "+rs.getString("f.name")+" is "+rs.getInt("f.age"));
                }
            }
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
        System.out.println("Query " + query + " executed");
    }
}