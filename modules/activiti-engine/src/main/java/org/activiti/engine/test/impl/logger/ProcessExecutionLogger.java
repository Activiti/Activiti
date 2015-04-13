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
package org.activiti.engine.test.impl.logger;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbarrez
 */
public class ProcessExecutionLogger {

	private static final Logger logger = LoggerFactory.getLogger(LoggingCommandInvoker.class);
	
	protected Map<String, List<DebugInfo>> debugInfoMap = new HashMap<String, List<DebugInfo>>();
	
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
	
	public ProcessExecutionLogger() {
		
	}
	
	public synchronized void addDebugInfo(DebugInfo debugInfo) {
		String threadName = Thread.currentThread().getName();
		if (!debugInfoMap.containsKey(threadName)) {
			debugInfoMap.put(threadName, new ArrayList<DebugInfo>());
		}
		debugInfoMap.get(threadName).add(debugInfo);
	}
	
	public void logDebugInfo() {
		logDebugInfo(false);
	}
	
	public void logDebugInfo(boolean clearAfterLogging) {
		
		logger.info("--------------------------------");
		logger.info("CommandInvoker debug information");
		logger.info("--------------------------------");
		for (String threadName : debugInfoMap.keySet()) {
			
			logger.info("");
			logger.info("Thread '" + threadName + "':");
			logger.info("");
			
			for (DebugInfo debugInfo : debugInfoMap.get(threadName)) {
				
				StringBuilder strb = new StringBuilder();
				
				// Timing info
				strb.append("[" + dateFormat.format(new Date(debugInfo.getPreExecutionTime())) + " - " + dateFormat.format(new Date(debugInfo.getPostExecutionTime())) 
					+ " (" + (debugInfo.getPostExecutionTime() - debugInfo.getPreExecutionTime()) + "ms)]");
				
				// Operation info
				strb.append(" " + debugInfo.getOperation().getClass().getSimpleName() + " ");
				
				// Execution info
				if (debugInfo.getExecutionId() != null) {
					strb.append("with execution " + debugInfo.getExecutionId());
					
					if (debugInfo.getFlowElementId() != null) {
						strb.append(" at flow element " + debugInfo.getFlowElementId() + " (" + debugInfo.getFlowElementClass().getSimpleName() + ")");
					}
				}
				
				logger.info(strb.toString()); 
			}
			
		}
		
		logger.info("");
		
		if (clearAfterLogging) {
			clear();
		}
	}
	
	public void clear() {
		debugInfoMap.clear();
	}
	
	
}
