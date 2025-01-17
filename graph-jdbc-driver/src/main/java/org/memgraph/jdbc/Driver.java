/*
 *
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
 * Created on 19/4/2016
 *
 */

package org.memgraph.jdbc;

import org.memgraph.jdbc.bolt.BoltDriver;
import org.memgraph.jdbc.boltrouting.BoltRoutingGraphDriver;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Driver extends GraphDriver {

	/**
	 * Prefix/class hashMap of all available Driver.
	 */
	@SuppressWarnings({ "rawtypes", "serial" })
	private static final Map<String, Class> DRIVERS = new HashMap<>();

	static {
		DRIVERS.put(BoltRoutingGraphDriver.JDBC_BOLT_ROUTING_PREFIX, BoltRoutingGraphDriver.class);
		DRIVERS.put(BoltDriver.JDBC_BOLT_PREFIX, BoltDriver.class);
	}

	/**
	 * Default constructor.
	 * @throws SQLException sqlexception
	 */
	public Driver() throws SQLException {
		super(null);
	}

	@Override public Connection connect(String url, Properties info) throws SQLException {
		Connection connection = null;
		if(null != getDriver(url)) {
			connection = getDriver(url).connect(url, info);
		}
		return connection;
	}

	/**
	 * Retrieve the corresponding driver from the JDBC url.
	 * @param url The JDBC url
	 * @return The driver
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private GraphDriver getDriver(String url) throws SQLException {
		GraphDriver driver = null;

		if (url == null) {
			throw new SQLException("null is not a valid url");
		}

		// We search the driver prefix from the url
		if (url.startsWith(JDBC_PREFIX)) {
			String[] pieces = url.split(":");
			if (pieces.length > 3) {
				String prefix = pieces[2];

				// We look into driver map is it known
				driver = getDriverForPrefix(prefix);
			}
		}
		return driver;
	}

	private GraphDriver getDriverForPrefix(String prefix) throws SQLException {
		GraphDriver driver = null;
		try {
			for (Map.Entry<String, Class> entry : DRIVERS.entrySet()) {
				if (prefix.matches(entry.getKey())) {
					Constructor constructor = entry.getValue().getDeclaredConstructor();
					driver = (GraphDriver) constructor.newInstance();
				}
			}
		} catch (Exception e) {
			throw new SQLException(e);
		}
		return driver;
	}
}
