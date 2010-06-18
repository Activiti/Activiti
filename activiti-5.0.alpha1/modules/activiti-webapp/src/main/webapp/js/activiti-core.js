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
 * Activiti top-level widget namespace.
 *
 * @namespace Activiti
 * @class Activiti.widget
 */
Activiti.widget = Activiti.widget || {};

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
  var msg = p_messageId;

  if (typeof p_messageId != "string")
  {
    throw new Error("Missing or invalid argument: messageId");
  }

  var globalMsg = SpringSurf.messages.global ? SpringSurf.messages.global[p_messageId] : null;
  if (typeof globalMsg == "string")
  {
    msg = globalMsg;
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
    tokens = Array.prototype.slice.call(arguments).slice(2);
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
     * Shall be fired when a task filter has been clicked.
     *
     * @property TASKS_FILTER_CLICKED
     * @type string
     */
    TASKS_FILTER_CLICKED: "Activiti.event.TASK_FILTER_CLICKED",

    /**
     * Fires an bubbling event
     *
     * @method fire
     */
    fire: function fire(event, value, owner) {
      YAHOO.Bubbling.fire(event,
      {
        owner: owner,
        value: value
      });
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
     * Fires an event setting this component as the owner
     *
     * @method fire
     * @param eventName {string} The event name
     * @param value {object|string|int} The event value
     */
    fireEvent: function Base_fire(eventName, value)
    {
      Activiti.event.fire(eventName, value, this.id);
    },


    /**
     * Fires an event setting this component as the owner
     *
     * @method fire
     * @param eventName {string} The event name
     * @param value {object|string|int} The event value
     */
    onEvent: function Base_fire(eventName, method)
    {
      Activiti.event.on(eventName, method, this);
    },

    /**
     * Checks if event was fired by this component
     *
     * @method eventIsMine
     * @param eventArgs {object} The event
     */
    isEventOwner: function Base_eventIsMine(eventArgs)
    {
      return Activiti.event.isOwner(this.id, eventArgs);
    }

  };
})();

/**
 * Provides a common interface for displaying popups in various forms
 *
 * @class Activiti.widget.PopupManager
 */
Activiti.widget.PopupManager = function()
{
  /**
   * Activiti Slingshot aliases
   */
  var $html = Activiti.util.encodeHTML;

  return (
  {

    /**
     * The html zIndex startvalue that will be incremented for each popup
     * that is displayed to make sure the popup is visible to the user.
     *
     * @property zIndex
     * @type int
     */
    zIndex: 15,

    /**
     * The default config for the displaying messages, can be overriden
     * when calling displayMessage()
     *
     * @property defaultDisplayMessageConfig
     * @type object
     */
    defaultDisplayMessageConfig:
    {
      title: null,
      text: null,
      spanClass: "message",
      displayTime: 2.5,
      effect: YAHOO.widget.ContainerEffect.FADE,
      effectDuration: 0.5,
      visible: false,
      noEscape: false
    },

    /**
     * Intended usage: To quickly assure the user that the expected happened.
     *
     * Displays a message as a popup on the screen.
     * In default mode it fades, is visible for half a second and then fades out.
     *
     * @method displayMessage
     * @param config {object}
     * The config object is in the form of:
     * {
     *    text: {string},         // The message text to display, mandatory
     *    spanClass: {string},    // The class of the span wrapping the text
     *    effect: {YAHOO.widget.ContainerEffect}, // the effect to use when shpwing and hiding the message,
     *                                            // default is YAHOO.widget.ContainerEffect.FADE
     *    effectDuration: {int},  // time in seconds that the effect should be played, default is 0.5
     *    displayTime: {int},     // time in seconds that the message will be displayed, default 2.5
     *    modal: {true}           // if the message should modal (the background overlayed with a gray transparent layer), default is false
     * }
     * @param parent {HTMLElement} (optional) Parent element in which to render prompt. Defaults to document.body if not provided
     */
    displayMessage: function(config, parent)
    {
      var parent = parent || document.body;
      // Merge the users config with the default config and check mandatory properties
      var c = YAHOO.lang.merge(this.defaultDisplayMessageConfig, config);
      if (c.text === undefined)
      {
        throw new Error("Property text in userConfig must be set");
      }
      var dialogConfig =
      {
        modal: false,
        visible: c.visible,
        close: false,
        draggable: false,
        effect:
        {
          effect: c.effect,
          duration: c.effectDuration
        },
        zIndex: this.zIndex++
      };
      // IE browsers don't deserve fading, as they can't handle it properly
      if (c.effect === null || YAHOO.env.ua.ie > 0)
      {
        delete dialogConfig.effect;
      }
      // Construct the YUI Dialog that will display the message
      var message = new YAHOO.widget.Dialog("message", dialogConfig);

      // Set the message that should be displayed
      var bd =  "<span class='" + c.spanClass + "'>" + (c.noEscape ? c.text : $html(c.text)) + "</span>";
      message.setBody(bd);

      /**
       * Add it to the dom, center it, schedule the fade out of the message
       * and show it.
       */
      message.render(parent);
      message.center();
      // Need to schedule a fade-out?
      if (c.displayTime > 0)
      {
        message.subscribe("show", this._delayPopupHide,
        {
          popup: message,
          displayTime: (c.displayTime * 1000)
        }, true);
      }
      message.show();

      return message;
    },

    /**
     * Gets called after the message has been displayed as long as it was
     * configured.
     * Hides the message from the user.
     *
     * @method _delayPopupHide
     */
    _delayPopupHide: function()
    {
      YAHOO.lang.later(this.displayTime, this, function()
      {
        this.popup.destroy();
      });
    },

    /**
     * The default config for displaying "prompt" messages, can be overriden
     * when calling displayPrompt()
     *
     * @property defaultDisplayPromptConfig
     * @type object
     */
    defaultDisplayPromptConfig:
    {
      title: null,
      text: null,
      icon: null,
      close: false,
      constraintoviewport: true,
      draggable: true,
      effect: null,
      effectDuration: 0.5,
      modal: true,
      visible: false,
      noEscape: false,
      buttons: [
        {
          text: null, // Too early to localize at this time, do it when called instead
          callbackHandler: function()
          {
            this.destroy();
          },
          isDefault: true
        }]
    },

    /**
     * Intended usage: To inform the user that something unexpected happened
     * OR that ask the user if if an action should be performed.
     *
     * Displays a message as a popup on the screen with a button to make sure
     * the user responds to the prompt.
     *
     * In default mode it shows with an OK button that needs clicking to get closed.
     *
     * @method displayPrompt
     * @param config {object}
     * The config object is in the form of:
     * {
     *    title: {string},       // the title of the dialog, default is null
     *    text: {string},        // the text to display for the user, mandatory
     *    icon: null,            // the icon to display next to the text, default is null
     *    effect: {YAHOO.widget.ContainerEffect}, // the effect to use when showing and hiding the prompt, default is null
     *    effectDuration: {int}, // the time in seconds that the effect should run, default is 0.5
     *    modal: {boolean},      // if a grey transparent overlay should be displayed in the background
     *    close: {boolean},      // if a close icon should be displayed in the right upper corner, default is false
     *    buttons: []            // an array of button configs as described by YUI's SimpleDialog, default is a single OK button
     *    noEscape: {boolean}    // indicates the the message has already been escaped (e.g. to display HTML-based messages)
     * }
     * @param parent {HTMLElement} (optional) Parent element in which to render prompt. Defaults to document.body if not provided
     */
    displayPrompt: function(config, parent)
    {
      var parent = parent || document.body;
      if (this.defaultDisplayPromptConfig.buttons[0].text === null)
      {
        /**
         * This default value could not be set at instantion time since the
         * localized messages weren't present at that time
         */
        this.defaultDisplayPromptConfig.buttons[0].text = Activiti.i18n.getMessage("button.ok", this.name);
      }
      // Merge users config and the default config and check manadatory properties
      var c = YAHOO.lang.merge(this.defaultDisplayPromptConfig, config);
      if (c.text === undefined)
      {
        throw new Error("Property text in userConfig must be set");
      }

      // Create the SimpleDialog that will display the text
      var prompt = new YAHOO.widget.SimpleDialog("prompt",
      {
        close: c.close,
        constraintoviewport: c.constraintoviewport,
        draggable: c.draggable,
        effect: c.effect,
        modal: c.modal,
        visible: c.visible,
        zIndex: this.zIndex++
      });

      // Show the title if it exists
      if (c.title)
      {
        prompt.setHeader($html(c.title));
      }

      // Show the prompt text
      prompt.setBody(c.noEscape ? c.text : $html(c.text));

      // Show the icon if it exists
      if (c.icon)
      {
        prompt.cfg.setProperty("icon", c.icon);
      }

      // Add the buttons to the dialog
      if (c.buttons)
      {
        prompt.cfg.queueProperty("buttons", c.buttons);
      }

      // Add the dialog to the dom, center it and show it.
      prompt.render(parent);
      prompt.center();
      prompt.show();
    },

    /**
     * The default config for the getting user input, can be overriden
     * when calling getUserInput()
     *
     * @property defaultGetUserInputConfig
     * @type object
     */
    defaultGetUserInputConfig:
    {
      title: null,
      text: null,
      value: "",
      icon: null,
      close: true,
      constraintoviewport: true,
      draggable: true,
      effect: null,
      effectDuration: 0.5,
      modal: true,
      visible: false,
      initialShow: true,
      noEscape: true,
      html: null,
      callback: null,
      buttons: [
        {
          text: null, // OK button. Too early to localize at this time, do it when called instead
          callbackHandler: null,
          isDefault: true
        },
        {
          text: null, // Cancel button. Too early to localize at this time, do it when called instead
          callbackHandler: function()
          {
            this.destroy();
          }
        }]
    },

    /**
     * Intended usage: To ask the user for a simple text input, similar to JavaScript's prompt() function.
     *
     * @method getUserInput
     * @param config {object}
     * The config object is in the form of:
     * {
     *    title: {string},       // the title of the dialog, default is null
     *    text: {string},        // optional label next to input box
     *    value: {string},       // optional default value to populate textbox with
     *    callback: {object}     // Object literal specifying function callback to receive user input. Only called if default button config used.
     *                           // fn: function, obj: optional pass-thru object, scope: callback scope
     *    icon: null,            // the icon to display next to the text, default is null
     *    effect: {YAHOO.widget.ContainerEffect}, // the effect to use when showing and hiding the prompt, default is null
     *    effectDuration: {int}, // the time in seconds that the effect should run, default is 0.5
     *    modal: {boolean},      // if a grey transparent overlay should be displayed in the background
     *    initialShow {boolean}  // whether to call show() automatically on the panel
     *    close: {boolean},      // if a close icon should be displayed in the right upper corner, default is true
     *    buttons: []            // an array of button configs as described by YUI's SimpleDialog, default is a single OK button
     *    okButtonText: {string} // Allows just the label of the OK button to be overridden
     *    noEscape: {boolean}    // indicates the the text property has already been escaped (e.g. to display HTML-based messages)
     *    html: {string},        // optional override for function-generated HTML <input> field. Note however that you must supply your own
     *                           //    button handlers in this case in order to get the user's input from the Dom.
     * }
     * @return {YAHOO.widget.SimpleDialog} The dialog widget
     */
    getUserInput: function(config)
    {
      if (this.defaultGetUserInputConfig.buttons[0].text === null)
      {
        /**
         * This default value could not be set at instantion time since the
         * localized messages weren't present at that time
         */
        this.defaultGetUserInputConfig.buttons[0].text = Activiti.i18n.getMessage("button.ok", this.name);
      }
      if (this.defaultGetUserInputConfig.buttons[1].text === null)
      {
        this.defaultGetUserInputConfig.buttons[1].text = Activiti.i18n.getMessage("button.cancel", this.name);
      }

      // Merge users config and the default config and check manadatory properties
      var c = YAHOO.lang.merge(this.defaultGetUserInputConfig, config);

      // Create the SimpleDialog that will display the text
      var prompt = new YAHOO.widget.SimpleDialog("userInput",
      {
        close: c.close,
        constraintoviewport: c.constraintoviewport,
        draggable: c.draggable,
        effect: c.effect,
        modal: c.modal,
        visible: c.visible,
        zIndex: this.zIndex++
      });

      // Show the title if it exists
      if (c.title)
      {
        prompt.setHeader($html(c.title));
      }

      // Generate the HTML mark-up if not overridden
      var html = c.html,
          id = Activiti.util.generateDomId();
      if (html === null)
      {
        html = "";
        if (c.text)
        {
          html += '<label for="' + id + '">' + (c.noEscape ? c.text : $html(c.text)) + '</label>';
        }
        html += '<textarea id="' + id + '" tabindex="0">' + c.value + '</textarea>';
      }
      prompt.setBody(html);

      // Show the icon if it exists
      if (c.icon)
      {
        prompt.cfg.setProperty("icon", c.icon);
      }

      // Add the buttons to the dialog
      if (c.buttons)
      {
        if (c.okButtonText)
        {
          // Override OK button label
          c.buttons[0].text = c.okButtonText;
        }

        // Default handler if no custom button passed-in
        if (typeof config.buttons == "undefined" || typeof config.buttons[0] == "undefined")
        {
          // OK button click handler
          c.buttons[0].callbackHandler =
          {
            fn: function(event, obj)
            {
              // Grab the input, destroy the pop-up, then callback with the value
              var value = null;
              if (YUIDom.get(obj.id))
              {
                value = YUIDom.get(obj.id).value;
              }
              this.destroy();
              if (obj.callback.fn)
              {
                obj.callback.fn.call(obj.callback.scope || window, value, obj.callback.obj);
              }
            },
            obj:
            {
              id: id,
              callback: c.callback
            }
          };
        }
        prompt.cfg.queueProperty("buttons", c.buttons);
      }

      // Add the dialog to the dom, center it and show it (unless flagged not to).
      prompt.render(document.body);
      prompt.center();
      if (c.initialShow)
      {
        prompt.show();
      }

      // If a default value was given, set the selectionStart and selectionEnd properties
      if (c.value !== "")
      {
        YUIDom.get(id).selectionStart = 0;
        YUIDom.get(id).selectionEnd = c.value.length;
      }

      // Register the ESC key to close the panel
      var escapeListener = new YAHOO.util.KeyListener(document,
      {
        keys: YAHOO.util.KeyListener.KEY.ESCAPE
      },
      {
        fn: function(id, keyEvent)
        {
          this.destroy();
        },
        scope: prompt,
        correctScope: true
      });
      escapeListener.enable();

      if (YUIDom.get(id))
      {
        YUIDom.get(id).focus();
      }

      return prompt;
    }
  });
}();

(function()
{
  /**
   *
   * @param callbackHandler The object that shall implement the callbacks
   * @param dataTableElId The id of the element in which to create the data table
   * @param paginationElIds The ids of the elements in which to create paginators
   * @param listFieldKeys The data fields in the data response from the server
   * @param listColumnDefs The columns in the data table
   */
  Activiti.widget.DataTable = function List_constructor(callbackHandler, dataTableElId, paginationElIds, listFieldKeys, listColumnDefs)
  {
    this.id = Activiti.util.generateDomId(null, "dataTable");
    this._callbackHandler = callbackHandler;
    this._dataTable = {};
    this._dataSource = {};
    this._init(dataTableElId, paginationElIds, listFieldKeys, listColumnDefs);
    return this;
  };

  Activiti.widget.DataTable.prototype = {

    /**
     * This components unique id (used as the module name for the browser history management)
     *
     * @type string
     * @private
     */
    id: null,

    /**
     * The callback handler that implements the callback methods
     *
     * @type object
     * @private
     */
    _callbackHandler: null,

    /**
     * The data table
     *
     * @type YAHOO.widget.DataTable
     * @private
     */
    _dataTable: null,

    /**
     * The data source
     *
     * @type YAHOO.util.DataSource
     * @private
     */
    _dataSource: null,

    /**
     * Initialises the data table, paginators and browser history navigation.
     *
     * @param dataTableElId The id of the element in which to create the data table
     * @param paginationElIds The id of the elements in which to create the paginators
     * @param listFieldKeys The field keys that match the json response from the server
     * @param listColumnDefs The columns to create in the data table
     * @private
     */
    _init: function (dataTableElId, paginationElIds, listFieldKeys, listColumnDefs)
    {
      var me = this;

      // Create the html elements needed for history management
      var historyManagementEl = document.createElement("div"),
          str = '';
      if (YAHOO.env.ua.ie > 0) {
        str += '<iframe id="' + dataTableElId + '-yui-history-iframe" src="' + Activiti.constants.URL_CONTEXT + 'res/yui/blank.html" class="hidden"></iframe>';
      }
      str +='<input id="' + dataTableElId + '-yui-history-field" type="hidden" />'
      historyManagementEl.innerHTML = str;
      YUIDom.get(dataTableElId).parentNode.appendChild(historyManagementEl);

      // Create the DataSource
      me._dataSource = new YAHOO.util.DataSource("");
      me._dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
      me._dataSource.responseSchema = {
        resultsList: "data",
        fields: listFieldKeys,
        metaFields: {
          totalRecords: "totalRecords",
          paginationRecordOffset : "startIndex",
          paginationRowsPerPage : "pageSize",
          sortKey: "sort",
          sortDir: "dir"
        }
      };

      // Create the Paginator
      me._paginator = new YAHOO.widget.Paginator({
        containers : paginationElIds,
        firstPageLinkLabel: Activiti.i18n.getMessage("Activiti.widget.DataTable.paginator.firstPageLinkLabel"),
        previousPageLinkLabel: Activiti.i18n.getMessage("Activiti.widget.DataTable.paginator.previousPageLinkLabel"),
        nextPageLinkLabel: Activiti.i18n.getMessage("Activiti.widget.DataTable.paginator.nextPageLinkLabel"),
        lastPageLinkLabel: Activiti.i18n.getMessage("Activiti.widget.DataTable.paginator.lastPageLinkLabel"),
        rowsPerPageOptions : [10, 25, 50, 100]
      });

      // Hook in formatter so we can call it in the callbackHandler's scope
      for (var i = 0, il = listColumnDefs.length; i <il; i++) {
        if (!listColumnDefs[i].formatter) {
          listColumnDefs[i].formatter = function(el, oRecord, oColumn, oData)
          {
            // Apply widths on each cell so it actually works as given in the column definitions
            if (oColumn.width) {
              YUIDom.setStyle(el, "width", oColumn.width + "px");
              YUIDom.setStyle(el.parentNode, "width", oColumn.width + "px");
            }

            var fieldKey = oColumn.getField(),
              methodName = "onDataTableRenderCell" + fieldKey.substring(0, 1).toUpperCase() + fieldKey.substring(1);
            if (YAHOO.lang.isFunction(me._callbackHandler[methodName])) {
              me._callbackHandler[methodName].call(me._callbackHandler, el, oRecord, oColumn, oData);
            }
          };
        }
      }
      
      // Instantiate DataTable
      me._dataTable = new YAHOO.widget.DataTable(dataTableElId, listColumnDefs, me._dataSource, {
        paginator : me._paginator,
        dynamicData : true,
        initialLoad : false,
        className: "activiti-datatable"
      });

      // Show loading message while page is being rendered
      me._dataTable.showTableMessage(me._dataTable.get("MSG_LOADING"), YAHOO.widget.DataTable.CLASS_LOADING);

      // Integrate with Browser History Manager
      var History = YAHOO.util.History;

      // Define a custom function to route sorting through the Browser History Manager
      var handleSorting = function (oColumn) {
        // Calculate next sort direction for given Column
        var sDir = this.getColumnSortDir(oColumn);

        // The next state will reflect the new sort values
        // while preserving existing pagination rows-per-page
        // As a best practice, a new sort will reset to page 0
        var newState = generateRequest(0, oColumn.key, sDir, this.get("paginator").getRowsPerPage());

        // Pass the state along to the Browser History Manager
        if (newState) {
          History.navigate(me.id, newState);
        }
      };
      me._dataTable.sortColumn = handleSorting;

      // Define a custom function to route pagination through the Browser History Manager
      var handlePagination = function(state) {
        // The next state will reflect the new pagination values
        // while preserving existing sort values
        // Note that the sort direction needs to be converted from DataTable format to server value
        var sortedBy  = this.get("sortedBy"),
            newState = generateRequest(
                state.recordOffset, sortedBy.key, sortedBy.dir, state.rowsPerPage
                );

        // Pass the state along to the Browser History Manager
        if (newState) {
          History.navigate(me.id, newState);
        }
      };
      // First we must unhook the built-in mechanism...
      me._paginator.unsubscribe("changeRequest", me._dataTable.onPaginatorChangeRequest);
      // ...then we hook up our custom function
      me._paginator.subscribe("changeRequest", handlePagination, me._dataTable, true);

      // Update payload data on the fly for tight integration with latest values from server
      me._dataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {
        var meta = oResponse.meta;
        oPayload.totalRecords = meta.totalRecords || oPayload.totalRecords;
        oPayload.pagination = {
          rowsPerPage: meta.paginationRowsPerPage || 10,
          recordOffset: meta.paginationRecordOffset || 0
        };
        oPayload.sortedBy = {
          key: meta.sortKey || "id",
          dir: (meta.sortDir) ? "yui-dt-" + meta.sortDir : "yui-dt-asc" // Convert from server value to DataTable format
        };
        return true;
      };

      // Returns a request string for consumption by the DataSource
      var generateRequest = function(startIndex, sortKey, dir, pageSize) {
        return me._callbackHandler.onDataTableCreateURL.call(me._callbackHandler, (startIndex || 0), sortKey, dir, pageSize);
      };

      // Called by Browser History Manager to trigger a new state
      var handleHistoryNavigation = function (request) {
        // Sends a new request to the DataSource
        me._dataSource.sendRequest(request, {
          success : me._dataTable.onDataReturnSetRows,
          failure : me._dataTable.onDataReturnSetRows,
          scope : me._dataTable,
          argument : {} // Pass in container for population at runtime via doBeforeLoadData
        });
      };

      // Calculate the first request
      var initialRequest = History.getBookmarkedState(me.id) || generateRequest();

      // Register the module      
      History.register(me.id, initialRequest, handleHistoryNavigation);

      // Render the first view
      History.onReady(function() {
        // Current state after BHM is initialized is the source of truth for what state to render
        var currentState = History.getCurrentState(me.id);
        handleHistoryNavigation(currentState);
      });

      // Initialize the Browser History Manager.
      YAHOO.util.History.initialize(dataTableElId + "-yui-history-field", dataTableElId + "-yui-history-iframe");
    },

    /**
     * Load the datatable with data form the url
     *  
     * @param url The url to load data from
     * @param force Set to true if load shall be performed even if the current data is based on the same url
     */
    load: function (url, force)
    {
      if (YAHOO.util.History.getCurrentState(this.id) == url && force) {
        this._dataSource.sendRequest(url, {
          success : this._dataTable.onDataReturnSetRows,
          failure : this._dataTable.onDataReturnSetRows,
          scope : this._dataTable,
          argument : {} // Pass in container for population at runtime via doBeforeLoadData
        });
      }
      else {
        YAHOO.util.History.navigate(this.id, url);
      }
    }
  }
})();
