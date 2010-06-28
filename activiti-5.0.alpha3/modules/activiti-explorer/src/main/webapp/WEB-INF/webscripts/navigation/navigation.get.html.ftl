<#assign el=args.htmlid/>
<ul class="activiti-menu actions">
  <li>
    <button id="${el}-startProcess-button" tabindex="0">${msg("button.start-process")}</button>
    <select id="${el}-startProcess-menu">
       <#list processDefinitions as processDefinition>
       <option value="${processDefinition.id!""}">${processDefinition.name!processDefinition.id}</option>
       </#list>
    </select>
  </li>
</ul>
<ul class="activiti-menu pages">
<#list pages as p>
  <li>
    <a href="${url.context}/${p.id}" class="<#if context.page.id == p.id>current<#else>normal</#if> <#if !p_has_next>last</#if>">${p.title}</a>
  </li>
</#list>
</ul>
<script type="text/javascript">//<![CDATA[
   new Activiti.component.Navigation("${el}").setMessages(${messages});
//]]></script>
