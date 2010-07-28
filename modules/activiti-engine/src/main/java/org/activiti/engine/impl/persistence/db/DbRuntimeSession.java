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

package org.activiti.engine.impl.persistence.db;

import org.activiti.engine.impl.persistence.RuntimeSession;
import org.activiti.engine.impl.persistence.runtime.ActivityInstanceEntity;
import org.activiti.engine.impl.persistence.runtime.ProcessInstanceEntity;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.tx.Session;


/**
 * @author Tom Baeyens
 */
public class DbRuntimeSession implements Session, RuntimeSession {
  
  protected DbSqlSession dbSqlSession;
  
  public DbRuntimeSession() {
    dbSqlSession = CommandContext
      .getCurrent()
      .getSession(DbSqlSession.class);
  }

  public void close() {
  }

  public void flush() {
  }

  public void delete(ProcessInstanceEntity processInstance) {
  }

  public void delete(ActivityInstanceEntity activityInstance) {
  }

  public void insert(ProcessInstanceEntity processInstance) {
  }

  public void insert(ActivityInstanceEntity activityInstance) {
  }
}
