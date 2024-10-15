package org.activiti.engine.impl.db;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.activiti.engine.impl.persistence.entity.AbstractEntity;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DbSqlSessionTest
{
  @Mock
  private DbSqlSessionFactory dbSqlSessionFactoryMock;

  @Mock
  private SqlSessionFactory sqlSessionFactoryMock;

  @Mock
  private SqlSession sqlSessionMock;

  private DbSqlSession dbSqlSession;

  @Before
  public void constructDbSqlSession()
  {
    when(dbSqlSessionFactoryMock.getSqlSessionFactory()).thenReturn(sqlSessionFactoryMock);
    when(sqlSessionFactoryMock.openSession()).thenReturn(sqlSessionMock);
    when(dbSqlSessionFactoryMock.mapStatement("someStatement")).thenReturn("someMappedStatement");

    dbSqlSession = new DbSqlSession(dbSqlSessionFactoryMock, null);
  }

  @Test
  public void testFlushDeletesWithCustomEntityBulkDeletion()
  {
    dbSqlSession.delete("someStatement", "myParam", CustomEntity.class);

    // act
    dbSqlSession.flushDeletes();

    // assert
    verify(sqlSessionMock).delete("someMappedStatement", "myParam");
  }

  private static class CustomEntity extends AbstractEntity
  {
    @Override
    public Object getPersistentState()
    {
      return null;
    }
  }
}
