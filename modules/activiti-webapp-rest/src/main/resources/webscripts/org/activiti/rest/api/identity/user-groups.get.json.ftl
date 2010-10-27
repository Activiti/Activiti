<#import "../activiti.lib.ftl" as activitiLib>
<#import "identity.lib.ftl" as identityLib/>
{
  "data": <@identityLib.printGroupList groups/>,
  <@activitiLib.printPagination/>
}


