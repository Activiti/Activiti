<?
$id = $_REQUEST["id"];
if (isset($id)) {
	$id = str_replace(":", "-", $id);
	
	if (isset($_REQUEST["callback"])) {
		echo $_REQUEST["callback"]."(";
		include("./process-definition/$id.json");
		echo ")";
	} else {
		include("./process-definition/$id.json");
	}
}
	
?>