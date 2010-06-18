<#escape x as jsonUtils.encodeJSONString(x)>
{
  "id": "${user.id!""}",
  "firstName": "${user.firstName!""}",
  "lastName": "${user.lastName!""}",
  "email": "${user.email!""}"
}
</#escape>