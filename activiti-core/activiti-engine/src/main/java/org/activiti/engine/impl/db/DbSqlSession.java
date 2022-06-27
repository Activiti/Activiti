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


package org.activiti.engine.impl.db;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiWrongDbException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.ModelQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.upgrade.DbUpgradeStep;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.cache.CachedEntity;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSqlSession implements Session {

    private static final Logger log = LoggerFactory.getLogger(DbSqlSession.class);

    protected static final String LAST_V5_VERSION = "5.99.0.0";

    protected static final List<ActivitiVersion> ACTIVITI_VERSIONS = new ArrayList<ActivitiVersion>();

    static {

    /* Previous */

        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.7"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.8"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.9"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.10"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.11"));

        // 5.12.1 was a bugfix release on 5.12 and did NOT change the version in ACT_GE_PROPERTY
        // On top of that, DB2 create script for 5.12.1 was shipped with a 'T' suffix ...
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.12",
                                                  asList("5.12.1",
                                                         "5.12T")));

        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.13"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.14"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.15"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.15.1"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.1"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.2-SNAPSHOT"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.2"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.3.0"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.16.4.0"));

        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.17.0.0"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.17.0.1"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.17.0.2"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.18.0.0"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.18.0.1"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.20.0.0"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.20.0.1"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.20.0.2"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("5.21.0.0"));

    /*
     * Version 5.18.0.1 is the latest v5 version in the list here, although if you would look at the v5 code,
     * you'll see there are a few other releases afterwards.
     *
     * The reasoning is as follows: after 5.18.0.1, no database changes were done anymore.
     * And if there would be database changes, they would have been part of both 5.x _and_ 6.x upgrade scripts.
     * The logic below will assume it's one of these releases in case it isn't found in the list here
     * and do the upgrade from the 'virtual' release 5.99.0.0 to make sure th v6 changes are applied.
     */

        // This is the latest version of the 5 branch. It's a 'virtual' version cause it doesn't exist, but it is
        // there to make sure all previous version can upgrade to the 6 version correctly.
        ACTIVITI_VERSIONS.add(new ActivitiVersion(LAST_V5_VERSION));

        // Version 6
        ACTIVITI_VERSIONS.add(new ActivitiVersion("6.0.0.0"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("6.0.0.1"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("6.0.0.2"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("6.0.0.3"));

        // Version 7
        ACTIVITI_VERSIONS.add(new ActivitiVersion("7.0.0.0"));
        ACTIVITI_VERSIONS.add(new ActivitiVersion("7.1.0.0"));

        /* Current */
        ACTIVITI_VERSIONS.add(new ActivitiVersion(ProcessEngine.VERSION));
    }

    protected SqlSession sqlSession;
    protected DbSqlSessionFactory dbSqlSessionFactory;
    protected EntityCache entityCache;

    protected Map<Class<? extends Entity>, Map<String, Entity>> insertedObjects
            = new HashMap<Class<? extends Entity>, Map<String, Entity>>();
    protected Map<Class<? extends Entity>, Map<String, Entity>> deletedObjects
            = new HashMap<Class<? extends Entity>, Map<String, Entity>>();
    protected Map<Class<? extends Entity>, List<BulkDeleteOperation>> bulkDeleteOperations
            = new HashMap<Class<? extends Entity>, List<BulkDeleteOperation>>();
    protected List<Entity> updatedObjects = new ArrayList<Entity>();

    protected String connectionMetadataDefaultCatalog;
    protected String connectionMetadataDefaultSchema;

    public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory,
                        EntityCache entityCache) {
        this.dbSqlSessionFactory = dbSqlSessionFactory;
        this.sqlSession = dbSqlSessionFactory.getSqlSessionFactory().openSession();
        this.entityCache = entityCache;
        this.connectionMetadataDefaultCatalog = dbSqlSessionFactory.getDatabaseCatalog();
        this.connectionMetadataDefaultSchema = dbSqlSessionFactory.getDatabaseSchema();
    }

    public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory,
                        EntityCache entityCache,
                        Connection connection,
                        String catalog,
                        String schema) {
        this.dbSqlSessionFactory = dbSqlSessionFactory;
        this.sqlSession = dbSqlSessionFactory.getSqlSessionFactory().openSession(connection); // Note the use of connection param here, different from other constructor
        this.entityCache = entityCache;
        this.connectionMetadataDefaultCatalog = catalog;
        this.connectionMetadataDefaultSchema = schema;
    }

    // insert ///////////////////////////////////////////////////////////////////

    public void insert(Entity entity) {
        if (entity.getId() == null) {
            String id = dbSqlSessionFactory.getIdGenerator().getNextId();
            entity.setId(id);
        }

        Class<? extends Entity> clazz = entity.getClass();
        if (!insertedObjects.containsKey(clazz)) {
            insertedObjects.put(clazz,
                                new LinkedHashMap<String, Entity>()); // order of insert is important, hence LinkedHashMap
        }

        insertedObjects.get(clazz).put(entity.getId(),
                                       entity);
        entityCache.put(entity,
                        false); // False -> entity is inserted, so always changed
        entity.setInserted(true);
    }

    // update
    // ///////////////////////////////////////////////////////////////////

    public void update(Entity entity) {
        entityCache.put(entity,
                        false); // false -> we don't store state, meaning it will always be seen as changed
        entity.setUpdated(true);
    }

    public int update(String statement,
                      Object parameters) {
        String updateStatement = dbSqlSessionFactory.mapStatement(statement);
        return getSqlSession().update(updateStatement,
                                      parameters);
    }

    // delete
    // ///////////////////////////////////////////////////////////////////

    /**
     * Executes a {@link BulkDeleteOperation}, with the sql in the statement parameter.
     * The passed class determines when this operation will be executed: it will be executed
     * when the particular class has passed in the {@link EntityDependencyOrder}.
     */
    public void delete(String statement,
                       Object parameter,
                       Class<? extends Entity> entityClass) {
        if (!bulkDeleteOperations.containsKey(entityClass)) {
            bulkDeleteOperations.put(entityClass,
                                     new ArrayList<BulkDeleteOperation>(1));
        }
        bulkDeleteOperations.get(entityClass).add(new BulkDeleteOperation(dbSqlSessionFactory.mapStatement(statement),
                                                                          parameter));
    }

    public void delete(Entity entity) {
        Class<? extends Entity> clazz = entity.getClass();
        if (!deletedObjects.containsKey(clazz)) {
            deletedObjects.put(clazz,
                               new LinkedHashMap<String, Entity>()); // order of insert is important, hence LinkedHashMap
        }
        deletedObjects.get(clazz).put(entity.getId(),
                                      entity);
        entity.setDeleted(true);
    }

    // select
    // ///////////////////////////////////////////////////////////////////

    @SuppressWarnings({"rawtypes"})
    public List selectList(String statement) {
        return selectList(statement,
                          null,
                          0,
                          Integer.MAX_VALUE);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           Object parameter) {
        return selectList(statement,
                          parameter,
                          0,
                          Integer.MAX_VALUE);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           Object parameter,
                           boolean useCache) {
        return selectList(statement,
                          parameter,
                          0,
                          Integer.MAX_VALUE,
                          useCache);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           Object parameter,
                           Page page) {
        return selectList(statement,
                          parameter,
                          page,
                          true);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           Object parameter,
                           Page page,
                           boolean useCache) {
        if (page != null) {
            return selectList(statement,
                              parameter,
                              page.getFirstResult(),
                              page.getMaxResults(),
                              useCache);
        } else {
            return selectList(statement,
                              parameter,
                              0,
                              Integer.MAX_VALUE,
                              useCache);
        }
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           ListQueryParameterObject parameter,
                           Page page) {
        return selectList(statement,
                          parameter,
                          page,
                          true);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           ListQueryParameterObject parameter,
                           Page page,
                           boolean useCache) {

        ListQueryParameterObject parameterToUse = parameter;
        if (parameterToUse == null) {
            parameterToUse = new ListQueryParameterObject();
        }

        if (page != null) {
            parameterToUse.setFirstResult(page.getFirstResult());
            parameterToUse.setMaxResults(page.getMaxResults());
        }

        return selectList(statement,
                          parameterToUse,
                          useCache);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           Object parameter,
                           int firstResult,
                           int maxResults) {
        return selectList(statement,
                          parameter,
                          firstResult,
                          maxResults,
                          true);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           Object parameter,
                           int firstResult,
                           int maxResults,
                           boolean useCache) {
        return selectList(statement,
                          new ListQueryParameterObject(parameter,
                                                       firstResult,
                                                       maxResults),
                          useCache);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           ListQueryParameterObject parameter) {
        return selectList(statement,
                          parameter,
                          true);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String statement,
                           ListQueryParameterObject parameter,
                           boolean useCache) {
        return selectListWithRawParameter(statement,
                                          parameter,
                                          parameter.getFirstResult(),
                                          parameter.getMaxResults(),
                                          useCache);
    }

    @SuppressWarnings("rawtypes")
    public List selectListWithRawParameter(String statement,
                                           Object parameter,
                                           int firstResult,
                                           int maxResults) {
        return selectListWithRawParameter(statement,
                                          parameter,
                                          firstResult,
                                          maxResults,
                                          true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List selectListWithRawParameter(String statement,
                                           Object parameter,
                                           int firstResult,
                                           int maxResults,
                                           boolean useCache) {
        statement = dbSqlSessionFactory.mapStatement(statement);
        if (firstResult == -1 || maxResults == -1) {
            return emptyList();
        }

        List loadedObjects = sqlSession.selectList(statement,
                                                   parameter);
        if (useCache) {
            return cacheLoadOrStore(loadedObjects);
        } else {
            return loadedObjects;
        }
    }

    @SuppressWarnings({"rawtypes"})
    public List selectListWithRawParameterWithoutFilter(String statement,
                                                        Object parameter,
                                                        int firstResult,
                                                        int maxResults) {
        statement = dbSqlSessionFactory.mapStatement(statement);
        if (firstResult == -1 || maxResults == -1) {
            return emptyList();
        }
        return sqlSession.selectList(statement,
                                     parameter);
    }

    public Object selectOne(String statement,
                            Object parameter) {
        statement = dbSqlSessionFactory.mapStatement(statement);
        Object result = sqlSession.selectOne(statement,
                                             parameter);
        if (result instanceof Entity) {
            Entity loadedObject = (Entity) result;
            result = cacheLoadOrStore(loadedObject);
        }
        return result;
    }

    public <T extends Entity> T selectById(Class<T> entityClass,
                                           String id) {
        return selectById(entityClass,
                          id,
                          true);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T selectById(Class<T> entityClass,
                                           String id,
                                           boolean useCache) {
        T entity = null;

        if (useCache) {
            entity = entityCache.findInCache(entityClass,
                                             id);
            if (entity != null) {
                return entity;
            }
        }

        String selectStatement = dbSqlSessionFactory.getSelectStatement(entityClass);
        selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
        entity = (T) sqlSession.selectOne(selectStatement,
                                          id);
        if (entity == null) {
            return null;
        }

        entityCache.put(entity,
                        true); // true -> store state so we can see later if it is updated later on
        return entity;
    }

    // internal session cache
    // ///////////////////////////////////////////////////

    @SuppressWarnings("rawtypes")
    protected List cacheLoadOrStore(List<Object> loadedObjects) {
        if (loadedObjects.isEmpty()) {
            return loadedObjects;
        }
        if (!(loadedObjects.get(0) instanceof Entity)) {
            return loadedObjects;
        }

        List<Entity> filteredObjects = new ArrayList<Entity>(loadedObjects.size());
        for (Object loadedObject : loadedObjects) {
            Entity cachedEntity = cacheLoadOrStore((Entity) loadedObject);
            filteredObjects.add(cachedEntity);
        }
        return filteredObjects;
    }

    /**
     * Returns the object in the cache. If this object was loaded before, then the original object is returned (the cached version is more recent).
     * If this is the first time this object is loaded, then the loadedObject is added to the cache.
     */
    protected Entity cacheLoadOrStore(Entity entity) {
        Entity cachedEntity = entityCache.findInCache(entity.getClass(),
                                                      entity.getId());
        if (cachedEntity != null) {
            return cachedEntity;
        }
        entityCache.put(entity,
                        true);
        return entity;
    }

    // flush
    // ////////////////////////////////////////////////////////////////////

    public void flush() {
        determineUpdatedObjects(); // Needs to be done before the removeUnnecessaryOperations, as removeUnnecessaryOperations will remove stuff from the cache
        removeUnnecessaryOperations();

        if (log.isDebugEnabled()) {
            debugFlush();
        }

        flushInserts();
        flushUpdates();
        flushDeletes();
    }

    /**
     * Clears all deleted and inserted objects from the cache,
     * and removes inserts and deletes that cancel each other.
     * <p>
     * Also removes deletes with duplicate ids.
     */
    protected void removeUnnecessaryOperations() {

        for (Class<? extends Entity> entityClass : deletedObjects.keySet()) {

            // Collect ids of deleted entities + remove duplicates
            Set<String> ids = new HashSet<String>();
            Iterator<Entity> entitiesToDeleteIterator = deletedObjects.get(entityClass).values().iterator();
            while (entitiesToDeleteIterator.hasNext()) {
                Entity entityToDelete = entitiesToDeleteIterator.next();
                if (!ids.contains(entityToDelete.getId())) {
                    ids.add(entityToDelete.getId());
                } else {
                    entitiesToDeleteIterator.remove(); // Removing duplicate deletes
                }
            }

            // Now we have the deleted ids, we can remove the inserted objects (as they cancel each other)
            for (String id : ids) {
                if (insertedObjects.containsKey(entityClass) && insertedObjects.get(entityClass).containsKey(id)) {
                    insertedObjects.get(entityClass).remove(id);
                    deletedObjects.get(entityClass).remove(id);
                }
            }
        }
    }

    public void determineUpdatedObjects() {
        updatedObjects = new ArrayList<Entity>();
        Map<Class<?>, Map<String, CachedEntity>> cachedObjects = entityCache.getAllCachedEntities();
        for (Class<?> clazz : cachedObjects.keySet()) {

            Map<String, CachedEntity> classCache = cachedObjects.get(clazz);
            for (CachedEntity cachedObject : classCache.values()) {

                Entity cachedEntity = cachedObject.getEntity();

                // Executions are stored as a hierarchical tree, and updates are important to execute
                // even when the execution are deleted, as they can change the parent-child relationships.
                // For the other entities, this is not applicable and an update can be discarded when an update follows.

                if (!isEntityInserted(cachedEntity) &&
                        (ExecutionEntity.class.isAssignableFrom(cachedEntity.getClass()) || !isEntityToBeDeleted(cachedEntity)) &&
                        cachedObject.hasChanged()
                        ) {
                    updatedObjects.add(cachedEntity);
                }
            }
        }
    }

    protected void debugFlush() {
        log.debug("Flushing dbSqlSession");
        int nrOfInserts = 0, nrOfUpdates = 0, nrOfDeletes = 0;
        for (Map<String, Entity> insertedObjectMap : insertedObjects.values()) {
            for (Entity insertedObject : insertedObjectMap.values()) {
                log.debug("  insert {}",
                          insertedObject);
                nrOfInserts++;
            }
        }
        for (Entity updatedObject : updatedObjects) {
            log.debug("  update {}",
                      updatedObject);
            nrOfUpdates++;
        }
        for (Map<String, Entity> deletedObjectMap : deletedObjects.values()) {
            for (Entity deletedObject : deletedObjectMap.values()) {
                log.debug("  delete {} with id {}",
                          deletedObject,
                          deletedObject.getId());
                nrOfDeletes++;
            }
        }
        for (Collection<BulkDeleteOperation> bulkDeleteOperationList : bulkDeleteOperations.values()) {
            for (BulkDeleteOperation bulkDeleteOperation : bulkDeleteOperationList) {
                log.debug("  {}",
                          bulkDeleteOperation);
                nrOfDeletes++;
            }
        }
        log.debug("flush summary: {} insert, {} update, {} delete.",
                  nrOfInserts,
                  nrOfUpdates,
                  nrOfDeletes);
        log.debug("now executing flush...");
    }

    public boolean isEntityInserted(Entity entity) {
        return insertedObjects.containsKey(entity.getClass())
                && insertedObjects.get(entity.getClass()).containsKey(entity.getId());
    }

    public boolean isEntityToBeDeleted(Entity entity) {
        return deletedObjects.containsKey(entity.getClass())
                && deletedObjects.get(entity.getClass()).containsKey(entity.getId());
    }

    protected void flushInserts() {

        if (insertedObjects.size() == 0) {
            return;
        }

        // Handle in entity dependency order
        for (Class<? extends Entity> entityClass : EntityDependencyOrder.INSERT_ORDER) {
            if (insertedObjects.containsKey(entityClass)) {
                flushInsertEntities(entityClass,
                                    insertedObjects.get(entityClass).values());
                insertedObjects.remove(entityClass);
            }
        }

        // Next, in case of custom entities or we've screwed up and forgotten some entity
        if (insertedObjects.size() > 0) {
            for (Class<? extends Entity> entityClass : insertedObjects.keySet()) {
                flushInsertEntities(entityClass,
                                    insertedObjects.get(entityClass).values());
            }
        }

        insertedObjects.clear();
    }

    protected void flushInsertEntities(Class<? extends Entity> entityClass,
                                       Collection<Entity> entitiesToInsert) {
        if (entitiesToInsert.size() == 1) {
            flushRegularInsert(entitiesToInsert.iterator().next(),
                               entityClass);
        } else if (Boolean.FALSE.equals(dbSqlSessionFactory.isBulkInsertable(entityClass))) {
            for (Entity entity : entitiesToInsert) {
                flushRegularInsert(entity,
                                   entityClass);
            }
        } else {
            flushBulkInsert(entitiesToInsert,
                            entityClass);
        }
    }

    protected Collection<Entity> orderExecutionEntities(Map<String, Entity> executionEntities,
                                                        boolean parentBeforeChildExecution) {

        // For insertion: parent executions should go before child executions

        List<Entity> result = new ArrayList<Entity>(executionEntities.size());

        // Gather parent-child relationships
        Map<String, String> childToParentExecutionMapping = new HashMap<String, String>();
        Map<String, List<ExecutionEntity>> parentToChildrenMapping = new HashMap<String, List<ExecutionEntity>>();

        Collection<Entity> executionCollection = executionEntities.values();
        Iterator<Entity> executionIterator = executionCollection.iterator();
        while (executionIterator.hasNext()) {
            ExecutionEntity currentExecutionEntity = (ExecutionEntity) executionIterator.next();
            String parentId = currentExecutionEntity.getParentId();
            String superExecutionId = currentExecutionEntity.getSuperExecutionId();

            String parentKey = parentId != null ? parentId : superExecutionId;
            childToParentExecutionMapping.put(currentExecutionEntity.getId(),
                                              parentKey);

            if (!parentToChildrenMapping.containsKey(parentKey)) {
                parentToChildrenMapping.put(parentKey,
                                            new ArrayList<ExecutionEntity>());
            }
            parentToChildrenMapping.get(parentKey).add(currentExecutionEntity);
        }

        // Loop over all entities, and insert in the correct order
        Set<String> handledExecutionIds = new HashSet<String>(executionEntities.size());
        executionIterator = executionCollection.iterator();
        while (executionIterator.hasNext()) {
            ExecutionEntity currentExecutionEntity = (ExecutionEntity) executionIterator.next();
            String executionId = currentExecutionEntity.getId();

            if (!handledExecutionIds.contains(executionId)) {
                String parentId = childToParentExecutionMapping.get(executionId);
                if (parentId != null) {
                    while (parentId != null) {
                        String newParentId = childToParentExecutionMapping.get(parentId);
                        if (newParentId == null) {
                            break;
                        }
                        parentId = newParentId;
                    }
                }

                if (parentId == null) {
                    parentId = executionId;
                }

                if (executionEntities.containsKey(parentId) && !handledExecutionIds.contains(parentId)) {
                    handledExecutionIds.add(parentId);
                    if (parentBeforeChildExecution) {
                        result.add(executionEntities.get(parentId));
                    } else {
                        result.add(0,
                                   executionEntities.get(parentId));
                    }
                }

                collectChildExecutionsForInsertion(result,
                                                   parentToChildrenMapping,
                                                   handledExecutionIds,
                                                   parentId,
                                                   parentBeforeChildExecution);
            }
        }

        return result;
    }

    protected void collectChildExecutionsForInsertion(List<Entity> result,
                                                      Map<String, List<ExecutionEntity>> parentToChildrenMapping,
                                                      Set<String> handledExecutionIds,
                                                      String parentId,
                                                      boolean parentBeforeChildExecution) {
        List<ExecutionEntity> childExecutionEntities = parentToChildrenMapping.get(parentId);

        if (childExecutionEntities == null) {
            return;
        }

        for (ExecutionEntity childExecutionEntity : childExecutionEntities) {
            handledExecutionIds.add(childExecutionEntity.getId());
            if (parentBeforeChildExecution) {
                result.add(childExecutionEntity);
            } else {
                result.add(0,
                           childExecutionEntity);
            }

            collectChildExecutionsForInsertion(result,
                                               parentToChildrenMapping,
                                               handledExecutionIds,
                                               childExecutionEntity.getId(),
                                               parentBeforeChildExecution);
        }
    }

    protected void flushRegularInsert(Entity entity,
                                      Class<? extends Entity> clazz) {
        String insertStatement = dbSqlSessionFactory.getInsertStatement(entity);
        insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

        if (insertStatement == null) {
            throw new ActivitiException("no insert statement for " + entity.getClass() + " in the ibatis mapping files");
        }

        log.debug("inserting: {}",
                  entity);
        sqlSession.insert(insertStatement,
                          entity);

        // See https://activiti.atlassian.net/browse/ACT-1290
        if (entity instanceof HasRevision) {
            incrementRevision(entity);
        }
    }

    protected void flushBulkInsert(Collection<Entity> entities,
                                   Class<? extends Entity> clazz) {
        String insertStatement = dbSqlSessionFactory.getBulkInsertStatement(clazz);
        insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

        if (insertStatement == null) {
            throw new ActivitiException("no insert statement for " + entities.iterator().next().getClass() + " in the ibatis mapping files");
        }

        Iterator<Entity> entityIterator = entities.iterator();
        Boolean hasRevision = null;

        while (entityIterator.hasNext()) {
            List<Entity> subList = new ArrayList<Entity>();
            int index = 0;
            while (entityIterator.hasNext() && index < dbSqlSessionFactory.getMaxNrOfStatementsInBulkInsert()) {
                Entity entity = entityIterator.next();
                subList.add(entity);

                if (hasRevision == null) {
                    hasRevision = entity instanceof HasRevision;
                }
                index++;
            }
            sqlSession.insert(insertStatement,
                              subList);
        }

        if (hasRevision != null && hasRevision) {
            entityIterator = entities.iterator();
            while (entityIterator.hasNext()) {
                incrementRevision(entityIterator.next());
            }
        }
    }

    protected void incrementRevision(Entity insertedObject) {
        HasRevision revisionEntity = (HasRevision) insertedObject;
        if (revisionEntity.getRevision() == 0) {
            revisionEntity.setRevision(revisionEntity.getRevisionNext());
        }
    }

    protected void flushUpdates() {
        for (Entity updatedObject : updatedObjects) {
            String updateStatement = dbSqlSessionFactory.getUpdateStatement(updatedObject);
            updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);

            if (updateStatement == null) {
                throw new ActivitiException("no update statement for " + updatedObject.getClass() + " in the ibatis mapping files");
            }

            log.debug("updating: {}",
                      updatedObject);
            int updatedRecords = sqlSession.update(updateStatement,
                                                   updatedObject);
            if (updatedRecords == 0) {
                throw new ActivitiOptimisticLockingException(updatedObject + " was updated by another transaction concurrently");
            }

            // See https://activiti.atlassian.net/browse/ACT-1290
            if (updatedObject instanceof HasRevision) {
                ((HasRevision) updatedObject).setRevision(((HasRevision) updatedObject).getRevisionNext());
            }
        }
        updatedObjects.clear();
    }

    protected void flushDeletes() {

        if (deletedObjects.size() == 0 && bulkDeleteOperations.size() == 0) {
            return;
        }

        // Handle in entity dependency order
        for (Class<? extends Entity> entityClass : EntityDependencyOrder.DELETE_ORDER) {
            if (deletedObjects.containsKey(entityClass)) {
                flushDeleteEntities(entityClass,
                                    deletedObjects.get(entityClass).values());
                deletedObjects.remove(entityClass);
            }
            flushBulkDeletes(entityClass);
        }

        // Next, in case of custom entities or we've screwed up and forgotten some entity
        if (deletedObjects.size() > 0) {
            for (Class<? extends Entity> entityClass : deletedObjects.keySet()) {
                flushDeleteEntities(entityClass,
                                    deletedObjects.get(entityClass).values());
                flushBulkDeletes(entityClass);
            }
        }

        deletedObjects.clear();
    }

    protected void flushBulkDeletes(Class<? extends Entity> entityClass) {
        // Bulk deletes
        if (bulkDeleteOperations.containsKey(entityClass)) {
            for (BulkDeleteOperation bulkDeleteOperation : bulkDeleteOperations.get(entityClass)) {
                bulkDeleteOperation.execute(sqlSession);
            }
        }
    }

    protected void flushDeleteEntities(Class<? extends Entity> entityClass,
                                       Collection<Entity> entitiesToDelete) {
        for (Entity entity : entitiesToDelete) {
            String deleteStatement = dbSqlSessionFactory.getDeleteStatement(entity.getClass());
            deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
            if (deleteStatement == null) {
                throw new ActivitiException("no delete statement for " + entity.getClass() + " in the ibatis mapping files");
            }

            // It only makes sense to check for optimistic locking exceptions
            // for objects that actually have a revision
            if (entity instanceof HasRevision) {
                int nrOfRowsDeleted = sqlSession.delete(deleteStatement,
                                                        entity);
                if (nrOfRowsDeleted == 0) {
                    throw new ActivitiOptimisticLockingException(entity + " was updated by another transaction concurrently");
                }
            } else {
                sqlSession.delete(deleteStatement,
                                  entity);
            }
        }
    }

    public void close() {
        sqlSession.close();
    }

    public void commit() {
        sqlSession.commit();
    }

    public void rollback() {
        sqlSession.rollback();
    }

    // schema operations
    // ////////////////////////////////////////////////////////

    public void dbSchemaCheckVersion() {
        try {
            String dbVersion = getDbVersion();
            if (!ProcessEngine.VERSION.equals(dbVersion)) {
                throw new ActivitiWrongDbException(ProcessEngine.VERSION,
                                                   dbVersion);
            }

            String errorMessage = null;
            if (!isEngineTablePresent()) {
                errorMessage = addMissingComponent(errorMessage,
                                                   "engine");
            }
            if (dbSqlSessionFactory.isDbHistoryUsed() && !isHistoryTablePresent()) {
                errorMessage = addMissingComponent(errorMessage,
                                                   "history");
            }

            if (errorMessage != null) {
                throw new ActivitiException("Activiti database problem: " + errorMessage);
            }
        } catch (Exception e) {
            if (isMissingTablesException(e)) {
                throw new ActivitiException(
                        "no activiti tables in db. set <property name=\"databaseSchemaUpdate\" to value=\"true\" or value=\"create-drop\" (use create-drop for testing only!) in bean processEngineConfiguration in activiti.cfg.xml for automatic schema creation",
                        e);
            } else {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new ActivitiException("couldn't get db schema version",
                                                e);
                }
            }
        }

        log.debug("activiti db schema check successful");
    }

    protected String addMissingComponent(String missingComponents,
                                         String component) {
        if (missingComponents == null) {
            return "Tables missing for component(s) " + component;
        }
        return missingComponents + ", " + component;
    }

    protected String getDbVersion() {
        String selectSchemaVersionStatement = dbSqlSessionFactory.mapStatement("selectDbSchemaVersion");
        return (String) sqlSession.selectOne(selectSchemaVersionStatement);
    }

    public void dbSchemaCreate() {
        if (isEngineTablePresent()) {
            String dbVersion = getDbVersion();
            if (!ProcessEngine.VERSION.equals(dbVersion)) {
                throw new ActivitiWrongDbException(ProcessEngine.VERSION,
                                                   dbVersion);
            }
        } else {
            dbSchemaCreateEngine();
        }

        if (dbSqlSessionFactory.isDbHistoryUsed()) {
            dbSchemaCreateHistory();
        }
    }

    protected void dbSchemaCreateHistory() {
        executeMandatorySchemaResource("create",
                                       "history");
    }

    protected void dbSchemaCreateEngine() {
        executeMandatorySchemaResource("create",
                                       "engine");
    }

    public void dbSchemaDrop() {
        executeMandatorySchemaResource("drop",
                                       "engine");
        if (dbSqlSessionFactory.isDbHistoryUsed()) {
            executeMandatorySchemaResource("drop",
                                           "history");
        }
    }

    public void dbSchemaPrune() {
        if (isHistoryTablePresent() && !dbSqlSessionFactory.isDbHistoryUsed()) {
            executeMandatorySchemaResource("drop",
                                           "history");
        }
    }

    public void executeMandatorySchemaResource(String operation,
                                               String component) {
        executeSchemaResource(operation,
                              component,
                              getResourceForDbOperation(operation,
                                                        operation,
                                                        component),
                              false);
    }

    public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

    public String dbSchemaUpdate() {

        String feedback = null;
        boolean isUpgradeNeeded = false;
        int matchingVersionIndex = -1;

        if (isEngineTablePresent()) {

            PropertyEntity dbVersionProperty = selectById(PropertyEntity.class,
                                                          "schema.version");
            String dbVersion = dbVersionProperty.getValue();

            // Determine index in the sequence of Activiti releases
            matchingVersionIndex = findMatchingVersionIndex(dbVersion);

            // If no match has been found, but the version starts with '5.x',
            // we assume it's the last version (see comment in the VERSIONS list)
            if (matchingVersionIndex < 0 && dbVersion != null && dbVersion.startsWith("5.")) {
                matchingVersionIndex = findMatchingVersionIndex(LAST_V5_VERSION);
            }

            // Exception when no match was found: unknown/unsupported version
            if (matchingVersionIndex < 0) {
                throw new ActivitiException("Could not update Activiti database schema: unknown version from database: '" + dbVersion + "'");
            }

            isUpgradeNeeded = (matchingVersionIndex != (ACTIVITI_VERSIONS.size() - 1));

            if (isUpgradeNeeded) {
                dbVersionProperty.setValue(ProcessEngine.VERSION);

                PropertyEntity dbHistoryProperty;
                if ("5.0".equals(dbVersion)) {
                    dbHistoryProperty = Context.getCommandContext().getPropertyEntityManager().create();
                    dbHistoryProperty.setName("schema.history");
                    dbHistoryProperty.setValue("create(5.0)");
                    insert(dbHistoryProperty);
                } else {
                    dbHistoryProperty = selectById(PropertyEntity.class,
                                                   "schema.history");
                }

                // Set upgrade history
                String dbHistoryValue = dbHistoryProperty.getValue() + " upgrade(" + dbVersion + "->" + ProcessEngine.VERSION + ")";
                dbHistoryProperty.setValue(dbHistoryValue);

                // Engine upgrade
                dbSchemaUpgrade("engine",
                                matchingVersionIndex);
                feedback = "upgraded Activiti from " + dbVersion + " to " + ProcessEngine.VERSION;
            }
        } else {
            dbSchemaCreateEngine();
        }
        if (isHistoryTablePresent()) {
            if (isUpgradeNeeded) {
                dbSchemaUpgrade("history",
                                matchingVersionIndex);
            }
        } else if (dbSqlSessionFactory.isDbHistoryUsed()) {
            dbSchemaCreateHistory();
        }

        return feedback;
    }

    /**
     * Returns the index in the list of {@link #ACTIVITI_VERSIONS} matching the provided string version.
     * Returns -1 if no match can be found.
     */
    protected int findMatchingVersionIndex(String dbVersion) {
        int index = 0;
        int matchingVersionIndex = -1;
        while (matchingVersionIndex < 0 && index < ACTIVITI_VERSIONS.size()) {
            if (ACTIVITI_VERSIONS.get(index).matches(dbVersion)) {
                matchingVersionIndex = index;
            } else {
                index++;
            }
        }
        return matchingVersionIndex;
    }

    public boolean isEngineTablePresent() {
        return isTablePresent("ACT_RU_EXECUTION");
    }

    public boolean isHistoryTablePresent() {
        return isTablePresent("ACT_HI_PROCINST");
    }

    public boolean isTablePresent(String tableName) {
        // ACT-1610: in case the prefix IS the schema itself, we don't add the
        // prefix, since the check is already aware of the schema
        if (!dbSqlSessionFactory.isTablePrefixIsSchema()) {
            tableName = prependDatabaseTablePrefix(tableName);
        }

        Connection connection = null;
        try {
            connection = sqlSession.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tables = null;

            String catalog = this.connectionMetadataDefaultCatalog;
            if (dbSqlSessionFactory.getDatabaseCatalog() != null && dbSqlSessionFactory.getDatabaseCatalog().length() > 0) {
                catalog = dbSqlSessionFactory.getDatabaseCatalog();
            }

            String schema = this.connectionMetadataDefaultSchema;
            if (dbSqlSessionFactory.getDatabaseSchema() != null && dbSqlSessionFactory.getDatabaseSchema().length() > 0) {
                schema = dbSqlSessionFactory.getDatabaseSchema();
            }

            String databaseType = dbSqlSessionFactory.getDatabaseType();

            if ("postgres".equals(databaseType)) {
                tableName = tableName.toLowerCase();
            }

            if (schema != null && "oracle".equals(databaseType)) {
                schema = schema.toUpperCase();
            }

            if (catalog != null && catalog.length() == 0) {
                catalog = null;
            }

            try {
                tables = databaseMetaData.getTables(catalog,
                                                    schema,
                                                    tableName,
                                                    JDBC_METADATA_TABLE_TYPES);
                return tables.next();
            } finally {
                try {
                    tables.close();
                } catch (Exception e) {
                    log.error("Error closing meta data tables",
                              e);
                }
            }
        } catch (Exception e) {
            throw new ActivitiException("couldn't check if tables are already present using metadata: " + e.getMessage(),
                                        e);
        }
    }

    protected String prependDatabaseTablePrefix(String tableName) {
        return dbSqlSessionFactory.getDatabaseTablePrefix() + tableName;
    }

    protected void dbSchemaUpgrade(final String component,
                                   final int currentDatabaseVersionsIndex) {
        ActivitiVersion activitiVersion = ACTIVITI_VERSIONS.get(currentDatabaseVersionsIndex);
        String dbVersion = activitiVersion.getMainVersion();
        log.info("upgrading activiti {} schema from {} to {}",
                 component,
                 dbVersion,
                 ProcessEngine.VERSION);

        // Actual execution of schema DDL SQL
        for (int i = currentDatabaseVersionsIndex + 1; i < ACTIVITI_VERSIONS.size(); i++) {
            String nextVersion = ACTIVITI_VERSIONS.get(i).getMainVersion();

            // Taking care of -SNAPSHOT version in development
            if (nextVersion.endsWith("-SNAPSHOT")) {
                nextVersion = nextVersion.substring(0,
                                                    nextVersion.length() - "-SNAPSHOT".length());
            }

            dbVersion = dbVersion.replace(".",
                                          "");
            nextVersion = nextVersion.replace(".",
                                              "");
            log.info("Upgrade needed: {} -> {}. Looking for schema update resource for component '{}'",
                     dbVersion,
                     nextVersion,
                     component);
            executeSchemaResource("upgrade",
                                  component,
                                  getResourceForDbOperation("upgrade",
                                                            "upgradestep." + dbVersion + ".to." + nextVersion,
                                                            component),
                                  true);
            dbVersion = nextVersion;
        }
    }

    public String getResourceForDbOperation(String directory,
                                            String operation,
                                            String component) {
        String databaseType = dbSqlSessionFactory.getDatabaseType();
        return "org/activiti/db/" + directory + "/activiti." + databaseType + "." + operation + "." + component + ".sql";
    }

    public void executeSchemaResource(String operation,
                                      String component,
                                      String resourceName,
                                      boolean isOptional) {
        InputStream inputStream = null;
        try {
            inputStream = ReflectUtil.getResourceAsStream(resourceName);
            if (inputStream == null) {
                if (isOptional) {
                    log.info("no schema resource {} for {}",
                             resourceName,
                             operation);
                } else {
                    throw new ActivitiException("resource '" + resourceName + "' is not available");
                }
            } else {
                executeSchemaResource(operation,
                                      component,
                                      resourceName,
                                      inputStream);
            }
        } finally {
            IoUtil.closeSilently(inputStream);
        }
    }

    private void executeSchemaResource(String operation,
                                       String component,
                                       String resourceName,
                                       InputStream inputStream) {
        log.info("performing {} on {} with resource {}",
                 operation,
                 component,
                 resourceName);
        String sqlStatement = null;
        String exceptionSqlStatement = null;
        try {
            Connection connection = sqlSession.getConnection();
            Exception exception = null;
            byte[] bytes = IoUtil.readInputStream(inputStream,
                                                  resourceName);
            String ddlStatements = new String(bytes);

            // Special DDL handling for certain databases
            try {
                if (isMysql()) {
                    DatabaseMetaData databaseMetaData = connection.getMetaData();
                    int majorVersion = databaseMetaData.getDatabaseMajorVersion();
                    int minorVersion = databaseMetaData.getDatabaseMinorVersion();
                    log.info("Found MySQL: majorVersion=" + majorVersion + " minorVersion=" + minorVersion);

                    // Special care for MySQL < 5.6
                    if (majorVersion <= 5 && minorVersion < 6) {
                        ddlStatements = updateDdlForMySqlVersionLowerThan56(ddlStatements);
                    }
                }
            } catch (Exception e) {
                log.info("Could not get database metadata",
                         e);
            }

            BufferedReader reader = new BufferedReader(new StringReader(ddlStatements));
            String line = readNextTrimmedLine(reader);
            boolean inOraclePlsqlBlock = false;
            while (line != null) {
                if (line.startsWith("# ")) {
                    log.debug(line.substring(2));
                } else if (line.startsWith("-- ")) {
                    log.debug(line.substring(3));
                } else if (line.startsWith("execute java ")) {
                    String upgradestepClassName = line.substring(13).trim();
                    DbUpgradeStep dbUpgradeStep = null;
                    try {
                        dbUpgradeStep = (DbUpgradeStep) ReflectUtil.instantiate(upgradestepClassName);
                    } catch (ActivitiException e) {
                        throw new ActivitiException("database update java class '" + upgradestepClassName + "' can't be instantiated: " + e.getMessage(),
                                                    e);
                    }
                    try {
                        log.debug("executing upgrade step java class {}",
                                  upgradestepClassName);
                        dbUpgradeStep.execute(this);
                    } catch (Exception e) {
                        throw new ActivitiException("error while executing database update java class '" + upgradestepClassName + "': " + e.getMessage(),
                                                    e);
                    }
                } else if (line.length() > 0) {

                    if (isOracle() && line.startsWith("begin")) {
                        inOraclePlsqlBlock = true;
                        sqlStatement = addSqlStatementPiece(sqlStatement,
                                                            line);
                    } else if ((line.endsWith(";") && !inOraclePlsqlBlock) || (line.startsWith("/") && inOraclePlsqlBlock)) {

                        if (inOraclePlsqlBlock) {
                            inOraclePlsqlBlock = false;
                        } else {
                            sqlStatement = addSqlStatementPiece(sqlStatement,
                                                                line.substring(0,
                                                                               line.length() - 1));
                        }

                        Statement jdbcStatement = connection.createStatement();
                        try {
                            // no logging needed as the connection will log it
                            log.debug("SQL: {}",
                                      sqlStatement);
                            jdbcStatement.execute(sqlStatement);
                            jdbcStatement.close();
                        } catch (Exception e) {
                            if (exception == null) {
                                exception = e;
                                exceptionSqlStatement = sqlStatement;
                            }
                            log.error("problem during schema {}, statement {}",
                                      operation,
                                      sqlStatement,
                                      e);
                        } finally {
                            sqlStatement = null;
                        }
                    } else {
                        sqlStatement = addSqlStatementPiece(sqlStatement,
                                                            line);
                    }
                }

                line = readNextTrimmedLine(reader);
            }

            if (exception != null) {
                throw exception;
            }

            log.debug("activiti db schema {} for component {} successful",
                      operation,
                      component);
        } catch (Exception e) {
            throw new ActivitiException("couldn't " + operation + " db schema: " + exceptionSqlStatement,
                                        e);
        }
    }

    /**
     * MySQL is funny when it comes to timestamps and dates.
     * <p>
     * More specifically, for a DDL statement like 'MYCOLUMN timestamp(3)': - MySQL 5.6.4+ has support for timestamps/dates with millisecond (or smaller) precision. The DDL above works and the data in
     * the table will have millisecond precision - MySQL < 5.5.3 allows the DDL statement, but ignores it. The DDL above works but the data won't have millisecond precision - MySQL 5.5.3 < [version] <
     * 5.6.4 gives and exception when using the DDL above.
     * <p>
     * Also, the 5.5 and 5.6 branches of MySQL are both actively developed and patched.
     * <p>
     * Hence, when doing auto-upgrade/creation of the Activiti tables, the default MySQL DDL file is used and all timestamps/datetimes are converted to not use the millisecond precision by string
     * replacement done in the method below.
     * <p>
     * If using the DDL files directly (which is a sane choice in production env.), there is a distinction between MySQL version < 5.6.
     */
    protected String updateDdlForMySqlVersionLowerThan56(String ddlStatements) {
        return ddlStatements.replace("timestamp(3)",
                                     "timestamp").replace("datetime(3)",
                                                          "datetime").replace("TIMESTAMP(3)",
                                                                              "TIMESTAMP").replace("DATETIME(3)",
                                                                                                   "DATETIME");
    }

    protected String addSqlStatementPiece(String sqlStatement,
                                          String line) {
        if (sqlStatement == null) {
            return line;
        }
        return sqlStatement + " \n" + line;
    }

    protected String readNextTrimmedLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            line = line.trim();
        }
        return line;
    }

    protected boolean isMissingTablesException(Exception e) {
        String exceptionMessage = e.getMessage();
        if (e.getMessage() != null) {
            // Matches message returned from H2
            if ((exceptionMessage.indexOf("Table") != -1) && (exceptionMessage.indexOf("not found") != -1)) {
                return true;
            }

            // Message returned from MySQL and Oracle
            if (((exceptionMessage.indexOf("Table") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("doesn't exist") != -1)) {
                return true;
            }

            // Message returned from Postgres
            if (((exceptionMessage.indexOf("relation") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("does not exist") != -1)) {
                return true;
            }
        }
        return false;
    }

    public void performSchemaOperationsProcessEngineBuild() {
        String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
        log.debug("Executing performSchemaOperationsProcessEngineBuild with setting " + databaseSchemaUpdate);
        if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
            try {
                dbSchemaDrop();
            } catch (RuntimeException e) {
                // ignore
            }
        }
        if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)
                || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate) || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(databaseSchemaUpdate)) {
            dbSchemaCreate();
        } else if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
            dbSchemaCheckVersion();
        } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
            dbSchemaUpdate();
        }
    }

    public void performSchemaOperationsProcessEngineClose() {
        String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
        if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)) {
            dbSchemaDrop();
        }
    }

    public <T> T getCustomMapper(Class<T> type) {
        return sqlSession.getMapper(type);
    }

    public boolean isMysql() {
        return dbSqlSessionFactory.getDatabaseType().equals("mysql");
    }

    public boolean isOracle() {
        return dbSqlSessionFactory.getDatabaseType().equals("oracle");
    }

    // query factory methods
    // ////////////////////////////////////////////////////

    public DeploymentQueryImpl createDeploymentQuery() {
        return new DeploymentQueryImpl();
    }

    public ModelQueryImpl createModelQueryImpl() {
        return new ModelQueryImpl();
    }

    public ProcessDefinitionQueryImpl createProcessDefinitionQuery() {
        return new ProcessDefinitionQueryImpl();
    }

    public ProcessInstanceQueryImpl createProcessInstanceQuery() {
        return new ProcessInstanceQueryImpl();
    }

    public ExecutionQueryImpl createExecutionQuery() {
        return new ExecutionQueryImpl();
    }

    public TaskQueryImpl createTaskQuery() {
        return new TaskQueryImpl();
    }

    public JobQueryImpl createJobQuery() {
        return new JobQueryImpl();
    }

    public HistoricProcessInstanceQueryImpl createHistoricProcessInstanceQuery() {
        return new HistoricProcessInstanceQueryImpl();
    }

    public HistoricActivityInstanceQueryImpl createHistoricActivityInstanceQuery() {
        return new HistoricActivityInstanceQueryImpl();
    }

    public HistoricTaskInstanceQueryImpl createHistoricTaskInstanceQuery() {
        return new HistoricTaskInstanceQueryImpl();
    }

    public HistoricDetailQueryImpl createHistoricDetailQuery() {
        return new HistoricDetailQueryImpl();
    }

    public HistoricVariableInstanceQueryImpl createHistoricVariableInstanceQuery() {
        return new HistoricVariableInstanceQueryImpl();
    }

    // getters and setters
    // //////////////////////////////////////////////////////

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public DbSqlSessionFactory getDbSqlSessionFactory() {
        return dbSqlSessionFactory;
    }
}
