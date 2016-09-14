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
package org.activiti.app.model.editor.form;

/**
 * @author jbarrez
 */
public interface ConditionOperators {

	/* Element operator */
	
	String VALUE_EQUALS = "==";
	String VALUE_NOT_EQUALS = "!=";
	String VALUE_LOWER = "<";
	String VALUE_LOWER_OR_EQUALS = "<=";
	String VALUE_GREATER = ">";
	String VALUE_GREATER_THEN = ">=";
			
	
	/* Logical operators */
	
	String AND = "and";
	String AND_NOT = "and-not";
	String OR = "or";
	String OR_NOT = "or-not";
	
}
