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
package org.activiti.app.model.runtime;

/**
 * 
 * @author Yvo Swillens
 *
 */
public class ProcessInstanceVariableRepresentation {

    private String id;
    private String type;
    private Object value;
    
    public ProcessInstanceVariableRepresentation() {
    }
    
    public ProcessInstanceVariableRepresentation(String id, String type, Object value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }
    
    public String getId() {
        return id;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
}
