<ul class="activiti-menu pages">
<#list pages as p>
  <li>
    <a href="${url.context}/${p.id?js_string}" class="<#if context.page.id == p.id>current<#else>normal</#if> <#if !p_has_next>last</#if>">${p.title?html}</a>
  </li>
</#list>
