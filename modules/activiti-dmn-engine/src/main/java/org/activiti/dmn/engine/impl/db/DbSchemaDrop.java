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

package org.activiti.dmn.engine.impl.db;

import javax.sql.DataSource;

import org.activiti.dmn.engine.DmnEngine;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.DmnEngines;
import org.apache.commons.lang3.StringUtils;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * @author Tijs Rademakers
 */
public class DbSchemaDrop {

  public static void main(String[] args) {
    try {
      DmnEngine dmnEngine = DmnEngines.getDefaultDmnEngine();
      DataSource dataSource = dmnEngine.getDmnEngineConfiguration().getDataSource();
      
      DatabaseConnection connection = new JdbcConnection(dataSource.getConnection());
      Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
      database.setDatabaseChangeLogTableName(DmnEngineConfiguration.LIQUIBASE_CHANGELOG_PREFIX+database.getDatabaseChangeLogTableName());
      database.setDatabaseChangeLogLockTableName(DmnEngineConfiguration.LIQUIBASE_CHANGELOG_PREFIX+database.getDatabaseChangeLogLockTableName());
      
      if (StringUtils.isNotEmpty(dmnEngine.getDmnEngineConfiguration().getDatabaseSchema())) {
        database.setDefaultSchemaName(dmnEngine.getDmnEngineConfiguration().getDatabaseSchema());
        database.setLiquibaseSchemaName(dmnEngine.getDmnEngineConfiguration().getDatabaseSchema());
      }
      
      if (StringUtils.isNotEmpty(dmnEngine.getDmnEngineConfiguration().getDatabaseCatalog())) {
        database.setDefaultCatalogName(dmnEngine.getDmnEngineConfiguration().getDatabaseCatalog());
        database.setLiquibaseCatalogName(dmnEngine.getDmnEngineConfiguration().getDatabaseCatalog());
      }
  
      Liquibase liquibase = new Liquibase("org/activiti/dmn/db/liquibase/activiti-dmn-db-changelog.xml", new ClassLoaderResourceAccessor(), database);
      liquibase.dropAll();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
