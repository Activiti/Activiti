function main()
{
  var connector = remote.connect();

  // Load process engine info
  result = connector.get("/process-engine");
  if (result.status == 200)
  {
    model.engine = eval('(' + result + ')');
  }
}

main();