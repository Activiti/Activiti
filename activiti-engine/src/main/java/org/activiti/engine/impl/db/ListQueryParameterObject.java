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

package org.activiti.engine.impl.db;

/**

 */
public class ListQueryParameterObject {

  protected int maxResults = Integer.MAX_VALUE;
  protected int firstResult;
  protected Object parameter;
  protected String databaseType;

  public ListQueryParameterObject() {
  }

  public ListQueryParameterObject(Object parameter, int firstResult, int maxResults) {
    this.parameter = parameter;
    this.firstResult = firstResult;
    this.maxResults = maxResults;
  }

  public int getFirstResult() {
    return firstResult;
  }

  public int getFirstRow() {
    return firstResult + 1;
  }

  public int getLastRow() {
    if (maxResults == Integer.MAX_VALUE) {
      return maxResults;
    }
    return firstResult + maxResults + 1;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public Object getParameter() {
    return parameter;
  }

  public void setFirstResult(int firstResult) {
    this.firstResult = firstResult;
  }

  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  public void setParameter(Object parameter) {
    this.parameter = parameter;
  }

  public String getOrderBy() {
    // the default order column
    return "RES.ID_ asc";
  }
  
  public String getOrderByColumns() {
      return getOrderBy();
  }

  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
  }

  public String getDatabaseType() {
    return databaseType;
  }

}
