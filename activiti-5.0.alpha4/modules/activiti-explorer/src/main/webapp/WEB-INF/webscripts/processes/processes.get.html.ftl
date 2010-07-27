<#assign el=args.htmlid/>
<div id="processesContainer">
	<table id="processesTable">
	   <thead>
	   	<tr>
		      <th>Name</th>
		      <th>ID</th>
		      <th>Key</th>
		      <th>Version</th>
				<th>Actions</th>
			</tr>
	   </thead>
	   
	   <tbody>
	      <#list processDefinitions as processDefinition>
	         <tr>
                <td>${processDefinition.name}</td>
                <td>${processDefinition.id}</td>
                <td>${processDefinition.key}</td>
                <td>${processDefinition.version}</td>
					 <td>
					 	<a href="#start?id=${processDefinition.id}" class="startProcess">Start</a> 
					   <#-- <a href="#view?id=${processDefinition.id}" class="viewProcess">View</a> -->
					 </td>
	         </tr>
	      </#list>
	   </tbody>
	</table>
</div>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.Processes("${el}").setMessages(${messages});
//]]></script>