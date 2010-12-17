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

package org.activiti.engine.test.bpmn.deployment;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class DiagramGenerationTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testGeneratedDiagramMatchesExpected() throws IOException {
    String imageLocation = "org/activiti/engine/test/bpmn/deployment/DiagramGenerationTest.testGeneratedDiagramMatchesExpected.png";
    BufferedImage expectedImage = ImageIO.read(ReflectUtil.getResourceAsStream(imageLocation));

    Raster expectedRaster = expectedImage.getData();
    DataBuffer expectedDataBuffer = expectedRaster.getDataBuffer();
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    BufferedImage imageInRepo = ImageIO.read(repositoryService.getResourceAsStream(
            processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName()));
    assertNotNull(imageInRepo);
    
    write(repositoryService.getResourceAsStream(
            processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName()));
    
    Raster rasterFromRepo = imageInRepo.getData();
    DataBuffer dataBufferFromRepo = rasterFromRepo.getDataBuffer();
    assertEquals(dataBufferFromRepo.getSize(), expectedDataBuffer.getSize());
    
    for (int i=0; i<expectedDataBuffer.getSize(); i++) {
      assertEquals(expectedDataBuffer.getElem(i), dataBufferFromRepo.getElem(i));
    }
    
  }
  
  public static void write(InputStream inputStream) {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(new File("test.png")));
      outputStream.write(IoUtil.readInputStream(inputStream, null));
      outputStream.flush();
    } catch(Exception e) {
      throw new ActivitiException("Couldn't write file : " + e.getMessage());
    } finally {
      IoUtil.closeSilently(outputStream);
    }
  }

}
