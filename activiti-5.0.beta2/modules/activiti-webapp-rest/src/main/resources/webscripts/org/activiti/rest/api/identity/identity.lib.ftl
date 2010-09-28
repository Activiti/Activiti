<#macro printUserList userList>
[
  <#list userList as user><@printUser user/><#if user_has_next>,</#if></#list>
]
</#macro>

<#macro printUser user>
{
  "id": "${(user.id!"")?js_string}",
  "firstName": "${(user.firstName!"")?js_string}",
  "lastName": "${(user.lastName!"")?js_string}",
  "email": "${(user.email!"")?js_string}"
}
</#macro>

<#macro printGroupList groupList>
[
  <#list groupList as group><@printGroup group/><#if group_has_next>,</#if></#list>
]
</#macro>

<#macro printGroup group>
{
  "id": "${(group.id!"")?js_string}",
  "name": "${(group.name!"")?js_string}",
  "type": "${(group.type!"")?js_string}"
}
</#macro>