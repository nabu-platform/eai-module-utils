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
