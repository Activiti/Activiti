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
package org.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Joram Barrez
 */
public class NewEngineApp {
	
	public static void main(String[] args) {
	    
		System.out.println("Booting process engine");
		ProcessEngine processEngine = ProcessEngineConfiguration
	    		.createProcessEngineConfigurationFromResource("activiti.cfg.xml")
	    		.buildProcessEngine();
		
	    // Start a process in old engine
		System.out.println("Starting process instance from old engine ...");
		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		
		System.out.println("ProcessInstance = " + processInstance);
	    System.out.println("Done. Nr of tasks active = " + processEngine.getTaskService().createTaskQuery().count());
		
		// Start a process in new engine 
	    
	    System.out.println("Starting process instance from old engine ...");
	    RepositoryService repositoryService = processEngine.getRepositoryService();
	    repositoryService.createDeployment().addClasspathResource("oneTaskProcess.bpmn20.xml").deploy();
	    
	    processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
	    
	    System.out.println("ProcessInstance = " + processInstance);
	    System.out.println("Done. Nr of tasks active = " + processEngine.getTaskService().createTaskQuery().count());
	}

}
