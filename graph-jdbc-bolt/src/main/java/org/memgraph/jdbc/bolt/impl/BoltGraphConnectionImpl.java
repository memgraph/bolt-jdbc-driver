/*
 * Copyright (c) 2016 LARUS Business Automation [http://www.larus-ba.it]
 * <p>
 * This file is part of the "LARUS Integration Framework for Neo4j".
 * <p>
 * The "LARUS Integration Framework for Neo4j" is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.memgraph.jdbc.bolt.impl;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.internal.InternalBookmark;
import org.memgraph.jdbc.GraphDatabaseMetaData;
import org.memgraph.jdbc.GraphResultSet;
import org.memgraph.jdbc.bolt.BoltGraphConnection;
import org.memgraph.jdbc.bolt.BoltGraphDatabaseMetaData;
import org.memgraph.jdbc.bolt.BoltGraphPreparedStatement;
import org.memgraph.jdbc.bolt.BoltGraphResultSet;
import org.memgraph.jdbc.bolt.BoltGraphStatement;
import org.memgraph.jdbc.utils.BoltGraphUtils;
import org.memgraph.jdbc.boltrouting.BoltRoutingGraphDriver;
import org.memgraph.jdbc.impl.GraphConnectionImpl;
import org.memgraph.jdbc.utils.GraphInvocationHandler;
import org.memgraph.jdbc.utils.TimeLimitedCodeBlock;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author AgileLARUS
 * @since 3.0.0
 */
public class BoltGraphConnectionImpl extends GraphConnectionImpl implements BoltGraphConnection {
	public static final String BOOKMARK_SEPARATOR = ",";
	public static final String BOOKMARK_VALUES_SEPARATOR = ";";

	private Driver driver;
	private Session session;
	private Transaction transaction;
	private boolean autoCommit = true;
	private BoltGraphDatabaseMetaData metadata;
	private boolean readOnly = false;
	private boolean useBookmarks = true;
	private boolean closed = false;

	private static final Logger LOGGER = Logger.getLogger(BoltGraphConnectionImpl.class.getName());

    /**
	 * Constructor with Session and Properties.
	 *
	 * @param driver     Bolt driver
	 * @param properties Driver properties
	 * @param url        Url used for this connection
	 */
	public BoltGraphConnectionImpl(Driver driver, Properties properties, String url) {
		super(properties, url, BoltGraphResultSet.DEFAULT_HOLDABILITY);
		this.readOnly = Boolean.parseBoolean(getProperties().getProperty("readonly"));
		this.autoCommit = Boolean.parseBoolean(getProperties().getProperty("autocommit", "true"));
		this.useBookmarks = Boolean.parseBoolean(getProperties().getProperty("usebookmarks", "true"));
		this.driver = driver;
		this.initSession();
	}

	public static BoltGraphConnection newInstance(Driver driver, Properties info, String url) {
		BoltGraphConnection boltConnection = new BoltGraphConnectionImpl(driver, info, url);
		return (BoltGraphConnection) Proxy
				.newProxyInstance(BoltGraphConnectionImpl.class.getClassLoader(), new Class[] { Connection.class, BoltGraphConnection.class },
						new GraphInvocationHandler(boltConnection, BoltGraphConnectionImpl.hasDebug(info)));
	}

	/**
	 * Getter for transaction.
	 *
	 * @return the transaction
	 */
	@Override public Transaction getTransaction() {
	    initTransaction();
		return this.transaction;
	}

	/**
	 * Getter for session.
	 *
	 * @return the internal session
	 */
	@Override public Session getSession() {
		return this.session;
	}

	/**
	 * Build an internal neo4j session, without saving reference (stateless)
	 * Close using {@link #close()} for driver management
	 * @return
	 */
	@Override
	public Session newGraphSession() {
		try {
			final SessionConfig.Builder config = SessionConfig.builder()
					.withBookmarks(extractBookmarks())
					.withDefaultAccessMode(readOnly || getReadOnly() ? AccessMode.READ : AccessMode.WRITE);
			setDatabase(config);
			return this.driver.session(config.build());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void setDatabase(SessionConfig.Builder config) {
		String database = (String) this.getProperties().get(BoltGraphDriverImpl.DATABASE);
		if (database != null && !database.trim().isEmpty()) {
			config.withDatabase(database.trim());
		}
	}

	private Bookmark[] extractBookmarks() throws SQLException {
		String stringBookmark = this.getClientInfo(BoltRoutingGraphDriver.BOOKMARK);
		final Bookmark[] bookmarks;
		if (useBookmarks && StringUtils.isNotBlank(stringBookmark)) {
			bookmarks = Stream.of(stringBookmark.split(BOOKMARK_SEPARATOR))
					.map(b -> InternalBookmark.parse(Stream.of(b.split(BOOKMARK_VALUES_SEPARATOR)).collect(Collectors.toSet())))
					.toArray(Bookmark[]::new);
		} else {
			bookmarks = null;
		}
		return bookmarks;
	}


	@Override public GraphDatabaseMetaData getMetaData() throws SQLException {
		if(metadata == null){
			metadata = new BoltGraphDatabaseMetaData(this);
		}
		return metadata;
	}

    @Override public void setReadOnly(boolean readOnly) throws SQLException {
        this.checkClosed();
        if (this.transaction != null && this.transaction.isOpen()) {
            throw new SQLException("Method can't be called during a transaction");
        }
        super.doSetReadOnly(readOnly);
        closeSession();
        initSession();
    }

	/*------------------------------*/
	/*       Commit, rollback       */
	/*------------------------------*/

    @Override public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (this.autoCommit != autoCommit) {
            this.checkClosed();
            this.doCommit();
			this.autoCommit = autoCommit;
		}
	}

	@Override public boolean getAutoCommit() throws SQLException {
		this.checkClosed();
		return this.autoCommit;
	}

	@Override public void commit() throws SQLException {
		this.checkClosed();
		this.checkAutoCommit();
        doCommit();
	}

    @Override public void doCommit() throws SQLException {
    	if (this.transaction != null && this.transaction.isOpen()) {
			this.transaction.commit();
		}
		closeTransaction();
		setBookmark();
		closeSession();
	}

	private void setBookmark() throws SQLClientInfoException {
		if (!useBookmarks || this.session == null) {
			return;
		}
		InternalBookmark internalBookmark = (InternalBookmark) this.session.lastBookmark();
		if (internalBookmark != null && internalBookmark.values().iterator().hasNext()) {
			String bookmark = String.join(BOOKMARK_SEPARATOR, internalBookmark.values());
			this.setClientInfo(BoltRoutingGraphDriver.BOOKMARK, bookmark);
		}
	}

	@Override public void rollback() throws SQLException {
		this.checkClosed();
		this.checkAutoCommit();
        doRollback();
	}

    @Override public void doRollback() throws SQLClientInfoException {
		if (this.transaction != null && this.transaction.isOpen()) {
			this.transaction.rollback();
		}
		closeTransaction();
		setBookmark();
		closeSession();
    }

	/*------------------------------*/
	/*       Create Statement       */
	/*------------------------------*/

	@Override public Statement createStatement() throws SQLException {
		return createStatement(GraphResultSet.TYPE_FORWARD_ONLY, GraphResultSet.CONCUR_READ_ONLY, GraphResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	@Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return createStatement(resultSetType, resultSetConcurrency, GraphResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	@Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		this.checkClosed();
		this.checkTypeParams(resultSetType);
		this.checkConcurrencyParams(resultSetConcurrency);
		this.checkHoldabilityParams(resultSetHoldability);
		this.initTransaction();
		return BoltGraphStatement.newInstance(false, this, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*-------------------------------*/
	/*       Prepare Statement       */
	/*-------------------------------*/

	@Override public PreparedStatement prepareStatement(String sql) throws SQLException {
		return prepareStatement(nativeSQL(sql), GraphResultSet.TYPE_FORWARD_ONLY, GraphResultSet.CONCUR_READ_ONLY, GraphResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	@Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return prepareStatement(nativeSQL(sql), resultSetType, resultSetConcurrency, GraphResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	@Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		this.checkClosed();
		this.checkTypeParams(resultSetType);
		this.checkConcurrencyParams(resultSetConcurrency);
		this.checkHoldabilityParams(resultSetHoldability);
		this.initTransaction();
		return BoltGraphPreparedStatement.newInstance(false, this, nativeSQL(sql), resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*-------------------*/
	/*       Close       */
	/*-------------------*/

	@Override public boolean isClosed() throws SQLException {
        return this.closed || (this.session != null && !this.session.isOpen());
	}

	@Override public void close() throws SQLException {
		try {
			if (!this.isClosed()) {
				closeTransaction();
				closeSession();
			}
		} catch (Exception e) {
			throw new SQLException("A database access error has occurred: " + e.getMessage());
		} finally {
			this.closed = true;
		}
	}

	private void closeSession() {
		if (this.session != null && this.session.isOpen()) {
			BoltGraphUtils.closeSafely(this.session, LOGGER);
		}
		this.session = null;
	}

	private void closeTransaction() {
		if (this.transaction != null && this.transaction.isOpen()) {
			BoltGraphUtils.closeSafely(this.transaction, LOGGER);
		}
		this.transaction = null;
	}


	/*-------------------*/
	/*      isValid      */
	/*-------------------*/

	@Override public boolean isValid(int timeout) throws SQLException {
		if (timeout < 0) {
			throw new SQLException("Timeout can't be less than zero");
		}
		if (this.isClosed()) {
			return false;
		}

		Runnable r = () -> {
			try (Session s = newGraphSession(); Transaction tr = s.beginTransaction()) {
				tr.run(FASTEST_STATEMENT);
				tr.commit();
			}
		};

		try {
			TimeLimitedCodeBlock.runWithTimeout(r, timeout, TimeUnit.SECONDS);
		} catch (Exception e) { // also timeout
			LOGGER.log(Level.FINEST, "Catch exception totally fine", e);
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------*/
	/*       Some useful initializer and check method        */
	/*-------------------------------------------------------*/

	/**
	 * BOLT Session is initialized right before the very first statement is created
	 * in order to check whether the connection is in readonly mode or not.
	 * This way we point to the right cluster instance (core vs read replica).
	 */
	private void initSession() {
		this.session = newGraphSession();
    }

    private void initTransaction()  {
	    try {
			if (this.getAutoCommit()) {
				doCommit();
				initSession();
			} else if (this.session == null) {
				initSession();
			}
			if (this.transaction == null) {
				this.transaction = this.session.beginTransaction();
			}
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
