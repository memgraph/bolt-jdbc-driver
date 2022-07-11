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
 * Created on 09/03/16
 */
package org.memgraph.jdbc;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author AgileLARUS
 * @since 3.0.0
 */
public class MemgraphResultSetMetaDataTest {

	private MemgraphResultSetMetaData rsmd;

	@Rule public ExpectedException expectedEx = ExpectedException.none();

	@Before public void tearUp() throws SQLException {
		this.rsmd = mock(MemgraphResultSetMetaData.class, Mockito.CALLS_REAL_METHODS);
	}

	/*------------------------------*/
	/*       isAutoIncrement        */
	/*------------------------------*/
	@Test public void isAutoIncrementShouldAlwaysReturnFalse() throws SQLException {
		assertFalse(this.rsmd.isAutoIncrement(-1));
		assertFalse(this.rsmd.isAutoIncrement(0));
		assertFalse(this.rsmd.isAutoIncrement(1));
	}

	/*------------------------------*/
	/*         isSearchable         */
	/*------------------------------*/
	@Test public void isSearchableShouldReturnTrueIfExistingColumnIndex() throws SQLException {
		when(this.rsmd.getColumnCount()).thenReturn(2);
		assertTrue(this.rsmd.isSearchable(1));
		assertTrue(this.rsmd.isSearchable(2));
	}

	@Test public void isSearchableShouldAlwaysReturnFalseIfIndexLessThanZero() throws SQLException {
		when(this.rsmd.getColumnCount()).thenReturn(2);
		assertFalse(this.rsmd.isSearchable(-1));
	}

	@Test public void isSearchableShouldAlwaysReturnFalseIfIndexOutOfRange() throws SQLException {
		when(this.rsmd.getColumnCount()).thenReturn(2);
		assertFalse(this.rsmd.isSearchable(3));
	}

	/*------------------------------*/
	/*          isNullable          */
	/*------------------------------*/
	@Test public void isNullableShouldReturnAlwaysNotNull() throws SQLException {
		assertEquals(this.rsmd.isNullable(0), MemgraphResultSetMetaData.columnNoNulls);
		assertEquals(this.rsmd.isNullable(-1), MemgraphResultSetMetaData.columnNoNulls);
		assertEquals(this.rsmd.isNullable(1), MemgraphResultSetMetaData.columnNoNulls);
	}

	/*------------------------------*/
	/*         isWrapperFor         */
	/*------------------------------*/

	@Test public void isWrapperForShouldReturnTrue() throws SQLException {
		MemgraphResultSetMetaData resultSetMetaData = mock(MemgraphResultSetMetaData.class, Mockito.CALLS_REAL_METHODS);

		assertTrue(resultSetMetaData.isWrapperFor(MemgraphResultSetMetaData.class));
		assertTrue(resultSetMetaData.isWrapperFor(java.sql.ResultSetMetaData.class));
		assertTrue(resultSetMetaData.isWrapperFor(java.sql.Wrapper.class));
	}

	@Test public void isWrapperForShouldReturnFalse() throws SQLException {
		MemgraphResultSetMetaData resultSetMetaData = mock(MemgraphResultSetMetaData.class, Mockito.CALLS_REAL_METHODS);

		assertFalse(resultSetMetaData.isWrapperFor(MemgraphStatement.class));
		assertFalse(resultSetMetaData.isWrapperFor(java.sql.Driver.class));
		assertFalse(resultSetMetaData.isWrapperFor(MemgraphResultSet.class));
		assertFalse(resultSetMetaData.isWrapperFor(java.sql.ResultSet.class));
	}

	/*------------------------------*/
	/*            unwrap            */
	/*------------------------------*/

	@Test public void unwrapShouldReturnCorrectClass() throws SQLException {
		MemgraphResultSetMetaData resultSetMetaData = mock(MemgraphResultSetMetaData.class, Mockito.CALLS_REAL_METHODS);

		assertNotNull(resultSetMetaData.unwrap(MemgraphResultSetMetaData.class));
		assertNotNull(resultSetMetaData.unwrap(java.sql.ResultSetMetaData.class));
		assertNotNull(resultSetMetaData.unwrap(java.sql.Wrapper.class));
	}

	@Test public void unwrapShouldThrowException() throws SQLException {
		expectedEx.expect(SQLException.class);

		MemgraphResultSetMetaData resultSetMetaData = mock(MemgraphResultSetMetaData.class, Mockito.CALLS_REAL_METHODS);

		resultSetMetaData.unwrap(MemgraphStatement.class);
	}
}
