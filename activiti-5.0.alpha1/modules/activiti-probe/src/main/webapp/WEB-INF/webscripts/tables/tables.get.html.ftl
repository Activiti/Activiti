<div class="section">
  <table>
    <thead>
      <tr>
        <th>${msg("header.table-name")}</th>
        <th class="number">${msg("header.no-of-results")}</th>
      </tr>
    </thead>
    <tbody>
    <#list tables as table>
      <#assign rowClass = (table_index % 2 == 0)?string("even","odd")/>
      <tr class="${rowClass}">
        <td>${table.tableName}</td>
        <td class="number">${table.noOfResults}</td>
      </tr>
    </#list>
    </tbody>
  </table>
  <br/>
</div>