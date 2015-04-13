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
package org.activiti.engine.impl.interceptor;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.agenda.AbstractOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbarrez
 */
public class DebugCommandInvoker extends CommandInvoker {

	private static final Logger logger = LoggerFactory.getLogger(DebugCommandInvoker.class);
	
	protected Map<String, List<DebugInfo>> debugInfoMap = new HashMap<String, List<DebugInfo>>();
	
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
	
	@Override
	protected void executeOperation(Runnable runnable) {
		
		DebugInfo debugInfo = null;
		if (runnable instanceof AbstractOperation) {
			
			String threadName = Thread.currentThread().getName();
			if (!debugInfoMap.containsKey(threadName)) {
				debugInfoMap.put(threadName, new ArrayList<DebugCommandInvoker.DebugInfo>());
			}
			
			debugInfo = new DebugInfo((AbstractOperation) runnable);
			debugInfoMap.get(threadName).add(debugInfo);
			
			debugInfo.setPreExecutionTime(System.currentTimeMillis());
			
		}
		
	    super.executeOperation(runnable);
	    
	    if (debugInfo != null) {
	    	debugInfo.setPostExecutionTime(System.currentTimeMillis());
	    }
	}
	
	public void logDebugInfo() {
		
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
	}
	
	public void clear() {
		debugInfoMap.clear();
	}
	
	public Map<String, List<DebugInfo>> getDebugInfoMap() {
		return debugInfoMap;
	}

	public void setDebugInfoMap(Map<String, List<DebugInfo>> debugInfoMap) {
		this.debugInfoMap = debugInfoMap;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	public static final class DebugInfo {
		
		protected long preExecutionTime;
		protected long postExecutionTime;
		protected AbstractOperation operation;
		protected String executionId;
		protected String flowElementId;
		protected Class<?> flowElementClass;
		
		public DebugInfo(AbstractOperation operation) {
	        this.operation = operation;
	        
	        // Need to capture data here, as it will change when other steps are executed
	        if (operation.getExecution() != null) {
	        	this.executionId = operation.getExecution().getId();
	        	
	        	if (operation.getExecution().getCurrentFlowElement() != null) {
	        		this.flowElementId = operation.getExecution().getCurrentFlowElement().getId();
	        		this.flowElementClass = operation.getExecution().getCurrentFlowElement().getClass();
	        	}
	        }
        }
		public long getPreExecutionTime() {
			return preExecutionTime;
		}
		public void setPreExecutionTime(long preExecutionTime) {
			this.preExecutionTime = preExecutionTime;
		}
		public long getPostExecutionTime() {
			return postExecutionTime;
		}
		public void setPostExecutionTime(long postExecutionTime) {
			this.postExecutionTime = postExecutionTime;
		}
		public AbstractOperation getOperation() {
			return operation;
		}
		public void setOperation(AbstractOperation operation) {
			this.operation = operation;
		}
		public String getExecutionId() {
			return executionId;
		}
		public void setExecutionId(String executionId) {
			this.executionId = executionId;
		}
		public String getFlowElementId() {
			return flowElementId;
		}
		public void setFlowElementId(String flowElementId) {
			this.flowElementId = flowElementId;
		}
		public Class<?> getFlowElementClass() {
			return flowElementClass;
		}
		public void setFlowElementClass(Class<?> flowElementClass) {
			this.flowElementClass = flowElementClass;
		}

	}


}
