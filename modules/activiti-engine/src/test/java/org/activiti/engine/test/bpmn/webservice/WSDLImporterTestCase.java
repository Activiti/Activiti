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
package org.activiti.engine.test.bpmn.webservice;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.impl.bpmn.SimpleStructure;
import org.activiti.engine.impl.bpmn.Structure;
import org.activiti.engine.impl.webservice.WSDLImporter;
import org.junit.Test;

/**
 * @author Esteban Robles Luna
 */
public class WSDLImporterTestCase {

  @Test
  public void testImport() throws Exception {
    WSDLImporter importer = new WSDLImporter();
    URL url = Thread.currentThread().getContextClassLoader().getResource("org/activiti/engine/test/bpmn/servicetask/counter.wsdl");
    importer.importFrom(url, null);
    
    List<Structure> structures = new ArrayList<Structure>(importer.getStructures());
    Collections.sort(structures, new Comparator<Structure>() {
      @Override
      public int compare(Structure o1, Structure o2) {
        return o1.getId().compareTo(o2.getId());
      }
    });

    Assert.assertEquals(10, structures.size());
    
    this.assertStructure(structures.get(0), "getCount", new String[] {}, new Class<?>[] {});
    this.assertStructure(structures.get(1), "getCountResponse", new String[] {"count"}, new Class<?>[] {Integer.class});
    this.assertStructure(structures.get(2), "inc", new String[] {}, new Class<?>[] {});
    this.assertStructure(structures.get(3), "incResponse", new String[] {}, new Class<?>[] {});
    this.assertStructure(structures.get(4), "prettyPrintCount", new String[] {"prefix", "suffix"}, new Class<?>[] {String.class, String.class});
    this.assertStructure(structures.get(5), "prettyPrintCountResponse", new String[] {"prettyPrint"}, new Class<?>[] {String.class});
    this.assertStructure(structures.get(6), "reset", new String[] {}, new Class<?>[] {});
    this.assertStructure(structures.get(7), "resetResponse", new String[] {}, new Class<?>[] {});
    this.assertStructure(structures.get(8), "setTo", new String[] {"value"}, new Class<?>[] {Integer.class});
    this.assertStructure(structures.get(9), "setToResponse", new String[] {}, new Class<?>[] {});
  }

  private void assertStructure(Structure structure, String structureId, String[] parameters, Class<?>[] classes) {
    SimpleStructure simpleStructure = (SimpleStructure) structure;
    
    Assert.assertEquals(structureId, simpleStructure.getId());
    
    for (int i = 0; i < structure.getFieldSize(); i++) {
      Assert.assertEquals(parameters[i], structure.getFieldNameAt(i));
      Assert.assertEquals(classes[i], structure.getFieldTypeAt(i));
    }
  }
}
