<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "connectorId": "${connectorId}",
  "nodeId": "${nodeId}",
  "vFolderId": "${vFolderId!''}",
  <#if addRequirementAction??>
  	"addRequirementAction": {	
  		"connectorId": "${addRequirementAction.requirementsFolderConnectorId}",
  		"parentFolderId": "${addRequirementAction.requirementsFolderId}"
  	},
  </#if> 
  "contentRepresentations": [
  <#list contentRepresentations as contentRepresentation>
      "${contentRepresentation}"
      <#if contentRepresentation_has_next>,</#if>
  </#list>
  ],
  "actions": [
  <#list actions as action>
    {
      "name": "${action.id}",
      "label": "${action.id}"
    }
    <#if action_has_next>,</#if>
  </#list>
  ],
  "downloads": [
  <#list downloads as download>
    {
      "label": "${download.label}",
      "url": "${download.url}",
      "type": "${download.mimeType}",
      "name": "${download.name}"
    }
    <#if download_has_next>,</#if>
  </#list>
  ],
  "links": [
  <#list links as link>
    {
      "name": "${link.id}",
      "label": "${link.id}",
      "url": "${link.url}",
      "warning": "${link.warning!''}"
    }
    <#if link_has_next>,</#if>
  </#list>
  ]
}
</#escape>

</#if>