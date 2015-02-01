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
package org.activiti.cdi.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.impl.context.BusinessProcessContext;
import org.activiti.cdi.impl.util.ActivitiServices;
import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.cdi.spi.ProcessEngineLookup;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDI-Extension registering a custom context for {@link BusinessProcessScoped}
 * beans.
 * 
 * Also starts / stops the activiti {@link ProcessEngine} and deploys all
 * processes listed in the 'processes.xml'-file.
 * 
 * @author Daniel Meyer
 */
public class ActivitiExtension implements Extension {

  private static Logger logger = LoggerFactory.getLogger(ActivitiExtension.class);
  private ProcessEngineLookup processEngineLookup;

  public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
    event.addScope(BusinessProcessScoped.class, true, true);
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {       
    BeanManagerLookup.localInstance = manager;
    event.addContext(new BusinessProcessContext(manager));
  }

  public void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
    try {   
      logger.info("Initializing activiti-cdi.");      
      // initialize the process engine
      ProcessEngine processEngine = lookupProcessEngine(beanManager);      
      // deploy the processes if engine was set up correctly
      deployProcesses(processEngine);      
    } catch (Exception e) {
      // interpret engine initialization problems as definition errors
      event.addDeploymentProblem(e);
    }
  }

  protected ProcessEngine lookupProcessEngine(BeanManager beanManager) {    
    ServiceLoader<ProcessEngineLookup> processEngineServiceLoader = ServiceLoader.load(ProcessEngineLookup.class);
    Iterator<ProcessEngineLookup> serviceIterator = processEngineServiceLoader.iterator();
    List<ProcessEngineLookup> discoveredLookups = new ArrayList<ProcessEngineLookup>();
    while (serviceIterator.hasNext()) {
      ProcessEngineLookup serviceInstance = (ProcessEngineLookup) serviceIterator.next();
      discoveredLookups.add(serviceInstance);
    }
    
    Collections.sort(discoveredLookups, new Comparator<ProcessEngineLookup>() {
      public int compare(ProcessEngineLookup o1, ProcessEngineLookup o2) {       
        return (-1)*((Integer)o1.getPrecedence()).compareTo(o2.getPrecedence());
      }      
    });
    
    ProcessEngine processEngine = null;
    
    for (ProcessEngineLookup processEngineLookup : discoveredLookups) {
      processEngine = processEngineLookup.getProcessEngine();
      if(processEngine != null) {
        this.processEngineLookup = processEngineLookup;
        logger.debug("ProcessEngineLookup service {} returned process engine.", processEngineLookup.getClass());
        break;
      } else {
        logger.debug("ProcessEngineLookup service {} retuned 'null' value.", processEngineLookup.getClass());
      }
    }
    
    if(processEngineLookup == null) {
      throw new ActivitiException("Could not find an implementation of the org.activiti.cdi.spi.ProcessEngineLookup service " +
      		"returning a non-null processEngine. Giving up.");
    }
    
    ActivitiServices activitiServices = ProgrammaticBeanLookup.lookup(ActivitiServices.class, beanManager);
    activitiServices.setProcessEngine(processEngine);
    
    return processEngine;
  }

  private void deployProcesses(ProcessEngine processEngine) {
    new ProcessDeployer(processEngine).deployProcesses();
  }

  public void beforeShutdown(@Observes BeforeShutdown event) {
    if(processEngineLookup != null) {
      processEngineLookup.ungetProcessEngine();
      processEngineLookup = null;
    }
    logger.info("Shutting down activiti-cdi");    
  }

}
