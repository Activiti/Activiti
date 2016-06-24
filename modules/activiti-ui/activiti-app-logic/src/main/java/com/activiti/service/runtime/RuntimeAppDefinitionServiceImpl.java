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
package com.activiti.service.runtime;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.activiti.engine.runtime.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RuntimeApp;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;
import com.activiti.repository.runtime.AppRelationRepository;
import com.activiti.repository.runtime.RuntimeAppDefinitionRepository;
import com.activiti.repository.runtime.RuntimeAppDeploymentRepository;
import com.activiti.repository.runtime.RuntimeAppRepository;
import com.activiti.service.api.RuntimeAppDefinitionService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Service for accessing {@link RuntimeAppDefinition}s and {@link RuntimeApp}s.
 *  
 * @author Frederik Heremans
 */
@Service
public class RuntimeAppDefinitionServiceImpl implements RuntimeAppDefinitionService, RuntimeAppDefinitionInternalService {

    @Autowired
    private RuntimeAppRepository appRepository;
    
    @Autowired
    private RuntimeAppDefinitionRepository appDefinitionRepository;
    
    @Autowired
    private RuntimeAppDeploymentRepository appDeploymentRepository;
    
    @Autowired
    private AppRelationRepository appRelationRepository;
    
    @Autowired
    private Clock clock;
    
    private LoadingCache<Long, RuntimeAppDefinition> definitionCache;
    
    private Long maxCacheSize;
    
    /**
     * @return a {@link RuntimeAppDefinition} for the given id. Returns null, if no app definition
     * has been found.
     */
    @Override
    public RuntimeAppDefinition getRuntimeAppDefinition(Long id) {
        try {
            return definitionCache.get(id);
        } catch (ExecutionException e) {
            // Throw original exception if possible or wrap as a RtE
            if(e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new RuntimeException("Error while getting runtime app definition from cache", e.getCause());
            }
        }
    }
    
    /**
     * @return a {@link RuntimeAppDefinition} which is a deployed version of the model with the given id or {@literal null}
     * if the model currently not deployed.
     */
    @Override
    public RuntimeAppDefinition getRuntimeAppDefinitionForModel(Long modelId) {
        return appDefinitionRepository.findByModelId(modelId);
    }
    
    @Override
    public List<RuntimeAppDeployment> getRuntimeAppDeploymentsForApp(RuntimeAppDefinition appDefinition) {
        return appDeploymentRepository.findByAppDefinition(appDefinition);
    }
    
    @Override
    public List<RuntimeAppDeployment> getRuntimeAppDeploymentsForAppId(Long appId) {
        return appDeploymentRepository.findByAppDefinitionId(appId);
    }
    
    /**
     * @return a {@link RuntimeAppDefinition}, representing the deployed version of an app definition model with the given id. In case a {@link RuntimeAppDefinition} already exists
     * for the given model, only an additional {@link RuntimeApp} is created, referencing the existing definition and given user.
     */
    @Override
    public RuntimeAppDefinition createRuntimeAppDefinition(User user, String name, String description, Long modelId, String definition) {
        // Create new definition
        RuntimeAppDefinition appDefinition = new RuntimeAppDefinition();
        appDefinition.setCreated(clock.getCurrentTime());
        appDefinition.setCreatedBy(user);
        appDefinition.setDescription(description);
        appDefinition.setModelId(modelId);
        appDefinition.setName(name);
        appDefinition.setDefinition(definition);

        appDefinition = appDefinitionRepository.save(appDefinition);
        
        return appDefinition;
    }
    
    /**
     * @return a {@link RuntimeAppDeployment}, representing the deployed version of an app definition model with the given id.
     */
    @Override
    public RuntimeAppDeployment createRuntimeAppDeployment(User user, RuntimeAppDefinition appDefinition, Long modelId, String definition) {
        // Create new definition
        RuntimeAppDeployment appDeployment = new RuntimeAppDeployment();
        appDeployment.setCreated(clock.getCurrentTime());
        appDeployment.setCreatedBy(user);
        appDeployment.setAppDefinition(appDefinition);
        appDeployment.setModelId(modelId);
        appDeployment.setDefinition(definition);
        
        appDeployment = appDeploymentRepository.save(appDeployment);
        
        return appDeployment;
    }
    
    @Override
    public void updateRuntimeAppDefinition(RuntimeAppDefinition appDefinition) {
        appDefinitionRepository.save(appDefinition);
    }
    
    @Override
    public void updateRuntimeAppDeployment(RuntimeAppDeployment appDeployment) {
        appDeploymentRepository.save(appDeployment);
    }
    
    /**
     * @return all {@link RuntimeAppDefinition} a user has defined. The results are based on the presence
     * of {@link RuntimeApp} entities, referencing the given user.
     */
    @Override
    public List<RuntimeAppDefinition> getDefinitionsForUser(User user) {
        return appDefinitionRepository.findByUser(user);
    }
    
    /**
     * @return if there's an app definition created by a specific user for a specific model.
     */
    @Override
    public RuntimeAppDefinition getDefinitionForModelAndUser(Long modelId, User user) {
        return appDefinitionRepository.findByModelAndUser(modelId, user);
    }
    
    @Override
    public Long getDefinitionIdForModelAndUser(Long modelId, User user) {
        Long appDefinitionId = null;
        RuntimeAppDefinition appDefinition = appDefinitionRepository.findByModelAndUser(modelId, user);
        if (appDefinition != null) {
            appDefinitionId = appDefinition.getId();
        }
        return appDefinitionId;
    }

    /**
     * @return a new {@link RuntimeApp}, connecting a user with a {@link RuntimeAppDefinition}. No checks
     * are performed to see if a user has rights to ass this definition.
     */
    @Override
    public RuntimeApp addAppDefinitionForUser(User user, RuntimeAppDefinition rad) {
        RuntimeApp app = new RuntimeApp();
        app.setUser(user);
        app.setAppDefinition(rad);

        return appRepository.save(app);
    }
    
    /**
     * @return true, after deleting an existing {@link RuntimeApp} for the given user and the given definition. Returns
     * false if no valid {@link RuntimeApp} existed.
     */
    @Override
    public boolean deleteAppDefinitionForUser(User user, RuntimeAppDefinition appDefinition) {
        boolean deleted = false;
        
        RuntimeApp appDef = appRepository.findByUserAndAppDefinition(user, appDefinition);
        if(appDef != null) {
            appRepository.delete(appDef);
            deleted = true;
        }
        return deleted;
    }
    
    @Override
    @Transactional
    public boolean deleteAppDefinition(RuntimeAppDefinition appDefinition) {
        boolean deleted = false;
        if (appDefinition != null) {
        	
        	// Delete runtime apps
            appRepository.deleteInBatchByAppId(appDefinition.getId());
            
            // Delete the relations for the app deployment
            List<RuntimeAppDeployment> runtimeAppDeployments = appDeploymentRepository.findByAppDefinition(appDefinition);
            for (RuntimeAppDeployment runtimeAppDeployment : runtimeAppDeployments) {
            	appRelationRepository.deleteAppRelationsForRuntimeAppDeployment(runtimeAppDeployment.getId());
            }
            
            // Delete app deployments
            for (RuntimeAppDeployment runtimeAppDeployment : runtimeAppDeployments) {
            	appDeploymentRepository.delete(runtimeAppDeployment);
            }
            
            // Actually delete the appdefinition
            appDefinitionRepository.delete(appDefinition);
            deleted = true;
        }
        return deleted;
    }
    

    @PostConstruct
    protected void initialize() {
        definitionCache = CacheBuilder.newBuilder()
            .maximumSize(maxCacheSize != null ? maxCacheSize : 2048)
            .build(new CacheLoader<Long, RuntimeAppDefinition>() {
                @Override
                public RuntimeAppDefinition load(Long key) throws Exception {
                    return appDefinitionRepository.findOne(key);
                }
            });
    }
}
