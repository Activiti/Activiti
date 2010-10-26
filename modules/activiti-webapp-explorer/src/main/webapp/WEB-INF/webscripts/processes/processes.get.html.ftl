<#assign el=args.htmlid/>
<div class="processes">
  <div id="${el}-paginator"></div>
  <div id="${el}-datatable"></div>
</div>

<script type="text/javascript">//<![CDATA[
new Activiti.component.Processes("${el?js_string}").setMessages(${messages});
//]]></script>