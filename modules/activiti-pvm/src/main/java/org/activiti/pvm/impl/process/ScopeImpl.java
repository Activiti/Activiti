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

package org.activiti.pvm.impl.process;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public class ScopeImpl extends ProcessElementImpl {

  protected String id;
  protected String name;
  protected List<ActivityImpl> activities = new ArrayList<ActivityImpl>();
  
  public ActivityImpl findActivity(String activityName) {
    for (ActivityImpl activity: activities) {
      if (activityName.equals(activity.getId())) {
        return activity;
      }
    }
    for (ActivityImpl activity: activities) {
      ActivityImpl nestedActivity = activity.findActivity(activityName);
      if (nestedActivity!=null) {
        return nestedActivity;
      }
    }
    return null;
  }

  public ActivityImpl createActivity() {
    ActivityImpl activity = new ActivityImpl();
    activity.setParent(this);
    activities.add(activity);
    return  activity;
  }

  // restrictred setters //////////////////////////////////////////////////////
  
  protected void setActivities(List<ActivityImpl> activities) {
    this.activities = activities;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getId() {
    return id;
  }
  public void setId(String name) {
    this.id = name;
  }
  public List<ActivityImpl> getActivities() {
    return activities;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
}
