<#assign el=args.htmlid?js_string/>

<div class="deployments">
  <div id="${el}-paginator"></div>
  <div id="${el}-datatable"></div>
</div>
<button type="button" id="${el}-delete">${msg("button.delete")}</button>
<button type="button" id="${el}-deleteCascade">${msg("button.deleteCascade")}</button>
<button type="button" id="${el}-upload">${msg("button.upload")}</button>

<div id="${el}-uploadDeployment">
    <div class="bd">
      <h1>${msg("dialog.uploadDeployment.header")}</h1>
      <form method="POST" action="${url.context}/proxy/activiti-rest-endpoint/deployment?format=html" class="activiti-form">
        <input type="hidden" name="success" value="top.Activiti.util.ComponentManager.get('${el}').onUploadDeploymentSuccess"/>
        <input type="hidden" name="failure" value="top.Activiti.util.ComponentManager.get('${el}').onUploadDeploymentFailure"/>
        <label>
          ${msg("dialog.uploadDeployment.deployment")}:<br/>
          <input type="file" name="deployment" tabindex="0" />
        </label>
      </form>
    </div>
</div>

<script type="text/javascript">//<![CDATA[
   new Activiti.component.Deployments("${el}").setMessages(${messages});
//]]></script>