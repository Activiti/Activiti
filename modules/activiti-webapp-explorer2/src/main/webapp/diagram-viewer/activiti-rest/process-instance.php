<?
$id = $_REQUEST["id"];
if (isset($id)) {
	$id = str_replace(":", "-", $id);
	
	if (isset($_REQUEST["callback"])) {
		echo $_REQUEST["callback"]."(";
		include("./processinstance-highlights/$id.json");	
		echo ")";
	} else {
		include("./processinstance-highlights/$id.json");	
	}
}
?>