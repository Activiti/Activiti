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

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;


/**
 * @author Tijs Rademakers
 */
public class ModelQueryTest extends PluggableActivitiTestCase {
  
  private String modelOneId;
  
  @Override
  protected void setUp() throws Exception {
    Model model = repositoryService.newModel();
    model.setName("my model");
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

}
