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

package org.activiti.editor.test.listener;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.data.dao.ModelDao;
import org.activiti.editor.data.model.ModelData;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class TestDataListener implements ServletContextListener, ModelDataJsonConstants {

  protected static final Logger LOGGER = Logger.getLogger(TestDataListener.class.getName());
  
  private ObjectMapper objectMapper = new ObjectMapper();
  private ModelDao modelDao = new ModelDao();
  
  public void contextDestroyed(ServletContextEvent context) {
    // nothing to do
  }

  public void contextInitialized(ServletContextEvent context) {
    if(LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Adding test data");
    }
    
    createModelData("test 1 model", "This is a test model", "test.model.json");
  }
  
  private void createModelData(String name, String description, String jsonFile) {
    ModelData model = new ModelData();
    
    ObjectNode modelObjectNode = objectMapper.createObjectNode();
    modelObjectNode.put(MODEL_NAME, name);
    modelObjectNode.put(MODEL_REVISION, 1);
    modelObjectNode.put(MODEL_DESCRIPTION, description);
    model.setModelJson(modelObjectNode.toString());
    
    try {
      InputStream svgStream = this.getClass().getClassLoader().getResourceAsStream("test.svg");
      model.setModelSvg(IOUtils.toString(svgStream));
    } catch(Exception e) {
      LOGGER.log(Level.WARNING, "Failed to read SVG", e);
    }
    
    try {
      InputStream editorJsonStream = this.getClass().getClassLoader().getResourceAsStream(jsonFile);
      model.setModelEditorJson(IOUtils.toString(editorJsonStream));
    } catch(Exception e) {
      LOGGER.log(Level.WARNING, "Failed to read editor JSON", e);
    }
    
    modelDao.saveModel(model);
  }
}
