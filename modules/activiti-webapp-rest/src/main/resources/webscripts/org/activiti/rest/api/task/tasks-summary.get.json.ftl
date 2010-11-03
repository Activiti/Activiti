<#escape x as jsonUtils.encodeJSONString(x)>
{
  "assigned": {
    "total": ${assigned?c}
  },
  "unassigned": {
    "total": ${unassigned?c},
    "groups":
    {
      <#list unassignedByGroup?keys as group>
      "${group}": ${unassignedByGroup[group]?c}<#if group_has_next>,</#if>
      </#list>
    }
  }
}     
</#escape>