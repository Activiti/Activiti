<h2>${msg("header.status")}</h2>
<div class="section">
  <#if engine.exception??>
    <span class="status-error">${msg("label.engine-error")}</span>
    <span>${msg("label.view-details")}</span>
    <div class="hidden">
      <span>${msg("label.hide-details")}</span>
      <div>
        <pre>${engine.exception?html}</pre>
      </div>
    </div>
  <#else>
    <span class="status-ok">${msg("label.engine-ok")}</span>
  </#if>
</div>

<h2>${msg("header.information")}</h2>
<div class="section">
  <span class="label">${msg("label.name")}:</span>${engine.name?html}<br/>
  <span class="label">${msg("label.version")}:</span>${engine.version?html}<br/>
  <span class="label">${msg("label.resource-url")}:</span>${engine.resourceUrl?html}<br/>
</div>
