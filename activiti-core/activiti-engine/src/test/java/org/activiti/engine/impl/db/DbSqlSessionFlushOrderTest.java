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
    public void shouldEmitUpdatesInDeterministicOrderRegardlessOfCacheIterationOrder() {
        TestExec execHigh = new TestExec(EXEC_HIGH_ID);
        TestExec execLow = new TestExec(EXEC_LOW_ID);
        TestVar varHigh = new TestVar(VAR_HIGH_ID);
        TestVar varLow = new TestVar(VAR_LOW_ID);

        Map<Class<?>, Map<String, CachedEntity>> cache = new LinkedHashMap<>();
        cache.put(TestVar.class, mapOfChanged(varHigh, varLow));
        cache.put(TestExec.class, mapOfChanged(execHigh, execLow));
        given(entityCache.getAllCachedEntities()).willReturn(cache);

        dbSqlSession.determineUpdatedObjects();
        dbSqlSession.flushUpdates();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(sqlSession, times(4)).update(eq(UPDATE_STATEMENT), captor.capture());
        assertThat(captor.getAllValues())
            .extracting(value -> ((Entity) value).getClass(), value -> ((Entity) value).getId())
            .containsExactly(
                tuple(TestExec.class, EXEC_LOW_ID),
                tuple(TestExec.class, EXEC_HIGH_ID),
                tuple(TestVar.class, VAR_LOW_ID),
                tuple(TestVar.class, VAR_HIGH_ID)
            );
    }

    @Test
    public void shouldExcludeUnchangedEntitiesFromUpdatedObjects() {
        TestExec changed = new TestExec(EXEC_LOW_ID);
        TestExec unchanged = new TestExec("exec-bbb");

        LinkedHashMap<String, CachedEntity> classCache = new LinkedHashMap<>();
        classCache.put(changed.getId(), markChanged(changed));
        classCache.put(unchanged.getId(), new CachedEntity(unchanged, true));

        Map<Class<?>, Map<String, CachedEntity>> cache = new LinkedHashMap<>();
        cache.put(TestExec.class, classCache);
        given(entityCache.getAllCachedEntities()).willReturn(cache);

        dbSqlSession.determineUpdatedObjects();

        assertThat(dbSqlSession.updatedObjects)
            .extracting(Entity::getId)
            .containsExactly(EXEC_LOW_ID);
    }

    @Test
    public void shouldSortNonExecutionInsertsByIdWhenSizeGreaterThanOne() {
        TestVar varHigh = new TestVar(VAR_HIGH_ID);
        TestVar varLow = new TestVar(VAR_LOW_ID);

        dbSqlSession.flushInsertEntities(TestVar.class, asList(varHigh, varLow));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, varLow);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, varHigh);
    }

    @Test
    public void shouldPreserveCallerOrderForExecutionInsertsToHonourFkConstraints() {
        ExecutionEntityImpl parent = newExecutionEntity("4");
        ExecutionEntityImpl child = newExecutionEntity("10");

        dbSqlSession.flushInsertEntities(ExecutionEntityImpl.class, asList(parent, child));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, parent);
        ordered.verify(sqlSession).insert(INSERT_STATEMENT, child);
    }

    @Test
    public void shouldUseSingleInsertPathWithoutSortingWhenSizeIsOne() {
        TestVar onlyOne = new TestVar(VAR_HIGH_ID);

        dbSqlSession.flushInsertEntities(TestVar.class, asList(onlyOne));

        verify(sqlSession, times(1)).insert(INSERT_STATEMENT, onlyOne);
    }

    @Test
    public void shouldSortNonExecutionDeletesByIdWhenSizeGreaterThanOne() {
        TestVar varHigh = new TestVar(VAR_HIGH_ID);
        TestVar varLow = new TestVar(VAR_LOW_ID);

        dbSqlSession.flushDeleteEntities(TestVar.class, asList(varHigh, varLow));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, varLow);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, varHigh);
    }

    @Test
    public void shouldPreserveCallerOrderForExecutionDeletesToHonourFkConstraints() {
        ExecutionEntityImpl child = newExecutionEntity("10");
        ExecutionEntityImpl parent = newExecutionEntity("4");

        dbSqlSession.flushDeleteEntities(ExecutionEntityImpl.class, asList(child, parent));

        InOrder ordered = inOrder(sqlSession);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, child);
        ordered.verify(sqlSession).delete(DELETE_STATEMENT, parent);
    }

    private static ExecutionEntityImpl newExecutionEntity(String id) {
        ExecutionEntityImpl execution = new ExecutionEntityImpl();
        execution.setId(id);
        return execution;
    }

    private static Map<String, CachedEntity> mapOfChanged(BaseStubEntity... entities) {
        LinkedHashMap<String, CachedEntity> map = new LinkedHashMap<>();
        for (BaseStubEntity entity : entities) {
            map.put(entity.getId(), markChanged(entity));
        }
        return map;
    }

    private static CachedEntity markChanged(BaseStubEntity entity) {
        CachedEntity cached = new CachedEntity(entity, true);
        entity.markModified();
        return cached;
    }

    private abstract static class BaseStubEntity implements Entity {

        private final String id;
        private Object persistentState = "original";

        protected BaseStubEntity(String id) {
            this.id = id;
        }

        void markModified() {
            this.persistentState = "modified";
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            // not needed for this test
        }

        @Override
        public boolean isInserted() {
            return false;
        }

        @Override
        public void setInserted(boolean inserted) {
            // not needed for this test
        }

        @Override
        public boolean isUpdated() {
            return true;
        }

        @Override
        public void setUpdated(boolean updated) {
            // not needed for this test
        }

        @Override
        public boolean isDeleted() {
            return false;
        }

        @Override
        public void setDeleted(boolean deleted) {
            // not needed for this test
        }

        @Override
        public Object getPersistentState() {
            return persistentState;
        }
    }

    private static final class TestExec extends BaseStubEntity {

        TestExec(String id) {
            super(id);
        }
    }

    private static final class TestVar extends BaseStubEntity {

        TestVar(String id) {
            super(id);
        }
    }
}
