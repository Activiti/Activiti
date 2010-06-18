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
package org.activiti.test.persistence.inmemorylistsession;

import java.util.ArrayList;
import java.util.List;

import org.activiti.impl.execution.JobImpl;
import org.activiti.impl.tx.TransactionContext;

/**
 * A slightly hacky wrapper to let you stuff
 *  a group of jobs for the same thread into
 *  the in memory persistence.
 */
public class GroupOfJobs extends JobImpl<JobImpl>{
  private List<JobImpl> jobs = new ArrayList<JobImpl>();
  
  public void add(JobImpl job) {
    jobs.add(job);
  }
  public List<JobImpl> get() {
    return jobs;
  }

  @Override
  public JobImpl execute(TransactionContext transactionContext) {
    throw new IllegalStateException("Must be decomposed into individual jobs first!");
  }
}
