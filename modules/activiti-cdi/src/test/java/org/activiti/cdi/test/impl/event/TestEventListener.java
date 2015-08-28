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
import org.activiti.cdi.annotation.event.AssignTask;
import org.activiti.cdi.annotation.event.BusinessProcess;
import org.activiti.cdi.annotation.event.CompleteTask;
import org.activiti.cdi.annotation.event.CreateTask;
import org.activiti.cdi.annotation.event.DeleteTask;
import org.activiti.cdi.annotation.event.EndActivity;
import org.activiti.cdi.annotation.event.StartActivity;
import org.activiti.cdi.annotation.event.TakeTransition;

@ApplicationScoped
public class TestEventListener {
  
  public void reset() {
    startActivityService1WithLoopCounter = 0;
    startActivityService1WithoutLoopCounter = 0;
    endActivityService1 = 0;
    takeTransitiont1 = 0;
    takeTransitiont2 = 0;
    takeTransitiont3 = 0;
    startActivityService2WithLoopCounter = 0;
    startActivityService2WithoutLoopCounter = 0;
    endActivityService2 = 0;
    createTask1 = 0;
    createTask2 = 0;
    assignTask1 = 0;
    completeTask1 = 0;
    completeTask2 = 0;
    completeTask3 = 0;
    deleteTask3 = 0;
    
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
  
  private int startActivityService1WithLoopCounter = 0;
  private int startActivityService1WithoutLoopCounter = 0;
  private int endActivityService1 = 0;
  private int startActivityService2WithLoopCounter = 0;
  private int startActivityService2WithoutLoopCounter = 0;
  private int endActivityService2 = 0;
  private int takeTransitiont1 = 0;
  private int takeTransitiont2 = 0;
  private int takeTransitiont3 = 0;
  private int assignTask1 = 0;
  private int completeTask1 = 0;
  private int completeTask2 = 0;
  private int completeTask3 = 0;
  private int createTask1 = 0;
  private int createTask2 = 0;
  private int deleteTask3 = 0;
    
  public void onStartActivityService1(@Observes @StartActivity("service1") BusinessProcessEvent businessProcessEvent) {    
  	Integer loopCounter = (Integer) businessProcessEvent.getVariableScope().getVariable("loopCounter");
  	if (loopCounter != null) {
  		startActivityService1WithLoopCounter += 1;
  	} else {
  		startActivityService1WithoutLoopCounter += 1;
  	}
  }

  public void onEndActivityService1(@Observes @EndActivity("service1") BusinessProcessEvent businessProcessEvent) {
    endActivityService1 += 1;
  }
  
  public void onStartActivityService2(@Observes @StartActivity("service2") BusinessProcessEvent businessProcessEvent) {    
   	Integer loopCounter = (Integer) businessProcessEvent.getVariableScope().getVariable("loopCounter");
  	if (loopCounter != null) {
  		startActivityService2WithLoopCounter += 1;
  	} else {
  		startActivityService2WithoutLoopCounter += 1;
  	}
  }

  public void onEndActivityService2(@Observes @EndActivity("service2") BusinessProcessEvent businessProcessEvent) {
    endActivityService2 += 1;
  }

  public void takeTransitiont1(@Observes @TakeTransition("t1") BusinessProcessEvent businessProcessEvent) {
    takeTransitiont1 += 1;    
  }
  
  public void takeTransitiont2(@Observes @TakeTransition("t2") BusinessProcessEvent businessProcessEvent) {
    takeTransitiont2 += 1;    
  }
  
  public void takeTransitiont3(@Observes @TakeTransition("t3") BusinessProcessEvent businessProcessEvent) {
    takeTransitiont3 += 1;    
  }
  
  public void onCreateTask1(@Observes @CreateTask("usertask1") BusinessProcessEvent businessProcessEvent) {
    createTask1 += 1;
  }
  
  public void onCreateTask2(@Observes @CreateTask("usertask2") BusinessProcessEvent businessProcessEvent) {
    createTask2 += 1;
  }
  
  public void onAssignTask1(@Observes @AssignTask("usertask1") BusinessProcessEvent businessProcessEvent) {
    assignTask1 += 1;
  }
  
  public void onCompleteTask1(@Observes @CompleteTask("usertask1") BusinessProcessEvent businessProcessEvent) {
    completeTask1 += 1;
  }
  
  public void onCompleteTask2(@Observes @CompleteTask("usertask2") BusinessProcessEvent businessProcessEvent) {
    completeTask2 += 1;
  }
  
  public void onCompleteTask3(@Observes @CompleteTask("usertask3") BusinessProcessEvent businessProcessEvent) {
    completeTask3 += 1;
  }
  
  public void onDeleteTask3(@Observes @DeleteTask("usertask3") BusinessProcessEvent businessProcessEvent) {
    deleteTask3 += 1;
  }
  
  public int getEndActivityService1() {
    return endActivityService1;
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
  
  public int getCompleteTask3() {
    return completeTask3;
  }
  
  public int getCreateTask2() {
    return createTask2;
  }
  
  public int getEndActivityService2() {
    return endActivityService2;
  }
  
  public int getTakeTransitiont2() {
    return takeTransitiont2;
  }
  
  public int getTakeTransitiont3() {
    return takeTransitiont3;
  }
  
  public int getDeleteTask3() {
    return deleteTask3;
  }

	public int getStartActivityService1WithLoopCounter() {
		return startActivityService1WithLoopCounter;
	}

	public int getStartActivityService1WithoutLoopCounter() {
		return startActivityService1WithoutLoopCounter;
	}

	public int getStartActivityService2WithLoopCounter() {
		return startActivityService2WithLoopCounter;
	}

	public int getStartActivityService2WithoutLoopCounter() {
		return startActivityService2WithoutLoopCounter;
	}
  
}
