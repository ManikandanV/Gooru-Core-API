/**
 * 
 */
package org.ednovo.gooru.cassandra.core.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.Rows;

/**
 * @author Search Team
 * 
 */
public interface RawCassandraDao extends CassandraDao {

	void save(String key, Map<String, Object> entity);
	
	void save(String rowKey, String column, String value);
	
	void save(String key,
			Map<String, Object> entity,
			String prefix,
			boolean reset);
	
	ColumnList<String> read(String rowKey);
	
	Rows<String,String> getAll();
	
	Rows<String,String> read(Collection<String> keys);
	
	void delete(String... ids);
	
	void delete(String rowKey, String column);
	
	void addIndexQueueEntry(String key, String columnPrefix, List<String> gooruOids);

	Rows<String, String> readIndexQueuedData(Integer limit);
	
	void deleteIndexQueue(String rowKey, Collection<String> columns);

	void updateQueueStatus(String columnName, String rowKey, String prefix);

	void addIndexFailedEntry(String id, String type, String message, String date);

}
