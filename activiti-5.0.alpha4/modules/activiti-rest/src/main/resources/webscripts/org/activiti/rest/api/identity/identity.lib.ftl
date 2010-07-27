<#macro printUserList userList>
[
  <#list userList as user><@printUser user/><#if user_has_next>,</#if></#list>
]
</#macro>

<#macro printUser user>
{
  "id": "${user.id!""}",
  "firstName": "${user.firstName!""}",
  "lastName": "${user.lastName!""}",
  "email": "${user.email!""}"
}
</#macro>

<#macro printGroupList groupList>
[
  <#list groupList as group><@printGroup group/><#if group_has_next>,</#if></#list>
]
</#macro>

<#macro printGroup group>
{
  "id": "${group.id!""}",
  "name": "${group.name!""}",
  "type": "${group.type!""}"
}
</#macro>