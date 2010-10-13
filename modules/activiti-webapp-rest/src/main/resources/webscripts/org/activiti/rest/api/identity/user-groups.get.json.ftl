<#import "../activiti.lib.ftl" as activitiLib>
<#import "identity.lib.ftl" as identityLib/>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": <@identityLib.printGroupList groups/>,
  <@activitiLib.printPagination/>
}
</#escape>

