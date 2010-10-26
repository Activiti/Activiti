/**
 * Loads the tables in the engine from the server
 *
 * @method getTables
 * @param connector
 * @return {Array} A list with table objects
 */
function getTables(connector)
{
  var result = connector.get("/management/tables");
  if (result.status == 200)
  {
    return eval('(' + result + ')').data;
  }
  return null;
}

/**
 * Gets a list of the Deployments from the server
 *
 * @method getDeployments
 * @param connector
 * @return {Array} A list with deployment objects
 */
function getDeployments(connector)

{
	var result = connector.get("/management/deployments");
	if (result.status == 200) 
	{
		return eval('(' + result + ')').data;
	}
	return null;
}
