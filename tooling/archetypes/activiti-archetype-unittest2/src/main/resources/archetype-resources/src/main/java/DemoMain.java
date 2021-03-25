package ${package};

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.BooleanFormType;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author dgg-wxw
 * @date 2021/3/19 14:14
 */
@Slf4j
public class DemoMain {

    public static void main(String[] args) throws ParseException {
        // 1. 创建流程引擎
        ProcessEngine processEngine = getProcessEngine();

        // 2. 部署流程定义文件
        ProcessDefinition processDefinition = getProcessDefinition(processEngine);

        // 3. 启动运行流程
        ProcessInstance processInstance = getProcessInstance(processEngine, processDefinition);

        // 4. 处理流程任务
        processTask(processEngine, processInstance);

        log.warn("结束程序");
    }

    private static void processTask(ProcessEngine processEngine, ProcessInstance processInstance) throws ParseException {
        Scanner scanner = new Scanner(System.in);
        while (processInstance != null && !processInstance.isEnded()) {
            TaskService taskService = processEngine.getTaskService();
            List<Task> taskList = taskService.createTaskQuery().list();
            for (Task task : taskList) {
                log.info("待处理任务：[{}]", task.getName());

                FormService formService = processEngine.getFormService();
                TaskFormData taskFormData = formService.getTaskFormData(task.getId());
                List<FormProperty> formProperties = taskFormData.getFormProperties();
                Map<String, Object> variables = Maps.newHashMap();
                for (FormProperty property : formProperties) {
                    String line = null;
                    if (StringFormType.class.isInstance(property.getType())) {
                        log.info("请输入 {} ？", property.getName());
                        line = scanner.nextLine();
                        variables.put(property.getId(), line);
                    } else if (DateFormType.class.isInstance(property.getType())) {
                        log.info("请输入 {} ？格式（yyyy-MM-dd）", property.getName());
                        line = scanner.nextLine();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = simpleDateFormat.parse(line);
                        variables.put(property.getId(), date);
                    } else if (BooleanFormType.class.isInstance(property.getType())) {
                        log.info("请输入 {} ？", property.getName());
                        line = scanner.nextLine();
                        variables.put(property.getId(), Boolean.parseBoolean(line));
                    } else {
                        log.info("暂不支持 {} 类型", property.getType());
                    }
                    log.info("您输入的内容是：[{}]", line);
                }

                taskService.complete(task.getId(), variables);
                processInstance = processEngine.getRuntimeService()
                        .createProcessInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .singleResult();
            }
        }

        scanner.close();
    }

    private static ProcessInstance getProcessInstance(ProcessEngine processEngine, ProcessDefinition processDefinition) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        log.info("流程定义key：[{}]， 业务key：[{}]", processInstance.getProcessDefinitionKey(), processInstance.getBusinessKey());
        return processInstance;
    }

    private static ProcessDefinition getProcessDefinition(ProcessEngine processEngine) {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("second_approve.bpmn20.xml")
                .deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploy.getId())
                .singleResult();
        log.info("流程定义文件：[{}]， 流程ID：[{}]", processDefinition.getName(), processDefinition.getId());
        return processDefinition;
    }

    private static ProcessEngine getProcessEngine() {
        ProcessEngineConfiguration pec = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        ProcessEngine processEngine = pec.buildProcessEngine();
        String engineName = processEngine.getName();
        String engineVersion = ProcessEngine.VERSION;
        log.info("流程引擎名称：[{}]， 版本：[{}]", engineName, engineVersion);
        return processEngine;
    }
}
