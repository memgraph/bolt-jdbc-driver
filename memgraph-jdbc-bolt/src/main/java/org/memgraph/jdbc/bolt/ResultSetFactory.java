package org.memgraph.jdbc.bolt;

import org.neo4j.driver.Result;

import java.sql.ResultSet;
import java.sql.Statement;

@FunctionalInterface
interface ResultSetFactory {

    ResultSet create(boolean debug, Statement statement, Result iterator, int... params);
}
