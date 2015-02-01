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
package org.activiti.explorer.ui.management.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.runtime.Job;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * @author Frederik Heremans
 */
public class JobListQuery extends AbstractLazyLoadingQuery {
  
  protected transient ManagementService managementService;
  
  public JobListQuery() {
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
  }

  public int size() {
    return (int) managementService.createJobQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<Job> jobs = managementService.createJobQuery()
      .orderByJobDuedate().asc()
      .orderByJobId().asc()
      .list();
    
    List<Item> items = new ArrayList<Item>();
    for (Job job : jobs) {
      items.add(new JobListItem(job));
    }
    return items;
  }

  public Item loadSingleResult(String id) {
    Job job = managementService.createJobQuery().jobId(id).singleResult();
    if (job != null) {
      return new JobListItem(job);      
    }
    return null;
  }
  
  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  class JobListItem extends PropertysetItem implements Comparable<JobListItem> {
    
    private static final long serialVersionUID = 1L;
    
    
    public JobListItem(Job job) {
      addItemProperty("id", new ObjectProperty<String>(job.getId(), String.class));
      addItemProperty("dueDate", new ObjectProperty<Date>(job.getDuedate(), Date.class));
      addItemProperty("name", new ObjectProperty<String>(getName(job), String.class));
    }
    
    private String getName(Job theJob) {
      if(theJob instanceof TimerEntity) {
        return "Timer job " + theJob.getId();
      } else if (theJob instanceof MessageEntity) {
        return "Message job " + theJob.getId();
      } else {
        return "Job " + theJob.getId();
      }
    }

    public int compareTo(JobListItem other) {
      Date dueDate = (Date) getItemProperty("dueDate").getValue();
      Date otherDueDate = (Date) other.getItemProperty("dueDate").getValue();
      
      int comparison = compareObjects(dueDate, otherDueDate);
      if (comparison != 0) {
        return comparison;
      } else {
        String id = (String) getItemProperty("id").getValue();
        String otherId = (String) other.getItemProperty("id").getValue();
        return id.compareTo(otherId);
      }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Object> int  compareObjects(Comparable<T> object, Comparable<T> other) {
      if(object != null) {
        if(other != null) {
          return object.compareTo((T)other);
        } else {
          return 1;
        }
      } else {
        if(other == null) {
          return 0;
        } else {
          // Null is smaller than non-null value
          return -1;
        }
      }
    }
  }
  
}
