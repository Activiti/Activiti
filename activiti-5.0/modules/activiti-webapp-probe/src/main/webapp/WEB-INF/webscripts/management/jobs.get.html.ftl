<#assign el=args.htmlid?js_string/>

<div class="jobs">
  <div id="${el}-paginator"></div>
  <div id="${el}-datatable"></div>
</div>

<button type="button" id="${el}-execute">${msg("jobs.button.execute-selected")}</button>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.Jobs("${el}").setMessages(${messages});
//]]></script>