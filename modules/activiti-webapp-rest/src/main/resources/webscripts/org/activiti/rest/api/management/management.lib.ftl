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
  "id": "${job.id?js_string}",
  "executionId": "${job.executionId?js_string}",
  "retries": ${job.retries?c},
  "processInstanceId": "${(job.processInstanceId!"")?js_string}",
  "dueDate": <#if job.dueDate??>"${iso8601Date(job.dueDate)}"<#else>null</#if>,
  "assignee": <#if job.assignee??>"${job.assignee?js_string}"<#else>null</#if>,
  "exceptionMessage": <#if job.exceptionMessage??>"${job.exceptionMessage?js_string}"<#else>null</#if>
</#macro>
