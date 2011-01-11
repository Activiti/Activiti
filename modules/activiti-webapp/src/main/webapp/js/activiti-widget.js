/**
 * Activiti top-level widget namespace.
 *
 * @namespace Activiti
 * @class Activiti.widget
 */
Activiti.widget = Activiti.widget || {};

/**
 * Wrapper to create a YUI Button with common attributes.
 * All supplied object parameters are passed to the button constructor
 * e.g. Alfresco.util.createButton(this, "OK", this.onOK, {type: "submit"});
 *
 * @method Alfresco.util.createButton
 * @param component {object} Component containing button; must have "id" parameter
 * @param name {string} Dom element ID of markup that button is created from {p_scope.id}-{name}
 * @param callback {function} If supplied, registered with the button's click event
 * @param obj {object} Optional extra object parameters to pass to button constructor which also will be available as callback value
 * @param el {string|HTMLElement} Optional and accepts a string to use as an ID for getting a DOM reference or an actual DOM reference
 * @return {YAHOO.widget.Button} New Button instance
 * @static
 */
Activiti.widget.createButton = function(component, name, callback, obj, el)
{
  // Default button parameters
  var config =
  {
    type: "button",
    disabled: false
  };

  // Any extra parameters?
  if (typeof obj == "object")
  {
    config = YAHOO.lang.merge(config, obj);
  }

  // Fix-up the menu element ID
  if (typeof config.menu == "string")
  {
    config.type = "menu";
    config.menu = component.id + "-" + config.menu;
  }

  // Create the button
  var oElement = el ? el : component.id + "-" + name,
      button = null;

  if (YUIDom.get(oElement) !== null)
  {
    button = new YAHOO.widget.Button(oElement, obj);

    if (typeof button == "object")
    {
      // Register the click listener if one was supplied
      if (typeof callback == "function")
      {
        // Special case for a menu
        if (config.type == "menu")
        {
          button.getMenu().subscribe("click", callback, component, obj || true);
          button.getMenu().subscribe("keydown", function (p_sType, p_aArgs, p_oObj)
          {
            if (p_aArgs[0].keyCode == YAHOO.event.KeyListener.KEY.ENTER)
            {
              this.hide();
              p_oObj.fn.call(p_oObj.scope, p_sType, p_oObj.obj || p_aArgs);
            }
          },
          {
            scope: component,
            fn: callback,
            obj: obj
          });
        }
        else
        {
          button.on("click", callback, obj || button, component);
        }
      }

      // Special case if htmlName was passed-in as an option
      if (typeof config.htmlName != "undefined")
      {
        button.get("element").getElementsByTagName("button")[0].name = config.htmlName;
      }
    }
  }
  return button;
};

/**
 * Wrapper to create a YUI Button with common attributes.
 * All supplied object parameters are passed to the button constructor
 * e.g. Alfresco.util.createButton(this, "OK", this.onOK, {type: "submit"});
 *
 * @method Alfresco.util.createCellButton
 * @return {YAHOO.widget.Button} New Button instance
 * @static
 */
Activiti.widget.createCellButton = function(component, cell, label, css, method, data, dataTable)
{
  var cellButton = new YAHOO.widget.Button({
    label: label,
    id: Activiti.util.generateDomId(),
    container: cell
  });
  cellButton.addClass(css);
  cellButton.on("click", function(e, obj)
  {
    obj.method.call(this, obj.data, obj.dataTable, obj.button, obj.cell);
  },
  {
    cell: cell, 
    data: data,
    dataTable: dataTable,
    button: cellButton,
    method: method
  }, component);
  return cellButton;
};

/**
 * Wrapper to create a YUI Dialog with common attributes.
 * e.g. Alfresco.util.createDialog(this, "OK");
 *
 * @method Alfresco.util.createSubmitDialog
 * @param component {object} Component containing form
 * @param name {string} Dom element ID of markup that dialog is created from {component.id}-{name}
 * @return {YAHOO.widget.Dialog} New Dialog instance
 * @static
 */
Activiti.widget.createSubmitDialog = function(component, name)
{
  var dialog = new YAHOO.widget.Dialog(component.id + "-" + name,
  {
    fixedcenter: true,
    visible: false,
    constraintoviewport: true,
    modal: true,
    hideaftersubmit: false,
    buttons: [
      { text: Activiti.i18n.getMessage("button.ok") , handler: { fn: function () { this.submit(); } }, isDefault:true },
      { text: Activiti.i18n.getMessage("button.cancel"), handler: { fn: function() { this.cancel(); } } }
    ]
  });
  dialog.render();
  return dialog;
};

/**
 * Provides a common interface for displaying popups in various forms
 *
 * @class Activiti.widget.PopupManager
 */
Activiti.widget.PopupManager = function()
{
  /**
   * Short cuts
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
     *    effect: {YAHOO.widget.ContainerEffect}, // the effect to use when showing and hiding the message,
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
          handler: function()
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
     * Displays an error message
     *
     * @method displayError
     * @param text {String}
     * @param title {String} (Optional) Will default to "label.failure"
     */
    displayError: function(text, title) {
      this.displayPrompt({
        icon: "error",
        title: title ? title : Activiti.i18n.getMessage("label.failure"),
        text: text,
        close: true,
        buttons: []
      });

    },

    /**
     * Displays an error message
     *
     * @method displayError
     * @param url {String} The url to the image to display
     */
    displayImage: function(url, parent) {
      var imgEl = document.createElement("img");
      imgEl.onload = function() {
        var p = new YAHOO.widget.Panel(Activiti.util.generateDomId(), {
          close:true,
          visible:false,
          draggable:true,
          modal: true,
          constraintoviewport: true,
          fixedcenter: true,
          effect:
          {
            effect:YAHOO.widget.ContainerEffect.FADE,
            duration: 1
          }
        });
        var kl = new YAHOO.util.KeyListener(document, {
          keys: YAHOO.util.KeyListener.KEY.ESCAPE
        }, {
          fn: p.hide,
          scope: p,
          correctScope:true
        });
        p.cfg.queueProperty('keylisteners', kl);
        p.setBody('<img src="' + url + '"/>');
        p.render(document.body);
        p.show();
      };
      imgEl.setAttribute('src', url);
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
   * The Activiti datatable constructor
   *
   * @param id {string} The components id
   * @param callbackHandler The object that shall implement the callbacks:
   *        onDataTableCreateURL       - Called to get the url to use when loading the data
   *        onDataTableRenderCell<Key> - (Optional) Called to render the column with key <Key> (defined in listColumnDefs)
   *        onDataTableRenderCell      - (Optional) Called if there is no specific renderer for the column's key
   *        onAction<Name>             - (Optional) Called if a link with a 'class' with name onAction<Name>, i.e. onActionDelete
   * @param events {Array} The names of the events to listen for that shall contain filter attributes
   * @param dataTableElId {string} The id of the HTMLElement in which to create the data table
   * @param paginationElIds {Array} The ids of the HTMLElements in which to create paginators
   * @param listColumnDefs {Array} The columns defined as columnDefs for YAHOO.widget.DataTable
   * @param listFieldKeys {Array} (Optional) The data field keys in the data response from the server
   */
  Activiti.widget.DataTable = function DataTable_constructor(id, callbackHandler, events, dataTableElId, paginationElIds, listColumnDefs, listFieldKeys)
  {
    if (!callbackHandler || !events || !dataTableElId || !paginationElIds || !listColumnDefs) {
      throw new Error("Mandatory parameters are missing ");
    }
    this.id = id;
    this._eventPatterns = {};
    this._callbackHandler = callbackHandler;
    this._dataTable = {};
    this._dataSource = {};
    this._currentEventName = null;
    this._currentEventValue = {};
    this._init(events, dataTableElId, paginationElIds, listColumnDefs, listFieldKeys);
    return this;
  };

  Activiti.widget.DataTable.prototype = {

    /**
     * This components unique id.
     *
     * @type string
     * @private
     */
    id: null,

    /**
     * The events that this component shall listen for and the values it must have to be relevant
     *
     * @type Object
     * @private
     */
    _eventPatterns: null,

    /**
     * The callback handler that implements the callback methods.
     *
     * @type object
     * @private
     */
    _callbackHandler: null,

    /**
     * The data table.
     *
     * @type YAHOO.widget.DataTable
     * @private
     */
    _dataTable: null,

    /**
     * The data source.
     *
     * @type YAHOO.util.DataSource
     * @private
     */
    _dataSource: null,

    /**
     * The current event's name that will get re-fired on paging and sorting.
     *
     * @type string
     * @private
     */
    _currentEventName: null,

    /**
     * The current event's value that contains the actual info to get the data (excluding the pagination parameters).
     * Will be re-fired with pagination/filter parameters on paging and sorting.
     *
     * @type object
     * @private
     */
    _currentEventValue: null,

    /**
     * Initialises the data table, paginators and browser history navigation.
     *
     * @param events {Array} The events to listen for
     * @param dataTableElId {string} The id of the element in which to create the data table
     * @param paginationElIds {string} The id of the elements in which to create the paginators
     * @param listColumnDefs {Array} The columns to create in the data table
     * @param listFieldKeys {Array} (Optional) The field keys that match the json response from the server
     * @private
     */
    _init: function (events, dataTableElId, paginationElIds, listColumnDefs, listFieldKeys)
    {
      var me = this;

      // Create the DataSource
      me._dataSource = new YAHOO.util.DataSource("");
      me._dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
      me._dataSource.responseSchema = {
        resultsList: "data",
        fields: listFieldKeys,
        metaFields: {
          totalRecords: "total",
          paginationRecordOffset : "start",
          paginationRowsPerPage : "size",
          sortKey: "sort",
          sortDir: "order"
        }
      };

      // Create the Paginator
      me._paginator = new YAHOO.widget.Paginator({
        rowsPerPage: 10,
        recordOffset: 0,
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

          if (YAHOO.lang.isFunction(me._callbackHandler["onDataTableRenderCell" + listColumnDefs[i].key.substring(0, 1).toUpperCase() + listColumnDefs[i].key.substring(1)]) ||
              YAHOO.lang.isFunction(me._callbackHandler["onDataTableRenderCell"])) {
            listColumnDefs[i].formatter = function(el, oRecord, oColumn, oData)
            {
              // Apply widths on each cell so it actually works as given in the column definitions
              if (oColumn.width) {
                YUIDom.setStyle(el, "width", oColumn.width + "px");
                YUIDom.setStyle(el.parentNode, "width", oColumn.width + "px");
              }

              var fieldKey = oColumn.getField(),
                  defaultMethodName = "onDataTableRenderCell",
                  methodName = defaultMethodName + fieldKey.substring(0, 1).toUpperCase() + fieldKey.substring(1);
              if (YAHOO.lang.isFunction(me._callbackHandler[methodName])) {
                me._callbackHandler[methodName].call(me._callbackHandler, me, el, oRecord, oColumn, oData);
              }
              else if (YAHOO.lang.isFunction(me._callbackHandler[defaultMethodName])) {
                me._callbackHandler[defaultMethodName].call(me._callbackHandler, me, el, oRecord, oColumn, oData);
              }
            };
          }
          else {
            listColumnDefs[i].formatter = function(el, oRecord, oColumn, oData)
            {
              // Make sure we encode data so no xss code can run
              el.innerHTML = Activiti.util.encodeHTML(oData);
            };
          }
        }
      }

      // Instantiate DataTable
      me._dataTable = new YAHOO.widget.DataTable(dataTableElId, listColumnDefs, me._dataSource, {
        paginator : me._paginator,
        dynamicData : true,
        initialLoad : false,
        className: this._getDataTableClassName()
      });

      // Show loading message while page is being rendered
      me._dataTable.showTableMessage(me._dataTable.get("MSG_LOADING"), YAHOO.widget.DataTable.CLASS_LOADING);

      // Define a custom function to route sorting as bookmarked events
      var handleSorting = function (oColumn) {
        // Calculate next sort direction for given Column and reflect the new sort values while preserving existing pagination
        var order = this.getColumnSortDir(oColumn);
        order = order ? order.substring("yui-dt-".length) : null;
        me.fireEvent(0, oColumn.key, order, this.get("paginator").getRowsPerPage());
      };
      me._dataTable.sortColumn = handleSorting;

      // Define a custom function to route pagination through the Browser History Manager
      var handlePagination = function(state) {
        // Reflect the new pagination values while preserving existing sort values
        var sortedBy = this.get("sortedBy") || {};
        if (sortedBy.dir && sortedBy.dir.indexOf("yui-dt-") == 0) {
          sortedBy.dir = sortedBy.dir.substring("yui-dt-".length);
        }
        me.fireEvent(state.recordOffset, sortedBy.key, sortedBy.dir, state.rowsPerPage);
      };

      // First we must unhook the built-in mechanism...
      me._paginator.unsubscribe("changeRequest", me._dataTable.onPaginatorChangeRequest);

      // ...then we hook up our custom function
      me._paginator.subscribe("changeRequest", handlePagination, me._dataTable, true);

      // Update payload data with latest values from server
      me._dataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {

        // Convert from server value to DataTable format
        var meta = oResponse.meta;
        oPayload.totalRecords = meta.totalRecords || oPayload.totalRecords;
        oPayload.pagination = {
          //rowsPerPage: meta.paginationRowsPerPage || 10,
          recordOffset: meta.paginationRecordOffset || 0
        };
        oPayload.sortedBy = {};
        if (me._dataTable.getColumn(meta.sortKey)) {
          oPayload.sortedBy.key = meta.sortKey;
          oPayload.sortedBy.dir = (meta.sortDir) ? "yui-dt-" + meta.sortDir : "yui-dt-asc"
        }
        return true;
      };

      // Now that we are ready we may handle events
      for (var ei = 0, eil = events.length; ei < eil; ei++) 
      {
        // Listen for events that shall trigger reloads
        this._eventPatterns[events[ei].event] = events[ei].value;
        Activiti.event.on(events[ei].event, this.onEvent, this)
      }

      //Subscribe to events bubbled up to the YAHOO.widget.DataTable object
      this._dataTable.subscribe("linkClickEvent", this.onLinkClickEvent, null, this);
    },

    /**
     * Returns the css class name to use for the yui datatable
     */
    _getDataTableClassName: function()
    {
      return "activiti-datatable";
    },

    /**
     * Called when table is paginated or sorted and will fire an event so it gets put in the browser history.
     * Afterwards onEvent will be called so the actual data can be loaded.
     *
     * @method fireEvent
     * @param start {int}
     * @param sort {string}
     * @param order {string}
     * @param size {int}
     */
    fireEvent: function (start, sort, order, size)
    {
      var value = Activiti.util.deepCopy(this._currentEventValue);
      value.start = start;
      value.sort = sort;
      value.order = order;
      value.size = size;
      value.owner = this.id;
      Activiti.event.fire(this._currentEventName, value, null, true);
    },

    /**
     * Called when an event, that this component was instructed to listen for, is triggered.
     * The event shall contain filter/pagination attributes.
     *
     * @method onEvent
     * @param event {object} The event that was triggered
     * @param args {Array} The event values
     */
    onEvent: function (event, args)
    {
      // Save event and ask callbackHandler to generate the url for us and the load the data table
      if (Activiti.util.objectMatchesPattern(Activiti.event.getValue(args), this._eventPatterns[event])) {
        this._currentEventName = event;
        this._currentEventValue = Activiti.event.getValue(args);
        this.reload();
      }
    },

    /**
     * Called when a link inside the data table has been clicked.
     * Will look at the clicked link and see if it has a class of name "onAction<Name>".
     * If so it will call the callbackHandler if it has such a callback and pass in the row's data as value.
     * 
     * @param {Object} event Event Object
     * @param {Object} args Object of arguments
     */
    onLinkClickEvent: function(event, args)
    {
      if (event.target.tagName.toLowerCase() == "a")
      {
        var classNames = event.target.getAttribute("class").split(" "),
          className,
          data,
          stopEvent = 0;
        for (var i = 0, il = classNames.length; i < il; i++)
        {
          className = classNames[i];
          if (className.match(/^onAction.*$/))
          {

            if (YAHOO.lang.isFunction(this._callbackHandler[className]))
            {
              data = this._dataTable.getRecord(event.target.offsetParent).getData();
              this._callbackHandler[className].call(this._callbackHandler, data, this);
            }
          }
        }
        YAHOO.util.Event.stopEvent(event.event || window.event);
        return false;
      }
    },

    /**
     * Reloads the datatable with the current pagination settings
     */
    reload: function ()
    {
      // Use the url to load data from the server
      var url = this._callbackHandler.onDataTableCreateURL.call(this._callbackHandler, this, this._currentEventName, this._currentEventValue, this);
      this._dataSource.sendRequest(url, {
        success : this._dataTable.onDataReturnSetRows,
        failure : this._dataTable.onDataReturnSetRows,
        scope : this._dataTable,
        argument : {} // Pass in container for population at runtime via doBeforeLoadData
      });
    }
  }
})();

(function()
{

  /**
   * The Activiti datatable constructor
   *
   * @param id {string} The components id
   * @param callbackHandler The object that shall implement the callbacks
   * @param events {Array} The names of the events to listen for that shall contain filter attributes
   * @param dataTableElId {string} The id of the HTMLElement in which to create the data table
   * @param paginationElIds {Array} The ids of the HTMLElements in which to create paginators
   * @param listColumnDefs {Array} The columns defined as columnDefs for YAHOO.widget.DataTable
   * @param listFieldKeys {Array} (Optional) The data field keys in the data response from the server
   * @return {Activiti.widget.DataList} The new DataList instance
   * @constructor
   */
  Activiti.widget.DataList = function TaskList_constructor(id, callbackHandler, events, dataTableElId, paginationElIds, listColumnDefs, listFieldKeys)
  {
    Activiti.widget.DataList.superclass.constructor.call(this, id, callbackHandler, events, dataTableElId, paginationElIds, listColumnDefs, listFieldKeys);
    return this;
  };

  YAHOO.extend(Activiti.widget.DataList, Activiti.widget.DataTable, {

    /**
     * Returns the css class name to use for the yui datatable
     */
    _getDataTableClassName: function()
    {
      return "activiti-datalist";
    }

  });

})();

(function()
{

  /**
   * Short cuts
   */
  var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector,
      KeyListener = YAHOO.util.KeyListener,
      $msg = Activiti.i18n.getMessage;

  /**
   *
   * @param id {string} The components id
   */
  Activiti.widget.Form = function Form_constructor(id)
  {
    if (!id) {
      throw new Error("Mandatory parameters are missing ");
    }
    this.id = id;
    this.url = null;
    this.orgValuesForEl = {};
    this.dialog = null;
    return this;
  };

  Activiti.widget.Form.prototype = {

    /**
     * This components unique id.
     *
     * @type string
     * @private
     */
    id: null,

    dialog: null,

    service: null,

    originalAttributesFor: null,

    /**
     * Called when a task form existed and has been successfully loaded.
     *
     * @method onLoadFormSuccess
     * @param response {Object} The server response
     * @param obj {Object} The callback object
     */
    onLoadFormSuccess: function Form_onLoadFormSuccess(response, obj)
    {
      var dialog = document.createElement("div");
      dialog.innerHTML = '' +
          '<div class="bd">' +
          '  <form class="activiti-form">' + response.serverResponse.responseText + '</form>' +
          '</div>';

      // Use a Dialog (instead of a Panel) to take use of it's getData method
      this.dialog = new YAHOO.widget.Dialog(dialog, {
        fixedcenter: true,
        visible: false,
        constraintoviewport: true,
        modal: true,
        buttons: [
          { text: $msg("button.ok") , handler: { fn: this.onSubmit, scope: this }, isDefault:true },
          { text: $msg("button.cancel"), handler: { fn: this.onCancel, scope: this } }
        ]
      });

      // Render the Dialog
      this.dialog.render(document.body);

      // Add validations and save original attributes (title)
      var data = this.getData(),
        applyTabIndex = Selector.query("[tabindex]", this.dialog.form).length == 0,
        inputEl, title;
      for (var attr in data) {
				if (data.hasOwnProperty(attr)) {
					if (attr.lastIndexOf("_") < 0) {
						this.orgValuesForEl[attr] = {};
						inputEl = Selector.query("[name=" + attr + "]", this.dialog.form, true);
						if (inputEl) {
              if (applyTabIndex) {
                inputEl.setAttribute("tabindex", "0");
              }
              if (inputEl.tagName.toLowerCase() == "input") {
                if (inputEl.getAttribute("type").toLowerCase() == "checkbox") {
                  Event.addListener(inputEl, "mouseup", this.doValidate, inputEl, this);
                }
                else {
                  Event.addListener(inputEl, "keyup", this.doValidate, inputEl, this);
                }
              }
              else if (inputEl.tagName.toLowerCase() == "select") {
                Event.addListener(inputEl, "change", this.doValidate, inputEl, this);
              }
              this.orgValuesForEl[attr].title =  inputEl.getAttribute("title") || null;
            }
          } else
			 		{
			 			var attrName = attr.split("_");
			 			var attrMeta = attrName.length > 1 ? attrName[1] : null;
						if (attrMeta === "type" && data[attr] == "Date") {
							// set up date picker
							this.dateSetup(Selector.query("[name=" + attrName[0] + "]", this.dialog.form, true));
						} else if (attrMeta === "type" && data[attr] == "Event") {
							// set up custom event for file chooser
							this.addEventFieldsToForm(attrName[0], Selector.query("input[name^=" + attrName[0] + "]", this.dialog.form));
						}
			 		}
        }
      }

      // Make sure we catch the enter keys strokes and stops normal submits
      var me = this;
      var onEnterKeyEvent = function(id, keyEvent)
      {
        var event = keyEvent[1],
            target = event.target ? event.target : event.srcElement;

        if (target.tagName.toLowerCase() == "textarea")
        {
          // Allow line feeds in textareas
          return false;
        }
        else if (target.tagName.toLowerCase() == "button" || Dom.hasClass(target, "yuimenuitemlabel"))
        {
          // Event listeners for buttons and menus must be notified that the enter key was entered
        }
        else
        {
          var targetName = target.name;
          if (targetName && (targetName != "-"))
          {
            me.onSubmit();
          }
          Event.stopEvent(event);
          return false;
        }
      };
      var enterListener = new KeyListener(this.dialog.form,
      {
        keys: KeyListener.KEY.ENTER
      }, onEnterKeyEvent, YAHOO.env.ua.ie > 0 ? KeyListener.KEYDOWN : "keypress");
      enterListener.enable();

      // Display it
      this.dialog.show();

      // Run validations on empty form
      this.doValidate(null, null);

    },

    getData: function() {
      var data = this.dialog.getData();
      for (var attr in data) {
        if (data.hasOwnProperty(attr) && YAHOO.lang.isArray(data[attr])) {
          data[attr] = data[attr].length > 0 ? data[attr][0] : "";
        }
      }
      return data;
    },

    onSubmit: function() {
      if (this.doValidate(true)) {
        this.dialog.getButtons()[0].set("disabled", true);
        this.doSubmit(this.getData());
      }
    },

    doValidate: function(e, o) {
      // Validate data
      var errors = [],
          data = this.getData(),
          value, attrName, attrMeta, inputEl;
      for (var attr in data) {
        if (data.hasOwnProperty(attr)) {
          var errorMessage = null, requiredMessage = null, message = null;
          attrName = attr.split("_");
          attrMeta = attrName.length > 1 ? attrName[1] : null;
          attrName = attrName[0];
          value = data[attrName];
          inputEl = Selector.query("[name=" + attrName + "]", this.dialog.form, true);
          if (!inputEl) {
            Activiti.widget.PopupManager.displayError($msg("message.error.form-config.input.not-matching"));
            return;
          }
          if (attrMeta) {
            if (value && value.length > 0) {
              if (attrMeta == "type") {
                if (data[attr] == "Integer" && !(/^\d+$/.test(value))) {
                  errorMessage = $msg("message.error.invalid.Integer", value);
                }
                else if (data[attr] == "Boolean" && !(/^(true|false)$/.test(value))) {
                  errorMessage = $msg("message.error.invalid.Boolean", value);
                }
					 else if (data[attr] === "Date" && !Activiti.util.validDate(value)) {
					 	errorMessage = $msg("message.error.invalid.Date", value);
					 }
              }
            }
            else {
              if (attrMeta == "required" && data[attr] == "true") {
                if (YAHOO.lang.trim(value).length == 0) {
                  requiredMessage = $msg("message.error.required", value);
                }
              }
            }
            if (errorMessage) {
              Dom.addClass(inputEl, "error");
              message = errorMessage;
            }
            else if(requiredMessage) {
              Dom.addClass(inputEl, "required");
              message = requiredMessage;
            }
            if (message) {
              errors.push(attrName);
              var title = this.orgValuesForEl[attrName].title;
              inputEl.setAttribute("title", title ? title + " :: " + $msg("label.error", message) : message);
            }
          }
          else {
            if (!Activiti.util.arrayContains(errors, attrName)) {
              inputEl.setAttribute("title", this.orgValuesForEl[attrName].title);
              Dom.removeClass(inputEl, "error");
              Dom.removeClass(inputEl, "required");
            }
          }
        }
      }

      // Toggle buttons
      var valid = errors.length == 0;
      this.dialog.getButtons()[0].set("disabled", !valid);
      return valid;
    },

    onCancel: function() {
      this.dialog.destroy();
    },

    /**
     * Called when a task form didn't exist or failed to be loaded.
     *
     * @method onLoadTaskFormFailure
     * @param response {Object} The server response
     * @param obj {Object} The callback object
     */
    onLoadFormFailure: function Form_onLoadFormFailure(response, obj)
    {
      if (response.serverResponse.status == 404) {
        // There was no form defined, submit an empty form
        this.doSubmit({});
      }
      else {
        Activiti.widget.PopupManager.displayPrompt({ text: Activiti.i18n.getMessage("message.failure") });
      }
    },

    /**
     * Override this method to submit the form to the server
     *
     * @method doSubmitEmptyForm
     * @param variables {object} THe variables to submit from the form
     */
    doSubmit: function Form_doSubmit(variables){},

    /**
     * Called when a form was successfully submitted
     *
     * @method onSuccess
     * @param response {Object} The server response
     * @param obj {Object} The callback object
     */
    onSuccess: function Form_onSuccess(response, obj)
    {
      // Success message is displayed by service
      if (this.dialog) {
        this.dialog.destroy();
      }
    },

    /**
     * Called when a form failed to be submitted on server
     *
     * @method onFailure
     * @param response {Object} The server response
     * @param obj {Object} The callback object
     */
    onFailure: function Form_onSuccess(response, obj)
    {
      // Error message will be displayed by service
      if (this.dialog) {
        this.dialog.getButtons()[0].set("disabled", false);
      }
    },

	 /**
     * sets up the date picker if required (uses HTML5 input type=date otherwise)
     *
     * @method dateSetuo
     * @param dateEl {Dom Object} the date input element
     */
		dateSetup: function Form_dateSetup(dateEl)
		{
			if (!Activiti.support.inputDate) {
				//addcalendar pop up info & create bindings to populate date field
				var elementName = dateEl.name,
					buttonEl = document.createElement("a"),
					popupEl = document.createElement("div"),
					labelEl = Dom.getAncestorByTagName(dateEl, "label"),
					calendar = new YAHOO.widget.Calendar(popupEl, {close:true } ),
					zeroPad = Activiti.util.zeroPad;

				buttonEl.id = elementName + "_button";
				buttonEl.className = "datePicker";
				buttonEl.innerHTML = buttonEl.title = $msg("button.datePicker");

				Dom.addClass(labelEl, "date")

				popupEl.id = elementName + "_popup";
				Dom.addClass(popupEl, "datePickerPopup");

				Dom.insertAfter(buttonEl, dateEl);
				Dom.insertAfter(popupEl, buttonEl)

				calendar.render();
				Event.addListener(buttonEl, "click", calendar.show, calendar, true);

				function handleSelect(type,args,obj) {
		 			var dates = args[0], date = dates[0], year = date[0], month = date[1], day = date[2];
					dateEl.value = year + "-" + zeroPad(month) + "-" + zeroPad(day); // ISO8601
					calendar.hide();
					this.doValidate();
		 		}
		 		calendar.selectEvent.subscribe(handleSelect, calendar, this);
			}
		},
		
		/**
		 * This method adds a button next to the text input field and adds onFormEventButtonClick
		 * as event listener to the button. The onFormEventButtonClick method will then fire a 
		 * clickFormEventButton event.
     *
     * @method addEventFieldsToForm
     * @param inputName {String}
     * @param inputEls {Dom Objects} the label element that wraps the event input element
     */
		addEventFieldsToForm: function Form_addEventFieldsToForm(inputName, inputEls)
		{
			var spanEl = document.createElement("span"),
				buttonSpan1El = document.createElement("span"),
				buttonSpan2El = document.createElement("span"),
				buttonEl = document.createElement("a");
			
			// TODO: span should be highlighted like other mandatory fields...
			Dom.addClass(spanEl, "form-Event-value");
			spanEl.innerHTML = "...";
			Dom.addClass(buttonSpan1El, "file-chooser-button yui-button yui-push-button");
			Dom.addClass(buttonSpan2El, "first-child");
			Dom.addClass(buttonEl, "form-Event-" + inputName + "-button yui-button");
			buttonEl.innerHTML = buttonEl.title = $msg("button.fileChooser");

			Event.addListener(buttonEl, "click", this.onFormEventButtonClick, this, true);

			buttonSpan2El.appendChild(buttonEl);
			buttonSpan1El.appendChild(buttonSpan2El);

			for(var i=0; i<inputEls.length; i++) {
				if(inputEls[i].value == "Event") {
					inputEls[i].value = "String";
					Dom.insertAfter(spanEl, inputEls[i]);
					Dom.insertAfter(buttonSpan1El, spanEl);
				} else if(inputEls[i].type == "text") {
			    inputEls[i].type = "hidden";
			  }
			}
		},

		onFormEventButtonClick: function Form_onFormEventButtonClick(event, inputEl) {
      var me = this;
      Activiti.event.fire(Activiti.event.clickFormEventButton, {"callback": function (args) {

        var connectorIdInput = Selector.query("input[name=targetConnectorId]", me.dialog.form, true);
        connectorIdInput.value = args.connectorId;
        
        var targetFolderId = Selector.query("input[name=targetFolderId]", me.dialog.form, true);
        targetFolderId.value = args.nodeId;
        targetFolderId.type = "hidden";
        
        var formEventValueSpan = Selector.query("span[class=form-Event-value]", me.dialog.form, true);
        formEventValueSpan.innerHTML = args.nodeName;
        
        me.doValidate();
      }}, null, false);
		}

  }
})();


/**
 * Activiti StartProcessInstanceForm.
 *
 * @namespace Activiti.widget
 * @class Activiti.widget.StartProcessInstanceForm
 */
(function()
{

  processDefinitionId: null,

  /**
   * StartProcessInstanceForm constructor.
   *
   * @parameter handler {object} The response handler object
   * @return {Activiti.widget.StartProcessInstanceForm} The new Activiti.widget.StartProcessInstanceForm instance
   * @constructor
   */
  Activiti.widget.StartProcessInstanceForm = function StartProcessInstanceForm_constructor(id, processDefinitionId)
  {
    Activiti.widget.StartProcessInstanceForm.superclass.constructor.call(this, id);
    this.processDefinitionId = processDefinitionId;
    this.service = new Activiti.service.ProcessService(this);
    this.service.setCallback("loadProcessDefinitionForm", { fn: this.onLoadFormSuccess, scope: this }, {fn: this.onLoadFormFailure, scope: this });
    this.service.loadProcessDefinitionForm(this.processDefinitionId);
    return this;
  };

  YAHOO.extend(Activiti.widget.StartProcessInstanceForm, Activiti.widget.Form,
  {

     /**
     * Start a process instance
     *
     * @method doSubmit
     */
    doSubmit: function StartProcessInstanceForm__doSubmit(variables)
    {
      this.service.startProcessInstance(this.processDefinitionId, variables);
    }

  });

})();

/**
 * Activiti CompleteTaskForm.
 *
 * @namespace Activiti.widget
 * @class Activiti.widget.CompleteTaskForm
 */
(function()
{

  taskId: null,

  /**
   * CompleteTaskForm constructor.
   *
   * @return {Activiti.widget.CompleteTaskForm} The new Activiti.widget.CompleteTaskForm instance
   * @constructor
   */
  Activiti.widget.CompleteTaskForm = function CompleteTaskForm_constructor(id, taskId, callbackObj)
  {
    Activiti.widget.CompleteTaskForm.superclass.constructor.call(this, id);
    this.taskId = taskId;
    this.service = new Activiti.service.TaskService(this);
    this.service.setCallback("loadCompleteTaskForm", { fn: this.onLoadFormSuccess, scope: this }, {fn: this.onLoadFormFailure, scope: this });
    this.service.loadCompleteTaskForm(this.taskId);
    return this;
  };

  YAHOO.extend(Activiti.widget.CompleteTaskForm, Activiti.widget.Form,
  {

     /**
     * Start a process instance form
     *
     * @method doSubmit
     */
    doSubmit: function CompleteTaskForm__doSubmit(variables)
    {
      this.service.completeTask(this.taskId, variables);
    }

  });

})();