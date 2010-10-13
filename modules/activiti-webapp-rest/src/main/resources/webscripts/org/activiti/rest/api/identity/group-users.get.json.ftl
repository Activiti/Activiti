<#import "../activiti.lib.ftl" as activitiLib>
<#import "identity.lib.ftl" as identityLib/>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": <@identityLib.printUserList users/>,
  <@activitiLib.printPagination/>
}
</#escape>

