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
package org.activiti.engine.runtime;

import java.io.Serializable;
import java.util.Set;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.query.Query;

/**
 * Allows programmatic querying of {@link ProcessInstance}s.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 */
public interface ProcessInstanceQuery extends Query<ProcessInstanceQuery, ProcessInstance> {

  /** Select the process instance with the given id */
  ProcessInstanceQuery processInstanceId(String processInstanceId);
  
  /** Select process instances whose id is in the given set of ids */
  ProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds);
  
  /** Select process instances with the given business key */
  ProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey);
  
  /** Select process instance with the given business key, unique for the given process definition */
  ProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey, String processDefinitionKey);

  /**
   * Select the process instances which are defined by a process definition with
   * the given key.
   */
  ProcessInstanceQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Selects the process instances which are defined by a process definition
   * with the given id.
   */
  ProcessInstanceQuery processDefinitionId(String processDefinitionId);

  /**
   * Select the process instances which are a sub process instance of the given
   * super process instance.
   */
  ProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId);

  /**
   * Select the process instance that have as sub process instance the given
   * process instance. Note that there will always be maximum only <b>one</b>
   * such process instance that can be the result of this query.
   */
  ProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId);
  
  /** 
   * Only select process instances which have a global variable with the given value. The type
   * of variable is determined based on the value, using types configured in 
   * {@link ProcessEngineConfiguration#getVariableTypes()}. 
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ProcessInstanceQuery variableValueEquals(String name, Object value);
  
  /** 
   * Only select process instances which have a global variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ProcessInstanceQuery variableValueNotEquals(String name, Object value);
  

  /** 
   * Only select process instances which have a variable value greater than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueGreaterThan(String name, Object value);
  
  /** 
   * Only select process instances which have a global variable value greater than or equal to
   * the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which 
   * are not primitive type wrappers) are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value);
  
  /** 
   * Only select process instances which have a global variable value less than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueLessThan(String name, Object value);
  
  /** 
   * Only select process instances which have a global variable value less than or equal to the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value);
  
  /** 
   * Only select process instances which have a global variable value like the given value.
   * This be used on string variables only.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy: 
   * starts with (string%), ends with (%string) or contains (%string%).
   */
  ProcessInstanceQuery variableValueLike(String name, String value);
  
  /**
   * Only selects process instances which are suspended, either because the 
   * process instance itself is suspended or because the corresponding process 
   * definition is suspended
   */
  ProcessInstanceQuery suspended();
  
  /**
   * Only selects process instances which are active, which means that 
   * neither the process instance nor the corresponding process definition 
   * are suspended.
   */
  ProcessInstanceQuery active();
  
  //ordering /////////////////////////////////////////////////////////////////
  
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessInstanceId();
  
  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessDefinitionKey();
  
  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessDefinitionId();
  
}
