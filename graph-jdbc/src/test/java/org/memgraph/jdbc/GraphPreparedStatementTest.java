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
 * <p>
 * Created on 09/03/16
 */
package org.memgraph.jdbc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author AgileLARUS
 * @since 3.0.0
 */
public class GraphPreparedStatementTest {

	@Rule public ExpectedException expectedEx = ExpectedException.none();

	/*------------------------------*/
	/*         isWrapperFor         */
	/*------------------------------*/

	@Test public void isWrapperForShouldReturnTrue() throws SQLException {
		GraphPreparedStatement preparedStatement = mock(GraphPreparedStatement.class, Mockito.CALLS_REAL_METHODS);

		assertTrue(preparedStatement.isWrapperFor(GraphPreparedStatement.class));
		assertTrue(preparedStatement.isWrapperFor(java.sql.Statement.class));
		assertTrue(preparedStatement.isWrapperFor(java.sql.PreparedStatement.class));
		assertTrue(preparedStatement.isWrapperFor(java.sql.Wrapper.class));
		assertTrue(preparedStatement.isWrapperFor(java.lang.AutoCloseable.class));
	}

	@Test public void isWrapperForShouldReturnFalse() throws SQLException {
		GraphPreparedStatement preparedStatement = mock(GraphPreparedStatement.class, Mockito.CALLS_REAL_METHODS);

		assertFalse(preparedStatement.isWrapperFor(GraphResultSet.class));
		assertFalse(preparedStatement.isWrapperFor(java.sql.Driver.class));
		assertFalse(preparedStatement.isWrapperFor(GraphResultSet.class));
		assertFalse(preparedStatement.isWrapperFor(java.sql.ResultSet.class));
	}

	/*------------------------------*/
	/*            unwrap            */
	/*------------------------------*/

	@Test public void unwrapShouldReturnCorrectClass() throws SQLException {
		GraphPreparedStatement preparedStatement = mock(GraphPreparedStatement.class, Mockito.CALLS_REAL_METHODS);

		assertNotNull(preparedStatement.unwrap(GraphPreparedStatement.class));
		assertNotNull(preparedStatement.unwrap(java.sql.Statement.class));
		assertNotNull(preparedStatement.unwrap(java.sql.PreparedStatement.class));
		assertNotNull(preparedStatement.unwrap(java.sql.Wrapper.class));
		assertNotNull(preparedStatement.unwrap(java.lang.AutoCloseable.class));
	}

	@Test public void unwrapShouldThrowException() throws SQLException {
		expectedEx.expect(SQLException.class);

		GraphPreparedStatement preparedStatement = mock(GraphPreparedStatement.class, Mockito.CALLS_REAL_METHODS);

		preparedStatement.unwrap(GraphResultSet.class);
	}

	/*------------------------------*/
	/*           addBatch           */
	/*------------------------------*/

	@Test public void addBatchShouldThrowException() throws SQLException {
		expectedEx.expect(SQLException.class);

		GraphPreparedStatement stmt = Mockito.mock(GraphPreparedStatement.class, Mockito.CALLS_REAL_METHODS);

		stmt.addBatch("");
	}
}
