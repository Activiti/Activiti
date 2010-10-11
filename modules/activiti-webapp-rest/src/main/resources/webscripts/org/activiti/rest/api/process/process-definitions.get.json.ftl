<#import "../activiti.lib.ftl" as activitiLib>
<#import "process.lib.ftl" as processLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": <@processLib.printProcessDefinitionList processDefinitions/>,
  <@activitiLib.printPagination/>
}
</#escape>