/**
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
 * Created on 01/12/17
 */
package org.memgraph.jdbc.bolt;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.memgraph.jdbc.bolt.utils.JdbcConnectionTestUtils;
import org.testcontainers.containers.Neo4jContainer;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.memgraph.jdbc.bolt.utils.MemgraphContainerUtils.createNeo4jContainer;

/**
 * @author AgileLARUS
 *
 * @since 3.0.0
 */
public class BoltMemgraphDataSourceIT {
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@ClassRule
	public static final Neo4jContainer<?> neo4j = createNeo4jContainer();

	/*------------------------------*/
	/*        getConnection         */
	/*------------------------------*/

	@Test public void getConnectionShouldWork() throws SQLException {

		BoltMemgraphDataSource boltMemgraphDataSource = new BoltMemgraphDataSource();
		URI boltUri = URI.create(neo4j.getBoltUrl());
		boltMemgraphDataSource.setServerName(boltUri.getHost());
		boltMemgraphDataSource.setPortNumber(boltUri.getPort());
		boltMemgraphDataSource.setIsSsl(JdbcConnectionTestUtils.SSL_ENABLED);
		boltMemgraphDataSource.setUser(JdbcConnectionTestUtils.USERNAME);
		boltMemgraphDataSource.setPassword(JdbcConnectionTestUtils.PASSWORD);

		Connection connection = boltMemgraphDataSource.getConnection();
		assertNotNull(connection);

		Statement statement = connection.createStatement();
		assertTrue(statement.execute("RETURN 1"));

		statement.close();
		JdbcConnectionTestUtils.closeConnection(connection);
	}
}