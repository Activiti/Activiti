/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.profiler;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.impl.db.BulkDeleteOperation;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.entity.Entity;


public class ProfilingDbSqlSession extends DbSqlSession {

  protected CommandExecutionResult commandExecutionResult;

  public ProfilingDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, EntityCache entityCache) {
    super(dbSqlSessionFactory, entityCache);
  }

  public ProfilingDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, EntityCache entityCache, Connection connection, String catalog, String schema) {
    super(dbSqlSessionFactory, entityCache, connection, catalog, schema);
  }

  // public ProfilingDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
  // super(dbSqlSessionFactory);
  // }
  //
  // public ProfilingDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory,
  // Connection connection, String catalog, String schema) {
  // super(dbSqlSessionFactory, connection, catalog, schema);
  // }

  @Override
  public void flush() {
    long startTime = System.currentTimeMillis();
    super.flush();
    long endTime = System.currentTimeMillis();

    CommandExecutionResult commandExecutionResult = getCurrentCommandExecution();
    if (commandExecutionResult != null) {
      commandExecutionResult.addDatabaseTime(endTime - startTime);
    }
  }

  @Override
  public void commit() {

    long startTime = System.currentTimeMillis();
    super.commit();
    long endTime = System.currentTimeMillis();

    CommandExecutionResult commandExecutionResult = getCurrentCommandExecution();
    if (commandExecutionResult != null) {
      commandExecutionResult.addDatabaseTime(endTime - startTime);
    }
  }

  // SELECT ONE

  @Override
  public Object selectOne(String statement, Object parameter) {
    if (getCurrentCommandExecution() != null) {
      getCurrentCommandExecution().addDbSelect(statement);
    }
    return super.selectOne(statement, parameter);
  }

  @Override
  public <T extends Entity> T selectById(Class<T> entityClass, String id, boolean useCache) {
    if (getCurrentCommandExecution() != null) {
      getCurrentCommandExecution().addDbSelect("selectById " + entityClass.getName());
    }
    return super.selectById(entityClass, id, useCache);
  }

  // SELECT LIST

  @Override
  public List selectListWithRawParameter(String statement, Object parameter, int firstResult, int maxResults, boolean useCache) {
    if (getCurrentCommandExecution() != null) {
      getCurrentCommandExecution().addDbSelect(statement);
    }
    return super.selectListWithRawParameter(statement, parameter, firstResult, maxResults, useCache);
  }

  @Override
  public List selectListWithRawParameterWithoutFilter(String statement, Object parameter, int firstResult, int maxResults) {
    if (getCurrentCommandExecution() != null) {
      getCurrentCommandExecution().addDbSelect(statement);
    }
    return super.selectListWithRawParameterWithoutFilter(statement, parameter, firstResult, maxResults);
  }

  // INSERTS

  @Override
  protected void flushRegularInsert(Entity entity, Class<? extends Entity> clazz) {
    super.flushRegularInsert(entity, clazz);
    if (getCurrentCommandExecution() != null) {
      getCurrentCommandExecution().addDbInsert(clazz.getName());
    }
  }

  @Override
  protected void flushBulkInsert(Collection<Entity> entities, Class<? extends Entity> clazz) {
    if (getCurrentCommandExecution() != null && entities.size() > 0) {
      getCurrentCommandExecution().addDbInsert(clazz.getName() + "-bulk-with-" + entities.size());
    }
    super.flushBulkInsert(entities, clazz);
  }

  // UPDATES

  @Override
  protected void flushUpdates() {
    super.flushUpdates();
    if (getCurrentCommandExecution() != null) {
      for (Entity persistentObject : updatedObjects) {
        getCurrentCommandExecution().addDbUpdate(persistentObject.getClass().getName());
      }
    }
  }

  // DELETES

  @Override
  protected void flushDeleteEntities(Class<? extends Entity> entityClass, Collection<Entity> entitiesToDelete) {
    super.flushDeleteEntities(entityClass, entitiesToDelete);
    if (getCurrentCommandExecution() != null) {
      for (Entity entity : entitiesToDelete) {
        getCurrentCommandExecution().addDbDelete(entity.getClass().getName());
      }
    }
  }

  @Override
  protected void flushBulkDeletes(Class<? extends Entity> entityClass) {
    if (getCurrentCommandExecution() != null) {
      if (bulkDeleteOperations.containsKey(entityClass)) {
        for (BulkDeleteOperation bulkDeleteOperation : bulkDeleteOperations.get(entityClass)) {
          getCurrentCommandExecution().addDbDelete("Bulk-delete-" + bulkDeleteOperation.getClass());
        }
      }
    }
    super.flushBulkDeletes(entityClass);
  }

  public CommandExecutionResult getCurrentCommandExecution() {
    if (commandExecutionResult == null) {
      ProfileSession profileSession = ActivitiProfiler.getInstance().getCurrentProfileSession();
      if (profileSession != null) {
        this.commandExecutionResult = profileSession.getCurrentCommandExecution();
      }
    }
    return commandExecutionResult;
  }
}
