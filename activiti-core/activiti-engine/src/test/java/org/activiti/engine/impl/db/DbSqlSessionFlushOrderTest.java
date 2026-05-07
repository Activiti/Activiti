/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.LinkedHashMap;
import java.util.Map;
import org.activiti.engine.impl.persistence.cache.CachedEntity;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DbSqlSessionFlushOrderTest {

    private static final String UPDATE_STATEMENT = "test-update-statement";
    private static final String INSERT_STATEMENT = "test-insert-statement";
    private static final String DELETE_STATEMENT = "test-delete-statement";
    private static final String EXEC_LOW_ID = "exec-aaa";
    private static final String EXEC_HIGH_ID = "exec-zzz";
    private static final String VAR_LOW_ID = "var-aaa";
    private static final String VAR_HIGH_ID = "var-zzz";

    @Mock
    private DbSqlSessionFactory dbSqlSessionFactory;

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @Mock
    private SqlSession sqlSession;

    @Mock
    private EntityCache entityCache;

    private DbSqlSession dbSqlSession;

    @Before
    public void setUp() {
        given(dbSqlSessionFactory.getSqlSessionFactory()).willReturn(sqlSessionFactory);
        given(sqlSessionFactory.openSession()).willReturn(sqlSession);

        dbSqlSession = new DbSqlSession(dbSqlSessionFactory, entityCache);

        given(dbSqlSessionFactory.getUpdateStatement(any(Entity.class))).willReturn(UPDATE_STATEMENT);
        given(dbSqlSessionFactory.getInsertStatement(any(Entity.class))).willReturn(INSERT_STATEMENT);
        given(dbSqlSessionFactory.getDeleteStatement(any())).willReturn(DELETE_STATEMENT);
        given(dbSqlSessionFactory.mapStatement(anyString())).willAnswer(invocation -> invocation.getArgument(0));
        given(dbSqlSessionFactory.isBulkInsertable(any())).willReturn(Boolean.FALSE);
        given(sqlSession.update(anyString(), any())).willReturn(1);
        given(sqlSession.delete(anyString(), any())).willReturn(1);
    }

    @Test
    public void should_emitUpdatesUsingUpdateOrderClassRanking_when_cacheIterationOrderDiffers() {
        CachedEntity execHigh = dirtyExecution(EXEC_HIGH_ID);
        CachedEntity execLow = dirtyExecution(EXEC_LOW_ID);
        CachedEntity varHigh = dirtyVariable(VAR_HIGH_ID);
        CachedEntity varLow = dirtyVariable(VAR_LOW_ID);

        Map<Class<?>, Map<String, CachedEntity>> cache = new LinkedHashMap<>();
        cache.put(VariableInstanceEntityImpl.class, mapOf(varHigh, varLow));
        cache.put(ExecutionEntityImpl.class, mapOf(execHigh, execLow));
        given(entityCache.getAllCachedEntities()).willReturn(cache);

        dbSqlSession.determineUpdatedObjects();
        dbSqlSession.flushUpdates();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(sqlSession, times(4)).update(eq(UPDATE_STATEMENT), captor.capture());
        assertThat(captor.getAllValues())
            .extracting(value -> ((Entity) value).getClass(), value -> ((Entity) value).getId())
            .containsExactly(
                tuple(ExecutionEntityImpl.class, EXEC_LOW_ID),
                tuple(ExecutionEntityImpl.class, EXEC_HIGH_ID),
                tuple(VariableInstanceEntityImpl.class, VAR_LOW_ID),
                tuple(VariableInstanceEntityImpl.class, VAR_HIGH_ID)
            );
    }

    @Test
    public void should_excludeUnchangedEntities_when_determiningUpdatedObjects() {
        CachedEntity changed = dirtyExecution(EXEC_LOW_ID);
        CachedEntity unchanged = unchangedExecution("exec-bbb");

        LinkedHashMap<String, CachedEntity> classCache = new LinkedHashMap<>();
        classCache.put(changed.getEntity().getId(), changed);
        classCache.put(unchanged.getEntity().getId(), unchanged);

        Map<Class<?>, Map<String, CachedEntity>> cache = new LinkedHashMap<>();
        cache.put(ExecutionEntityImpl.class, classCache);
        given(entityCache.getAllCachedEntities()).willReturn(cache);

        dbSqlSession.determineUpdatedObjects();

        assertThat(dbSqlSession.updatedObjects)
            .extracting(Entity::getId)
            .containsExactly(EXEC_LOW_ID);
    }

    @Test
    public void should_sortNonExecutionInsertsById_when_collectionHasMoreThanOneEntity() {
        VariableInstanceEntityImpl varHigh = newVariable(VAR_HIGH_ID);
        VariableInstanceEntityImpl varLow = newVariable(VAR_LOW_ID);

        dbSqlSession.flushInsertEntities(VariableInstanceEntityImpl.class, asList(varHigh, varLow));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, varLow);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, varHigh);
    }

    @Test
    public void should_preserveCallerOrder_when_insertingExecutionEntities() {
        ExecutionEntityImpl parent = newExecution("4");
        ExecutionEntityImpl child = newExecution("10");

        dbSqlSession.flushInsertEntities(ExecutionEntityImpl.class, asList(parent, child));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, parent);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, child);
    }

    @Test
    public void should_useSingleInsertPath_when_collectionHasOneEntity() {
        VariableInstanceEntityImpl onlyOne = newVariable(VAR_HIGH_ID);

        dbSqlSession.flushInsertEntities(VariableInstanceEntityImpl.class, asList(onlyOne));

        verify(sqlSession, times(1)).insert(INSERT_STATEMENT, onlyOne);
    }

    @Test
    public void should_sortNonExecutionDeletesById_when_collectionHasMoreThanOneEntity() {
        VariableInstanceEntityImpl varHigh = newVariable(VAR_HIGH_ID);
        VariableInstanceEntityImpl varLow = newVariable(VAR_LOW_ID);

        dbSqlSession.flushDeleteEntities(VariableInstanceEntityImpl.class, asList(varHigh, varLow));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, varLow);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, varHigh);
    }

    @Test
    public void should_preserveCallerOrder_when_deletingExecutionEntities() {
        ExecutionEntityImpl child = newExecution("10");
        ExecutionEntityImpl parent = newExecution("4");

        dbSqlSession.flushDeleteEntities(ExecutionEntityImpl.class, asList(child, parent));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, child);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, parent);
    }

    private static ExecutionEntityImpl newExecution(String id) {
        ExecutionEntityImpl execution = new ExecutionEntityImpl();
        execution.setId(id);
        return execution;
    }

    private static VariableInstanceEntityImpl newVariable(String id) {
        VariableInstanceEntityImpl variable = new VariableInstanceEntityImpl();
        variable.setId(id);
        return variable;
    }

    private static CachedEntity dirtyExecution(String id) {
        ExecutionEntityImpl execution = newExecution(id);
        CachedEntity cached = new CachedEntity(execution, true);
        execution.setBusinessKey("modified-" + id);
        return cached;
    }

    private static CachedEntity unchangedExecution(String id) {
        return new CachedEntity(newExecution(id), true);
    }

    private static CachedEntity dirtyVariable(String id) {
        VariableInstanceEntityImpl variable = newVariable(id);
        CachedEntity cached = new CachedEntity(variable, true);
        variable.setTextValue("modified-" + id);
        return cached;
    }

    private static Map<String, CachedEntity> mapOf(CachedEntity... entries) {
        LinkedHashMap<String, CachedEntity> map = new LinkedHashMap<>();
        for (CachedEntity entry : entries) {
            map.put(entry.getEntity().getId(), entry);
        }
        return map;
    }
}
