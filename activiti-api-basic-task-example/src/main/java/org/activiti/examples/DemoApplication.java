package org.activiti.examples;

import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.event.TaskAssigned;
import org.activiti.runtime.api.event.listener.TaskRuntimeEventListener;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.builders.TaskPayloadBuilder;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {


    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        securityUtil.logInAs("salaboy");

        Task myFirstTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("First Team Task")
                .withDescription("This is something really important")
                .withGroup("activitiTeam")
                .withPriority(10)
                .build());

        securityUtil.logInAs("other");

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));

        System.out.println(">  Other cannot see the task: " + tasks.getTotalItems());


        securityUtil.logInAs("erdemedeiros");

        tasks = taskRuntime.tasks(Pageable.of(0, 10));

        System.out.println(">  erdemedeiros can see the task: " + tasks.getTotalItems());


        String availableTaskId = tasks.getContent().get(0).getId();
        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(availableTaskId).build());

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(availableTaskId).build());


    }

    @Bean
    public TaskRuntimeEventListener<TaskAssigned> taskAssignedListener() {
        return taskAssigned -> System.out.println(">>> Task Assigned: '"
                + taskAssigned.getEntity().getName() +
                "' We can send a notification to: " + taskAssigned.getEntity().getAssignee());
    }

    @Bean
    public TaskRuntimeEventListener<TaskAssigned> taskCompletedListener() {
        return taskAssigned -> System.out.println(">>> Task Completed: '"
                + taskAssigned.getEntity().getName() +
                "' We can send a notification to: " + taskAssigned.getEntity().getOwner());
    }


}
