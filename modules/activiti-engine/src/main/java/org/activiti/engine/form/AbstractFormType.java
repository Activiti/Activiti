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

package org.activiti.engine.form;



/**
 * Custom form types should extend this abstract class.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class AbstractFormType implements FormType {
	
  private static final long serialVersionUID = 1L;

  public abstract Object convertFormValueToModelValue(String propertyValue);

  public abstract String convertModelValueToFormValue(Object modelValue);

  public Object getInformation(String key) {
    return null;
  }

}
