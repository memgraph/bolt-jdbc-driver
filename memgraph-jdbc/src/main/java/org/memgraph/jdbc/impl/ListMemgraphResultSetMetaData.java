package org.memgraph.jdbc.impl;

import org.memgraph.jdbc.MemgraphResultSetMetaData;

import java.util.List;

public class ListMemgraphResultSetMetaData extends MemgraphResultSetMetaData {
    /**
     * Default constructor with the list of column.
     *
     * @param keys List of column of the ResultSet
     */
    public ListMemgraphResultSetMetaData(List<String> keys) {
        super(keys);
    }

}
