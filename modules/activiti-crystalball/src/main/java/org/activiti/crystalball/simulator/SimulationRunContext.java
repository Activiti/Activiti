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
package org.activiti.crystalball.simulator;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.runtime.Clock;

import java.util.Stack;

/**
 * Context in which simulation is run.
 * It contains references to process engine, event calendar.
 *
 * @author martin.grofcik
 */
public abstract class SimulationRunContext {
    
	//
	//  Process engine on which simulation will be executed
	//
	protected static ThreadLocal<Stack<ProcessEngineImpl>> processEngineThreadLocal = new ThreadLocal<Stack<ProcessEngineImpl>>();

  //
  // Simulation objects
  //
	protected static ThreadLocal<Stack<EventCalendar>> eventCalendarThreadLocal = new ThreadLocal<Stack<EventCalendar>>();

  //
  // Variable scope used for getting/setting variables to the simulationManager
  //
  protected static ThreadLocal<Stack<VariableScope>> executionThreadLocal = new ThreadLocal<Stack<VariableScope>>();

  public static RuntimeService getRuntimeService() {
		Stack<ProcessEngineImpl> stack = getStack(processEngineThreadLocal);
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek().getRuntimeService();
	}

	public static void setProcessEngine(ProcessEngineImpl processEngine) {
	  getStack(processEngineThreadLocal).push(processEngine);
	}

  public static ProcessEngineImpl getProcessEngine() {
    return getStack(processEngineThreadLocal).peek();
  }

	public static void removeProcessEngine() {
	  getStack(processEngineThreadLocal).pop();
	}

	public static TaskService getTaskService() {
		Stack<ProcessEngineImpl> stack = getStack(processEngineThreadLocal);
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek().getTaskService();
	}
	
	public static EventCalendar getEventCalendar() {
		Stack<EventCalendar> stack = getStack(eventCalendarThreadLocal);
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek();
	}

	public static void setEventCalendar(EventCalendar eventCalendar) {
	  getStack(eventCalendarThreadLocal).push(eventCalendar);
	}

	public static void removeEventCalendar() {
	  getStack(eventCalendarThreadLocal).pop();
	}

	public static HistoryService getHistoryService() {
		Stack<ProcessEngineImpl> stack = getStack(processEngineThreadLocal);
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek().getHistoryService();
	}

	
  public static RepositoryService getRepositoryService() {
		Stack<ProcessEngineImpl> stack = getStack(processEngineThreadLocal);
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek().getRepositoryService();
	}

  public static VariableScope getExecution() {
    Stack<VariableScope> stack = getStack(executionThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setExecution(VariableScope execution) {
    getStack(executionThreadLocal).push(execution);
  }

  public static Clock getClock() {
    return getProcessEngine().getProcessEngineConfiguration().getClock();
  }

  protected static <T> Stack<T> getStack(ThreadLocal<Stack<T>> threadLocal) {
		Stack<T> stack = threadLocal.get();
		if (stack == null) {
			stack = new Stack<T>();
			threadLocal.set(stack);
		}
		return stack;
	}

}
