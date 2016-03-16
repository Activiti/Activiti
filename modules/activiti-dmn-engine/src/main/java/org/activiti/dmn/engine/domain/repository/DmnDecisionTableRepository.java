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
package org.activiti.dmn.engine.domain.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.activiti.dmn.engine.domain.entity.DmnDecisionTable;
import org.apache.commons.lang3.StringUtils;

public class DmnDecisionTableRepository extends BaseRepository<DmnDecisionTable> {

    private final static Integer DEFAULT_SIZE = 100;
    
    public DmnDecisionTableRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, DmnDecisionTable.class);
    }
    
    public DmnDecisionTable findLatestDecisionTableByKey(String key) {
        String query = "SELECT d FROM DmnDecisionTable d WHERE d.key = :key AND d.version = (select max(def.version) from DmnDecisionTable def where def.key = :key)";
        return getSingleResult(query, "key", key);
    }
    
    public DmnDecisionTable findLatestDecisionTableByKeyAndTenantId(String key, String tenantId) {
        String query = "SELECT d FROM DmnDecisionTable d WHERE d.key = :key AND d.tenantId = :tenantId AND d.version = (select max(def.version) from DmnDecisionTable def where def.key = :key AND def.tenantId = :tenantId)";
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("key", key);
        parameterMap.put("tenantId", tenantId);
        return getSingleResult(query, parameterMap);
    }
    
    public List<DmnDecisionTable> findDecisionTablesByDeploymentId(Long deploymentId) {
        String query = "SELECT d FROM DmnDecisionTable d WHERE d.deploymentId = :deploymentId";
        return getQueryResult(query, "deploymentId", deploymentId);
    }

    public List<DmnDecisionTable> findDecisionTablesByTenantId(String tenantId) {
        String query = "SELECT d FROM DmnDecisionTable d WHERE d.tenantId = :tenantId";
        return getQueryResult(query, "tenantId", tenantId);
    }
    
    public DmnDecisionTable findDecisionTableByDeploymentAndKey(Long deploymentId, String key) {
        String query = "SELECT d FROM DmnDecisionTable d WHERE d.deploymentId = :deploymentId AND d.key = :key";
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("deploymentId", deploymentId);
        parameterMap.put("key", key);
        return getSingleResult(query, parameterMap);
    }
    
    public DmnDecisionTable findDecisionTableByDeploymentAndKeyAndTenantId(Long deploymentId, String key, String tenantId) {
        String query = "SELECT d FROM DmnDecisionTable d WHERE d.deploymentId = :deploymentId AND d.key = :key AND d.tenantId = :tenantId";
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("deploymentId", deploymentId);
        parameterMap.put("key", key);
        parameterMap.put("tenantId", tenantId);
        return getSingleResult(query, parameterMap);
    }
    
    public DmnDecisionTable findDecisionTableByKeyAndVersion(String key, int version) {
        String query = "SELECT d FROM DmnDecisionTable d WHERE d.key = :key AND d.version = :version";
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("key", key);
        parameterMap.put("version", version);
        return getSingleResult(query, parameterMap);
    }
    
    public List<DmnDecisionTable> findDecisionTables(String nameLike, String keyLike, String tenantIdLike, Long deploymentId, String sortBy, String order, Integer start, Integer size) {
        StringBuilder query =  new StringBuilder(" SELECT d FROM DmnDecisionTable d ");
        Map<String, Object> parameterMap = handleParams(query, nameLike, keyLike, tenantIdLike, deploymentId);
        
        if (StringUtils.isEmpty(sortBy)) {
            sortBy = "id";
        }
        
        if (StringUtils.isEmpty(order) || !"desc".equalsIgnoreCase(order)) {
            order = "asc";
        }
        
        query.append(" order by ")
        .append(sortBy).append(" ").append(order);
        
        if (start == null) {
            start = 0;
        }
        
        if (size == null) {
            size = DEFAULT_SIZE;
        }
        
        return getQueryResult(query.toString(), parameterMap, start ,size);
    }
    
    public Long countDecisionTables(String nameLike, String keyLike, String tenantIdLike, Long deploymentId) {
        StringBuilder query =  new StringBuilder(" SELECT count(d) FROM DmnDecisionTable d ");
        Map<String, Object> parameterMap = handleParams(query, nameLike, keyLike, tenantIdLike, deploymentId);
        return getQueryCount(query.toString(), parameterMap);
    }
    
    protected Map<String, Object> handleParams(StringBuilder query, String nameLike, String keyLike, String tenantIdLike, Long deploymentId) {
        List<String> whereParams = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(nameLike)) {
            whereParams.add("UPPER(d.name) LIKE UPPER(:name)");
            parameterMap.put("name", nameLike);
        }
        
        if (StringUtils.isNotEmpty(keyLike)) {
            whereParams.add("UPPER(d.key) LIKE UPPER(:key)");
            parameterMap.put("key", keyLike);
        }
        
        if (StringUtils.isNotEmpty(tenantIdLike)) {
            whereParams.add("UPPER(d.tenantId) LIKE UPPER(:tenantId)");
            parameterMap.put("tenantId", tenantIdLike);
        }
        
        if (deploymentId != null) {
            whereParams.add("deploymentId = :deploymentId");
            parameterMap.put("deploymentId", deploymentId);
        }
        
        if (!whereParams.isEmpty()) {
            query.append(" WHERE ");
            query.append(whereParams.get(0));
            for (int i=1; i< whereParams.size(); i++) {
                query.append(" AND ");
                query.append(whereParams.get(i));
            }
        }
        
        return parameterMap;
    }
    
}
