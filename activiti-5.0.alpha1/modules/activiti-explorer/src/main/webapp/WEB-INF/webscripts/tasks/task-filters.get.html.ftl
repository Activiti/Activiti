<ul>
  <li class="filter">
    <a href="#" rel="assignee=${user.id}" title="${msg("tooltip.filter.assigned-tasks")}">${msg("filter.assigned-tasks")}</a> <em class="assigned"></em>
  </li>
  <li class="filter">
    <a href="#" rel="candidate=${user.id}" title="${msg("tooltip.filter.unassigned-tasks")}">${msg("filter.unassigned-tasks")}</a> <em class="unassigned"></em>
    <ul>
      <#list user.properties.assignmentGroups?keys as groupId>
      <li class="filter">
        <span class="label">${msg("label.in")}</span><a href="#" rel="candidate-group=${groupId}" title="${msg("tooltip.filter.unassigned-tasks.group", user.properties.assignmentGroups[groupId])}">${user.properties.assignmentGroups[groupId]?html}</a> <em class="unassigned-group-${groupId}"></em>
      </li>
      </#list>
    </ul>
  </li>
</ul>
<script type="text/javascript">//<![CDATA[
   new Activiti.component.TaskFilters("${args.htmlid}").setMessages(${messages});   
//]]></script>
