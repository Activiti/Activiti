/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.db;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.management.TableMetaData;
/**
 * @author Christian Muelder
 */
public class PostgresDbMetaDataHandler extends StdDbMetaDataHandler implements DbMetaDataHandler {

	@Override
	public void addToCache(TableMetaData metaData) {
		toUpperCase(metaData);
		super.addToCache(metaData);
	}

	private void toUpperCase(TableMetaData metaData) {
		List<String> list = new ArrayList<String>();
		for(String columnNames : metaData.getColumnNames())
		{
			list.add(columnNames.toUpperCase());
		}
		metaData.setColumnNames(list);

		list = new ArrayList<String>();
		for(String columnTypes : metaData.getColumnTypes())
		{
			list.add(columnTypes.toUpperCase());
		}
		metaData.setColumnTypes(list);
	}

	@Override
	public String handleTableName(String table) {
		return table.toLowerCase();
	}

}
