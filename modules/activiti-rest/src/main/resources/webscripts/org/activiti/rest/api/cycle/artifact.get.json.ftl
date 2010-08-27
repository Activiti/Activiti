{
  "id": "${artifactId}",
  "contentViews": [
  <#list contentViews as contentView>
    {
      "type": "${contentView.key}",
      "name": "${contentView.name}",
      "content": <#escape x as jsonUtils.encodeJSONString(x)> "${contentView.value?html}" </#escape>
    }
    <#if contentView_has_next>,</#if>
  </#list>
  ],
  "actions": [
  <#list actions as action>
    {
      "name": "${action.name}",
      "label": "${action.label}"
    }
    <#if action_has_next>,</#if>
  </#list>
  ],
  "downloads": [
  <#list downloads as download>
    {
      "label": "${download.label}",
      "url": "${download.url}",
      "type": "${download.type}",
      "name": "${download.name}"
    }
    <#if download_has_next>,</#if>
  </#list>
  ],
  "links": [
  <#list links as link>
    {
      "name": "${link.name}",
      "label": "${link.label}",
      "url": "${link.url}"
    }
    <#if link_has_next>,</#if>
  </#list>
  ]
}
