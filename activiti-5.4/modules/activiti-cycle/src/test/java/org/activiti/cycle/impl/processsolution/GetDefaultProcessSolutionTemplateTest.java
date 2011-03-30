package org.activiti.cycle.impl.processsolution;

import org.activiti.cycle.impl.ActivitiCycleDbAwareTest;
import org.activiti.cycle.impl.CycleTestUtils;
import org.activiti.cycle.impl.db.entity.CycleConfigEntity;
import org.activiti.cycle.impl.service.CycleConfigurationServiceImpl;
import org.activiti.cycle.processsolution.ProcessSolutionTemplate;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * Test the
 * {@link CycleProcessSolutionService#getDefaultProcessSolutionTemplate()}-
 * method
 * 
 * @author daniel.meyer@camunda.com
 */
public class GetDefaultProcessSolutionTemplateTest extends ActivitiCycleDbAwareTest {

  public void testGetDefaultProcessSolutionTemplateTest() throws Exception {
    CycleConfigurationServiceImpl configurationService = (CycleConfigurationServiceImpl) CycleServiceFactory.getConfigurationService();
    // store defaultConfiguration
    configurationService.setConfigurationValue("processSolutionTemplates", "default",
            CycleTestUtils.loadResourceAsString("GetDefaultProcessSolutionTemplateTest.xml", this.getClass()));

    CycleProcessSolutionService processSolutionService = CycleServiceFactory.getProcessSolutionService();
    ProcessSolutionTemplate defaultTemplate = processSolutionService.getDefaultProcessSolutionTemplate();

    assertEquals(4, defaultTemplate.getVirtualRepositoryFolders().size());
    VirtualRepositoryFolder f1 = defaultTemplate.getVirtualRepositoryFolders().get(0);

    assertEquals(f1.getLabel(), "Management");
    assertEquals(f1.getType(), "Management");
    assertEquals(f1.getConnectorId(), "Workspace");
    assertEquals(f1.getReferencedNodeId(), "/");

    // cleanup
    CycleConfigEntity e = configurationService.getCycleConfigurationDao().selectCycleConfigByGroupAndKey("processSolutionTemplates", "default");
    configurationService.getCycleConfigurationDao().deleteCycleConfigurationEntry(e.getId());
  }

}
