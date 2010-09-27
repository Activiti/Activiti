<#import "identity.lib.ftl" as identityLib/>
<#escape x as jsonUtils.encodeJSONString(x)>
<@identityLib.printUser user/>
</#escape>