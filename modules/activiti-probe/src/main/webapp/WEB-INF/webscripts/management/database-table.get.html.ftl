<#assign el=args.htmlid/>
<div id="${el}-containers" class="database-table">
</div>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.DatabaseTable("${args.htmlid}").setMessages(${messages});
//]]></script>

