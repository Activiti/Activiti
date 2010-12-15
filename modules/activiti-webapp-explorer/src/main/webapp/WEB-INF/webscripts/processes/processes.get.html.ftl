<#assign el=args.htmlid?js_string/>
<div class="processes">
  <div id="${el}-paginator"></div>
  <div id="${el}-datatable"></div>
</div>

<script type="text/javascript">//<![CDATA[
new Activiti.component.Processes("${el}").setMessages(${messages});
//]]></script>