<ul>
  <li class="filter">
    <a href="#" rel="assignee=${user.id?js_string}" title="${msg("label.filter.assignee")}">${msg("label.filter.assignee")}</a> <em class="assigned"></em>
  </li>
  <li class="filter">
    <a href="#" rel="candidate=${user.id?js_string}" title="${msg("label.filter.unassigned-tasks")}">${msg("label.filter.candidate")}</a> <em class="unassigned"></em>
    <ul>
      <#list user.properties.assignmentGroups?keys?sort as groupId>
      <li class="filter">
        <span class="label">${msg("label.in")}</span><a href="#" rel="candidate-group=${groupId?js_string}" title="${msg("label.filter.candidate-group", user.properties.assignmentGroups[groupId]?js_string)}">${user.properties.assignmentGroups[groupId]?html}</a> <em class="unassigned-group-${groupId?js_string}"></em>
      </li>
      </#list>
    </ul>
  </li>
</ul>
<script type="text/javascript">//<![CDATA[
   new Activiti.component.TaskFilters("${args.htmlid?js_string}").setMessages(${messages});   
//]]></script>
