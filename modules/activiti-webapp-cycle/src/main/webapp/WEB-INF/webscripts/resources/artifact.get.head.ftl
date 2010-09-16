<script type="text/javascript" src="${url.context}/res/components/artifact/artifact.js"></script>
<link rel="stylesheet" type="text/css" href="${url.context}/res/components/artifact/artifact.css">

<!- MOVI API from Oryx -->
<script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/yuiloader/yuiloader-min.js"></script>
<script type="text/javascript" src="${url.context}/res/movi/movi-min.js"></script>
<link rel="stylesheet" type="text/css" href="${url.context}/res/movi/style/movi.css" />

<script type="text/javascript">
//<![CDATA[
MOVI.init = function(callback, moviBase, yuiReadyCallback, yuiModules) {
  var _YUI_BASE_DIR = "http://yui.yahooapis.com/2.7.0/build/";
  if(!YAHOO.lang.isFunction(callback)) callback = function(){};
  if(!YAHOO.lang.isFunction(yuiReadyCallback)) yuiReadyCallback = undefined;
  if(!YAHOO.lang.isArray(yuiModules)) yuiModules = [];

  // load required YUI modules
  var yuiLoader = new YAHOO.util.YUILoader({
	require: yuiModules.concat(["yahoo", "dom", "element", "get", "event", "logger", "slider", "container"]), 
	base: _YUI_BASE_DIR,
	loadOptional: true,
	filter: "RAW",
	onSuccess: function() {
	  if(yuiReadyCallback)
		yuiReadyCallback();
			
	  //YAHOO.widget.Logger.enableBrowserConsole();  // remove this line for production use 
	  	
	  if(!YAHOO.lang.isString(moviBase)) {
		throw new Error("MOVI base directory is not specified.", "movi.js");
		return false;
	  }
	  	
	  YAHOO.util.Get.css(moviBase + "/style/movi.css", {});
	  if (YAHOO.env.ua.ie > 0) { 
	  // load custom stylesheets for IE
		YAHOO.util.Get.css(moviBase + "/style/movi_ie.css", {});
	  }
	  // execute callback
      callback();
	},
	onFailure: function(msg, xhrobj) { 
	  var m = "Unable to load YUI modules from base dir '" + _YUI_BASE_DIR + "': " + msg; 
		if (xhrobj) { 
		  m += ", " + YAHOO.lang.dump(xhrobj); 
		} 
		throw new Error(m, "movi.js"); 
	}
	});
  yuiLoader.insert();
};
//]]></script>

<!-- JavaScript and css for xml syntax highlighting -->
<script type="text/javascript" src="${url.context}/res/js/prettify/prettify.js"></script>
<link rel="stylesheet" type="text/css" href="${url.context}/res/js/prettify/prettify.css">
