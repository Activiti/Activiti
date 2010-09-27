<#import "task.lib.ftl" as taskLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": <@taskLib.printTaskList tasks/>,
  "total": ${total},
  "start": ${start},
  "size": ${size},
  "sort": "id",
  "order": "asc"
}
</#escape>