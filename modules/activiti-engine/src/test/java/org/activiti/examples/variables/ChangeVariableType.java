<<<<<<< HEAD
package org.activiti.examples.variables;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ChangeVariableType implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		// Initially set to null, stored as NullType
		execution.setVariable("myVar", null);
		
		// Now set to something stored as SerializableType. This could happen much later on than this.
		execution.setVariable("myVar", new SomeSerializable("someValue"));
	}

}
=======
package org.activiti.examples.variables;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ChangeVariableType implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		// Initially set to null, stored as NullType
		execution.setVariable("myVar", null);
		
		// Now set to something stored as SerializableType. This could happen much later on than this.
		execution.setVariable("myVar", new SomeSerializable("someValue"));
	}

}
>>>>>>> upstream/master
