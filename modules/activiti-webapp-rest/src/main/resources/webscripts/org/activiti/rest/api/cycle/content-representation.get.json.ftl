<#escape x as jsonUtils.encodeJSONString(x)>
{
	"renderInfo": "${renderInfo}",
	"contentRepresentationId": "${contentRepresentationId}",
	"contentType": "${contentType}",
	
	<#if renderInfo == "IMAGE" >
		"imageUrl": "${imageUrl}"
	<#elseif renderInfo == "HTML">
		"content": "${content}"
	<#else>
		<#-- Content for "BINARY", "CODE" or "TEXT_PLAIN" needs to be HTML escaped. -->
		"content": "${content?html}"		
	</#if>
}
</#escape>