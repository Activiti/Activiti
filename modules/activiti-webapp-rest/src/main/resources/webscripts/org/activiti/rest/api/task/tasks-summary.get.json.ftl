<#escape x as jsonUtils.encodeJSONString(x)>
{
  "assigned": {
    "total": ${assigned}
  },
  "unassigned": {
    "total": ${unassigned},
    "groups":
    {
      <#list unassignedByGroup?keys as group>
      "${group}": ${unassignedByGroup[group]}<#if group_has_next>,</#if>
      </#list>
    }
  }
}     
</#escape>