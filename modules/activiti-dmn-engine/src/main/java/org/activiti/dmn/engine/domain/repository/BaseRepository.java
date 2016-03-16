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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.activiti.dmn.engine.domain.entity.BaseDmnEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRepository<T extends BaseDmnEntity> {

    private static Logger logger = LoggerFactory.getLogger(BaseRepository.class);
    
    protected EntityManagerFactory entityManagerFactory;
    protected Class<T> typeParameterClass;
    
    public BaseRepository(EntityManagerFactory entityManagerFactory, Class<T> typeParameterClass) {
        this.entityManagerFactory = entityManagerFactory;
        this.typeParameterClass = typeParameterClass;
    }

    public T getObjectById(long id) {
        EntityManager localEntityManager = createEntityManager(false);
        T entity = null;
        try {
            entity = localEntityManager.find(typeParameterClass, id);
        } catch (final Exception e) {
            logger.error("Error getting object " + typeParameterClass + " by id " + id);
        } finally {
            closeEntityManager(localEntityManager, false);
        }
        return entity;
    }

    public T getSingleResult(String queryString, String key, Object value) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put(key, value);
        return getSingleResult(queryString, parameterMap);
    }

    @SuppressWarnings("unchecked")
    public T getSingleResult(String queryString, Map<String, Object> parameterMap) {
        EntityManager localEntityManager = createEntityManager(false);
        T result = null;
        try {
            final Query query = localEntityManager.createQuery(queryString);
            if (parameterMap != null) {
                for (String key : parameterMap.keySet()) {
                    query.setParameter(key, parameterMap.get(key));
                }
            }
            result = (T) query.getSingleResult();
            
        } catch (NoResultException nre) {
        } catch (Exception e) {
            logger.warn("Error querying for single result " + typeParameterClass + " with query " + queryString, e);
        } finally {
            closeEntityManager(localEntityManager, false);
        }
        return result;
    }

    public List<T> getQueryResult(String queryString, String key, Object value, Integer firstResult, Integer maxResults) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put(key, value);
        return getQueryResult(queryString, parameterMap);
    }

    public Long getQueryCount(String queryString, String key, Object value) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put(key, value);
        return getQueryCount(queryString, parameterMap);
    }

    public List<T> getQueryResult(String queryString, String key, Object value) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put(key, value);
        return getQueryResult(queryString, parameterMap);
    }

    public List<T> getQueryResult(String queryString, Map<String, Object> parameterMap) {
        return getQueryResult(queryString, parameterMap, null, null);
    }

    @SuppressWarnings("unchecked")
    protected List<T> getQueryResult(String queryString, Map<String, Object> parameterMap, Integer firstResult, Integer maxResults) {
        EntityManager localEntityManager = createEntityManager(false);
        List<T> result = null;
        try {
            Query query = localEntityManager.createQuery(queryString);
            if (parameterMap != null) {
                for (final String key : parameterMap.keySet()) {
                    query.setParameter(key, parameterMap.get(key));
                }
            }
            if (firstResult != null && firstResult >= 0) {
                query.setFirstResult(firstResult);
            }
            if (maxResults != null && maxResults >= 0) {
                query.setMaxResults(maxResults);
            }
            result = query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error query for result " + typeParameterClass + " with query " + queryString, e);
        } finally {
            closeEntityManager(localEntityManager, false);
        }
        return result;
    }

    protected Long getQueryCount(String queryString, Map<String, Object> parameterMap) {
        EntityManager localEntityManager = createEntityManager(false);
        Long result = null;
        try {
            Query query = localEntityManager.createQuery(queryString);
            if (parameterMap != null) {
                for (String key : parameterMap.keySet()) {
                    query.setParameter(key, parameterMap.get(key));
                }
            }
            result = (Long) query.getSingleResult();
            
        } catch (Exception e) {
            logger.error("Error get count for result " + typeParameterClass + " with query " + queryString, e);
        } finally {
            closeEntityManager(localEntityManager, false);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<Object[]> getRawQueryResult(String queryString, Map<String, Object> parameterMap) {
        EntityManager localEntityManager = createEntityManager(false);
        try {
            Query query = localEntityManager.createQuery(queryString);
            if (parameterMap != null) {
                for (final String key : parameterMap.keySet()) {
                    query.setParameter(key, parameterMap.get(key));
                }
            }
            return query.getResultList();
            
        } catch (final Exception e) {
            throw new RuntimeException("Erro while getting raw results from query", e);
        } finally {
            closeEntityManager(localEntityManager, false);
        }
    }

    public long saveObject(BaseDmnEntity object) {
        EntityManager localEntityManager = createEntityManager(true);
        try {
            if (object.getId() != null && object.getId() > 0) {
                localEntityManager.merge(object);
            } else {
                localEntityManager.persist(object);
            }

        } catch (Exception e) {
            logger.error("Error saving object " + object, e);
        } finally {
            closeEntityManager(localEntityManager, true);
        }
        return object.getId();
    }

    public void removeObject(BaseDmnEntity object) {
        final EntityManager localEntityManager = createEntityManager(true);
        try {
            if (object.getId() != null && object.getId() > 0) {
                BaseDmnEntity attachedEntity = localEntityManager.merge(object);
                localEntityManager.remove(attachedEntity);
            }
        } catch (Exception e) {
            logger.error("Error while removing object " + object, e);
        } finally {
            closeEntityManager(localEntityManager, true);
        }
    }
    
    protected EntityManager createEntityManager(boolean transaction) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        if (transaction) {
            entityManager.getTransaction().begin();
        }
        return entityManager;
    }

    protected void closeEntityManager(EntityManager entityManager, boolean transaction) {
        try {
            if (transaction) {
                entityManager.flush();
                entityManager.getTransaction().commit();
            }
        } catch (final Exception e) {
            logger.error("Error while committing transaction", e);
        } finally {
            try {
                entityManager.close();
            } catch (final Exception e) {
                logger.error("Error while closing entity manager", e);
            }
        }
    }

    public List<T> findAll() {

        EntityManager localEntityManager = createEntityManager(false);
        List<T> entities = null;
        try {
            entities = localEntityManager.createQuery("FROM "+typeParameterClass.getName()).getResultList();
        } catch (final Exception e) {
            logger.error("Error getting all " + typeParameterClass + " entities");
        } finally {
            closeEntityManager(localEntityManager, false);
        }
        return entities;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
}
