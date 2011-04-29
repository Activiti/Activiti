<#assign el=args.htmlid?js_string/>
<div id="${el}-containers" class="database-table">
</div>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.DatabaseTable("${el}").setMessages(${messages});
//]]></script>

