<#macro printTaskList taskList>
[
  <#list tasks as task><@printTask task/><#if task_has_next>,</#if></#list>
]
</#macro>

<#macro printTask task>
{
  "id": ${task.id},
  "name": "${task.name}",
  "description": "${task.description}",
  "priority": ${task.priority},
  "assignee": <#if task.assignee??>"${task.assignee}"<#else>null</#if>,
  "executionId": ${task.executionId}
}
</#macro>