/**
 * Loads a list of process definitions
 *
 * @method getProcessDefinitions
 * @param connector
 * @return {Array} A list of process definition objects
 */
function getProcessDefinitions(connector)
{
  var result = connector.get("/process-definitions");
  if (result.status == 200)
  {
    return eval('(' + result + ')').data;
  }
  return null;
}
