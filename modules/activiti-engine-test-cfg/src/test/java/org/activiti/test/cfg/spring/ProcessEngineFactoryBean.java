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

package org.activiti.test.cfg.spring;

import org.activiti.DbProcessEngineBuilder;
import org.activiti.ProcessEngine;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Dave Syer
 */
public class ProcessEngineFactoryBean implements FactoryBean {

	private String configurationResource = "activiti.properties";

	public void setConfigurationResource(String configurationResource) {
		this.configurationResource = configurationResource;
	}

	public Object getObject() throws Exception {
		// ProcessEngines is not very friendly for configuration
		// ProcessEngines.init();
		// return ProcessEngines.getDefaultProcessEngine();
		return new DbProcessEngineBuilder().configureFromPropertiesResource(
				configurationResource).buildProcessEngine();
	}

	public Class<?> getObjectType() {
		return ProcessEngine.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
