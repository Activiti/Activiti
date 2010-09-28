<#if imageUrl??>
	<div id="artifact-image"><img id="${id}" src="${imageUrl}" border=0></img></div>
<#else>
	<div id="artifact-source"><pre id="${id}" class="prettyprint lang-${id}" >${content?html}</pre></div>
</#if>