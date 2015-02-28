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
package org.activiti.engine.test.jobexecutor;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Saeid Mirzaei
 */

public class RetryFlag implements JavaDelegate {
	static boolean visited = false;
	
	  private static final Logger log = LoggerFactory.getLogger(RetryFlag.class);
	
	static public void reset() {
		visited = false;
	}
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
	    visited =  true;
	    log.debug("Flag delegate class called");
	}
}
