<#assign el=args.htmlid/>
<div id="processesContainer">
	<table id="processesTable">
	   <thead>
	   	<tr>
		      <th>${msg("processes.name")}</th>
		      <th>${msg("processes.key")}</th>
		      <th>${msg("processes.version")}</th>
				<th>${msg("processes.actions")}</th>
			</tr>
	   </thead>
	   
	   <tbody>
	      <#list processDefinitions as processDefinition>
	         <tr>
                <td>${processDefinition.name!processDefinition.id}</td>
                <td>${processDefinition.key}</td>
                <td>${processDefinition.version}</td>
					 <td>
   				   <a href="#start?id=${processDefinition.id}" class="processAction startProcess">
							<#if processDefinition.startFormResourceKey??>
							  ${msg("processes.startForm")}
							<#else>
							  ${msg("processes.start")}
							</#if>
						</a>
					 </td>
	         </tr>
	      </#list>
	   </tbody>
	</table>
</div>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.Processes("${el}").setMessages(${messages});
//]]></script>