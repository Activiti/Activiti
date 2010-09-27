<#attempt>
	<div id="artifact-image"><img id="${id}" src="${imageUrl}" border=0></img></div>
<#recover>
	<div id="artifact-source"><pre id="${id}" class="prettyprint lang-${id}" >${content?html}</pre></div>
	<script type="text/javascript">//<![CDATA[
		prettyPrint();
	//]]></script>
</#attempt>