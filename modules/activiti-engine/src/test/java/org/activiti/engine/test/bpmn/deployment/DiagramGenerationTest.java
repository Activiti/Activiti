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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    BufferedImage imageInRepo = ImageIO.read(repositoryService.getResourceAsStream(
            processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName()));
    assertNotNull(imageInRepo);

    assertTrue(expectedImage.getWidth() > 0);
    assertTrue(expectedImage.getHeight() > 0);
    
    assertEquals(expectedImage.getWidth(), imageInRepo.getWidth());
    assertEquals(expectedImage.getHeight(), imageInRepo.getHeight());
    for (int i=0; i<expectedImage.getWidth(); i++) {
      for (int j=0; j<expectedImage.getHeight(); j++) {
        setAlphaToWhite(expectedImage, i, j);
        setAlphaToWhite(imageInRepo, i, j);
        assertEquals(expectedImage.getRGB(i, j), imageInRepo.getRGB(i, j));
      }
    }
    
  }
  
  protected void setAlphaToWhite(BufferedImage image, int x, int y) {
    int rgb = image.getRGB(x, y);
    int alpha = (rgb >> 24) & 0xff;
    if(alpha != 255) {
        image.setRGB(x, y,-1); //set white
    } 
  }
  
  public static void write(InputStream inputStream) {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(new File("test.jpg")));
      outputStream.write(IoUtil.readInputStream(inputStream, null));
      outputStream.flush();
    } catch(Exception e) {
      throw new ActivitiException("Couldn't write file : " + e.getMessage());
    } finally {
      IoUtil.closeSilently(outputStream);
    }
  }
  
}
