/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package nabu.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebParam;

import nabu.utils.types.ParameterDescription;
import nabu.utils.types.Table;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.DataSourceProviderArtifact;

public class Database {
	public List<Table> tables(@WebParam(name = "connectionId") java.lang.String connectionId) throws SQLException {
		if (connectionId == null) {
			return null;
		}
		DataSourceProviderArtifact resolve = (DataSourceProviderArtifact) EAIResourceRepository.getInstance().resolve(connectionId);
		if (resolve == null) {
			throw new IllegalArgumentException("Could not find connection: " + connectionId);
		}
		List<Table> tables = new ArrayList<Table>();
		Connection connection = resolve.getDataSource().getConnection();
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet tableResult = metaData.getTables(null, null, null, new java.lang.String[] { "TABLE", "VIEW" });
			try {
				while (tableResult.next()) {
					java.lang.String catalog = tableResult.getString("TABLE_CAT");
					Table table = new Table();
					table.setName(tableResult.getString("TABLE_NAME"));
					table.setSchema(tableResult.getString("TABLE_SCHEM"));
					
					ResultSet columnResult = metaData.getColumns(catalog, table.getSchema(), table.getName(), null);
					try {
						List<ParameterDescription> descriptions = new ArrayList<ParameterDescription>();
						while (columnResult.next()) {
							ParameterDescription description = new ParameterDescription();
							description.setName(columnResult.getString("COLUMN_NAME"));
							// TODO
							descriptions.add(description);
						}
					}
					finally {
						columnResult.close();
					}
				}
			}
			finally {
				tableResult.close();
			}
		}
		finally {
			connection.close();
		}
		return tables;
	}
}
