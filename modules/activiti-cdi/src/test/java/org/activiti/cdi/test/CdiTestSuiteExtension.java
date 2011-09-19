package org.activiti.cdi.test;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.activiti.cdi.impl.ProcessEngineLookup;
import org.activiti.engine.ProcessEngine;
import org.jboss.seam.solder.reflection.annotated.AnnotatedTypeBuilder;

/**
 * Extension preventing the booting of the activiti engine when {@link #isPluginTest} == true
 * 
 * @author Daniel Meyer
 */
public class CdiTestSuiteExtension implements Extension {

  public static boolean isPluginTest = true;
  
  public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery evt, BeanManager beanManager) {
    if (!isPluginTest) {
      return;
    } 
    AnnotatedType<NoopProcessEngineLookup> noopLookup = beanManager.createAnnotatedType(NoopProcessEngineLookup.class);    
    noopLookup = new AnnotatedTypeBuilder<CdiTestSuiteExtension.NoopProcessEngineLookup>()
            .readFromType(noopLookup)
            .removeFromClass(Alternative.class)
            .create();
    evt.addAnnotatedType(noopLookup);
  }

  @Alternative
  final static class NoopProcessEngineLookup implements ProcessEngineLookup {    
    public ProcessEngine getProcessEngine() {
      return null;
    }
    public void ungetProcessEngine() {
    }
  }

}
