package org.activiti.kickstart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.ActivitiTestCase;
import org.activiti.kickstart.diagram.ProcessDiagramGenerator;
import org.activiti.kickstart.dto.BaseTaskDto;
import org.activiti.kickstart.dto.KickstartWorkflowDto;
import org.activiti.kickstart.dto.UserTaskDto;
import org.activiti.kickstart.service.KickstartService;
import org.activiti.kickstart.service.KickstartServiceImpl;
import org.junit.After;
import org.junit.Test;

public class KickstartTest extends ActivitiTestCase {

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        // delete deployments
        List<org.activiti.engine.repository.Deployment> deployments = processEngine
            .getRepositoryService()
            .createDeploymentQuery()
            .list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    @Test
    public void testAdhocWorkflowDiagram() throws Exception {
        KickstartWorkflowDto wf = new KickstartWorkflowDto();
        wf.setName("Test process");
        wf.setTasks(getTaskList());

        assertEquals("Test process", wf.getName());
        assertEquals(2, wf.getTasks().size());

        ProcessDiagramGenerator diagramGenerator = new ProcessDiagramGenerator(wf);
        InputStream diagram = diagramGenerator.execute();
        byte[] data = getBytesFromInputStream(diagram);

        assertNotNull(data);
        assertTrue("byte array is not empty", data.length > 0);

        // write diagram to file
//        writeToFile("test/test_process_diagram.png", data);
    }

    @Test
    public void testAdhocWorkflowDeployment() throws Exception {
        KickstartWorkflowDto wf = new KickstartWorkflowDto();
        wf.setName("Test process");
        wf.setTasks(getTaskList());

        KickstartService service = new KickstartServiceImpl(processEngine);
        service.deployKickstartWorkflow(wf);

        List<ProcessDefinition> definitions = processEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .list();
        assertEquals(1, definitions.size());
        assertEquals("Test process", definitions.get(0).getName());

        // get process as AdhocWorkflowDto
        wf = service.findKickstartWorkflowById(definitions.get(0).getId());
        assertEquals("Test process", wf.getName());
    }

    private List<BaseTaskDto> getTaskList() {
        List<BaseTaskDto> tasks = new ArrayList<BaseTaskDto>();
        UserTaskDto task = new UserTaskDto();
        task.setId("T1");
        task.setName("Test 1");
        task.setDescription("First Task");
        tasks.add(task);

        task = new UserTaskDto();
        task.setId("T2");
        task.setName("Test 2");
        task.setDescription("Second Task");
        task.setStartWithPrevious(true);
        tasks.add(task);
        return tasks;
    }

    private byte[] getBytesFromInputStream(final InputStream is) throws IOException {
        int size = is.available();
        byte[] buffer = new byte[size];
        int bytesRead = is.read(buffer);
        if (bytesRead != size) {
            throw new IOException("Only " + bytesRead + " of " + size + " could be read.");
        }
        return buffer;
    }

    private void writeToFile(final String file, final byte[] data) throws IOException {
        File f = new File(file);
        FileOutputStream fout = new FileOutputStream(f);
        fout.write(data);
        fout.flush();
        fout.close();
    }

}
