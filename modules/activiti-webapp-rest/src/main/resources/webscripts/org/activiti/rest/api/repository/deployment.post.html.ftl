<html>
<script type="text/javascript">
  <#if failure??>
    ${failure}(<#if error??>'${error?js_string}'<#else>null</#if>);
  <#elseif success??>
    ${success}();
  </#if>
</script>
</html>
