<#assign el=args.htmlid/>

<div class="jobs">
  <div id="${el?html}-paginator"></div>
  <div id="${el?html}-datatable"></div>
</div>

<button type="button" id="${el}-execute">${msg("jobs.button.execute-selected")}</button>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.Jobs("${el?js_string}").setMessages(${messages});
//]]></script>