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
package org.activiti.compatibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.compatibility.testdata.Activiti5TestDataGenerator;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  
  public static void main(String[] args) throws Exception {
    logger.info("Booting up Activiti 5 Process Engine");
    ProcessEngine processEngine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti5.cfg.xml").buildProcessEngine();
    logger.info("Starting test data generation.");
    List<String> executedGenerators = new ArrayList<String>();
    Reflections reflections = new Reflections("org.activiti.compatibility.testdata.generator");    
    Set<Class<? extends Activiti5TestDataGenerator>> generatorClasses = reflections.getSubTypesOf(Activiti5TestDataGenerator.class);
    for (Class<? extends Activiti5TestDataGenerator> generatorClass : generatorClasses) {
      Activiti5TestDataGenerator testDataGenerator = generatorClass.newInstance();
      testDataGenerator.generateTestData(processEngine);
      executedGenerators.add(testDataGenerator.getClass().getCanonicalName());
    }

    logger.info("Test data generation completed.");
    for (String generatorClass : executedGenerators) {
      logger.info("Executed test data generator " + generatorClass);
    }
  }
  
}
