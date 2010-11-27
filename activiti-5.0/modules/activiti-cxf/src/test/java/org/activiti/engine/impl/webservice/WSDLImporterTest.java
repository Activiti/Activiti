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
package org.activiti.engine.impl.webservice;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.impl.bpmn.SimpleStructureDefinition;
import org.activiti.engine.impl.bpmn.StructureDefinition;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.webservice.WSDLImporter;
import org.activiti.engine.impl.webservice.WSOperation;
import org.activiti.engine.impl.webservice.WSService;
import org.junit.Test;

/**
 * @author Esteban Robles Luna
 */
public class WSDLImporterTest {

  @Test
  public void testImport() throws Exception {
    WSDLImporter importer = new WSDLImporter();
    URL url = ReflectUtil.getResource("org/activiti/engine/impl/webservice/counter.wsdl");
    importer.importFrom(url.toString());
    
    List<WSService> services = new ArrayList<WSService>(importer.getServices());
    Assert.assertEquals(1, services.size());
    WSService service = services.get(0);
    
    Assert.assertEquals("Counter", service.getName());
    Assert.assertEquals("http://localhost:63081/counter", service.getLocation());
    
    List<StructureDefinition> structures = new ArrayList<StructureDefinition>(importer.getStructures());
    Collections.sort(structures, new Comparator<StructureDefinition>() {
      public int compare(StructureDefinition o1, StructureDefinition o2) {
        return o1.getId().compareTo(o2.getId());
      }
    });

    
    List<WSOperation> operations = new ArrayList<WSOperation>(importer.getOperations());
    Collections.sort(operations, new Comparator<WSOperation>() {
      public int compare(WSOperation o1, WSOperation o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    Assert.assertEquals(5, operations.size());

    this.assertOperation(operations.get(0), "getCount", service);
    this.assertOperation(operations.get(1), "inc", service);
    this.assertOperation(operations.get(2), "prettyPrintCount", service);
    this.assertOperation(operations.get(3), "reset", service);
    this.assertOperation(operations.get(4), "setTo", service);
    

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

  private void assertOperation(WSOperation wsOperation, String name, WSService service) {
    Assert.assertEquals(name, wsOperation.getName());
    Assert.assertEquals(service, wsOperation.getService());
  }

  private void assertStructure(StructureDefinition structure, String structureId, String[] parameters, Class<?>[] classes) {
    SimpleStructureDefinition simpleStructure = (SimpleStructureDefinition) structure;
    
    Assert.assertEquals(structureId, simpleStructure.getId());
    
    for (int i = 0; i < simpleStructure.getFieldSize(); i++) {
      Assert.assertEquals(parameters[i], simpleStructure.getFieldNameAt(i));
      Assert.assertEquals(classes[i], simpleStructure.getFieldTypeAt(i));
    }
  }
}
