<#assign el=args.htmlid?js_string/>
<div class="task-list">
  <div id="${el}-paginator"></div>
  <h1>${msg("header")}</h1>
  <div id="${el}-datatable"></div>
</div>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.TaskList("${el}").setMessages(${messages});
//]]></script>

