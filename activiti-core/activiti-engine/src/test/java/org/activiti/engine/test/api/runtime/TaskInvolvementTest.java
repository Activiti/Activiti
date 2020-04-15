package org.activiti.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;

public class TaskInvolvementTest  extends PluggableActivitiTestCase {

    public void testQueryByInvolvedGroupOrUserO() {
        try {
            Task adhocTask = taskService.newTask();
            adhocTask.setAssignee("kermit");
            adhocTask.setOwner("involvedUser");
            adhocTask.setPriority(10);
            taskService.saveTask(adhocTask);


            List<String> groups = new ArrayList<String>();
            groups.add("group1");

            assertThat(taskService.createTaskQuery()
                    .or()
                    .taskInvolvedUser("involvedUser")
                    .taskInvolvedGroupsIn(groups)
                    .endOr()
                    .count()).isEqualTo(1);

            if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                assertThat(historyService.createHistoricTaskInstanceQuery()
                        .or()
                        .taskInvolvedUser("involvedUser")
                        .taskInvolvedGroupsIn(groups)
                        .endOr()
                        .count()).isEqualTo(1);
            }

        } finally {
            List<Task> allTasks = taskService.createTaskQuery().list();
            for(Task task : allTasks) {
                if(task.getExecutionId() == null) {
                    taskService.deleteTask(task.getId());
                    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                        historyService.deleteHistoricTaskInstance(task.getId());
                    }
                }
            }
        }
    }

    public void testQueryByInvolvedGroupOrUser() {
        try {
            Task adhocTask = taskService.newTask();
            adhocTask.setAssignee("kermit");
            adhocTask.setOwner("involvedUser");
            adhocTask.setPriority(10);
            taskService.saveTask(adhocTask);
            taskService.addGroupIdentityLink(adhocTask.getId(), "group1", IdentityLinkType.PARTICIPANT);

            List<String> groups = new ArrayList<String>();
            groups.add("group1");

            assertThat(taskService.getIdentityLinksForTask(adhocTask.getId())).hasSize(3);
            assertThat(taskService.createTaskQuery()
                    //.taskId(adhocTask.getId())
                    .or()
                    .taskInvolvedUser("involvedUser")
                    .taskInvolvedGroupsIn(groups)
                    .endOr()
                    .count()).isEqualTo(1);

            if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                assertThat(historyService.createHistoricTaskInstanceQuery()
                        .or().taskCategory("j").taskPriority(10).endOr()
                        .or()
                        .taskInvolvedUser("involvedUser")
                        .taskInvolvedGroupsIn(groups)
                        .endOr()
                        .count()).isEqualTo(1);
            }
        } finally {
            List<Task> allTasks = taskService.createTaskQuery().list();
            for(Task task : allTasks) {
                if(task.getExecutionId() == null) {
                    taskService.deleteTask(task.getId());
                    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                        historyService.deleteHistoricTaskInstance(task.getId());
                    }
                }
            }
        }
    }

    public void testQueryByInvolvedGroupOrUser2() {
        try {
            Task taskUser1WithGroups = taskService.newTask();
            taskUser1WithGroups.setAssignee("kermit");
            taskUser1WithGroups.setOwner("user1");
            taskUser1WithGroups.setPriority(10);
            taskService.saveTask(taskUser1WithGroups);
            taskService.addGroupIdentityLink(taskUser1WithGroups.getId(), "group1", IdentityLinkType.PARTICIPANT);


            Task taskUser1WithGroupsCandidateUser = taskService.newTask();
            taskUser1WithGroupsCandidateUser.setAssignee("kermit");
            taskUser1WithGroupsCandidateUser.setOwner("involvedUser");
            taskUser1WithGroupsCandidateUser.setPriority(10);
            taskService.saveTask(taskUser1WithGroupsCandidateUser);
            taskService.addGroupIdentityLink(taskUser1WithGroupsCandidateUser.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addCandidateUser(taskUser1WithGroupsCandidateUser.getId(), "candidateUser1");



            List<String> groups = new ArrayList<String>();
            groups.add("group1");

            assertThat(taskService.createTaskQuery()
                    //.taskId(adhocTask.getId())
                    .or()
                    .taskInvolvedUser("user1")
                    .taskInvolvedGroupsIn(groups)
                    .endOr()
                    .count()).isEqualTo(2);

            assertThat(taskService.createTaskQuery()
                    //.taskId(adhocTask.getId())
                    .or()
                    .taskCandidateUser("user1")
                    .taskInvolvedGroupsIn(groups)
                    .endOr()
                    .count()).isEqualTo(2);

            assertThat(taskService.createTaskQuery()
                    //.taskId(adhocTask.getId())
                    .or()
                    .taskCandidateGroup("group2")
                    .taskInvolvedGroupsIn(groups)
                    .endOr()
                    .count()).isEqualTo(2);

        } finally {
            List<Task> allTasks = taskService.createTaskQuery().list();
            for(Task task : allTasks) {
                if(task.getExecutionId() == null) {
                    taskService.deleteTask(task.getId());
                    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                        historyService.deleteHistoricTaskInstance(task.getId());
                    }
                }
            }
        }
    }

    public void testQueryByInvolvedGroupAndUser() {
        try {
            Task adhocTask = taskService.newTask();
            adhocTask.setAssignee("kermit");
            adhocTask.setOwner("involvedUser");
            adhocTask.setPriority(10);
            taskService.saveTask(adhocTask);
            taskService.addGroupIdentityLink(adhocTask.getId(), "group1", IdentityLinkType.PARTICIPANT);

            List<String> groups = new ArrayList<String>();
            groups.add("group2");

            assertThat(taskService.getIdentityLinksForTask(adhocTask.getId())).hasSize(3);
            assertThat(taskService.createTaskQuery()
                    .taskInvolvedUser("involvedUser")
                    .taskInvolvedGroupsIn(groups)
                    .count()).isEqualTo(0);

            if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                assertThat(historyService.createHistoricTaskInstanceQuery()
                        .taskInvolvedUser("involvedUser")
                        .taskInvolvedGroupsIn(groups)
                        .count()).isEqualTo(0);
            }

        } finally {
            List<Task> allTasks = taskService.createTaskQuery().list();
            for(Task task : allTasks) {
                if(task.getExecutionId() == null) {
                    taskService.deleteTask(task.getId());
                    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                        historyService.deleteHistoricTaskInstance(task.getId());
                    }
                }
            }
        }
    }


    public void testQueryMultipleAndAndSingleOr() {
        try {
            Task taskUser1Group1 = taskService.newTask();
            taskUser1Group1.setAssignee("kermit");
            taskUser1Group1.setOwner("user1");
            taskUser1Group1.setPriority(10);
            taskService.saveTask(taskUser1Group1);
            taskService.addGroupIdentityLink(taskUser1Group1.getId(), "group1", IdentityLinkType.PARTICIPANT);

            Task taskUser1Group2 = taskService.newTask();
            taskUser1Group2.setAssignee("kermit");
            taskUser1Group2.setOwner("user1");
            taskUser1Group2.setPriority(10);
            taskService.saveTask(taskUser1Group2);
            taskService.addGroupIdentityLink(taskUser1Group2.getId(), "group2", IdentityLinkType.PARTICIPANT);

            Task taskUser1Group1and2 = taskService.newTask();
            taskUser1Group1and2.setAssignee("kermit");
            taskUser1Group1and2.setOwner("user1");
            taskUser1Group1and2.setPriority(10);
            taskService.saveTask(taskUser1Group1and2);
            taskService.addGroupIdentityLink(taskUser1Group2.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group2.getId(), "group2", IdentityLinkType.PARTICIPANT);


            Task taskUser1Group1and3 = taskService.newTask();
            taskUser1Group1and3.setAssignee("kermit");
            taskUser1Group1and3.setOwner("user1");
            taskUser1Group1and3.setPriority(10);
            taskService.saveTask(taskUser1Group1and3);
            taskService.addGroupIdentityLink(taskUser1Group1and3.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group1and3.getId(), "group3", IdentityLinkType.PARTICIPANT);


            Task taskUser1Group1and4 = taskService.newTask();
            taskUser1Group1and4.setAssignee("kermit");
            taskUser1Group1and4.setOwner("user1");
            taskUser1Group1and4.setPriority(10);
            taskService.saveTask(taskUser1Group1and4);
            taskService.addGroupIdentityLink(taskUser1Group1and4.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group1and4.getId(), "group4", IdentityLinkType.PARTICIPANT);



            List<String> andGroup = new ArrayList<String>();
            andGroup.add("group1");

            List<String> orGroup = new ArrayList<String>();
            orGroup.add("group2");
            orGroup.add("group4");

            assertThat(taskService.createTaskQuery()
                    .taskInvolvedUser("user1")
                    .taskInvolvedGroupsIn(andGroup)
                    .or().taskInvolvedGroupsIn(orGroup).endOr()
                    .count()).isEqualTo(2);

        } finally {
            List<Task> allTasks = taskService.createTaskQuery().list();
            for(Task task : allTasks) {
                if(task.getExecutionId() == null) {
                    taskService.deleteTask(task.getId());
                    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                        historyService.deleteHistoricTaskInstance(task.getId());
                    }
                }
            }
        }
    }


    public void testQueryMultipleAndAndMultipleOr() {
        try {
            Task taskUser1Group1 = taskService.newTask();
            taskUser1Group1.setAssignee("kermit");
            taskUser1Group1.setOwner("user1");
            taskUser1Group1.setPriority(10);
            taskService.saveTask(taskUser1Group1);
            taskService.addGroupIdentityLink(taskUser1Group1.getId(), "group1", IdentityLinkType.PARTICIPANT);

            Task taskUser1Group2 = taskService.newTask();
            taskUser1Group2.setAssignee("kermit");
            taskUser1Group2.setOwner("user1");
            taskUser1Group2.setPriority(10);
            taskService.saveTask(taskUser1Group2);
            taskService.addGroupIdentityLink(taskUser1Group2.getId(), "group2", IdentityLinkType.PARTICIPANT);

            Task taskUser1Group1and2 = taskService.newTask();
            taskUser1Group1and2.setAssignee("kermit");
            taskUser1Group1and2.setOwner("user1");
            taskUser1Group1and2.setPriority(10);
            taskService.saveTask(taskUser1Group1and2);
            taskService.addGroupIdentityLink(taskUser1Group2.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group2.getId(), "group2", IdentityLinkType.PARTICIPANT);


            Task taskUser1Group1and3 = taskService.newTask();
            taskUser1Group1and3.setAssignee("kermit");
            taskUser1Group1and3.setOwner("user1");
            taskUser1Group1and3.setPriority(10);
            taskService.saveTask(taskUser1Group1and3);
            taskService.addGroupIdentityLink(taskUser1Group1and3.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group1and3.getId(), "group3", IdentityLinkType.PARTICIPANT);


            Task taskUser1Group1and4 = taskService.newTask();
            taskUser1Group1and4.setAssignee("kermit");
            taskUser1Group1and4.setOwner("user1");
            taskUser1Group1and4.setPriority(10);
            taskService.saveTask(taskUser1Group1and4);
            taskService.addGroupIdentityLink(taskUser1Group1and4.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group1and4.getId(), "group4", IdentityLinkType.PARTICIPANT);


            Task taskUser1Group1andUser2 = taskService.newTask();
            taskUser1Group1andUser2.setAssignee("kermit");
            taskUser1Group1andUser2.setOwner("user1");
            taskUser1Group1andUser2.setPriority(10);
            taskService.saveTask(taskUser1Group1andUser2);
            taskService.addGroupIdentityLink(taskUser1Group1andUser2.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addUserIdentityLink(taskUser1Group1andUser2.getId(), "user2", IdentityLinkType.PARTICIPANT);

            Task taskUser1Group1andUser2Group4 = taskService.newTask();
            taskUser1Group1andUser2Group4.setAssignee("kermit");
            taskUser1Group1andUser2Group4.setOwner("user1");
            taskUser1Group1andUser2Group4.setPriority(10);
            taskService.saveTask(taskUser1Group1andUser2Group4);
            taskService.addGroupIdentityLink(taskUser1Group1andUser2Group4.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group1andUser2Group4.getId(), "group4", IdentityLinkType.PARTICIPANT);
            taskService.addUserIdentityLink(taskUser1Group1andUser2Group4.getId(), "user2", IdentityLinkType.PARTICIPANT);

            Task taskUser1Group5andUser2Group4 = taskService.newTask();
            taskUser1Group5andUser2Group4.setAssignee("kermit");
            taskUser1Group5andUser2Group4.setOwner("user1");
            taskUser1Group1andUser2Group4.setPriority(10);
            taskService.saveTask(taskUser1Group5andUser2Group4);
            taskService.addGroupIdentityLink(taskUser1Group5andUser2Group4.getId(), "group5", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser1Group5andUser2Group4.getId(), "group4", IdentityLinkType.PARTICIPANT);
            taskService.addUserIdentityLink(taskUser1Group5andUser2Group4.getId(), "user2", IdentityLinkType.PARTICIPANT);


            Task taskUser2Group1andUser2Group4 = taskService.newTask();
            taskUser2Group1andUser2Group4.setAssignee("kermit");
            taskUser2Group1andUser2Group4.setOwner("user3");
            taskUser2Group1andUser2Group4.setPriority(10);
            taskService.saveTask(taskUser2Group1andUser2Group4);
            taskService.addGroupIdentityLink(taskUser2Group1andUser2Group4.getId(), "group1", IdentityLinkType.PARTICIPANT);
            taskService.addGroupIdentityLink(taskUser2Group1andUser2Group4.getId(), "group4", IdentityLinkType.PARTICIPANT);
            taskService.addUserIdentityLink(taskUser2Group1andUser2Group4.getId(), "user2", IdentityLinkType.PARTICIPANT);

            List<String> andGroup = new ArrayList<String>();
            andGroup.add("group1");

            List<String> orGroup = new ArrayList<String>();
            orGroup.add("group2");
            orGroup.add("group4");

            assertThat(taskService.createTaskQuery()
                    .taskInvolvedUser("user1")
                    .taskInvolvedGroupsIn(andGroup)
                    .or()
                    .taskInvolvedGroupsIn(orGroup)
                    .taskInvolvedUser("user2")
                    .endOr()
                    .count()).isEqualTo(4);

            if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

                assertThat(historyService.createHistoricTaskInstanceQuery()
                        .taskInvolvedUser("user1")
                        .taskInvolvedGroupsIn(andGroup)
                        .or()
                        .taskInvolvedGroupsIn(orGroup)
                        .taskInvolvedUser("user2")
                        .endOr()
                        .count()).isEqualTo(4);
            }



        } finally {
            List<Task> allTasks = taskService.createTaskQuery().list();
            for(Task task : allTasks) {
                if(task.getExecutionId() == null) {
                    taskService.deleteTask(task.getId());
                    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                        historyService.deleteHistoricTaskInstance(task.getId());
                    }
                }
            }
        }
    }
}
