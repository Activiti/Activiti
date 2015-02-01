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
package org.activiti.validation.validator;

import org.activiti.validation.validator.impl.ActivitiEventListenerValidator;
import org.activiti.validation.validator.impl.AssociationValidator;
import org.activiti.validation.validator.impl.BoundaryEventValidator;
import org.activiti.validation.validator.impl.DataObjectValidator;
import org.activiti.validation.validator.impl.DiagramInterchangeInfoValidator;
import org.activiti.validation.validator.impl.EndEventValidator;
import org.activiti.validation.validator.impl.ErrorValidator;
import org.activiti.validation.validator.impl.EventGatewayValidator;
import org.activiti.validation.validator.impl.EventSubprocessValidator;
import org.activiti.validation.validator.impl.EventValidator;
import org.activiti.validation.validator.impl.ExclusiveGatewayValidator;
import org.activiti.validation.validator.impl.ExecutionListenerValidator;
import org.activiti.validation.validator.impl.FlowElementValidator;
import org.activiti.validation.validator.impl.IntermediateCatchEventValidator;
import org.activiti.validation.validator.impl.IntermediateThrowEventValidator;
import org.activiti.validation.validator.impl.MessageValidator;
import org.activiti.validation.validator.impl.OperationValidator;
import org.activiti.validation.validator.impl.BpmnModelValidator;
import org.activiti.validation.validator.impl.ScriptTaskValidator;
import org.activiti.validation.validator.impl.SendTaskValidator;
import org.activiti.validation.validator.impl.SequenceflowValidator;
import org.activiti.validation.validator.impl.ServiceTaskValidator;
import org.activiti.validation.validator.impl.SignalValidator;
import org.activiti.validation.validator.impl.StartEventValidator;
import org.activiti.validation.validator.impl.SubprocessValidator;
import org.activiti.validation.validator.impl.UserTaskValidator;

/**
 * @author jbarrez
 */
public class ValidatorSetFactory {
	
	public ValidatorSet createActivitiExecutableProcessValidatorSet() {
		ValidatorSet validatorSet = new ValidatorSet(ValidatorSetNames.ACTIVITI_EXECUTABLE_PROCESS);
		
		validatorSet.addValidator(new AssociationValidator());
		validatorSet.addValidator(new SignalValidator());
		validatorSet.addValidator(new OperationValidator());
		validatorSet.addValidator(new ErrorValidator());
		validatorSet.addValidator(new DataObjectValidator());
		
		validatorSet.addValidator(new BpmnModelValidator());
		validatorSet.addValidator(new FlowElementValidator());
		
		validatorSet.addValidator(new StartEventValidator());
		validatorSet.addValidator(new SequenceflowValidator());
		validatorSet.addValidator(new UserTaskValidator());
		validatorSet.addValidator(new ServiceTaskValidator());
		validatorSet.addValidator(new ScriptTaskValidator());
		validatorSet.addValidator(new SendTaskValidator());
		validatorSet.addValidator(new ExclusiveGatewayValidator());
		validatorSet.addValidator(new EventGatewayValidator());
		validatorSet.addValidator(new SubprocessValidator());
		validatorSet.addValidator(new EventSubprocessValidator());
		validatorSet.addValidator(new BoundaryEventValidator());
		validatorSet.addValidator(new IntermediateCatchEventValidator());
		validatorSet.addValidator(new IntermediateThrowEventValidator());
		validatorSet.addValidator(new MessageValidator());
		validatorSet.addValidator(new EventValidator());
		validatorSet.addValidator(new EndEventValidator());
		
		validatorSet.addValidator(new ExecutionListenerValidator());
		validatorSet.addValidator(new ActivitiEventListenerValidator());
		
		validatorSet.addValidator(new DiagramInterchangeInfoValidator());
		
		return validatorSet;
	}

}
