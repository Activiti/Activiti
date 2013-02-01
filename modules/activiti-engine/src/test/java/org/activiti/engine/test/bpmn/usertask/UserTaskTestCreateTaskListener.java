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

package org.activiti.engine.test.bpmn.usertask;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.el.Expression;



/**
 * @author Saeid Mirzaei
 */

/**
 * This is for test case UserTaskTest.testCompleteAfterParallelGateway
 * 
 */

public class UserTaskTestCreateTaskListener implements TaskListener {

  private static final long serialVersionUID = 1L;
  private Expression expression;
	
	@Override
	public void notify(DelegateTask delegateTask) {
		
		if (this.expression != null && this.expression.getValue(delegateTask) != null) {
			// get the expression variable 
			String expression = this.expression.getValue(delegateTask).toString();
			
			// this expression will be evaluated when completing the task
			delegateTask.setVariableLocal("validationRule", expression);
		}
		
	}
	
}


