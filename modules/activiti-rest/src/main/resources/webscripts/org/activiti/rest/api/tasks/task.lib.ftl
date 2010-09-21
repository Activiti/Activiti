<#macro printTaskList taskList>
[
  <#list tasks as task><@printTask task/><#if task_has_next>,</#if></#list>
]
</#macro>

<#macro printTask task>
{
  "id": ${task.id},
  "name": "${task.name?js_string}",
  "description": "${(task.description!"")?js_string}",
  "priority": <#if task.priority??>${task.priority}<#else>50</#if>,
  "assignee": <#if task.assignee??>"${task.assignee?js_string}"<#else>null</#if>,
  "executionId": ${task.executionId},
  "formResourceKey": <#if task.formResourceKey??>"${task.formResourceKey?js_string}"<#else>null</#if>
}
</#macro>