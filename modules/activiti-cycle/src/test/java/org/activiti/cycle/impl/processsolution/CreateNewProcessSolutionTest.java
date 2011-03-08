package org.activiti.cycle.impl.processsolution;

import java.io.File;
import java.util.UUID;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.impl.ActivitiCycleDbAwareTest;
import org.activiti.cycle.impl.CycleTestUtils;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.fs.FileSystemConnector;
import org.activiti.cycle.impl.db.entity.CycleConfigEntity;
import org.activiti.cycle.impl.service.CycleConfigurationServiceImpl;
import org.activiti.cycle.impl.service.CycleProcessSolutionServiceImpl;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleServiceFactory;

public class CreateNewProcessSolutionTest extends ActivitiCycleDbAwareTest {

  public void testCreateNewProcessSolutionTest() {
    // store default configuration:
    CycleConfigurationServiceImpl configurationService = (CycleConfigurationServiceImpl) CycleServiceFactory.getConfigurationService();
    // store defaultConfiguration
    configurationService.setConfigurationValue("processSolutionTemplates", "default",
            CycleTestUtils.loadResourceAsString("GetDefaultProcessSolutionTemplateTest.xml", this.getClass()));

    CycleProcessSolutionServiceImpl processSolutionServiceImpl = (CycleProcessSolutionServiceImpl) CycleServiceFactory.getProcessSolutionService();

    RuntimeConnectorList runtimeConnectorList = CycleComponentFactory.getCycleComponentInstance(RuntimeConnectorList.class, RuntimeConnectorList.class);
    FileSystemConnector connector = new FileSystemConnector();
    connector.setName("Workspace");
    connector.setId("Workspace");
    connector.startConfiguration();
    File tmpDir = null;
    try {
      File tempFile = File.createTempFile("connectorTest", "");
      tmpDir = new File(tempFile.getParentFile().getAbsoluteFile() + File.separator + UUID.randomUUID().toString());
      tmpDir.mkdir();

      connector.addConfigurationEntry(FileSystemConnector.CONFIG_KEY_BASE_PATH, tmpDir.getAbsolutePath());
      connector.configurationFinished();
      runtimeConnectorList.registerConnector(connector);

      processSolutionServiceImpl.createNewProcessSolution("test process solution");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      CycleTestUtils.deleteFileRec(tmpDir);
    }

    for (ProcessSolution solution : processSolutionServiceImpl.getProcessSolutions()) {
      for (VirtualRepositoryFolder virtualFolderEntity : processSolutionServiceImpl.getFoldersForProcessSolution(solution.getId())) {
        processSolutionServiceImpl.getDao().deleteVirtualRepositoryFolderById(virtualFolderEntity.getId());
      }
      processSolutionServiceImpl.getDao().deleteProcessSolutionById(solution.getId());
    }

    // cleanup
    CycleConfigEntity e = configurationService.getCycleConfigurationDao().selectCycleConfigByGroupAndKey("processSolutionTemplates", "default");
    configurationService.getCycleConfigurationDao().deleteCycleConfigurationEntry(e.getId());
  }

}
