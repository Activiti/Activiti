<#include "activiti.template.lib.ftl" />
<@templateHeader/>
<@templateBody>
  <div id="header">
    <@region id="header" scope="template"/>
  </div>
  <div id="navigation">
    <@region id="navigation" scope="template"/>
  </div>
  <div id="content" class="yui-gf">
    <div id="left" class="yui-u first">
      <@region id="left1" scope="page"/>
      <@region id="left2" scope="page"/>
      <@region id="left3" scope="page"/>
    </div>
    <div id="main" class="yui-u">
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
