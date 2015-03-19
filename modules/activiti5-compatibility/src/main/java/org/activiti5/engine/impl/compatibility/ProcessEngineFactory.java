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
package org.activiti5.engine.impl.compatibility;

import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti5.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class ProcessEngineFactory {
	
	/**
	 * Takes in an Activiti 6 process engine config, spits out an Activiti 5 Process engine.
	 */
	public static ProcessEngine buildProcessEngine(org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl activiti6Configuration) {
		
		// TODO: jta/spring/custom type
		
		ProcessEngineConfigurationImpl activiti5Configuration = null;
		if (activiti6Configuration instanceof org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration) {
			activiti5Configuration = new StandaloneProcessEngineConfiguration();
			
			activiti5Configuration.setDataSource(activiti6Configuration.getDataSource());
			
			
		} else {
			throw new RuntimeException("Unsupported process engine configuration");
		}
		
		return activiti5Configuration.buildProcessEngine();
		
	}

}
