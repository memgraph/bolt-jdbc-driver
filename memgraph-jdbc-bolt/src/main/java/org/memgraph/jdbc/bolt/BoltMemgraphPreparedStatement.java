/*
 * Copyright (c) 2016 LARUS Business Automation [http://www.larus-ba.it]
 * <p>
 * This file is part of the "LARUS Integration Framework for Memgraph".
 * <p>
 * The "LARUS Integration Framework for Memgraph" is licensed under the Apache License, Version 2.0 (the "License");
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
 * <p>
 * Created on 23/03/16
 */
package org.memgraph.jdbc.bolt;

import org.neo4j.driver.Result;
import org.neo4j.driver.summary.SummaryCounters;
import org.memgraph.jdbc.Loggable;
import org.memgraph.jdbc.MemgraphParameterMetaData;
import org.memgraph.jdbc.MemgraphPreparedStatement;
import org.memgraph.jdbc.MemgraphResultSetMetaData;
import org.memgraph.jdbc.bolt.impl.BoltMemgraphConnectionImpl;
import org.memgraph.jdbc.utils.BoltMemgraphUtils;
import org.memgraph.jdbc.utils.MemgraphInvocationHandler;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.copyOf;
import static org.memgraph.jdbc.utils.BoltMemgraphUtils.executeInTx;
import static org.memgraph.jdbc.utils.BoltMemgraphUtils.hasResultSet;

public class BoltMemgraphPreparedStatement extends MemgraphPreparedStatement implements Loggable {

	private final ResultSetFactory resultSetFactory;

	private BoltMemgraphPreparedStatement(BoltMemgraphConnectionImpl connection, String rawStatement, int... rsParams) {
		this(connection, BoltMemgraphResultSet::newInstance, rawStatement, rsParams);
	}

	// visible for testing
	BoltMemgraphPreparedStatement(BoltMemgraphConnectionImpl connection, ResultSetFactory resultSetFactory, String rawStatement, int... rsParams) {
		super(connection, rawStatement);
		this.resultSetFactory = resultSetFactory;
		this.resultSetParams = rsParams;
	}

	public static PreparedStatement newInstance(boolean debug, BoltMemgraphConnectionImpl connection, String rawStatement, int... rsParams) {
		PreparedStatement ps = new BoltMemgraphPreparedStatement(connection, rawStatement, rsParams);
		((MemgraphPreparedStatement) ps).setDebug(debug);
		return (PreparedStatement) Proxy.newProxyInstance(BoltMemgraphPreparedStatement.class.getClassLoader(), new Class[] { PreparedStatement.class },
				new MemgraphInvocationHandler(ps, debug));
	}

	@Override public ResultSet executeQuery() throws SQLException {
		return executeInternal((result) -> {
			this.currentResultSet = this.resultSetFactory.create(this.hasDebug(), this, result, this.resultSetParams);
			this.currentUpdateCount = -1;
			return currentResultSet;
		});
	}

	@Override public int executeUpdate() throws SQLException {
		return executeInternal((result) -> {
			SummaryCounters stats = result.consume().counters();
			this.currentUpdateCount = BoltMemgraphUtils.calculateUpdateCount(stats);
			this.currentResultSet = null;
			return this.currentUpdateCount;
		});
	}

	@Override public boolean execute() throws SQLException {
		return executeInternal((result) -> {
			boolean hasResultSet = hasResultSet((BoltMemgraphConnection) this.connection, this.statement);
			if (hasResultSet) {
				this.currentResultSet = this.resultSetFactory.create(this.hasDebug(), this, result, this.resultSetParams);
				this.currentUpdateCount = -1;
			} else {
				this.currentResultSet = null;
				SummaryCounters stats = result.consume().counters();
				this.currentUpdateCount = BoltMemgraphUtils.calculateUpdateCount(stats);
			}
			return hasResultSet;
		});
	}

	private <T> T executeInternal(Function<Result, T> body) throws SQLException {
		this.checkClosed();
		return executeInTx((BoltMemgraphConnection) this.connection, this.statement, this.parameters, body);
	}

	@Override public MemgraphParameterMetaData getParameterMetaData() throws SQLException {
		this.checkClosed();
		return new BoltMemgraphParameterMetaData(this);
	}

	@Override public MemgraphResultSetMetaData getMetaData() throws SQLException {
		return this.currentResultSet == null ? null : (MemgraphResultSetMetaData) this.currentResultSet.getMetaData();
	}

	/*-------------------*/
	/*       Batch       */
	/*-------------------*/

	@Override public int[] executeBatch() throws SQLException {
		this.checkClosed();
		int[] result = new int[0];
		try {
			BoltMemgraphConnection connection = (BoltMemgraphConnection) this.connection;
			for (Map<String, Object> parameter : this.batchParameters) {
				int count = executeInTx(connection, this.statement, parameter, (statementResult) -> {
					SummaryCounters counters = statementResult.consume().counters();
					return counters.nodesCreated() + counters.nodesDeleted();
				});
				result = copyOf(result, result.length + 1);
				result[result.length - 1] = count;
			}
		} catch (Exception e) {
			throw new BatchUpdateException(result, e);
		}
		return result;
	}

	/*-------------------*/
	/*   setParameter    */
	/*-------------------*/

	protected void setTemporal(int parameterIndex, long epoch, ZoneId zone, Function<ZonedDateTime, Temporal> extractTemporal) throws SQLException {
		checkClosed();
		checkParamsNumber(parameterIndex);

		ZonedDateTime zdt = Instant.ofEpochMilli(epoch).atZone(zone);

		insertParameter(parameterIndex, extractTemporal.apply(zdt));
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		setTemporal(parameterIndex, x.getTime(),ZoneId.systemDefault(), (zdt)-> zdt.toLocalDate());
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		setTemporal(parameterIndex, x.getTime(),ZoneId.systemDefault(), (zdt)-> zdt.toLocalTime());
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		setTemporal(parameterIndex, x.getTime(),ZoneId.systemDefault(), (zdt)-> zdt.toLocalDateTime());
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		setTemporal(parameterIndex, x.getTime(),cal.getTimeZone().toZoneId(), (zdt)-> zdt);
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		setTemporal(parameterIndex, x.getTime(),cal.getTimeZone().toZoneId(), (zdt)-> zdt);
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		setTemporal(parameterIndex, x.getTime(),cal.getTimeZone().toZoneId(), (zdt)-> zdt.toOffsetDateTime().toOffsetTime());
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		checkClosed();
		insertParameter(parameterIndex, x.getArray());
	}

	// visible for testing
	Map<String, Object> getParameters() {
		return this.parameters;
	}

	// visible for testing
	List<Map<String, Object>> getBatchParameters() {
		return this.batchParameters;
	}
}