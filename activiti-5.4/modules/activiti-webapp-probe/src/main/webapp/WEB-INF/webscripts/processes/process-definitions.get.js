function getProcesses(connector)
{
  var result = connector.get("/process-definitions");
  if (result.status == 200)
  {
    return eval('(' + result + ')').data;
  }
  return null;
}
model.processes = getProcesses(remote.connect());
