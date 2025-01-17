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
 * Created on 03/02/16
 */
package org.memgraph.jdbc;

import org.memgraph.jdbc.utils.ExceptionBuilder;

import java.sql.SQLException;

/**
 * @author AgileLARUS
 * @since 3.0.0
 */
public abstract class GraphParameterMetaData implements java.sql.ParameterMetaData {

	@Override
	public int getParameterCount() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override
	public int isNullable(int param) throws SQLException {
		return parameterNullable;
	}

	@Override
	public boolean isSigned(int param) throws SQLException {
		return false;
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		return 0;
	}

	@Override
	public int getScale(int column) throws SQLException {
		return 0;
	}

	@Override public int getParameterType(int param) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public String getParameterTypeName(int param) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override
	public String getParameterClassName(int param) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override
	public int getParameterMode(int param) throws SQLException {
		return parameterModeUnknown;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return Wrapper.unwrap(iface, this);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return Wrapper.isWrapperFor(iface, this.getClass());
	}
}
