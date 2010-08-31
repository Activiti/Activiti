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

package org.activiti.engine.repository;

import java.util.List;


/**
 * @author Tom Baeyens
 */
public interface DeploymentQuery {

  String PROPERTY_ID = "ID_";
  String PROPERTY_NAME = "NAME_";
  String PROPERTY_DEPLOY_TIME = "DEPLOY_TIME_";
  
  DeploymentQuery deploymentId(String deploymentId);
  
  DeploymentQuery nameLike(String nameLike);
  
  DeploymentQuery orderAsc(String property);
  
  DeploymentQuery orderDesc(String property);

  long count();
  
  Deployment singleResult();
  
  List<Deployment> list();
  
  List<Deployment> listPage(int firstResult, int maxResults);
}
