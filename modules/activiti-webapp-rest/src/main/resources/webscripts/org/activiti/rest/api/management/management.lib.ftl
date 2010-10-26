<#macro printJobList jobList>
[
  <#list jobs as job>
  {
    <@printJob job/>
  }<#if job_has_next>,</#if>  
  </#list>
]
</#macro>

<#macro printJob job>
  "id": "${job.id}",
  "executionId": "${job.executionId}",
  "retries": ${job.retries?c},
  "processInstanceId": "${(job.processInstanceId!"")}",
  "dueDate": <#if job.dueDate??>"${iso8601Date(job.dueDate)}"<#else>null</#if>,
  "assignee": <#if job.assignee??>"${job.assignee}"<#else>null</#if>,
  "exceptionMessage": <#if job.exceptionMessage??>"${job.exceptionMessage}"<#else>null</#if>
</#macro>
