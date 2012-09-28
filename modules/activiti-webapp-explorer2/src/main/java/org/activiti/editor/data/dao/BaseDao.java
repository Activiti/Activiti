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

package org.activiti.editor.data.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.activiti.editor.data.model.BaseDatabaseObject;
import org.activiti.editor.servlet.listener.EntityManagerListener;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
public abstract class BaseDao {
  
  protected static final Logger LOGGER = Logger.getLogger(BaseDao.class.getName());
  protected ObjectMapper objectMapper = new ObjectMapper();
  
  protected EntityManager createEntityManager() {
    return EntityManagerListener.getEntityManagerFactory().createEntityManager();
  }
  
  protected <T> T getObjectById(Class<T> entityClass, long id) {
    EntityManager entityManager = createEntityManager();
    T entity = null;
    try {
      entity = entityManager.find(entityClass, id);
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error getting object " + entityClass + " by id " + id, e);
    } finally {
      entityManager.close();
    }
    return entity;
  }
  
  protected <T> T getSingleResult(String queryString, String key, Object value, Class<T> entityClass) {
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put(key, value);
    return getSingleResult(queryString, parameterMap, entityClass);
  }
  
  @SuppressWarnings("unchecked")
  protected <T> T getSingleResult(String queryString, Map<String, Object> parameterMap, Class<T> entityClass) {
    EntityManager entityManager = createEntityManager();
    T result = null;
    try {
      Query query = entityManager.createQuery(queryString);
      if (parameterMap != null) {
        for (String key : parameterMap.keySet()) {
          query.setParameter(key, parameterMap.get(key));
        }
      }
      result = (T) query.getSingleResult();
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error query for single result " + entityClass + " with query " + queryString, e);
    } finally {
      entityManager.close();
    }
    return result;
  }
  
  protected <T> List<T> getQueryResult(String queryString, String key, Object value, Class<T> entityClass) {
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put(key, value);
    return getQueryResult(queryString, parameterMap, entityClass);
  }
  
  @SuppressWarnings("unchecked")
  protected <T> List<T> getQueryResult(String queryString, Map<String, Object> parameterMap, Class<T> entityClass) {
    EntityManager entityManager = createEntityManager();
    List<T> result = null;
    try {
      Query query = entityManager.createQuery(queryString);
      if (parameterMap != null) {
        for (String key : parameterMap.keySet()) {
          query.setParameter(key, parameterMap.get(key));
        }
      }
      result = (List<T>) query.getResultList();
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error query for result " + entityClass + " with query " + queryString, e);
    } finally {
      entityManager.close();
    }
    return result;
  }
  
  protected long saveObject(BaseDatabaseObject object) {
    EntityManager entityManager = createEntityManager();
    try {
      entityManager.getTransaction().begin();
      if (object.getObjectId() > 0) {
        entityManager.merge(object);
      } else {
        entityManager.persist(object);
      }
      entityManager.flush();
      entityManager.getTransaction().commit();
      
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error saving object " + object, e);
    } finally {
      entityManager.close();
    }
    return object.getObjectId();
  }
  
  protected void deleteObject(BaseDatabaseObject object) {
    EntityManager entityManager = createEntityManager();
    try {
      entityManager.getTransaction().begin();
      BaseDatabaseObject databaseObject = entityManager.find(object.getClass(), object.getObjectId());
      entityManager.remove(databaseObject);
      entityManager.flush();
      entityManager.getTransaction().commit();
      
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error deleting object " + object, e);
    } finally {
      entityManager.close();
    }
  }
}
