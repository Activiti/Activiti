<#import "management.lib.ftl" as managementLib>
<#import "../activiti.lib.ftl" as restLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": <@managementLib.printJobList jobs/>,
  <@restLib.printPagination/>
}
</#escape>
