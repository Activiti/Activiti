<#assign el=args.htmlid/>
<div class="task-list">
  <div id="${el}-paginator"></div>
  <h1>${msg("header")}</h1>
  <div id="${el}-datatable"></div>
</div>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.TaskList("${args.htmlid}").setMessages(${messages});   
//]]></script>

