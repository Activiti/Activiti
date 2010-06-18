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

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.job.JobImpl;
import org.activiti.impl.persistence.PersistenceSession;


/**
 * @author Nick Burch
 */
public class AcquireJobCmd implements Command<Collection<String>> {

  private String lockOwner;
  private int lockTimeInMillis;
  
  public AcquireJobCmd(String lockOwner, int lockTimeInMillis){
    this.lockOwner = lockOwner;
    this.lockTimeInMillis = lockTimeInMillis;
  }

  public Collection<String> execute(CommandContext commandContext) {
    PersistenceSession persistenceSession = commandContext.getPersistenceSession();
    
    JobImpl job = persistenceSession.findNextJobToExecute();
    job.setLockOwner(lockOwner);
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockExpirationTime(gregorianCalendar.getTime());
    
    Collection<String> jobIds = new HashSet<String>();
    jobIds.add(job.getId());
    
    if (job.isExclusive()) {
      // TODO acquire other exclusive jobs for the same process instance.
    }
    
    return jobIds;
  }
}
