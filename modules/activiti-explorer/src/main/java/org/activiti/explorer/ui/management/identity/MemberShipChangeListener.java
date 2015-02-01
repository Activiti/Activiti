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
package org.activiti.explorer.ui.management.identity;


/**
 * Interface for {@link UserDetailPanel} and {@link GroupDetailPanel},
 * since they both contain a table showing the memberships
 * and they both must react to changes of the currently displayed memberships.
 * 
 * By using this interface, ClickListeners and others can be easily reused.
 * 
 * @author Joram Barrez
 */
public interface MemberShipChangeListener {
  
  void notifyMembershipChanged();

}
