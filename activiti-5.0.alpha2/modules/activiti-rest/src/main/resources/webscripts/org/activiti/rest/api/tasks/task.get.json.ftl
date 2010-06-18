<#import "task.lib.ftl" as taskLib>
<#escape x as jsonUtils.encodeJSONString(x)>
<@taskLib.printTask task/>
</#escape>