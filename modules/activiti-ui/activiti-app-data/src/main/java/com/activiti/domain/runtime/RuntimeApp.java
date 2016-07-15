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
package com.activiti.domain.runtime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.activiti.engine.identity.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;

/**
 * An entity connecting a {@link User} with a {@link RuntimeAppDefinition}. Indicates
 * a user sees the deployed app on the landing page.
 * 
 * @author Frederik Heremans
 */
@Entity
@Table(name="RUNTIME_APP")
public class RuntimeApp {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "runtimeAppIdGenerator")
    @TableGenerator(name = "runtimeAppIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    private Long id;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="app_definition")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    protected RuntimeAppDefinition appDefinition;
    
    @Column(name="app_user")
    protected String user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RuntimeAppDefinition getAppDefinition() {
        return appDefinition;
    }

    public void setAppDefinition(RuntimeAppDefinition appDefinition) {
        this.appDefinition = appDefinition;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
}
