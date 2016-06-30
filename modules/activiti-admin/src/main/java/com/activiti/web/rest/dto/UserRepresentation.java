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
package com.activiti.web.rest.dto;

import com.activiti.domain.Authority;
import com.activiti.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class UserRepresentation {

    protected String login;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected Boolean adminUser;
    protected Boolean clusterUser;

    public UserRepresentation() {
    }

    public UserRepresentation(User user) {
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();

        if (user.getAuthorities() != null) {
            for (Authority authority : user.getAuthorities()) {
                if (Authority.ROLE_ADMIN.equals(authority.getName())) {
                    this.adminUser = true;
                } else if (Authority.ROLE_CLUSTER_MANAGER.equals(authority.getName())) {
                    this.clusterUser = true;
                }
            }
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
    
    @JsonInclude(Include.NON_NULL)
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
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

    @JsonInclude(Include.NON_NULL)
    public Boolean getAdminUser() {
        return adminUser;
    }

    public void setIsAdmin(Boolean adminUser) {
        this.adminUser = adminUser;
    }
    
    @JsonInclude(Include.NON_NULL)
    public Boolean getClusterUser() {
        return clusterUser;
    }
    
    public void setClusterUser(Boolean clusterUser) {
        this.clusterUser = clusterUser;
    }
    
}
