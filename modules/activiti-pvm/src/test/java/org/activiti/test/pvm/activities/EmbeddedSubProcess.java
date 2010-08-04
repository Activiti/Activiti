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

package org.activiti.test.pvm.activities;

import java.util.ArrayList;
import java.util.List;

import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.activity.CompositeActivityBehavior;
import org.activiti.pvm.activity.SignallableActivityBehaviour;
import org.activiti.pvm.process.PvmActivity;
import org.activiti.pvm.process.PvmTransition;


/**
 * @author Tom Baeyens
 */
public class EmbeddedSubProcess implements CompositeActivityBehavior, SignallableActivityBehaviour {

  public void start(ActivityContext activityContext) throws Exception {
    List<PvmActivity> startActivities = new ArrayList<PvmActivity>();
    for (PvmActivity activity: activityContext.getActivity().getActivities()) {
      if (activity.getIncomingTransitions().isEmpty()) {
        startActivities.add(activity);
      }
    }
    
    for (PvmActivity startActivity: startActivities) {
      activityContext.executeActivity(startActivity);
    }
  }

  public void activityInstanceEnded(ActivityContext activityContext) throws Exception {
    if (activityContext.getActivityInstance().getActivityInstances().isEmpty()) {
      for (PvmTransition transition: activityContext.getActivity().getOutgoingTransitions()) {
        activityContext.take(transition);
      }
    }
  }

  // used by timers
  public void signal(ActivityContext activityContext, String signalName, Object signalData) throws Exception {
    PvmActivity timerActivity = activityContext.getActivity().findActivity(signalName);
    boolean isInterrupting = (Boolean) timerActivity.getProperty("isInterrupting");
    if (!isInterrupting) {
      activityContext.keepAlive();
    }
    for (PvmTransition transition: timerActivity.getOutgoingTransitions()) {
      activityContext.take(transition);
    }
  }
}
