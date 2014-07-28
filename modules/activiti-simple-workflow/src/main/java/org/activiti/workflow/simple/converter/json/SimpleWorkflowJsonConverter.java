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
package org.activiti.workflow.simple.converter.json;

import java.io.InputStream;
import java.io.Writer;
import java.util.List;

import org.activiti.workflow.simple.definition.ChoiceStepsDefinition;
import org.activiti.workflow.simple.definition.DelayStepDefinition;
import org.activiti.workflow.simple.definition.FeedbackStepDefinition;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.ListConditionStepDefinition;
import org.activiti.workflow.simple.definition.ListStepDefinition;
import org.activiti.workflow.simple.definition.ParallelStepsDefinition;
import org.activiti.workflow.simple.definition.ScriptStepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.definition.form.BooleanPropertyDefinition;
import org.activiti.workflow.simple.definition.form.DatePropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyDefinition;
import org.activiti.workflow.simple.definition.form.NumberPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ReferencePropertyDefinition;
import org.activiti.workflow.simple.definition.form.TextPropertyDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A converter capable of converting {@link WorkflowDefinition}s from and to a
 * JSON representation.
 * <br>
 * <p>Instances of this class <b>are thread-safe</b>.</p>
 * 
 * @author Frederik Heremans
 */
public class SimpleWorkflowJsonConverter {

	protected ObjectMapper objectMapper;
	protected List<Class<?>> additionalModelClasses;

	/**
	 * @param inputStream the stream to read the JSON from.
	 * @return The workflow definition instance, read from the given input-stream.
	 * @throws SimpleWorkflowException when an error occurs while reading or parsing the definition.
	 */
	public WorkflowDefinition readWorkflowDefinition(InputStream inputStream) throws SimpleWorkflowException {
		try {
	    return getObjectMapper().readValue(inputStream, WorkflowDefinition.class);
    } catch (Exception e) {
    	throw wrapExceptionRead(e);
    }
	}
	
	/**
	 * @param bytes array representing the definition JSON.
	 * @return The workflow definition instance, parsed from the given array.
	 * @throws SimpleWorkflowException when an error occurs while parsing the definition.
	 */
	public WorkflowDefinition readWorkflowDefinition(byte[] bytes) throws SimpleWorkflowException {
		try {
	    return getObjectMapper().readValue(bytes, WorkflowDefinition.class);
    } catch (Exception e) {
    	throw wrapExceptionRead(e);
    }
	}
	
	public void writeWorkflowDefinition(WorkflowDefinition definition, Writer writer) {
		try {
	    getObjectMapper().writeValue(writer, definition);
    } catch (Exception e) {
    	throw wrapExceptionWrite(e);
    }
	}
	
	/**
	 * @param inputStream the stream to read the JSON from.
	 * @return The workflow definition instance, read from the given input-stream.
	 * @throws SimpleWorkflowException when an error occurs while reading or parsing the definition.
	 */
	public FormDefinition readFormDefinition(InputStream inputStream) {
		try {
	    return getObjectMapper().readValue(inputStream, FormDefinition.class);
    } catch (Exception e) {
    	throw wrapExceptionRead(e);
    }
	}
	
	public void writeFormDefinition(FormDefinition definition, Writer writer) {
		try {
	    getObjectMapper().writeValue(writer, definition);
    } catch (Exception e) {
    	throw wrapExceptionWrite(e);
    }
	}
	
	/**
	 * @param e exception to wrap
	 * @return an {@link SimpleWorkflowException} to throw, wrapping the given exception.
	 */
	protected SimpleWorkflowException wrapExceptionRead(Exception e) {
	  return new SimpleWorkflowException("Error while parsing JSON", e);
  }
	
	/**
	 * @param e exception to wrap
	 * @return an {@link SimpleWorkflowException} to throw, wrapping the given exception.
	 */
	protected SimpleWorkflowException wrapExceptionWrite(Exception e) {
		return new SimpleWorkflowException("Error while writing JSON", e);
	}

	protected ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			
			// Ensure ObjectMapper is only initialized once
			synchronized (this) {
				if (objectMapper == null) {
					objectMapper = new ObjectMapper();
				
					// Register all property-definition model classes as sub-types
					objectMapper.registerSubtypes(ListPropertyDefinition.class, TextPropertyDefinition.class,
					    ReferencePropertyDefinition.class, DatePropertyDefinition.class, NumberPropertyDefinition.class, BooleanPropertyDefinition.class);

					// Register all step-types
					objectMapper.registerSubtypes(HumanStepDefinition.class, FeedbackStepDefinition.class, ParallelStepsDefinition.class, ChoiceStepsDefinition.class, 
					    ListStepDefinition.class, ListConditionStepDefinition.class, ScriptStepDefinition.class, DelayStepDefinition.class);
					// Register additional sub-types to allow custom model entities to be
					// deserialized correctly
					if (additionalModelClasses != null) {
						objectMapper.registerSubtypes(additionalModelClasses.toArray(new Class<?>[] {}));
					}
				}
			}
		}
		return objectMapper;
	}
}
