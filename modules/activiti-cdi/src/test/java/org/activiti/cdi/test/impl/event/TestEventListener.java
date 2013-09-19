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
package org.activiti.cdi.test.impl.event;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.activiti.cdi.BusinessProcessEvent;
import org.activiti.cdi.annotation.event.BusinessProcess;
import org.activiti.cdi.annotation.event.CreateTask;
import org.activiti.cdi.annotation.event.EndActivity;
import org.activiti.cdi.annotation.event.StartActivity;
import org.activiti.cdi.annotation.event.TakeTransition;

@ApplicationScoped
public class TestEventListener {
  
  public void reset() {
    startActivityService1 = 0;
    endActivityService1 = 0;
    takeTransitiont1 = 0;
    
    eventsReceivedByKey.clear();
    eventsReceived.clear();
  }

  private final Set<BusinessProcessEvent> eventsReceivedByKey = new HashSet<BusinessProcessEvent>();
  
  // receives all events related to "process1"
  public void onProcessEventByKey(@Observes @BusinessProcess("process1") BusinessProcessEvent businessProcessEvent) {
    eventsReceivedByKey.add(businessProcessEvent);
  }

  public Set<BusinessProcessEvent> getEventsReceivedByKey() {
    return eventsReceivedByKey;
  }

  
  // ---------------------------------------------------------
  
  private final Set<BusinessProcessEvent> eventsReceived = new HashSet<BusinessProcessEvent>();
  
  // receives all events
  public void onProcessEvent(@Observes BusinessProcessEvent businessProcessEvent) {
    eventsReceived.add(businessProcessEvent);
  }

  public Set<BusinessProcessEvent> getEventsReceived() {
    return eventsReceived;
  }
  
  // ---------------------------------------------------------
  
  private int startActivityService1 = 0;
  private int endActivityService1 = 0;
  private int takeTransitiont1 = 0;
  private int assignTask1 = 0;
  private int completeTask1 = 0;
  private int completeTask2 = 0;
  private int createTask1 = 0;
  private int createTask2 = 0;
    
  public void onStartActivityService1(@Observes @StartActivity("service1") BusinessProcessEvent businessProcessEvent) {    
    startActivityService1 += 1;
  }

  public void onEndActivityService1(@Observes @EndActivity("service1") BusinessProcessEvent businessProcessEvent) {
    endActivityService1 += 1;
  }

  public void takeTransitiont1(@Observes @TakeTransition("t1") BusinessProcessEvent businessProcessEvent) {
    takeTransitiont1 += 1;    
  }
  
  public void onCreateTask1(@Observes @CreateTask("usertask1") BusinessProcessEvent businessProcessEvent) {
    createTask1 += 1;
  }
  
  public void onCreateTask2(@Observes @CreateTask("usertask2") BusinessProcessEvent businessProcessEvent) {
    createTask2 += 1;
  }
  
  public void onAssignTask1(@Observes @CreateTask("usertask1") BusinessProcessEvent businessProcessEvent) {
    assignTask1 += 1;
  }
  
  public void onCompleteTask1(@Observes @CreateTask("usertask1") BusinessProcessEvent businessProcessEvent) {
    completeTask1 += 1;
  }
  
  public void onCompleteTask2(@Observes @CreateTask("usertask2") BusinessProcessEvent businessProcessEvent) {
    completeTask2 += 1;
  }
  
  public int getEndActivityService1() {
    return endActivityService1;
  }
    
  public int getStartActivityService1() {
    return startActivityService1;
  }
    
  public int getTakeTransitiont1() {
    return takeTransitiont1;
  }
  
  public int getCreateTask1() {
    return createTask1;
  }
  
  public int getAssignTask1() {
    return assignTask1;
  }
  
  public int getCompleteTask1() {
    return completeTask1;
  }
  
  
  public int getCompleteTask2() {
    return completeTask2;
  }
  
  public int getCreateTask2() {
    return createTask2;
  }
}
