<#assign el=args.htmlid/>

<div class="deployments">
  <div id="${el?html}-paginator"></div>
  <div id="${el?html}-datatable"></div>
</div>
<button type="button" id="${el}-delete">${msg("deployments.delete")}</button>
<button type="button" id="${el}-deleteCascade">${msg("deployments.deleteCascade")}</button>


<script type="text/javascript">//<![CDATA[
   new Activiti.component.Deployments("${el?js_string}").setMessages(${messages});
//]]></script>