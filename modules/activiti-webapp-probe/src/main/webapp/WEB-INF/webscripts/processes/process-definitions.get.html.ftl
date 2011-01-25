<ul>
  <#list processes as process>
  <li class="table">
    <a href="#" rel="processId=${process.id}&diagram=${process.graphicNotationDefined?string}" title="${process.name}">${process.name}</a><sup class="version">${msg("label.version", process.version)}</sup>
  </li>
  </#list>
</ul>
<script type="text/javascript">//<![CDATA[
   new Activiti.component.Processes("${args.htmlid?js_string}").setMessages(${messages});
//]]></script>
