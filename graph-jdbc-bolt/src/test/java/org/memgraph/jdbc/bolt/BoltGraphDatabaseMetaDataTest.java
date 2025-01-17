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
 * Created on 01/03/2016
 */
package org.memgraph.jdbc.bolt;

import org.junit.Test;
import org.mockito.Mockito;
import org.memgraph.jdbc.bolt.impl.BoltGraphConnectionImpl;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * GraphDatabaseMetaData Tests class
 *
 * @author AgileLARUS
 * @since 3.0.0
 */
public class BoltGraphDatabaseMetaDataTest {

	/*------------------------------*/
	/*        getConnection         */
	/*------------------------------*/

	@Test public void getConnectionShouldGetConnection() throws SQLException {
		BoltGraphConnectionImpl connection = Mockito.mock(BoltGraphConnectionImpl.class, Mockito.RETURNS_DEEP_STUBS);
		BoltGraphDatabaseMetaData boltDatabaseMetaData = new BoltGraphDatabaseMetaData(connection);

		assertEquals(connection, boltDatabaseMetaData.getConnection());
	}
}
