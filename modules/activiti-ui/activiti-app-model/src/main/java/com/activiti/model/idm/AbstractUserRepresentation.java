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
package com.activiti.model.idm;

import com.activiti.domain.idm.User;
import com.activiti.model.common.AbstractRepresentation;

/**
 * @author Bassam Al-Sarori
 */
public class AbstractUserRepresentation extends AbstractRepresentation {

    protected Long id;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String externalId;
    protected Long pictureId;
    
    public AbstractUserRepresentation () {
    }

    public AbstractUserRepresentation (User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.pictureId = user.getPictureImageId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPictureId(Long pictureId) {
        this.pictureId = pictureId;
    }
    
    public Long getPictureId() {
        return pictureId;
    }
}
