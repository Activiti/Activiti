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

package org.activiti.standalone.jpa;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;


/**
 * @author Frederik Heremans
 */
@Entity
public class CompoundIdJPAEntity implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  @EmbeddedId
  private EmbeddableCompoundId id;

  public EmbeddableCompoundId getId() {
    return id;
  }

  public void setId(EmbeddableCompoundId id) {
    this.id = id;
  }
  
  @Override
  public boolean equals(Object obj) {
    return id.equals(((CompoundIdJPAEntity)obj).getId());
  }
}
