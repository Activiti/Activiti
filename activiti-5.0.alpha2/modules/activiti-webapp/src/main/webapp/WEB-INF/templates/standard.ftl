<#include "activiti.template.lib.ftl" />
<@templateHeader/>
<@templateBody>
  <div id="header">
    <@region id="header" scope="template"/>
  </div>
  <div id="navigation">
    <@region id="navigation" scope="template"/>
  </div>
  <div id="content">
    <div id="main">
      <h1>${page.title}</h1>
      <@region id="main1" scope="page"/>
      <@region id="main2" scope="page"/>
      <@region id="main3" scope="page"/>
    </div>
  </div>
</@>
<@templateFooter>
  <div id="footer">
    <@region id="footer" scope="template"/>
  </div>
</@>
