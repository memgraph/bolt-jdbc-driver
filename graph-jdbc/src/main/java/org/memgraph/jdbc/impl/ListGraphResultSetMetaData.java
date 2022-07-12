package org.memgraph.jdbc.impl;

import org.memgraph.jdbc.GraphResultSetMetaData;

import java.util.List;

public class ListGraphResultSetMetaData extends GraphResultSetMetaData {
    /**
     * Default constructor with the list of column.
     *
     * @param keys List of column of the ResultSet
     */
    public ListGraphResultSetMetaData(List<String> keys) {
        super(keys);
    }

}
