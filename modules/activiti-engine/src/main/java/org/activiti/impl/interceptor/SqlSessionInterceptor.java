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
package org.activiti.impl.interceptor;

import org.activiti.impl.Cmd;
import org.activiti.impl.ProcessEngineImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Interceptor that opens a new {@link SqlSession} through the
 * {@link SqlSessionFactory} configured in the process engine context.
 * 
 * A new JDBC transaction is started when the sqlSession is opened.
 * At the end of this interceptor, the sqlSession is closed which
 * also commits the transaction.
 * 
 * TODO: remove: this is no longer necessary: a persistenceSession will be created
 * in the transactionContext and will commit/rollbacl in case of errors
 * 
 * @author Joram Barrez
 */
public class SqlSessionInterceptor extends Interceptor {

  public <T> T execute(Cmd<T> cmd, ProcessEngineImpl processEngine) {
    throw new UnsupportedOperationException();
//    SqlSessionFactory sqlSessionFactory = processEngine.getProcessEngine().getConfigObject(Configuration.NAME_IBATISSQLSESSIONFACTORY, SqlSessionFactory.class);
//    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH); // Not quite sure if this is the right setting? We do want multiple updates to be batched for performance ...
//    boolean success = false;
//    try {
//      processEngine.setConfiguredObject(sqlSession);
//      T result = next.execute(cmd, processEngine);
//      success = true;
//      return result;
//    } finally {
//      if (success) {
//        sqlSession.commit(true);
//      } else {
//        sqlSession.rollback(true);        
//      }
//      sqlSession.close();        
//    }
  }

}
