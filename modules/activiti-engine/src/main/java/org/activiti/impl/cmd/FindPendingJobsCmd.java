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
package org.activiti.impl.cmd;

import java.util.Collection;
import java.util.List;

import org.activiti.impl.Cmd;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.tx.TransactionContext;


/**
 * Finds the IDs of jobs which need to be executed.
 * Returns something like:
 * [
 *    [JobA]
 *    [JobB1,JobB2,JobB3],
 *    [JobC]
 * ]
 * Where jobs A and C can be run on their own,
 *  but the B ones must be run one after another
 *  on the same Thread.
 * @author Nick Burch
 */
public class FindPendingJobsCmd implements Cmd<Collection<Collection<Long>>> {
  private int limit;
  
  /**
   * @param limit The maximum number of groups of jobs to return.
   */
  public FindPendingJobsCmd(int limit) {
    this.limit = limit;
  }

  @SuppressWarnings("unchecked")
  public Collection<Collection<Long>> execute(TransactionContext transactionContext) {
    PersistenceSession persistenceSession = transactionContext.getTransactionalObject(PersistenceSession.class);
    List<List<Long>> jobIds = persistenceSession.findPendingJobs(limit);
    return (Collection<Collection<Long>>)(Collection)jobIds;
  }
}
