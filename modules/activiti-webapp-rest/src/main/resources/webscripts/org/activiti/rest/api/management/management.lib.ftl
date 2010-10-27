<#escape x as jsonUtils.encodeJSONString(x)>

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
  "dueDate": <#if job.duedate??>"${iso8601Date(job.duedate)}"<#else>null</#if>,
  "exceptionMessage": <#if job.exceptionMessage??>"${job.exceptionMessage}"<#else>null</#if>
</#macro>

</#escape>