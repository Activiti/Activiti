<#import "management.lib.ftl" as managementLib>
<#import "../activiti.lib.ftl" as restLib>
{
  "data": <@managementLib.printJobList jobs/>,
  <@restLib.printPagination/>
}

