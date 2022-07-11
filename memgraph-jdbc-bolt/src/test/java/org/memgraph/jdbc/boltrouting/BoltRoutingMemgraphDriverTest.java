/*
 * Copyright (c) 2018 LARUS Business Automation [http://www.larus-ba.it]
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
 */
package org.memgraph.jdbc.boltrouting;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.memgraph.jdbc.MemgraphDriver;

import java.sql.SQLException;

import static junit.framework.TestCase.assertFalse;

/**
 * @author AgileLARUS
 * @since 3.3.1
 */
public class BoltRoutingMemgraphDriverTest {

	@Rule public ExpectedException expectedEx = ExpectedException.none();

	@Test public void shoulNotAcceptURL() throws SQLException {
		MemgraphDriver driver = new BoltRoutingMemgraphDriver();
		assertFalse(driver.acceptsURL("jdbc:neo4j:http://localhost:7687"));
		assertFalse(driver.acceptsURL("jdbc:file://192.168.0.1:7687"));
		assertFalse(driver.acceptsURL("bolt://localhost:7687"));
	}

	@Test public void shouldThrowException() throws SQLException {
		expectedEx.expect(SQLException.class);

		MemgraphDriver driver = new BoltRoutingMemgraphDriver();
		assertFalse(driver.acceptsURL(null));
	}
}
