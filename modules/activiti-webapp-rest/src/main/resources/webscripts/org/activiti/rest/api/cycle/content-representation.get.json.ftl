<#escape x as jsonUtils.encodeJSONString(x)>
{
	"connectorId": "${connectorId}",
	"artifactId": "${artifactId}",
	"renderInfo": "${renderInfo}",
	"contentRepresentationId": "${contentRepresentationId}",
	"contentType": "${contentType}"
	
	<#if renderInfo == "IMAGE" >
		<#-- For images we don't need to send the content since it will be requested through a URL and the content.get webscript. -->
	<#elseif renderInfo == "HTML">
		<#-- For HTML we don't need to send the content since it will be requested through a URL and the content.get webscript. -->
	<#elseif renderInfo == "HTML_REFERENCE">
		,"url": "${content}"
	<#else>
		<#-- Content for "BINARY", "CODE" or "TEXT_PLAIN" needs to be HTML escaped. -->
		,"content": "${content?html}"
	</#if>
}
</#escape>