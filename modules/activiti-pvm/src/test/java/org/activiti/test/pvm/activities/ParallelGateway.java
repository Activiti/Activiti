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

import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.process.PvmTransition;

/**
 * @author Tom Baeyens
 */
public class ParallelGateway implements ActivityBehavior {
  
  public void start(ActivityContext activityContext) {
    int incomingTransitionsSize = activityContext.getActivity().getIncomingTransitions().size();
    if (incomingTransitionsSize==1) {
      activate(activityContext);
      
    } else { //incomingTransitionsSize > 1
      
      Integer joinCount = (Integer) activityContext.getSystemVariable("joinCount");
      if (joinCount==null) {
        activityContext.setSystemVariable("joinCount", new Integer(1));
      } else {
        joinCount = joinCount + 1;
        if (joinCount==incomingTransitionsSize) {
          activate(activityContext);
        }
      }
    }
  }

  protected void activate(ActivityContext activityContext) {
    for (PvmTransition transition: activityContext.getActivity().getOutgoingTransitions()) {
      activityContext.take(transition);
    }
  }
}
