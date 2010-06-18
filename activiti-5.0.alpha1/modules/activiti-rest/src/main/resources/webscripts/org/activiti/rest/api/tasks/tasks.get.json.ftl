<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
  <#list tasks as task>
    {
      "id": ${task.id},
      "name": "${task.name}",
      "description": "${task.description}",
      "priority": ${task.priority},
      "assignee": <#if task.assignee??>"${task.assignee}"<#else>null</#if>,
      "executionId": ${task.executionId}
    }<#if task_has_next>,</#if>
  </#list>
  ],
  "totalRecords": ${totalRecords},
  "startIndex": ${startIndex},
  "sort": "id",
  "dir": "asc",
  "pageSize": ${pageSize}
}
</#escape>