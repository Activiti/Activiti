
/**
 * Activiti root & service name spaces.
 *
 * @namespace Activiti
 */
// Ensure Activiti root object exists
if (typeof Activiti == "undefined" || !Activiti)
{
  var Activiti = {};
}

Activiti.service = Activiti.service || {
  REST_ENPOINT: null,
  REST_PROXY_URI: null,
  REST_PROXY_URI_RELATIVE: null
};

/**
 * Helper class for submitting data to server that wraps a
 * YAHOO.util.Connect.asyncRequest call.
 *
 * The request method provides default behaviour for displaying messages on
 * success and error events and simplifies json handling with encoding and decoding.
 *
 * @class Activiti.util.Ajax
 */
Activiti.service.Ajax = function() {
  return {

    /**
     * Constant for contentType of type json
     *
     * @property JSON
     * @type string
     */
    JSON: "application/json",

    /**
     * Constant for method of type GET
     *
     * @property GET
     * @type string
     */
    GET: "GET",

    /**
     * Constant for method of type POST
     *
     * @property POST
     * @type string
     */
    POST: "POST",

    /**
     * Constant for method of type PUT
     *
     * @property PUT
     * @type string
     */
    PUT: "PUT",

    /**
     * Constant for method of type DELETE
     *
     * @property DELETE
     * @type string
     */
    DELETE: "DELETE",

    /**
     * The default request config used by method request()
     *
     * @property defaultRequestConfig
     * @type object
     */
    defaultRequestConfig:
    {
      method: "GET",        // GET, POST, PUT or DELETE
      url: null,            // Must be set by user
      dataObj: null,        // Will be encoded to parameters (key1=value1&key2=value2)
      // or a json string if contentType is set to JSON
      dataStr: null,        // Will be used in the request body, could be a already created parameter or json string
      // Will be overriden by the encoding result from dataObj if dataObj is provided
      dataForm: null,       // A form object or id that contains the data to be sent with request
      requestContentType: null,    // Set to JSON if json should be used
      responseContentType: null,    // Set to JSON if json should be used
      successCallback: null,// Object literal representing callback upon successful operation
      successMessage: null, // Will be displayed by Activiti.widget.PopupManager.displayMessage if no success handler is provided
      failureCallback: null,// Object literal representing callback upon failed operation
      failureMessage: null,  // Will be displayed by Activiti.util.displayPrompt if no failure handler is provided
      execScripts: false,    // Whether embedded <script> tags will be executed within the successful response
      noReloadOnAuthFailure: false, // Default to reloading the page on HTTP 401 response, which will redirect through the login page
      object: null           // An object that can be passed to be used by the success or failure handlers
    },

    /**
     * Wraps a YAHOO.util.Connect.asyncRequest call and provides some default
     * behaviour for displaying error or success messages, uri encoding and
     * json encoding and decoding.
     *
     * JSON
     *
     * If requestContentType is JSON, config.dataObj (if available) is encoded
     * to a json string and set in the request body.
     *
     * If a json string already has been created by the application it should
     * be passed in as the config.dataStr which will be put in the rewuest body.
     *
     * If responseContentType is JSON the server response is decoded to a
     * json object and set in the "json" attribute in the response object
     * which is passed to the succes or failure callback.
     *
     * PARAMETERS
     *
     * If requestContentType is null, config.dataObj (if available) is encoded
     * to a normal parameter string which is added to the url if method is
     * GET or DELETE and to the request body if method is POST or PUT.
     *
     * FORMS
     * A form can also be passed it and submitted just as desccribed in the
     * YUI documentation.
     *
     * SUCCESS
     *
     * If the request is successful successCallback.fn is called.
     * If successCallback.fn isn't provided successMessage is displayed.
     * If successMessage isn't provided nothing happens.
     *
     * FAILURE
     *
     * If the request fails failureCallback.fn is called.
     * If failureCallback.fn isn't displayed failureMessage is displayed.
     * If failureMessage isn't provided the "best error message as possible"
     * from the server response is displayed.
     *
     * CALLBACKS
     *
     * The success or failure handlers can expect a response object of the
     * following form (they will be called in the scope defined by config.scope)
     *
     * {
     *   config: {object},         // The config object passed in to the request,
     *   serverResponse: {object}, // The response provided by YUI
     *   json: {object}            // The serverResponse parsed and ready as an object
     * }
     *
     * @method request
     * @param config {object} Description of the request that should be made
     * The config object has the following form:
     * {
     *    method: {string}               // GET, POST, PUT or DELETE, default is GET
     *    url: {string},                 // the url to send the request to, mandatory
     *    dataObj: {object},             // Will be encoded to parameters (key1=value1&key2=value2) or a json string if requestContentType is set to JSON
     *    dataStr: {string},             // the request body, will be overriden by the encoding result from dataObj if dataObj is provided
     *    dataForm: {HTMLElement},       // A form object or id that contains the data to be sent with request
     *    requestContentType: {string},  // Set to JSON if json should be used
     *    responseContentType: {string}, // Set to JSON if json should be used
     *    successCallback: {object},     // Callback for successful request, should have the following form: {fn: successHandler, scope: scopeForSuccessHandler}
     *    successMessage: {string},      // Will be displayed using Activiti.widget.PopupManager.displayMessage if successCallback isn't provided
     *    failureCallback: {object},     // Callback for failed request, should have the following form: {fn: failureHandler, scope: scopeForFailureHandler}
     *    failureMessage: {string},      // Will be displayed by Activiti.util.displayPrompt if no failureCallback isn't provided
     *    execScripts: {boolean},        // Whether embedded <script> tags will be executed within the successful response
     *    noReloadOnAuthFailure: {boolean}, // Set to TRUE to prevent an automatic page refresh on HTTP 401 response
     *    object: {object}               // An object that can be passed to be used by the success or failure handlers
     * }
     */
    request: function(config)
    {
      // Merge the user config with the default config and check for mandatory parameters
      var c = YAHOO.lang.merge(this.defaultRequestConfig, config);

      // If a contentType is provided set it in the header
      if (c.requestContentType)
      {
        YAHOO.util.Connect.setDefaultPostHeader(c.requestContentType);
        YAHOO.util.Connect.initHeader("Content-Type", c.requestContentType);
      }

      if (c.requestContentType === this.JSON)
      {
        if (c.method.toUpperCase() === this.GET)
        {
          if (c.dataObj)
          {
            throw new Error("Parameter 'method' can not be 'GET' when trying to submit data in dataObj with contentType '" + c.requestContentType + "'");
          }
        }
        else
        {
          // If json is used encode the dataObj parameter and put it in the body
          c.dataStr = YAHOO.lang.JSON.stringify(c.dataObj || {});
        }
      }
      else
      {
        if (c.dataObj)
        {
          // Normal URL parameters
          if (c.method.toUpperCase() === this.GET)
          {
            // Encode the dataObj and put it in the url
            c.url += (c.url.indexOf("?") == -1 ? "?" : "&") + this.jsonToParamString(c.dataObj, true);
          }
          else
          {
            // Encode the dataObj and put it in the body
            c.dataStr = this.jsonToParamString(c.dataObj, true);
          }
        }
      }

      if (c.dataForm !== null)
      {
        // Set the form on the connection manager
        YAHOO.util.Connect.setForm(c.dataForm);
      }

      /**
       * The private "inner" callback that will handle json and displaying
       * of messages and prompts
       */
      var callback =
      {
        success: this._successHandler,
        failure: this._failureHandler,
        scope: this,
        argument:
        {
          config: config
        }
      };

      // Make the request
      YAHOO.util.Connect.asyncRequest (c.method, c.url, callback, c.dataStr);
    },

    /**
     * Helper function for pure json requests, where both the request and
     * response are using json. Will result in a call to request() with
     * requestContentType and responseContentType set to JSON.
     *
     * @method request
     * @param config {object} Description of the request that should be made
     */
    jsonRequest: function(config)
    {
      config.requestContentType = this.JSON;
      config.responseContentType = this.JSON;
      this.request(config);
    },

    /**
     * Helper function for pure json requests, where both the request and
     * response are using json. Will result in a call to request() with
     * requestContentType and responseContentType set to JSON and method set to GET.
     *
     * @method request
     * @param config {object} Description of the request that should be made
     */
    jsonGet: function(config)
    {
      config.method = this.GET;
      this.jsonRequest(config);
    },

    /**
     * Helper function for pure json requests, where both the request and
     * response are using json. Will result in a call to request() with
     * requestContentType and responseContentType set to JSON and method set to POST.
     *
     * @method request
     * @param config {object} Description of the request that should be made
     */
    jsonPost: function(config)
    {
      config.method = this.POST;
      this.jsonRequest(config);
    },

    /**
     * Helper function for pure json requests, where both the request and
     * response are using json. Will result in a call to request() with
     * requestContentType and responseContentType set to JSON and method set to PUT.
     *
     * @method request
     * @param config {object} Description of the request that should be made
     */
    jsonPut: function(config)
    {
      config.method = this.PUT;
      this.jsonRequest(config);
    },

    /**
     * Helper function for pure json requests, where both the request and
     * response are using json. Will result in a call to request() with
     * requestContentType and responseContentType set to JSON and method set to DELETE.
     *
     * @method request
     * @param config {object} Description of the request that should be made
     */
    jsonDelete: function(config)
    {
      config.method = this.DELETE;
      this.jsonRequest(config);
    },

    /**
     * Takes an object and creates a decoded URL parameter string of it.
     * Note! Does not contain a '?' character in the beginning.
     *
     * @method request
     * @param obj
     * @param encode   indicates whether the parameter values should be encoded or not
     * @private
     */
    jsonToParamString: function(obj, encode)
    {
      var params = "", first = true, attr;

      for (attr in obj)
      {
        if (obj.hasOwnProperty(attr))
        {
          if (first)
          {
            first = false;
          }
          else
          {
            params += "&";
          }

          // Make sure no user input destroys the url
          if (encode)
          {
            params += encodeURIComponent(attr) + "=" + encodeURIComponent(obj[attr]);
          }
          else
          {
            params += attr + "=" + obj[attr];
          }
        }
      }
      return params;
    },


    /**
     * Parses a string to a json object and returns it.
     * If str contains invalid json code that is displayed using displayPrompt().
     *
     * @method parseJSON
     * @param jsonStr {string} The JSON string to be parsed
     * @param displayError {boolean} Set true to display a message informing about bad JSON syntax
     * @return {object} The object representing the JSON string
     * @static
     */
    parseJSON: function(jsonStr, displayError)
    {
      try
      {
        return YAHOO.lang.JSON.parse(jsonStr);
      }
      catch (error)
      {
        if (displayError)
        {
          this.displayErrorMessage("Can't parse response as json: '" + jsonStr + "'", null);
        }
      }
      return null;
    },

    /**
     * Handles successful request triggered by the request() method.
     * If execScripts was requested, retrieve and execute the script(s).
     * Otherwise, fall through to the _successHandlerPostExec function immediately.
     *
     * @method request
     * @param serverResponse
     * @private
     */
    _successHandler: function(serverResponse)
    {
      // Get the config that was used in the request() method
      var config = serverResponse.argument.config;

      // Need to execute embedded "<script>" tags?
      if (config.execScripts)
      {
        var scripts = [];
        var script = null;
        var regexp = /<script[^>]*>([\s\S]*?)<\/script>/gi;
        while ((script = regexp.exec(serverResponse.responseText)))
        {
          scripts.push(script[1]);
        }
        scripts = scripts.join("\n");

        // Remove the script from the responseText so it doesn't get executed twice
        serverResponse.responseText = serverResponse.responseText.replace(regexp, "");

        // Use setTimeout to execute the script. Note scope will always be "window"
        window.setTimeout(scripts, 0);

        // Delay-call the PostExec function to continue response processing after the setTimeout above
        YAHOO.lang.later(0, this, this._successHandlerPostExec, serverResponse);
      }
      else
      {
        this._successHandlerPostExec(serverResponse);
      }
    },

    /**
     * Follow-up handler after successful request triggered by the request() method.
     * If execScripts was requested, this function continues after the scripts have been run.
     * If the responseContentType was set to json the response is decoded for easy access to the success callback.
     * If no successCallback is provided the successMessage is displayed using Activiti.service.displaySuccessMessage().
     * If no successMessage is provided nothing happens.
     *
     * @method request
     * @param serverResponse
     * @private
     */
    _successHandlerPostExec: function(serverResponse)
    {
      // Get the config that was used in the request() method
      var config = serverResponse.argument.config;
      var callback = config.successCallback;
      if (callback && typeof callback.fn == "function")
      {
        var contentType = serverResponse.getResponseHeader["Content-Type"] || config.responseContentType;
        // User provided a custom successHandler
        var json = null;

        if (/^\s*application\/json/.test(contentType))
        {
          // Decode the response since it should be json
          json = this.parseJSON(serverResponse.responseText);
        }

        // Call the success callback in the correct scope
        callback.fn.call((typeof callback.scope == "object" ? callback.scope : this),
        {
          config: config,
          json: json,
          serverResponse: serverResponse
        }, callback.obj);
      }
      if (config.successMessage)
      {
        /**
         * User provided successMessage.
         */
        this.displaySuccessMessage(config.successMessage, serverResponse);
      }
    },

    /**
     * Handles failed request triggered by the request() method.
     * If the responseContentType was set to json the response is decoded for easy access to the failure callback.
     * If no failureCallback is provided the message is displayed using Activiti.service.displayErrorMessage().
     * If no failureMessage is provided "the best available server response" is created and displayed using the same methods as above.
     *
     * @method request
     * @param serverResponse
     * @private
     */
    _failureHandler: function(serverResponse)
    {
      // Get the config that was used in the request() method
      var config = serverResponse.argument.config;

      // Our session has likely timed-out, so refresh to offer the login page
      if (serverResponse.status == 401 && !config.noReloadOnAuthFailure)
      {
        window.location.reload(true);
        return;
      }

      // Invoke the callback
      var callback = config.failureCallback, json = null;

      if ((callback && typeof callback.fn == "function") || (config.failureMessage))
      {
        if (callback && typeof callback.fn == "function")
        {
          // If the caller has defined an error message display that instead of displaying message about bad json syntax
          var displayBadJsonResult = true;
          if (config.failureMessage || config.failureCallback)
          {
            displayBadJsonResult = false;
          }

          // User provided a custom failureHandler
          if (config.responseContentType === "application/json")
          {
            json = this.parseJSON(serverResponse.responseText, displayBadJsonResult);
          }
          callback.fn.call((typeof callback.scope == "object" ? callback.scope : this),
          {
            config: config,
            json: json,
            serverResponse: serverResponse
          }, callback.obj);
        }
        if (config.failureMessage)
        {
          /**
           * User did not provide a custom failureHandler, display mesage instead
           */
          this.displayErrorMessage(config.failureMessage, serverResponse);
        }
      }
      else
      {
        /**
         * User did not provide any failure info at all, display as good
         * info as possible from the server response.
         */
        if (config.responseContentType == "application/json")
        {
          json = this.parseJSON(serverResponse.responseText);
          this.displayErrorMessage(json.message, serverResponse);
        }
        else if (serverResponse.statusText)
        {
          this.displayErrorMessage(serverResponse.statusText, serverResponse);
        }
        else
        {
          this.displayErrorMessage("Error sending data to server.", serverResponse);
        }
      }
    },

    /**
     * Override this method to display success messages
     *
     * @method displaySuccessMessage
     * @param msgKey
     * @param serverResponse
     */
    displaySuccessMessage: function Ajax_displaySuccessMessage(msgKey, serverResponse) {
    },

    /**
     * Override this method to display error messages
     *
     * @method displayFailureMessage
     * @param msgKey
     * @param serverResponse
     */
    displayFailureMessage: function Ajax_displayFailureMessage(msgKey, serverResponse) {
    }

  };
}();

(function()
{

  /**
   * The base class for the rest service classes.
   *
   * @param name The name of the rest service
   * @param callbackHandler The object that shall implement the callback methods
   */
  Activiti.service.RestService = function RestService_constructor(name, callbackHandler)
  {
    this.name = name;
    this.callbackHandler = callbackHandler;
    this.successCallbacks = {};
    this.failureCallbacks = {};
    return this;
  };

  Activiti.service.RestService.prototype = {

    /**
     * The name of the service.
     *
     * @type string
     * @private
     */
    name: null,

    /**
     * The callback object that shal implement the callback methods:
     * - on<MethodName>Success (or onSuccess)
     * - on<MethodName>Failure (or onFailure)
     *
     * @type object
     * @private
     */
    callbackHandler: null,

    /**
     * Specific success callbacks to avoid name clashes
     *
     * @type object
     * @private
     */
    successCallbacks: null,

    /**
     * Specific failure callbacks to avoid name clashes
     *
     * @type object
     * @private
     */
    failureCallbacks: null,

    /**
     * Sets specific callbacks for a method.
     * Normally after a service method is invoked a callback matching on<MethodName>Success or on<MethodName>Failure 
     * is invoked on the callbackHandler (supplied to the service's constructor).
     * But the callbackHandler for some reason needs to use those method names for something else, its possible to
     * override these conventions by using this method.
     *
     * @param methodName
     * @param successCallback
     * @param failureCallback
     */
    setCallback: function (methodName, successCallback, failureCallback) {
      if (successCallback)
      {
        this.successCallbacks[methodName] = successCallback;
      }
      if (failureCallback)
      {
        this.failureCallbacks[methodName] = failureCallback;
      }
    },

    /**
     * Performs a HTTP GET request against the url.
     *
     * Success or failure callbacks will be called afterwards based on the method name
     * and success or failure events will be fired based on the event name.
     *
     * @param url The rest api url
     * @param responseObj Object to pass to the callback
     * @param methodName The name of the service method
     * @param eventName The base name of the service event
     */
    jsonGet: function (url, responseObj, methodName, eventName) {
      this.jsonRequest(Activiti.service.Ajax.GET, url, null, responseObj, methodName, eventName);
    },

    /**
     * Performs a HTTP POST request against the url with dataObj serialised to a json string as the body value.
     *
     * Success or failure callbacks will be called afterwards based on the method name
     * and success or failure events will be fired based on the event name.
     *
     * @param url The rest api url
     * @param dataObj The body object that will be serialized to a json string.
     * @param responseObj Object to pass to the callback
     * @param methodName The name of the service method
     * @param eventName The base name of the service event
     */
    jsonPost: function (url, dataObj, responseObj, methodName, eventName) {
      this.jsonRequest(Activiti.service.Ajax.POST, url, dataObj, responseObj, methodName, eventName);
    },

    /**
     * Performs a HTTP PUT request against the url with dataObj serialised to a json string as the body value.
     *
     * Success or failure callbacks will be called afterwards based on the method name
     * and success or failure events will be fired based on the event name.
     *
     * @param url The rest api url
     * @param dataObj The body object that will be serialized to a json string.
     * @param responseObj Object to pass to the callback
     * @param methodName The name of the service method
     * @param eventName The base name of the service event
     */
    jsonPut: function (url, dataObj,responseObj, methodName, eventName) {
      this.jsonRequest(Activiti.service.Ajax.PUT, url, dataObj, responseObj, methodName, eventName);
    },


    /**
     * Performs a HTTP DELETE request against the url.
     *
     * Success or failure callbacks will be called afterwards based on the method name
     * and success or failure events will be fired based on the event name.
     *
     * @param url The rest api url
     * @param responseObj Object to pass to the callback
     * @param methodName The name of the service method
     * @param eventName The base name of the service event
     */
    jsonDelete: function (url, responseObj, methodName, eventName) {
      this.jsonRequest(Activiti.service.Ajax.DELETE, url, null, responseObj, methodName, eventName);
    },


    /**
     * Performs a GET http request against the url.
     *
     * Success or failure callbacks will be called afterwards based on the method name
     * and success or failure events will be fired based on the event name.
     *
     * @param method THe HTTP method to use
     * @param url The rest api url
     * @param dataObj The body object that will be serialized to a json string (Shall be null for GET & DELETE).
     * @param responseObj Object to pass to the callback (May be null for GET)
     * @param methodName The name of the service method
     * @param eventName The base name of the service event
     */
    jsonRequest: function (method, url, dataObj, responseObj, methodName, eventName) {
      Activiti.service.Ajax.jsonRequest({
        method: method,
        url: url,
        dataObj: dataObj,
        successCallback: {
          fn: this.successDispatcher,
          scope: this,
          obj: {
            methodName: methodName,
            eventName: eventName + "_SUCCESS",
            responseObj: responseObj
          }
        },
        failureCallback: {
          fn: this.failureDispatcher,
          scope: this,
          obj: {
            methodName: methodName,
            eventName: eventName + "_FAILURE",
            responseObj: responseObj
          }
        }
      });
    },

    /**
     * Called after a successful HTTP request.
     *
     * Success callbacks will be called afterwards based on the obj.methodName
     * and success events will be fired based on the obj.eventName.

     * @param response
     * @param obj
     */
    successDispatcher: function (response, obj)
    {
      var designatedCallback = this.successCallbacks[obj.eventName],
        result = null;
      if (designatedCallback) {
        result = designatedCallback.fn.call(designatedCallback.scope ? designatedCallback.scope : this, response, response.json);
      }
      else {
        var callbackFn = this.callbackHandler["on" + obj.methodName.substring(0, 1).toUpperCase() + obj.methodName.substring(1) + "Success"];
        if (YAHOO.lang.isFunction(callbackFn)) {
          result = callbackFn.call(this.callbackHandler, response, obj.responseObj);
        }
        else {
          callbackFn = this.callbackHandler["onSuccess"];
          if (YAHOO.lang.isFunction(callbackFn)) {
            result = callbackFn.call(this.callbackHandler, response, obj.responseObj);
          }
          else {
            Activiti.service.Ajax.displaySuccessMessage(this.name + "." + obj.methodName + ".SUCCESS", response);
          }
        }
      }
      if (result == false) {
        // The callback explicitly told the service NOT to dispatch an event
      }
      else if (Activiti.event) {
        // Dispatch an event if event package is present
        Activiti.event.fire(obj.eventName, response.json, this.callbackHandler);
      }
    },

    /**
     * Called after a successful HTTP request.
     *
     * Failure callbacks will be called afterwards based on the obj.methodName
     * and failure events will be fired based on the obj.eventName.

     * @param response
     * @param obj
     */
    failureDispatcher: function (response, obj)
    {
      var designatedCallback = this.failureCallbacks[obj.methodName],
        result = null;
      if (designatedCallback) {
        result = designatedCallback.fn.call(designatedCallback.scope ? designatedCallback.scope : this, response, response.json, designatedCallback.fn.obj);
      }
      else {
        var callbackFn = this.callbackHandler["on" + obj.methodName.substring(0, 1).toUpperCase() + obj.methodName.substring(1) + "Failure"];
        if (YAHOO.lang.isFunction(callbackFn)) {
          result = callbackFn.call(this.callbackHandler, response, obj.responseObj);
        }
        else {
          callbackFn = this.callbackHandler["onFailure"];
          if (YAHOO.lang.isFunction(callbackFn)) {
            result = callbackFn.call(this.callbackHandler, response, obj.responseObj);
          }
          else {
            Activiti.service.Ajax.displayFailureMessage(this.name + "." + obj.methodName + ".FAILURE", response);
          }
        }
      }
      if (result == false) {
        // The callback explicitly told the service NOT to dispatch an event
      }
      else if (Activiti.event) {
        // Dispatch an event if event package is present
        Activiti.event.fire(obj.eventName, response.json, this.callbackHandler);
      }
    }
  }
})();

/**
 * Activiti ProcessService.
 *
 * @namespace Activiti.service
 * @class Activiti.service.ProcessService
 */
(function()
{
  /**
   * ProcessService constructor.
   *
   * @parameter handler {object} The response handler object
   * @return {Activiti.service.ProcessService} The new Activiti.service.ProcessService instance
   * @constructor
   */
  Activiti.service.ProcessService = function ProcessService_constructor(callbackHandler)
  {
    Activiti.service.ProcessService.superclass.constructor.call(this, "Activiti.service.ProcessService", callbackHandler);
    return this;
  };


  /**
   * Event constants
   */
  YAHOO.lang.augmentObject(Activiti.service.ProcessService,
  {
    event: {
      START_PROCESS_SUCCESS: "Activiti.service.ProcessService.event.START_PROCESS_SUCCESS",
      START_PROCESS_FAILURE: "Activiti.service.ProcessService.event.START_PROCESS_FAILURE"
    }
  });

  YAHOO.extend(Activiti.service.ProcessService, Activiti.service.RestService,
  {
    /**
     * Helper event base constants
     *
     * @private
     */
    _events: {
      START_PROCESS: "Activiti.service.ProcessService.event.START_PROCESS"
    },

    /**
     * Gets a task summary for an assignee
     *
     * @method startProcess
     * @param processDefinitionId {string} The id of the process definition to start a process for
     * @param obj {Object} Helper object to be sent to the callback
     */
    startProcess: function ProcessService_startProcess(processDefinitionId, obj)
    {
      this.jsonPost(this.startProcessURL(processDefinitionId), {
        processDefinitionId: processDefinitionId
      }, obj, "startProcess", this._events.START_PROCESS);
    },

    /**
     * Creates the PUT url to use when starting a process
     *
     * @method startProcessURL
     * @return {string} The url
     */
    startProcessURL: function ProcessService_startProcessURL()
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "process-instance";
    }

  });
})();


/**
 * Activiti TaskService.
 *
 * @namespace Activiti.service
 * @class Activiti.service.TaskService
 */
(function()
{
  /**
   * TaskService constructor.
   *
   * @parameter handler {object} The response handler object
   * @return {Activiti.service.TaskService} The new Activiti.service.TaskService instance
   * @constructor
   */
  Activiti.service.TaskService = function TaskService_constructor(callbackHandler)
  {
    Activiti.service.TaskService.superclass.constructor.call(this, "Activiti.service.TaskService", callbackHandler);
    return this;
  };

  /**
   * Event constants
   */
   YAHOO.lang.augmentObject(Activiti.service.TaskService,
   {
     event: {
       LOAD_TASK_SUMMARY_SUCCESS: "Activiti.service.TaskService.event.LOAD_TASK_SUMMARY_SUCCESS",
       LOAD_TASK_SUMMARY_FAILURE: "Activiti.service.TaskService.event.LOAD_TASK_SUMMARY_FAILURE",
       LOAD_TASKS_SUCCESS: "Activiti.service.TaskService.event.LOAD_TASKS_SUCCESS",
       LOAD_TASKS_FAILURE: "Activiti.service.TaskService.event.LOAD_TASKS_FAILURE",
       CLAIM_TASK_SUCCESS: "Activiti.service.TaskService.event.CLAIM_TASK_SUCCESS",
       CLAIM_TASK_FAILURE: "Activiti.service.TaskService.event.CLAIM_TASK_FAILURE",
       COMPLETE_TASK_SUCCESS: "Activiti.service.TaskService.event.COMPLETE_TASK_SUCCESS",
       COMPLETE_TASK_FAILURE: "Activiti.service.TaskService.event.COMPLETE_TASK_FAILURE"
     }
   });

  YAHOO.extend(Activiti.service.TaskService, Activiti.service.RestService,
  {
    /**
     * Helper event base constants
     *
     * @private
     */
    _events: {
      LOAD_TASK_SUMMARY: "Activiti.service.TaskService.event.LOAD_TASK_SUMMARY",
      LOAD_TASKS: "Activiti.service.TaskService.event.LOAD_TASKS",
      CLAIM_TASK: "Activiti.service.TaskService.event.CLAIM_TASK",
      COMPLETE_TASK: "Activiti.service.TaskService.event.COMPLETE_TASK"
    },

    /**
     * Loads a task summary for an assignee
     *
     * @method loadTaskSummary
     * @param user {string} The id of the user to load the summary for
     * @param obj {Object} Helper object to be sent to the callback
     */
    loadTaskSummary: function TaskService_loadTaskSummary(user, obj)
    {
      this.jsonGet(this.loadTaskSummaryURL(user), obj, "loadTaskSummary", this._events.LOAD_TASK_SUMMARY);
    },

    /**
     * Creates the GET url used to load the task summary
     *
     * @method loadTaskSummary
     * @param user {string} The id of the user to load the summary for
     * @return {string} The url
     */
    loadTaskSummaryURL: function TaskService_loadTaskSummaryURL(user)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tasks-summary?user=" + encodeURIComponent(user);
    },

    /**
     * Loads a task summary for an assignee
     *
     * @method loadTasks
     * @param filters {string} The filters to use in teh search
     * @param obj {Object} Helper object to be sent to the callback
     */
    loadTasks: function TaskService_loadTasks(filters, obj)
    {
      this.jsonGet(this.loadTasksURL(filters), obj, "loadTasks", this._events.LOAD_TASKS);
    },

    /**
     * Creates the GET url used to get the task
     *
     * @method loadTasksURL
     * @param filters {string} The filters to use in the search
     * @param filters.assignee {string} The user id for the assigned user of the task
     * @param filters.candidate {string} The user id for the candidate for the unassigned tasks
     * @param filters.startIndex {number} Pagination start
     * @param filters.pageSize {number} Pagination size
     * @param filters.sort {string} Column key to sort after
     * @param filters.dir {string} Sort order ("asc"|"desc")
     * @return {string} The url
     */
    loadTasksURL: function TaskService_loadTasksURL(filters)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "tasks?",
          validFilters = ["assignee", "candidate", "candidate-group", "startIndex", "pageSize", "sort", "dir"];
      for (var filter in filters) {
        if (Activiti.util.arrayContains(validFilters, filter)) {
          url += (url[url.length - 1] == "?" ? "" : "&") + encodeURIComponent(filter) + "=" + encodeURIComponent(filters[filter]);
        }
        else {
          throw new Error("'" + filter + "' is not a valid filter");
        }
      }
      return url;
    },

    /**
     * Claims a task for the user
     *
     * @method claimTask
     * @param taskId {string} The task id of the task to claim
     * @param user {string} The user id of the assignee to the summary for
     * @param obj {Object} Helper object to be sent to the callback
     */
    claimTask: function TaskService_claimTask(taskId, user, obj)
    {
      this.jsonPut(this.claimTaskURL(taskId), {}, obj, "claimTask", this._events.CLAIM_TASK);
    },

    /**
     * Creates the PUT url used to claim the task
     *
     * @method claimTaskURL
     * @param taskId {string} The task id of the task to claim
     * @return {string} The url
     */
    claimTaskURL: function TaskService_claimTaskURL(taskId)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "task/" + encodeURIComponent(taskId) + "/claim";
    },

    /**
     * Claims a task for the user
     *
     * @method completeTask
     * @param taskId {string} The task id of the task to complete
     * @param user {string} The user id of the assignee to the summary for
     * @param obj {Object} Helper object to be sent to the callback
     */
    completeTask: function TaskService_completeTask(taskId, user, obj)
    {
      this.jsonPut(this.completeTaskURL(taskId), {}, obj, "completeTask", this._events.COMPLETE_TASK);
    },

    /**
     * Creates the PUT url used to complete the task
     *
     * @method completeTaskURL
     * @param taskId {string} The task id of the task to complete
     * @return {string} The url
     */
    completeTaskURL: function TaskService_completeTaskURL(taskId)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "task/" + encodeURIComponent(taskId) + "/complete";
    }

  });
})();
