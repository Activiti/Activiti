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
package org.activiti.ldap;

import javax.naming.directory.InitialDirContext;


/**
 * Used to wrap ldap logic so it can be used by the {@link LDAPTemplate},
 * so that no boilerplate code needs to be repeated every time.
 * 
 * @author Joram Barrez
 */
public interface LDAPCallBack<T> {
  
  T executeInContext(InitialDirContext initialDirContext);

}
