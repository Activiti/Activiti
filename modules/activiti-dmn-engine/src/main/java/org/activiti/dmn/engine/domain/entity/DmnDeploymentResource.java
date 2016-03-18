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
package org.activiti.dmn.engine.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * @author Tijs Rademakers
 */
@Entity
@Table(name="DMN_DEPLOYMENT_RESOURCE")
public class DmnDeploymentResource implements BaseDmnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "dmnDeploymentResourceIdGenerator")
    @TableGenerator(name = "dmnDeploymentResourceIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    protected Long id;
    
    @Column(name="name")
    protected String name;
    
    @Column(name="resource_bytes")
    protected byte[] resourceBytes;
    
    @Column(name="deployment_id")
    protected Long deploymentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getResourceBytes() {
        return resourceBytes;
    }

    public void setResourceBytes(byte[] resourceBytes) {
        this.resourceBytes = resourceBytes;
    }

    public Long getDeploymentId() {
        return deploymentId;
    }

    public void setDeployment(Long deploymentId) {
        this.deploymentId = deploymentId;
    }
}
