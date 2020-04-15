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
import static org.assertj.core.api.Assertions.assertThat;

/**

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
    assertThat(model.getId()).isNotNull();
    assertThat(model.getCategory()).isNotNull();
    assertThat(model.getKey()).isNotNull();
    assertThat(model.getName()).isNotNull();
    assertThat(model.getVersion()).isNotNull();
    assertThat(model.getCreateTime()).isNotNull();
    assertThat(model.getLastUpdateTime()).isNotNull();
  }

  public void testQueryNoCriteria() {
    ModelQuery query = repositoryService.createModelQuery();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByName() throws Exception {
    ModelQuery query = repositoryService.createModelQuery().modelName("my model");
    Model model = query.singleResult();
    assertThat(model).isNotNull();
    assertThat(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8")).isEqualTo("bytes");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidName() {
    ModelQuery query = repositoryService.createModelQuery().modelName("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);
  }

  public void testQueryByNameLike() throws Exception {
    ModelQuery query = repositoryService.createModelQuery().modelNameLike("%model%");
    Model model = query.singleResult();
    assertThat(model).isNotNull();
    assertThat(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8")).isEqualTo("bytes");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidNameLike() {
    ModelQuery query = repositoryService.createModelQuery().modelNameLike("%invalid%");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);
  }

  public void testQueryByKey() {
    ModelQuery query = repositoryService.createModelQuery().modelName("my model").modelKey("someKey");
    Model model = query.singleResult();
    assertThat(model).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByNameAndKey() {
    ModelQuery query = repositoryService.createModelQuery().modelKey("someKey");
    Model model = query.singleResult();
    assertThat(model).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidKey() {
    ModelQuery query = repositoryService.createModelQuery().modelKey("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);
  }

  public void testQueryByCategory() {
    ModelQuery query = repositoryService.createModelQuery().modelCategory("test");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidCategory() {
    ModelQuery query = repositoryService.createModelQuery().modelCategory("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);
  }

  public void testQueryByCategoryLike() {
    ModelQuery query = repositoryService.createModelQuery().modelCategoryLike("%te%");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidCategoryLike() {
    ModelQuery query = repositoryService.createModelQuery().modelCategoryLike("%invalid%");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);
  }

  public void testQueryByCategoryNotEquals() {
    ModelQuery query = repositoryService.createModelQuery().modelCategoryNotEquals("aap");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByVersion() {
    ModelQuery query = repositoryService.createModelQuery().modelVersion(1);
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testByDeploymentId() {
    Deployment deployment = repositoryService.createDeployment().addString("test", "test").deploy();

    assertThat(repositoryService.createModelQuery().deploymentId(deployment.getId()).count()).isEqualTo(0);
    assertThat(repositoryService.createModelQuery().deployed().count()).isEqualTo(0);
    assertThat(repositoryService.createModelQuery().notDeployed().count()).isEqualTo(1);

    Model model = repositoryService.createModelQuery().singleResult();
    model.setDeploymentId(deployment.getId());
    repositoryService.saveModel(model);

    assertThat(repositoryService.createModelQuery().deploymentId(deployment.getId()).count()).isEqualTo(1);
    assertThat(repositoryService.createModelQuery().deployed().count()).isEqualTo(1);
    assertThat(repositoryService.createModelQuery().notDeployed().count()).isEqualTo(0);

    // Cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);

    // After cleanup the model should still exist
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
    assertThat(repositoryService.createModelQuery().deploymentId(deployment.getId()).count()).isEqualTo(0);
    assertThat(repositoryService.createModelQuery().notDeployed().count()).isEqualTo(1);
    assertThat(repositoryService.createModelQuery().count()).isEqualTo(1);
  }

  public void testByInvalidDeploymentId() {
    ModelQuery query = repositoryService.createModelQuery().deploymentId("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.count()).isEqualTo(0);
  }

  public void testNotDeployed() {
    ModelQuery query = repositoryService.createModelQuery().notDeployed();
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.list()).hasSize(1);
  }

  public void testOrderBy() {
    ModelQuery query = repositoryService.createModelQuery();
    assertThat(query.orderByCreateTime().asc().count()).isEqualTo(1);
    assertThat(query.orderByLastUpdateTime().asc().count()).isEqualTo(1);
    assertThat(query.orderByModelCategory().asc().count()).isEqualTo(1);
    assertThat(query.orderByModelId().desc().count()).isEqualTo(1);
    assertThat(query.orderByModelName().desc().count()).isEqualTo(1);
    assertThat(query.orderByModelVersion().desc().count()).isEqualTo(1);
    assertThat(query.orderByModelKey().desc().count()).isEqualTo(1);
  }

  public void testByLatestVersion() {
    ModelQuery query = repositoryService.createModelQuery().latestVersion().modelKey("someKey");
    Model model = query.singleResult();
    assertThat(model).isNotNull();

    // Add a new version of the model
    Model newVersion = repositoryService.newModel();
    newVersion.setName("my model");
    newVersion.setKey("someKey");
    newVersion.setCategory("test");
    newVersion.setVersion(model.getVersion() + 1);
    repositoryService.saveModel(newVersion);

    // Verify query
    model = query.singleResult();
    assertThat(model).isNotNull();
    assertThat(model.getVersion() == 2).isTrue();

    // Cleanup
    repositoryService.deleteModel(model.getId());
  }

  public void testVerifyModelProperties() {
    List<Model> models = repositoryService.createModelQuery().orderByModelName().asc().list();

    Model modelOne = models.get(0);
    assertThat(modelOne.getName()).isEqualTo("my model");
    assertThat(modelOne.getId()).isEqualTo(modelOneId);

    models = repositoryService.createModelQuery().modelNameLike("%model%").orderByModelName().asc().list();

    assertThat(models.get(0).getName()).isEqualTo("my model");
    assertThat(models).hasSize(1);

    assertThat(repositoryService.createModelQuery().orderByModelId().asc().list()).hasSize(1);
  }

  public void testNativeQuery() {
    assertThat(managementService.getTableName(Model.class)).isEqualTo("ACT_RE_MODEL");
    assertThat(managementService.getTableName(ModelEntity.class)).isEqualTo("ACT_RE_MODEL");
    String tableName = managementService.getTableName(Model.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    assertThat(repositoryService.createNativeModelQuery().sql(baseQuerySql).list()).hasSize(1);

    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql + " where NAME_ = #{name}").parameter("name", "my model").list()).hasSize(1);

    // paging
    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(0, 1)).hasSize(1);
    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(1, 5)).hasSize(0);
  }

  public void testKeyAndLatest() throws Exception {

    ModelEntity model1 = null;
    ModelEntity model2 = null;
    try {
      model1 = processEngineConfiguration.getModelEntityManager().create();
      model1.setKey("key1");
      model1.setVersion(1);
      repositoryService.saveModel(model1);

      model2 = processEngineConfiguration.getModelEntityManager().create();
      model2.setKey("key2");
      model2.setVersion(2);
      repositoryService.saveModel(model2);

      Model model = repositoryService.createModelQuery().modelKey("key1").latestVersion().singleResult();
      assertThat(model).isNotNull();
    } finally {
      try {
        if (model1 != null) {
          repositoryService.deleteModel(model1.getId());
        }
        if (model2 != null) {
          repositoryService.deleteModel(model2.getId());
        }
      } catch (Throwable ignore) {

      }
    }
  }
}
