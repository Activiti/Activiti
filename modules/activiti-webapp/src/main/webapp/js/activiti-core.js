/**
 * YUI Library aliases
 * Deliberately named differently to the ones various components and modules use, to avoid unexpected behaviour.
 */
var YUIDom = YAHOO.util.Dom,
    YUIEvent = YAHOO.util.Event,
    YUISelector = YAHOO.util.Selector;

/**
 * Activiti root namespace.
 *
 * @namespace Activiti
 */
// Ensure Activiti root object exists
if (typeof Activiti == "undefined" || !Activiti)
{
  var Activiti = {};
}

/**
 * Activiti top-level constants namespace.
 *
 * @namespace Activiti
 * @class Activiti.constants
 */
Activiti.constants = Activiti.constants || {};

/**
 * Activiti top-level component namespace.
 *
 * @namespace Activiti
 * @class Activiti.component
 */
Activiti.component = Activiti.component || {};

/**
 * Activiti top-level i18n namespace.
 *
 * @namespace Activiti
 * @class Activiti.i18n
 */
Activiti.i18n = Activiti.i18n || {};

/**
 * Activiti top-level util namespace.
 *
 * @namespace Activiti
 * @class Activiti.util
 */
Activiti.util = Activiti.util || {};

/**
 * Activiti top-level thirdparty namespace.
 * Used for importing third party javascript functions
 *
 * @namespace Activiti
 * @class Activiti.thirdparty
 */
Activiti.thirdparty = Activiti.thirdparty || {};

/**
 * Activit top-level support namespace.
 * Used for storing feature support flags
 * @namespace Activiti
 * @class Activiti.support
 */

Activiti.support = Activiti.support || {}

/**
 * Copies the values in an object into a new instance.
 *
 * Note 1. This method ONLY copy values of type object, array, date, boolean, string or number.
 * Note 2. Functions are not copied.
 * Note 3. Objects with a constructor other than of type Object are still in the result but aren't copied.
 *         This means that objects of HTMLElements or window will be in the result but will not be copied and hence
 *         shared between p_obj and the returned copy og p_obj.
 *
 * @method Activiti.util.deepCopy
 * @param p_obj {object|array|date|string|number|boolean} The object to copy
 * @param p_oInstructions {object} (Optional) Contains special non default copy instructions
 * @param p_oInstructions.copyFunctions {boolean} (Optional) false by default
 * @return {object|array|date|string|number|boolean} A new instance of the same type as o with the same values
 * @static
 */
Activiti.util.deepCopy = function(p_oObj, p_oInstructions)
{
  if (!p_oObj)
  {
    return p_oObj;
  }
  if (!p_oInstructions)
  {
    p_oInstructions = {};
  }

  if (YAHOO.lang.isArray(p_oObj))
  {
    var arr = [];
    for (var i = 0, il = p_oObj.length, arrVal; i < il; i++)
    {
      arrVal = p_oObj[i];
      if (!YAHOO.lang.isFunction(arrVal) || p_oInstructions.copyFunctions == true)
      {
        arr.push(Activiti.util.deepCopy(arrVal, p_oInstructions));
      }
    }
    return arr;
  }

  if (Activiti.util.isDate(p_oObj))
  {
    return new Date(p_oObj.getTime());
  }

  if (YAHOO.lang.isString(p_oObj) || YAHOO.lang.isNumber(p_oObj) || YAHOO.lang.isBoolean(p_oObj))
  {
    return p_oObj;
  }

  if (YAHOO.lang.isObject(p_oObj))
  {
    if (p_oObj.toString() == "[object Object]")
    {
      var obj = {}, objVal;
      for (var name in p_oObj)
      {
        if (p_oObj.hasOwnProperty(name))
        {
          objVal = p_oObj[name];
          if (!YAHOO.lang.isFunction(objVal) || p_oInstructions.copyFunctions == true)
          {
            obj[name] = Activiti.util.deepCopy(objVal, p_oInstructions);
          }
        }
      }
      return obj;
    }
    else
    {
      // The object was
      return p_oObj;
    }
  }

  return null;
};

/**
 * Tests if o is of type date
 *
 * @method Activiti.util.isDate
 * @param o {object} The object to test
 * @return {boolean} True if o is of type date
 * @static
 */
Activiti.util.isDate = function(o)
{
  return o.constructor && o.constructor.toString().indexOf("Date") != -1;
};

/**
 * Creates an object from an argument string, i.e. "firstName=John&lastName=Doe"
 *
 * @method Activiti.util.argumentStringToObject
 * @param str {string} Argument string to create an object from
 * @param validArguments {Array} (Optional) An array that contains a list of valid arguments
 * @return {object} An object containing the arguments as attributes
 * @static
 */
Activiti.util.argumentStringToObject = function(str, validArguments) {
  var obj = {},
    argumentPairs = str ? str.split("&") : [],
    argumentPair;
  for (var i = 0, il = argumentPairs.length; i < il; i++) {
    argumentPair = argumentPairs[i].split("=");
    if (argumentPair.length == 2 && (!validArguments || Activiti.util.arrayContains(validArguments, argumentPair[0])))
    {
      obj[argumentPair[0]] = decodeURIComponent(argumentPair[1]);
    }
  }
  return obj;
};

/**
 * Creates an argument string, i.e. "firstName=John&lastName=Doe", from an object
 *
 * @method Activiti.util.objectToArgumentString
 * @param obj {string} Obejct to create an argument string from
 * @param validArguments {Array} (Optional) An array that contains a list of valid arguments
 * @return {string} A string containing the attributes as url arguments
 * @static
 */
Activiti.util.objectToArgumentString = function(obj, validArguments) {
  var str;
  for (var attr in obj) {
    if (obj.hasOwnProperty(attr) && obj[attr] != undefined  && obj[attr] != null && (!validArguments || Activiti.util.arrayContains(validArguments, attr)))
    {
      str = str ? str + "&" : "";
      str += encodeURIComponent(attr) + "=" + encodeURIComponent(obj[attr]);
    }
  }
  return str;
};

/**
 * Finds the index of an object in an array
 *
 * @method Activiti.util.arrayIndex
 * @param arr {array} Array to search in
 * @param el {object} The element to find the index for in the array
 * @return {integer} -1 if not found, other wise the index
 * @static
 */
Activiti.util.arrayIndex = function(arr, el)
{
   if (arr)
   {
      for (var i = 0, ii = arr.length; i < ii; i++)
      {
          if (arr[i] == el)
          {
             return i;
          }
      }
   }
   return -1;
};

/**
 * Check if an array contains an object
 * @method Activiti.util.arrayContains
 * @param arr {array} Array to convert to object
 * @param el {object} The element to be searched for in the array
 * @return {boolean} True if arr contains el
 * @static
 */
Activiti.util.arrayContains = function(arr, el)
{
  return Activiti.util.arrayIndex(arr, el) !== -1;
};

/**
 * Convert an array into an object
 *
 * @method Activiti.util.arrayToObject
 * @param arr {array} Array to convert to object
 * @param p_value {object} Optional: default value for property.
 * @return {object} Object conversion of array
 * @static
 */
Activiti.util.arrayToObject = function(arr, p_value)
{
  var obj = {},
      value = (p_value !== undefined ? p_value : true);

  if (YAHOO.lang.isArray(arr))
  {
    for (var i = 0, ii = arr.length; i < ii; i++)
    {
      if (arr[i] !== undefined)
      {
        obj[arr[i]] = value;
      }
    }
  }
  return obj;
};

/**
 * Decodes an HTML-encoded string
 * Replaces &lt; &gt; and &amp; entities with their character equivalents
 *
 * @method Activiti.util.decodeHTML
 * @param html {string} The string containing HTML entities
 * @return {string} Decoded string
 * @static
 */
Activiti.util.decodeHTML = function(html)
{
  if (html)
  {
    return html.split("&lt;").join("<").split("&gt;").join(">").split("&amp;").join("&").split("&quot;").join('"');
  }
  return "";
};

/**
 * Encodes a potentially unsafe string with HTML entities
 * Replaces <pre><, >, &</pre> characters with their entity equivalents.
 * Based on the equivalent encodeHTML and unencodeHTML functions in Prototype.
 *
 * @method Activiti.util.encodeHTML
 * @param text {string} The string to be encoded
 * @param justified {boolean} If true, don't render lines 2..n with an indent
 * @return {string} Safe HTML string
 * @static
 */
Activiti.util.encodeHTML = function(text, justified)
{
  if (text === null || typeof text == "undefined")
  {
    return "";
  }

  var indent = justified === true ? "" : "&nbsp;&nbsp;&nbsp;";

  if (YAHOO.env.ua.ie > 0)
  {
    text = "" + text;
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "<br />" + indent);
  }
  var me = arguments.callee;
  me.text.data = text;
  return me.div.innerHTML.replace(/\n/g, "<br />" + indent).replace('"','&quot;');
};
Activiti.util.encodeHTML.div = document.createElement("div");
Activiti.util.encodeHTML.text = document.createTextNode("");
Activiti.util.encodeHTML.div.appendChild(Activiti.util.encodeHTML.text);

/**
 * Encodes a folder path string for use in a REST URI.
 * First performs a encodeURI() pass so that the '/' character is maintained
 * as the path must be intact as URI elements. Then encodes further characters
 * on the path that would cause problems in URLs, such as '&', '=' and '#'.
 *
 * @method Activiti.util.encodeURIPath
 * @param text {string} The string to be encoded
 * @return {string} Encoded path URI string.
 * @static
 */
Activiti.util.encodeURIPath = function(text)
{
  return encodeURI(text).replace(/#/g, "%23").replace(/&/g, "%26").replace(/\=/g, "%3D");
};

/**
 * Returns the value of the specified query string parameter.
 *
 * @method getQueryStringParameter
 * @param {string} paramName Name of the parameter we want to look up.
 * @param {string} queryString Optional URL to look at. If not specified,
 *     this method uses the URL in the address bar.
 * @return {string} The value of the specified parameter, or null.
 * @static
 */
Activiti.util.getQueryStringParameter = function(paramName, url)
{
    var params = this.getQueryStringParameters(url);
    
    if (paramName in params)
    {
       return params[paramName];
    }

    return null;
};

/**
 * Returns the query string parameters as an object literal.
 * Parameters appearing more than once are returned an an array.
 * This method has been extracted from the YUI Browser History Manager.
 * It can be used here without the overhead of the History JavaScript include.
 *
 * @method getQueryStringParameters
 * @param queryString {string} Optional URL to look at. If not specified,
 *     this method uses the URL in the address bar.
 * @return {object} Object literal containing QueryString parameters as name/value pairs
 * @static
 */
Activiti.util.getQueryStringParameters = function(url)
{
   var i, len, idx, queryString, params, tokens, name, value, objParams;

   url = url || window.location.href;

   idx = url.indexOf("?");
   queryString = idx >= 0 ? url.substr(idx + 1) : url;

   // Remove the hash if any
   idx = queryString.lastIndexOf("#");
   queryString = idx >= 0 ? queryString.substr(0, idx) : queryString;

   params = queryString.split("&");

   objParams = {};

   for (i = 0, len = params.length; i < len; i++)
   {
      tokens = params[i].split("=");
      if (tokens.length >= 2)
      {
         name = tokens[0];
         value = decodeURIComponent(tokens[1]);
         switch (typeof objParams[name])
         {
            case "undefined":
               objParams[name] = value;
               break;

            case "string":
               objParams[name] = [objParams[name]].concat(value);
               break;

            case "object":
               objParams[name] = objParams[name].concat(value);
               break;
         }
      }
   }

   return objParams;
};

/**
 * Returns a unique DOM ID for dynamically-created content. Optionally applies the new ID to an element.
 *
 * @method Activiti.util.generateDomId
 * @param p_el {HTMLElement} Optional Applies new ID to element
 * @param p_prefix {string} Optional prefix instead of "act-id" default
 * @return {string} Dom Id guaranteed to be unique on the current page
 */
Activiti.util.generateDomId = function(p_el, p_prefix)
{
  var domId, prefix = p_prefix || "act-id";
  do
  {
    domId = prefix + Activiti.util.generateDomId._nId++;
  } while (YUIDom.get(domId) !== null);

  if (p_el) { 
   Activiti.util.setDomId(p_el, domId);
  }

  return domId;
};
Activiti.util.generateDomId._nId = 0;

/**
 * Returns true if obj matches all attributes and their values in pattern.
 * Attribute values in pattern may contain wildcards ("*").
 *
 * @method objectMatchesPattern
 * @param obj {object} The object to match pattern against
 * @param pattern {object} An object with attributes to match against obj
 * @return {boolean} true if obj matches pattern, false otherwise
 */
Activiti.util.objectMatchesPattern = function(obj, pattern)
{
   for (var attrName in pattern)
   {
      if (pattern.hasOwnProperty(attrName) &&
          (!pattern.hasOwnProperty(attrName) || (obj[attrName] != pattern[attrName] && pattern[attrName] != "*")))
      {
         return false;
      }
   }
   return true;
};

/**
 * Sets the domId as the html dom id on el
 *
 * @method Activiti.util.setDomId
 * @param p_el {HTMLElement} Applies new ID to element
 * @param p_domId {string} The dom id to apply
 */
Activiti.util.setDomId = function(p_el, p_domId)
{
  if (p_el)
  {
    if (YAHOO.env.ua.ie > 0 && YAHOO.env.ua.ie < 8)
    {
      // MSIE 6 & 7-safe method
      p_el.attributes["id"].value = p_domId;
    }
    else
    {
      p_el.setAttribute("id", p_domId);
    }
  }
};

/**
 * Contains the name of the parameters to use for pagination
 */
Activiti.util.Pagination = function() {
  var p = {
    START: "start",
    SIZE: "size",
    SORT: "sort",
    ORDER: "order",
    defaults: {
      start: 0,
      size: 10,
      order: "asc"
    }
  };
  p.list = function () {
    return [p.START, p.SIZE, p.SORT, p.ORDER];
  };
  p.get = function (obj) {
    return YAHOO.lang.merge(Activiti.util.deepCopy(p.defaults), obj);
  };
  p.args = function (obj) {
    return Activiti.util.objectToArgumentString(this.get(obj), p.list());
  };
  return p;
}();

/**
 * It check the entered parameters to ensure they're a valid date
 *
 * @method Activiti.util.validDate
 * @param year {Number| String} YYYY | ISO 8601 string
 * @param month {Number} MM 
 * @param day {Number} DD
 * @static
 */
Activiti.util.validDate = function(year, month, day) 
{
	var dateObj, dates;
	if (typeof(year) === "string") 
	{
		dateObj = Activiti.thirdparty.fromISO8601(year);
		dates = year.split("-"); 
		year = parseInt(dates[0], 10);
		month = parseInt(dates[1], 10);
		day = parseInt(dates[2], 10);
	} else 
	{
		dateObj = new Date(year, month, day); 
	}
	return (dateObj !== null && (dateObj.getFullYear() == year && dateObj.getMonth() + 1 == month && dateObj.getDate() == day));
}

/**
 * Adds a leading zero to a number if required
 * 
 * @method Activiti.util.zeroPad
 * @param {Number} value
 */

Activiti.util.zeroPad = function(value)
   {
      return (value < 10) ? '0' + value : value;
   };

/**
 * Removes selectClass from all elements in els but adds it to el.
 *
 * @method Activiti.util.toggleClass
 * @param els {Array} The elements to deselect
 * @param toggleEl {HTMLElement} The element to select
 * @param cssClass {string} The css class to remove from unselected and add to the selected element
 * @static
 */
Activiti.util.toggleClass = function(els, toggleEl, cssClass)
{
  for (var el, i = 0, il = els.length; i < il; i++)
  {
    el = els[i];
    if (el != toggleEl)
    {
      YUIDom.removeClass(el, cssClass);
    }
  }
  YUIDom.addClass(toggleEl, cssClass);
};

/**
 * Add a component's messages to the central message store.
 *
 * @method Activiti.util.addMessages
 * @param p_obj {object} Object literal containing messages in the correct locale
 * @param p_messageScope {string} Message scope to add these to, e.g. componentId
 * @return {boolean} true if messages added
 * @throws {Error}
 * @static
 */
Activiti.i18n.addMessages = function(p_obj, p_messageScope)
{
  if (p_messageScope === undefined)
  {
    throw new Error("messageScope must be defined");
  }
  else if (p_messageScope == "global")
  {
    throw new Error("messageScope cannot be 'global'");
  }
  else
  {
    SpringSurf.messages.scope[p_messageScope] = YAHOO.lang.merge(SpringSurf.messages.scope[p_messageScope] || {}, p_obj);
    return true;
  }
  // for completeness...
  return false;
};

/**
 * Resolve a messageId into a message.
 * If a messageScope is supplied, that container will be searched first
 * followed by the "global" message scope.
 *
 * @method Activiti.util.getMessage
 * @param p_messageId {string} Message id to resolve
 * @param p_messageScope {string} Message scope, e.g. componentId
 * @param multiple-values {string} Values to replace tokens with
 * @return {string} The localized message string or the messageId if not found
 * @throws {Error}
 * @static
 */
Activiti.i18n.getMessage = function(p_messageId, p_messageScope)
{
  var msg = p_messageId,
      global = false;;

  if (typeof p_messageId != "string")
  {
    throw new Error("Missing or invalid argument: messageId");
  }

  var globalMsg = SpringSurf.messages.global ? SpringSurf.messages.global[p_messageId] : null;
  if (typeof globalMsg == "string")
  {
    msg = globalMsg;
    global = true;
  }
  
  if ((typeof p_messageScope == "string") && (typeof SpringSurf.messages.scope[p_messageScope] == "object"))
  {
    var scopeMsg = SpringSurf.messages.scope[p_messageScope][p_messageId];
    if (typeof scopeMsg == "string")
    {
      msg = scopeMsg;
    }
  }

  // Search/replace tokens
  var tokens;
  if ((arguments.length == 3) && (typeof arguments[2] == "object"))
  {
    tokens = arguments[2];
  }
  else
  {
    tokens = Array.prototype.slice.call(arguments).slice(global ? 1 : 2);
  }
  msg = YAHOO.lang.substitute(msg, tokens);

  return msg;
};

/**
 * Keeps track of Activiti components on a page. Components should register() upon creation to be compliant.
 * @class Activiti.util.ComponentManager
 */
Activiti.util.ComponentManager = function()
{
  /**
   * Array of registered components.
   *
   * @property components
   * @type Array
   */
  var components = [];

  return (
  {
    /**
     * Main entrypoint for components wishing to register themselves with the ComponentManager
     * @method register
     * @param p_aComponent {object} Component instance to be registered
     */
    register: function CM_register(p_oComponent)
    {
      components.push(p_oComponent);
      components[p_oComponent.id] = p_oComponent;
    },

    /**
     * Unregister a component from the ComponentManager
     * @method unregister
     * @param p_aComponent {object} Component instance to be unregistered
     */
    unregister: function CM_unregister(p_oComponent)
    {
      for (var i = 0; i < components.length; i++) // Do not optimize
      {
        if (components[i] == p_oComponent)
        {
          components.splice(i, 1);
          delete components[p_oComponent.id];
          break;
        }
      }
    },

    /**
     * Re-register a component with the ComponentManager
     * Component ID cannot be updated via this function, use separate unregister(), register() calls instead.
     * @method reregister
     * @param p_aComponent {object} Component instance to be unregistered, then registered again
     */
    reregister: function CM_reregister(p_oComponent)
    {
      this.unregister(p_oComponent);
      this.register(p_oComponent);
    },

    /**
     * Allows components to find other registered components by name, id or both
     * e.g. find({name: "Activiti.DocumentLibrary"})
     * @method find
     * @param p_oParams {object} List of paramters to search by
     * @return {Array} Array of components found in the search
     */
    find: function CM_find(p_oParams)
    {
      var found = [];
      var bMatch, component;

      for (var i = 0, j = components.length; i < j; i++)
      {
        component = components[i];
        bMatch = true;
        for (var key in p_oParams)
        {
          if (p_oParams[key] != component[key])
          {
            bMatch = false;
          }
        }
        if (bMatch)
        {
          found.push(component);
        }
      }
      return found;
    },

    /**
     * Allows components to find first registered components by name only
     * e.g. findFirst("Activiti.Tasks")
     * @method findFirst
     * @param p_sName {string} Name of registered component to search on
     * @return {object|null} Component found in the search
     */
    findFirst: function CM_findFirst(p_sName)
    {
      var found = Activiti.util.ComponentManager.find(
      {
        name: p_sName
      });

      return (typeof found[0] == "object" ? found[0] : null);
    },

    /**
     * Get component by Id
     * e.g. get("template_x002e_navigation-menu")
     * @method get
     * @param p_sId {string} Id of registered component to return
     * @return {object|null} Component with given Id
     */
    get: function CM_get(p_sId)
    {
      return (components[p_sId] || null);
    }
  });
}();

/**
 * Transition methods that handles browser limitations.
 *
 * @class Activiti.util.Anim
 */
Activiti.util.Anim = function()
{
  return (
  {
    /**
     * The default attributes for a fadeIn or fadeOut call.
     *
     * @property fadeAttributes
     * @type {object} An object literal of the following form:
     * {
     *    adjustDisplay: true, // Will handle style attribute "display" in
     *                         // the appropriate way depending on if its
     *                         // fadeIn or fadeOut, default is true.
     *    callback: null,      // A function that will get called after the fade
     *    scope: this,         // The scope the callback function will get called in
     */
    fadeAttributes:
    {
      adjustDisplay: true,
      callback: null,
      scope: this
    },

    /**
     * Displays an object with opacity 0, increases the opacity during
     * 0.5 seconds for browsers supporting opcaity.
     *
     * (IE does not support opacity)
     *
     * @method fadeIn
     * @param el {HTMLElement} element to fade in
     * @param attributes
     */
    fadeIn: function A_fadeIn(el, attributes)
    {
      return this._fade(el, true, attributes);
    },

    /**
     * Displays an object with opacity 1, decreases the opacity during
     * 0.5 seconds for browsers supporting opacity and finally hides it.
     *
     * (IE does not support opacity)
     *
     * @method fadeOut
     * @param el {HTMLElement} element to fade out
     * @param attributes
     */
    fadeOut: function A_fadeOut(el, attributes)
    {
      return this._fade(el, false, attributes);
    },

    /**
     * @method _fade
     * @param el {HTMLElement} element to fade in
     * @param fadeIn {boolean} true if fadeIn false if fadeOut
     * @param attributes
     */
    _fade: function A__fade(el, fadeIn, attributes)
    {
      el = YUIDom.get(el);
      // No manadatory elements in attributes, avoid null checks below though
      attributes = YAHOO.lang.merge(this.fadeAttributes, attributes ? attributes : {});
      var adjustDisplay = attributes.adjustDisplay;

      // todo test against functionality instead of browser
      var supportsOpacity = YAHOO.env.ua.ie === 0;

      // Prepare el before fade
      if (supportsOpacity)
      {
        YUIDom.setStyle(el, "opacity", fadeIn ? 0 : 1);
      }

      // Show the element, transparent if opacity supported,
      // otherwise its visible and the "fade in" is finished
      if (supportsOpacity)
      {
        YUIDom.setStyle(el, "visibility", "visible");
      }
      else
      {
        YUIDom.setStyle(el, "visibility", fadeIn ? "visible" : "hidden");
      }

      // Make sure element is displayed
      if (adjustDisplay && YUIDom.getStyle(el, "display") === "none")
      {
        YUIDom.setStyle(el, "display", "block");
      }

      // Put variables in scope so they can be used in the callback below
      var fn = attributes.callback;
      var scope = attributes.scope;
      var myEl = el;
      if (supportsOpacity)
      {
        // Do the fade (from value/opacity has already been set above)
        var fade = new YAHOO.util.Anim(el,
        {
          opacity:
          {
            to: fadeIn ? (YAHOO.env.ua.webkit > 0 ? 0.99 : 1) : 0
          }
        }, 0.5);
        fade.onComplete.subscribe(function(e)
        {
          if (!fadeIn && adjustDisplay)
          {
            // Hide element from Dom if its a fadeOut
            YUIDom.setStyle(myEl, "display", "none");
          }
          if (fn)
          {
            // Call custom callback
            fn.call(scope ? scope : this);
          }
        });
        fade.animate();
      }
      else
      {
        if (!fadeIn && adjustDisplay)
        {
          // Hide element from Dom if its a fadeOut
          YUIDom.setStyle(myEl, "display", "none");
        }
        if (fn)
        {
          // Call custom callback
          fn.call(scope ? scope : this);
        }
      }
    },

    /**
     * Default attributes for a pulse call.
     *
     * @property pulseAttributes
     * @type {object} An object literal containing:
     *    callback: {object} Function definition for callback on complete. {fn, scope, obj}
     *    inColor: {string} Optional colour for the pulse (default is #ffffb3)
     *    outColor: {string} Optional colour to fade back to (default is original element backgroundColor)
     *    inDuration: {int} Optional time for "in" animation (default 0.2s)
     *    outDuration: {int} Optional time for "out" animation (default 1.2s)
     *    clearOnComplete: {boolean} Set to clear the backgroundColor style on pulse complete (default true)
     */
    pulseAttributes:
    {
      callback: null,
      inColor: "#ffffb3",
      inDuration: 0.2,
      outDuration: 1.2,
      clearOnComplete: true
    },

    /**
     * Pulses the background colo(u)r of an HTMLELement
     *
     * @method pulse
     * @param el {HTMLElement|string} element to fade out
     * @param attributes {object} Object literal containing optional custom values
     */
    pulse: function A_pulse(p_el, p_attributes)
    {
      // Shortcut return if animation library not loaded
      if (!YAHOO.util.ColorAnim)
      {
        return;
      }

      var el = YUIDom.get(p_el);
      if (el)
      {
        // Set outColor to existing backgroundColor
        var attr = YAHOO.lang.merge(this.pulseAttributes,
        {
          outColor: YUIDom.getStyle(el, "backgroundColor")
        });
        if (typeof p_attributes == "object")
        {
          attr = YAHOO.lang.merge(attr, p_attributes);
        }

        // The "in" animation class
        var animIn = new YAHOO.util.ColorAnim(el,
        {
          backgroundColor:
          {
            to: attr.inColor
          }
        }, attr.inDuration);

        // The "out" animation class
        var animOut = new YAHOO.util.ColorAnim(el,
        {
          backgroundColor:
          {
            to: attr.outColor
          }
        }, attr.outDuration);

        // onComplete functions
        animIn.onComplete.subscribe(function A_aI_onComplete()
        {
          animOut.animate();
        });

        animOut.onComplete.subscribe(function A_aO_onComplete()
        {
          if (attr.clearOnComplete)
          {
            YUIDom.setStyle(el, "backgroundColor", "");
          }
          if (attr.callback && (typeof attr.callback.fn == "function"))
          {
            attr.callback.fn.call(attr.callback.scope || this, attr.callback.obj);
          }
        });

        // Kick off the pulse animation
        animIn.animate();
      }
    }
  });
}();

/**
 * Helper methods for handling events
 *
 * @class Activiti.event
 */
Activiti.event = function() {
  return {

    /**
     * Remembers if all the event functoinalities dependencies have been loaded (read the browser history manager)
     *
     * @property _initialized
     * @type boolean
     * @private
     */
    _initialized: false,

    /**
     * Events that was fired before initialization was done, will be fired when browser history manager is ready.
     *
     * @property _pendingFireEventArguements
     * @type Array
     * @private
     */
    _pendingFireEventArguments: [],

    /**
     * If an event was brought in from the url it will be stored here so its info can be accessed using
     * Activiti.event.getMainEventName()  and Activiti.event.getMainEventValue()
     *
     * @property _mainEvent
     * @type object
     * @private
     */
    _mainEventDescriptor: null,

    /**
     * Shall be fired when a task filter link has been selected.
     *
     * @property selectTaskFilter
     * @type string
     */
    selectTaskFilter: "selectTaskFilter",

    /**
     * Shall be fired when a database table has been selected.
     *
     * @property selectDatabaseTable
     * @type string
     */
    selectDatabaseTable: "selectDatabaseTable",

		/**
		 * Shall be fired when a tree node is selected.
		 * 
		 * @property selectTreeLabel
		 * @type string
		 */
		
		updateArtifactView: "updateArtifactView",

		expandTreeNode: "expandTreeNode",

		collapseTreeNode: "collapseTreeNode",

    /**
     * Called from the activiti initializer when the first body element is ready.
     *
     * The event flow of the Activit webapp is:
     * 1. Head scripts are loaded and initiliased
     * 2. The first body element ("ActivitiInitilizer") is ready which calls ACtiviti.init()
     * 3. Activiti.events.init() is called which saves the init event and initialises history management
     * 4. The rest of the body elements are created
     * 5. The components' onReady method is calledonce their content is ready and they can decide if they shall wait
     *    for an event that interest them (using Activiti.events.isInitEvent) or provide default functionality.
     * 6. The Dom is ready and the init event is fired.
     * 7. Other events are fired...
     *
     * @method init
     */
    init: function() {

      var History = YAHOO.util.History,
          module = "event";

      // Create the html elements needed for history management
      var historyId = Activiti.util.generateDomId(),
          historyManagementEl = document.createElement("div"),
          str = '';
      if (YAHOO.env.ua.ie > 0) {
        str += '<iframe id="' + historyId + '-yui-history-iframe" src="' + Activiti.constants.URL_CONTEXT + 'res/yui/blank.html" style="display: none;"></iframe>';
      }
      str +='<input id="' + historyId + '-yui-history-field" type="hidden" />'
      historyManagementEl.innerHTML = str;
      document.body.appendChild(historyManagementEl);

      // See if there is an event defined in the url
      var initialState = History.getBookmarkedState(module) || "";
      this._mainEventDescriptor = this._stateToEventDescriptor(initialState);

      // Register the module
      History.register(module, initialState, Activiti.event._onHistoryEvent, null, Activiti.event);

      // Render the first view when history and dom are ready so all other components have been initialised
      History.onReady(function() {
        // Delay the call since all components might not have got their onReady event called
        YAHOO.lang.later(YAHOO.util.Event.POLL_INTERVAL * 2, this, function(){
          // Set to true so events are fired instead of added to the pending list
          Activiti.event._initialized = true;

          // Current state after BHM is initialized is the source of truth for what state to render
          var currentState = History.getCurrentState(module);
          Activiti.event._onHistoryEvent(currentState);

          for (var i = 0, il = Activiti.event._pendingFireEventArguments.length, pendingArgs; i < il; i++) {
            pendingArgs = Activiti.event._pendingFireEventArguments[i];
            Activiti.event.fire(pendingArgs[0], pendingArgs[1], pendingArgs[2], pendingArgs[3]);
          }
        });
      });

      // Initialize the Browser History Manager.
      History.initialize(historyId + "-yui-history-field", historyId + "-yui-history-iframe");
    },

    /**
     *
     * @mwthod isInitEvent
     * @param eventName {string|null} The event name to test against or null to test if no event exits
     * @return {boolean} Return true if evenName matches the
     */
    isInitEvent: function (eventName) {
      return this._mainEventDescriptor[0] == eventName;
    },

    /**
     * Fires an bubbling event, triggers url history if parameters are provided and stops the originalEvent if provided
     *
     * @method fire
     * @param event {string|event} The event name or the full event
     * @param value {object|string|int} The event value
     * @param originalEvent {object} (Optional) The event that was originally fired, that will if supplied, get stopped.
     * @param bookmark {boolean} Will if true make the event being put in the browser's address bar (note that only
     *        the event name and its top level value attributes will "survive" the process of being put in the address bar)
     */
    fire: function fire(event, value, originalEvent, bookmark) {
      if (this._initialized) {
        if (bookmark) {

          // Navigate with url history first which will make _onHistoryEvent get called so it can fire event
          var state = encodeURIComponent(event);
          for (var name in value) {
            if (value.hasOwnProperty(name)) {
              state += "|" + encodeURIComponent(name) + "|" + encodeURIComponent(value[name]);
            }
          }
          YAHOO.util.History.navigate("event", state);
        }
        else {
          // Fire event directly
          YAHOO.Bubbling.fire(event,
          {
            value: value
          });
        }
      }
      else {
        this._pendingFireEventArguments.push([event, value, originalEvent, bookmark]);
      }
      // Stop the previous event, if provided, from being used
      if (originalEvent) {
        YAHOO.util.Event.preventDefault(originalEvent);
      }
    },

    /**
     * Listens for a bubbling event
     *
     * @method onEvent
     */
    on: function fire(eventName, method, scope) {
      YAHOO.Bubbling.on(eventName, method, scope);
    },

    /**
     * Determines whether a possible owner actually is the owner of the bubbling event
     *
     * @method isOwner
     * @param possibleOwner {string} Component's id
     * @param eventArgs {object} The event's args
     * @return {boolean} false to ignore event
     */
    isOwner: function(possibleOwner, eventArgs)
    {
      return eventArgs[1].owner == possibleOwner;
    },

    /**
     * Determines whether a possible owner actually is the owner of the bubbling event
     *
     * @method getValue
     * @param eventArgs {object} The event's args
     * @return {object} The value that was sent for the event
     */
    getValue: function(eventArgs, deleteAttributes, deleteOwner)
    {
      var value = Activiti.util.deepCopy(eventArgs[1].value);
      if (deleteOwner != false && value.hasOwnProperty("owner")) {
        delete value.owner;
      }
      if (YAHOO.lang.isArray(deleteAttributes)) {
        for (var i = 0, il = deleteAttributes.length; i < il; i++) {
          if (value.hasOwnProperty(deleteAttributes[i])) {
            delete value[deleteAttributes[i]];
          }
        }
      }
      return value;
    },

    /**
     * Determines whether a possible owner actually is the owner of the bubbling event
     *
     * @method _stateToEventDescriptor
     * @param state {string} The event modules state
     * @return {Array} An event descriptor with the event name in index 0 and the event value in index 1
     * @private
     */
    _stateToEventDescriptor: function(state)
    {
      var event, value, name;
      if (state && state.length > 0) {
        var tokens = state.split("|");
        if (tokens.length > 0) {
          event = decodeURIComponent(tokens[0]);
          value = {};
          for (var i = 1, il = tokens.length; i < il;) {
            if (i < il) {
              name = decodeURIComponent(tokens[i]);
              value[name] = null;
            }
            i++;
            if (i < il) {
              value[name] = decodeURIComponent(tokens[i]);
            }
            i++;
          }
        }
      }
      return [event, value];
    },

    /**
     * Determines whether a possible owner actually is the owner of the bubbling event
     *
     * @method isOwner
     * @param state {string} The event moduels state
     * @private
     */
    _onHistoryEvent: function(state)
    {
      var eventDescriptor = this._stateToEventDescriptor(state);
      if (eventDescriptor[0]) {
        Activiti.event.fire(eventDescriptor[0], eventDescriptor[1], null, false);
      }
    }

  };
}();

/**
 * Format a date object to a user-specified mask
 * Modified to retrieve i18n strings from Activiti.messages
 *
 * Original code:
 *    Date Format 1.1
 *    (c) 2007 Steven Levithan <stevenlevithan.com>
 *    MIT license
 *    With code by Scott Trenda (Z and o flags, and enhanced brevity)
 *
 * http://blog.stevenlevithan.com/archives/date-time-format
 *
 * @method Activiti.thirdparty.dateFormat
 * @return {string}
 * @static
 */
Activiti.thirdparty.dateFormat = function()
{
  /*** dateFormat
   Accepts a date, a mask, or a date and a mask.
   Returns a formatted version of the given date.
   The date defaults to the current date/time.
   The mask defaults ``"ddd mmm d yyyy HH:MM:ss"``.
   */
  var dateFormat = function()
  {
    var   token        = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloZ]|"[^"]*"|'[^']*'/g,
        timezone     = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
        timezoneClip = /[^-+\dA-Z]/g,
        pad = function (value, length)
        {
          value = String(value);
          length = parseInt(length, 10) || 2;
          while (value.length < length)
          {
            value = "0" + value;
          }
          return value;
        };

    // Regexes and supporting functions are cached through closure
    return function (date, mask)
    {
      // Treat the first argument as a mask if it doesn't contain any numbers
      if (arguments.length == 1 &&
          (typeof date == "string" || date instanceof String) &&
          !/\d/.test(date))
      {
        mask = date;
        date = undefined;
      }

      if (typeof date == "string")
      {
        date = date.replace(".", "");
      }

      date = date ? new Date(date) : new Date();
      if (isNaN(date))
      {
        throw "invalid date";
      }

      mask = String(this.masks[mask] || mask || this.masks["default"]);

      var d = date.getDate(),
          D = date.getDay(),
          m = date.getMonth(),
          y = date.getFullYear(),
          H = date.getHours(),
          M = date.getMinutes(),
          s = date.getSeconds(),
          L = date.getMilliseconds(),
          o = date.getTimezoneOffset(),
          flags =
          {
            d:    d,
            dd:   pad(d),
            ddd:  this.i18n.dayNames[D],
            dddd: this.i18n.dayNames[D + 7],
            m:    m + 1,
            mm:   pad(m + 1),
            mmm:  this.i18n.monthNames[m],
            mmmm: this.i18n.monthNames[m + 12],
            yy:   String(y).slice(2),
            yyyy: y,
            h:    H % 12 || 12,
            hh:   pad(H % 12 || 12),
            H:    H,
            HH:   pad(H),
            M:    M,
            MM:   pad(M),
            s:    s,
            ss:   pad(s),
            l:    pad(L, 3),
            L:    pad(L > 99 ? Math.round(L / 10) : L),
            t:    H < 12 ? this.TIME_AM.charAt(0) : this.TIME_PM.charAt(0),
            tt:   H < 12 ? this.TIME_AM : this.TIME_PM,
            T:    H < 12 ? this.TIME_AM.charAt(0).toUpperCase() : this.TIME_PM.charAt(0).toUpperCase(),
            TT:   H < 12 ? this.TIME_AM.toUpperCase() : this.TIME_PM.toUpperCase(),
            Z:    (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
            o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4)
          };

      return mask.replace(token, function ($0)
      {
        return ($0 in flags) ? flags[$0] : $0.slice(1, $0.length - 1);
      });
    };
  }();

  /**
   * Activiti wrapper: delegate to wrapped code
   */
  return dateFormat.apply(arguments.callee, arguments);
};
Activiti.thirdparty.dateFormat.DAY_NAMES = (Activiti.i18n.getMessage("days.medium") + "," + Activiti.i18n.getMessage("days.long")).split(",");
Activiti.thirdparty.dateFormat.MONTH_NAMES = (Activiti.i18n.getMessage("months.short") + "," + Activiti.i18n.getMessage("months.long")).split(",");
Activiti.thirdparty.dateFormat.TIME_AM = Activiti.i18n.getMessage("date-format.am");
Activiti.thirdparty.dateFormat.TIME_PM = Activiti.i18n.getMessage("date-format.pm");
Activiti.thirdparty.dateFormat.masks =
{
  "default":       Activiti.i18n.getMessage("date-format.default"),
  defaultDateOnly: Activiti.i18n.getMessage("date-format.defaultDateOnly"),
  shortDate:       Activiti.i18n.getMessage("date-format.shortDate"),
  mediumDate:      Activiti.i18n.getMessage("date-format.mediumDate"),
  longDate:        Activiti.i18n.getMessage("date-format.longDate"),
  fullDate:        Activiti.i18n.getMessage("date-format.fullDate"),
  shortTime:       Activiti.i18n.getMessage("date-format.shortTime"),
  mediumTime:      Activiti.i18n.getMessage("date-format.mediumTime"),
  longTime:        Activiti.i18n.getMessage("date-format.longTime"),
  isoDate:         "yyyy-mm-dd",
  isoTime:         "HH:MM:ss",
  isoDateTime:     "yyyy-mm-dd'T'HH:MM:ss",
  isoFullDateTime: "yyyy-mm-dd'T'HH:MM:ss.lo"
};
Activiti.thirdparty.dateFormat.i18n =
{
  dayNames: Activiti.thirdparty.dateFormat.DAY_NAMES,
  monthNames: Activiti.thirdparty.dateFormat.MONTH_NAMES
};


/**
 * Converts an ISO8601-formatted date into a JavaScript native Date object
 *
 * Original code:
 *    dojo.date.stamp.fromISOString
 *    Copyright (c) 2005-2008, The Dojo Foundation
 *    All rights reserved.
 *    BSD license (http://trac.dojotoolkit.org/browser/dojo/trunk/LICENSE)
 *
 * @method Activiti.thirdparty.fromISO8601
 * @param formattedString {string} ISO8601-formatted date string
 * @return {Date|null}
 * @static
 */
Activiti.thirdparty.fromISO8601 = function()
{
  var fromISOString = function()
  {
    //   summary:
    //      Returns a Date object given a string formatted according to a subset of the ISO-8601 standard.
    //
    //   description:
    //      Accepts a string formatted according to a profile of ISO8601 as defined by
    //      [RFC3339](http://www.ietf.org/rfc/rfc3339.txt), except that partial input is allowed.
    //      Can also process dates as specified [by the W3C](http://www.w3.org/TR/NOTE-datetime)
    //      The following combinations are valid:
    //
    //         * dates only
    //         |   * yyyy
    //         |   * yyyy-MM
    //         |   * yyyy-MM-dd
    //          * times only, with an optional time zone appended
    //         |   * THH:mm
    //         |   * THH:mm:ss
    //         |   * THH:mm:ss.SSS
    //          * and "datetimes" which could be any combination of the above
    //
    //      timezones may be specified as Z (for UTC) or +/- followed by a time expression HH:mm
    //      Assumes the local time zone if not specified.  Does not validate.  Improperly formatted
    //      input may return null.  Arguments which are out of bounds will be handled
    //      by the Date constructor (e.g. January 32nd typically gets resolved to February 1st)
    //      Only years between 100 and 9999 are supported.
    //
    //   formattedString:
    //      A string such as 2005-06-30T08:05:00-07:00 or 2005-06-30 or T08:05:00

    var isoRegExp = /^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;

    return function(formattedString)
    {
      var match = isoRegExp.exec(formattedString);
      var result = null;

      if (match)
      {
        match.shift();
        if (match[1]){match[1]--;} // Javascript Date months are 0-based
        if (match[6]){match[6] *= 1000;} // Javascript Date expects fractional seconds as milliseconds

        result = new Date(match[0]||1970, match[1]||0, match[2]||1, match[3]||0, match[4]||0, match[5]||0, match[6]||0);

        var offset = 0;
        var zoneSign = match[7] && match[7].charAt(0);
        if (zoneSign != 'Z')
        {
          offset = ((match[8] || 0) * 60) + (Number(match[9]) || 0);
          if (zoneSign != '-')
          {
            offset *= -1;
          }
        }
        if (zoneSign)
        {
          offset -= result.getTimezoneOffset();
        }
        if (offset)
        {
          result.setTime(result.getTime() + offset * 60000);
        }
      }

      return result; // Date or null
    };
  }();

  return fromISOString.apply(arguments.callee, arguments);
};

/**
 * Converts a JavaScript native Date object into a ISO8601-formatted string
 *
 * Original code:
 *    dojo.date.stamp.toISOString
 *    Copyright (c) 2005-2008, The Dojo Foundation
 *    All rights reserved.
 *    BSD license (http://trac.dojotoolkit.org/browser/dojo/trunk/LICENSE)
 *
 * @method Activiti.thirdparty.toISO8601
 * @param dateObject {Date} JavaScript Date object
 * @param options {object} Optional conversion options
 *    zulu = true|false
 *    selector = "time|date"
 *    milliseconds = true|false
 * @return {string}
 * @static
 */
Activiti.thirdparty.toISO8601 = function()
{
  var toISOString = function()
  {
    //   summary:
    //      Format a Date object as a string according a subset of the ISO-8601 standard
    //
    //   description:
    //      When options.selector is omitted, output follows [RFC3339](http://www.ietf.org/rfc/rfc3339.txt)
    //      The local time zone is included as an offset from GMT, except when selector=='time' (time without a date)
    //      Does not check bounds.  Only years between 100 and 9999 are supported.
    //
    //   dateObject:
    //      A Date object
    var _ = function(n){ return (n < 10) ? "0" + n : n; };

    return function(dateObject, options)
    {
      options = options || {};
      var formattedDate = [];
      var getter = options.zulu ? "getUTC" : "get";
      var date = "";
      if (options.selector != "time")
      {
        var year = dateObject[getter+"FullYear"]();
        date = ["0000".substr((year+"").length)+year, _(dateObject[getter+"Month"]()+1), _(dateObject[getter+"Date"]())].join('-');
      }
      formattedDate.push(date);
      if (options.selector != "date")
      {
        var time = [_(dateObject[getter+"Hours"]()), _(dateObject[getter+"Minutes"]()), _(dateObject[getter+"Seconds"]())].join(':');
        var millis = dateObject[getter+"Milliseconds"]();
        if (options.milliseconds === undefined || options.milliseconds)
        {
          time += "."+ (millis < 100 ? "0" : "") + _(millis);
        }
        if (options.zulu)
        {
          time += "Z";
        }
        else if (options.selector != "time")
        {
          var timezoneOffset = dateObject.getTimezoneOffset();
          var absOffset = Math.abs(timezoneOffset);
          time += (timezoneOffset > 0 ? "-" : "+") +
              _(Math.floor(absOffset/60)) + ":" + _(absOffset%60);
        }
        formattedDate.push(time);
      }
      return formattedDate.join('T'); // String
    };
  }();

  return toISOString.apply(arguments.callee, arguments);
};

/**
 *  Activiti.support.inputDate
 *  
 *  Determines support for HTML5 input type=date
 *  @method inputDate
 *  @return {Boolean} HTML5 input type=date supported?
 */

Activiti.support.inputDate = function()
{
	var i = document.createElement("input");
	i.setAttribute("type", "date");
	return (i.type !== "text")
}();


/**
 * The Activiti.component.Base class provides core component functions
 * and is intended to be extended by other UI components, rather than
 * instantiated on it's own.
 */
(function()
{
  /**
   * Activiti.component.Base constructor.
   *
   * @param name {String} The name of the component
   * @param id {String} he DOM ID of the parent element
   * @return {object} The new instance
   * @constructor
   */
  Activiti.component.Base = function(name, id)
  {
    // Mandatory properties
    this.name = (typeof name == "undefined" || name === null) ? "Activiti.component.Base" : name;
    this.id = (typeof id == "undefined" || id === null) ? Activiti.util.generateDomId() : id;

    // Initialise default prototype properties
    this.options = Activiti.util.deepCopy(this.options);
    this.widgets = {};
    this.modules = {};
    this.services = {};

    // Register this component
    Activiti.util.ComponentManager.register(this);

    if (this.onReady && this.onReady.call && this.id !== "null")
    {
      YUIEvent.onContentReady(this.id, this.onReady, this, true);
    }

    return this;
  };

  Activiti.component.Base.prototype =
  {
    /**
     * Object container for initialization options
     *
     * @property options
     * @type object
     * @default {}
     */
    options: {},

    /**
     * Object container for storing YUI widget instances.
     *
     * @property widgets
     * @type object
     * @default null
     */
    widgets: null,

    /**
     * Object container for storing module instances.
     *
     * @property modules
     * @type object
     * @default null
     */
    modules: null,

    /**
     * Object container for storing service instances.
     *
     * @property services
     * @type object
     */
    services: null,

    /**
     * Set multiple initialization options at once.
     *
     * @method setOptions
     * @param obj {object} Object literal specifying a set of options
     * @return {object} returns 'this' for method chaining
     */
    setOptions: function Base_setOptions(obj)
    {
      this.options = YAHOO.lang.merge(this.options, obj);
      return this;
    },

    /**
     * Set messages for this component.
     *
     * @method setMessages
     * @param obj {object} Object literal specifying a set of messages
     * @return {object} returns 'this' for method chaining
     */
    setMessages: function Base_setMessages(obj)
    {
      Activiti.i18n.addMessages(obj, this.name);
      return this;
    },

    /**
     * Gets a custom message
     *
     * @method msg
     * @param messageId {string} The messageId to retrieve
     * @return {string} The custom message
     */
    msg: function Base_msg(messageId)
    {
      return Activiti.i18n.getMessage.call(this, messageId, this.name, Array.prototype.slice.call(arguments).slice(1));
    },

    /**
     * Fires an event setting this component as the owner.
     *
     * @method fireEvent
     * @param eventName {string} The event name
     * @param value {object|string|int} The event value
     * @param originalEvent {object} (Optional) The event that was originally fired, that will if supplied, get stopped.
     * @param bookmark {boolean} (Optional) Set to tru if event shall be put in browsers hostory state before dispatched
     */
    fireEvent: function Base_fireEvent(eventName, value, originalEvent, bookmark)
    {
      value.owner = this.id;
      Activiti.event.fire(eventName, value, originalEvent, bookmark);
    },


    /**
     * Fires an event setting this component as the owner
     *
     * @method onEvent
     * @param eventName {string} The event name
     * @param method {function} The event function
     */
    onEvent: function Base_onEvent(eventName, method)
    {
      Activiti.event.on(eventName, method, this);
    },

    /**
     * Checks if event was fired by this component
     *
     * @method isEventOwner
     * @param eventArgs {object} The event
     */
    isEventOwner: function Base_isEventOwner(eventArgs)
    {
      return Activiti.event.isOwner(this.id, eventArgs);
    },


    /**
     * Gets the sent value form the bubbling event arguments and strips out the owner attribute
     *
     * @method getEventValue
     * @param eventArgs {object} The event
     */
    getEventValue: function Base_getEventValue(eventArgs, deleteAttributes, deleteOwner)
    {
      return Activiti.event.getValue(eventArgs, deleteAttributes, deleteOwner);
    }

  };
})();

/**
 * Perform Activiti initialization before the rest of the dom content is ready.
 */
YUIEvent.onContentReady("ActiviInitializer", function(){
  Activiti.event.init();
});

