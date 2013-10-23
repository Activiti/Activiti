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

package org.activiti.engine.test.api.repository;

import java.util.List;

import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.junit.Assert;


/**
 * @author Tijs Rademakers
 */
public class ModelQueryTest extends PluggableActivitiTestCase {
  
  private String modelOneId;
  
  @Override
  protected void setUp() throws Exception {
    Model model = repositoryService.newModel();
    model.setName("my model");
    model.setKey("someKey");
    model.setCategory("test");
    repositoryService.saveModel(model);
    modelOneId = model.getId();
    
    repositoryService.addModelEditorSource(modelOneId, "bytes".getBytes("utf-8"));
    
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteModel(modelOneId);
  }
  
  public void testModelProperties() {
    ModelQuery query = repositoryService.createModelQuery();
    Model model = query.singleResult();
    assertNotNull(model.getId());
    assertNotNull(model.getCategory());
    assertNotNull(model.getKey());
    assertNotNull(model.getName());
    assertNotNull(model.getVersion());
    assertNotNull(model.getCreateTime());
    assertNotNull(model.getLastUpdateTime());
  }
  
  public void testQueryNoCriteria() {
    ModelQuery query = repositoryService.createModelQuery();
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByName() throws Exception {
    ModelQuery query = repositoryService.createModelQuery().modelName("my model");
    Model model = query.singleResult();
    assertNotNull(model);
    assertEquals("bytes", new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidName() {
    ModelQuery query = repositoryService.createModelQuery().modelName("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryByNameLike() throws Exception {
    ModelQuery query = repositoryService.createModelQuery().modelNameLike("%model%");
    Model model = query.singleResult();
    assertNotNull(model);
    assertEquals("bytes", new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidNameLike() {
    ModelQuery query = repositoryService.createModelQuery().modelNameLike("%invalid%");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryByKey() {
    ModelQuery query = repositoryService.createModelQuery().modelName("my model").modelKey("someKey");
    Model model = query.singleResult();
    assertNotNull(model);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByNameAndKey() {
    ModelQuery query = repositoryService.createModelQuery().modelKey("someKey");
    Model model = query.singleResult();
    assertNotNull(model);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidKey() {
    ModelQuery query = repositoryService.createModelQuery().modelKey("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryByCategory() {
    ModelQuery query = repositoryService.createModelQuery().modelCategory("test");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidCategory() {
    ModelQuery query = repositoryService.createModelQuery().modelCategory("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryByCategoryLike() {
    ModelQuery query = repositoryService.createModelQuery().modelCategoryLike("%te%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidCategoryLike() {
    ModelQuery query = repositoryService.createModelQuery().modelCategoryLike("%invalid%");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryByCategoryNotEquals() {
    ModelQuery query = repositoryService.createModelQuery().modelCategoryNotEquals("aap");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByVersion() {
    ModelQuery query = repositoryService.createModelQuery().modelVersion(1);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testByDeploymentId() {
    Deployment deployment = repositoryService.createDeployment().addString("test", "test").deploy();
    
    assertEquals(0, repositoryService.createModelQuery().deploymentId(deployment.getId()).count());
    assertEquals(0, repositoryService.createModelQuery().deployed().count());
    assertEquals(1, repositoryService.createModelQuery().notDeployed().count());
    
    Model model = repositoryService.createModelQuery().singleResult();
    model.setDeploymentId(deployment.getId());
    repositoryService.saveModel(model);
    
    assertEquals(1, repositoryService.createModelQuery().deploymentId(deployment.getId()).count());
    assertEquals(1, repositoryService.createModelQuery().deployed().count());
    assertEquals(0, repositoryService.createModelQuery().notDeployed().count());
    
    // Cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);
    
    // After cleanup the model should still exist
    assertEquals(0, repositoryService.createDeploymentQuery().count());
    assertEquals(0, repositoryService.createModelQuery().deploymentId(deployment.getId()).count());
    assertEquals(1, repositoryService.createModelQuery().notDeployed().count());
    assertEquals(1, repositoryService.createModelQuery().count());
  }
  
  public void testByInvalidDeploymentId() {
    ModelQuery query = repositoryService.createModelQuery().deploymentId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.count());
  }
  
  public void testNotDeployed() {
    ModelQuery query = repositoryService.createModelQuery().notDeployed();
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }
  
  public void testOrderBy() {
    ModelQuery query = repositoryService.createModelQuery();
    assertEquals(1, query.orderByCreateTime().asc().count());
    assertEquals(1, query.orderByLastUpdateTime().asc().count());
    assertEquals(1, query.orderByModelCategory().asc().count());
    assertEquals(1, query.orderByModelId().desc().count());
    assertEquals(1, query.orderByModelName().desc().count());
    assertEquals(1, query.orderByModelVersion().desc().count());
    assertEquals(1, query.orderByModelKey().desc().count());
  }
  
  public void testByLatestVersion() {
    ModelQuery query = repositoryService.createModelQuery().latestVersion().modelKey("someKey");
    Model model = query.singleResult();
    assertNotNull(model);
    
    // Add a new version of the model
    Model newVersion = repositoryService.newModel();
    newVersion.setName("my model");
    newVersion.setKey("someKey");
    newVersion.setCategory("test");
    newVersion.setVersion(model.getVersion() + 1);
    repositoryService.saveModel(newVersion);
    
    // Verify query
    model = query.singleResult();
    assertNotNull(model);
    assertTrue(model.getVersion() == 2);
    
    // Cleanup
    repositoryService.deleteModel(model.getId());
  }

  public void testVerifyModelProperties() {
    List<Model> models = repositoryService.createModelQuery()
      .orderByModelName()
      .asc()
      .list();
    
    Model modelOne = models.get(0);
    assertEquals("my model", modelOne.getName());
    assertEquals(modelOneId, modelOne.getId());

    models = repositoryService.createModelQuery()
      .modelNameLike("%model%")
      .orderByModelName()
      .asc()
      .list();
    
    assertEquals("my model", models.get(0).getName());
    assertEquals(1, models.size());

    assertEquals(1, repositoryService.createModelQuery()
      .orderByModelId()
      .asc()
      .list()
      .size());
  }

  public void testNativeQuery() {
    assertEquals("ACT_RE_MODEL", managementService.getTableName(Model.class));
    assertEquals("ACT_RE_MODEL", managementService.getTableName(ModelEntity.class));
    String tableName = managementService.getTableName(Model.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    assertEquals(1, repositoryService.createNativeModelQuery().sql(baseQuerySql).list().size());

    assertEquals(1, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql + " where NAME_ = #{name}")
        .parameter("name", "my model").list().size());

    // paging
    assertEquals(1, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(0, 1).size());
    assertEquals(0, repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(1, 5).size());
  }

  
  public void testKeyAndLatest() throws Exception {
  	
  	ModelEntity model1 = null;
  	ModelEntity model2 = null;
  	try {
			model1 = new ModelEntity();
			model1.setKey("key1");
			model1.setVersion(1);
			repositoryService.saveModel(model1);
			
			model2 = new ModelEntity();
			model2.setKey("key2");
			model2.setVersion(2);
			repositoryService.saveModel(model2);
			
			Model model = repositoryService.createModelQuery().modelKey("key1").latestVersion().singleResult();
			Assert.assertNotNull(model);
  	} finally {
  		try {
  			if(model1 != null) {
  				repositoryService.deleteModel(model1.getId());
  			}
  			if(model2 != null) {
  				repositoryService.deleteModel(model2.getId());
  			}
  		} catch(Throwable ignore) {
  			
  		}
  	}
  }
}
