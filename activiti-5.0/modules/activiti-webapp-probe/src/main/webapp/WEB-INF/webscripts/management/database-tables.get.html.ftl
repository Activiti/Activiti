<ul>
  <#list tables as table>
  <li class="table">
    <a href="#" rel="table=${table.tableName}" title="${table.tableName}">${table.tableName}</a> <em class="table-${table.tableName}">(${table.total})</em>
  </li>
  </#list>
</ul>
<script type="text/javascript">//<![CDATA[
   new Activiti.component.DatabaseTables("${args.htmlid?js_string}").setMessages(${messages});
//]]></script>
