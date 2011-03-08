(function() 
{
  /**
   * Shortcuts
   */
  var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      Event = YAHOO.util.Event,
      Pagination = Activiti.util.Pagination,
      $html = Activiti.util.decodeHTML;

  /**
   * Links constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.Links} The new component.Links instance
   * @constructor
   */
  Activiti.component.Links = function Links_constructor(htmlId, connectorId, nodeId, vFolderId, activeNavigationTabIndex, activeArtifactViewTabIndex)
  {
    Activiti.component.Links.superclass.constructor.call(this, "Activiti.component.Links", htmlId);

    if (!connectorId || !nodeId) {
      throw new Error("Mandatory parameters are missing ");
    }

    this.service = new Activiti.service.RepositoryService(this);

    this._connectorId = connectorId;
    this._nodeId = nodeId;
    this._vFolderId = vFolderId||'';
    this._activeNavigationTabIndex = activeNavigationTabIndex||0;
    this._activeArtifactViewTabIndex = activeArtifactViewTabIndex||0;

    this._linksDataTable = {};
    this._incomingLinksDataTable = {};

    return this;
  };

  YAHOO.extend(Activiti.component.Links, Activiti.component.Base,
  {
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Links_onReady()
    {
      var me = this;

      var linksElId = this.id + '-links-div',
      linksPaginationElId = this.id + '-links-pagination-div',
      incomingLinksPaginationElId = this.id + '-incoming-links-pagination-div',
      addLinkElId = this.id + 'add-link',
      incomingLinksElId = this.id + 'incoming-links-div';

      var el = document.getElementById(this.id);

      var linksWrapperEl = document.createElement("div");
      linksWrapperEl.setAttribute('class', 'links-wrapper');
      linksWrapperEl.innerHTML = '<h3>Outgoing Links</h3><div id="' + linksElId + '"></div><span id="' + linksPaginationElId + '"></span><span id="' + addLinkElId + '" class="yui-button"><span class="first-child"><button type="button">Add link</button></span></span>';

      el.appendChild(linksWrapperEl);

      var incomingLinksWrapperEl = document.createElement('div');
      incomingLinksWrapperEl.setAttribute('class', 'incoming-links-wrapper');
      incomingLinksWrapperEl.innerHTML = '<h3>Incoming Links</h3><div id="' + incomingLinksElId + '"></div><span id="' + incomingLinksPaginationElId + '"></span>';

      el.appendChild(incomingLinksWrapperEl);

      this._linksDataTable = new Activiti.widget.DataTable(
        this.id + "-links",
        {
          /**
           * Activiti.widget.DataTable-callback to construct the url to use to load data into the data table.
           *
           * @method onDataTableCreateURL
           * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
           * @param eventName The name of the event to create a url from
           * @param eventValue The event values to create a url from
           * @return A url, based on the event, to use when loading data into the data table
           */
          onDataTableCreateURL: function onDataTableCreateURL(dataTable, eventName, eventValue)
          {
            return me.service.loadArtifactLinksURL(eventValue);
          },
          
          /**
           * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Actions collumn
           *
           * @method onDataTableRenderCellAction
           * @param {Object} dataTable
           * @param {Object} el
           * @param {Object} oRecord
           * @param {Object} oColumn
           * @param {Object} oData
           */
          onDataTableRenderCellAction: function onDataTableRenderCellAction(dataTable, el, oRecord, oColumn, oData) {
            var actions = [],
            data = oRecord.getData();
            // TODO: i18n
            actions.push('<a href="#" class="onActionGoToArtifact" title="Go to ' + data["targetArtifact.label"] + '" tabindex="0">&nbsp;</a>');
            actions.push('<a href="#" class="onActionDeleteLink" title="Delete Link" tabindex="0">&nbsp;</a>');
            el.innerHTML = actions.join("");
          },

          /**
           * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Name collumn
           *
           * @method onDataTableRenderCellAction
           * @param {Object} dataTable
           * @param {Object} el
           * @param {Object} oRecord
           * @param {Object} oColumn
           * @param {Object} oData
           */
          onDataTableRenderCellName: function onDataTableRenderCellName(dataTable, el, oRecord, oColumn, oData) {
            var data = oRecord.getData();
            el.innerHTML = '<span class="' + me.getClassForContentType(data["targetArtifact.contentType"]) + ' artifact-name" title="' + data["targetArtifact.contentType"] + '">' + data["targetArtifact.label"] + '</span>';
          },

          /**
           * Event Listener for the delete link action.
           *
           * @param data {Object} The artifact link data
           * @param datatable {Activiti.widget.DataTable} The data table in which the link was clicked
           */
          onActionDeleteLink: function onActionDeleteLink(data, datatable) {
            me.service.deleteArtifactLink(data.id);
          },
          
          onActionGoToArtifact: function onActionGoToArtifact(data, datatable) {
            var connectorId = data["targetArtifact.connectorId"],
            nodeId = data["targetArtifact.nodeId"],
            artifactName = data["targetArtifact.label"];

            var eventDescriptor = {activeNavigationTabIndex: 1, activeArtifactViewTabIndex: 0, connectorId: connectorId, nodeId: nodeId, vFolderId: '', label: artifactName, file: true};
            if(datatable.id.indexOf('links-widget')  != -1) {
              var url = Activiti.constants.URL_CONTEXT + "start#event=" + Activiti.util.eventDescriptorToState('updateArtifactView', eventDescriptor);
              window.open(url);
            } else {
              me.fireEvent(Activiti.event.updateArtifactView, eventDescriptor, null, true);
            }
          }
        },
        [ { event: Activiti.event.displayLinks, value: {} } ],
        linksElId,
        [ linksPaginationElId ],
        [
          { key:"name", label: "Name", sortable:true },
          { key:"targetElementName", label: "Element", sortable:true },
          { key:"linkType",  label: "Type", sortable:true },
          { key:"action",  label: "Action", sortable:true }
        ],
        [
          { key:"id" },
          { key:"targetArtifact.connectorId" },
          { key:"targetArtifact.nodeId" },
          { key:"targetArtifact.label" },
          { key:"targetArtifact.contentType"},
          { key:"targetElementName" },
          { key:"linkType" }
        ]
      );

      this._incomingLinksDataTable = new Activiti.widget.DataTable(
        this.id + "-incoming-links",
        {
          /**
           * Activiti.widget.DataTable-callback to construct the url to use to load data into the data table.
           *
           * @method onDataTableCreateURL
           * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
           * @param eventName The name of the event to create a url from
           * @param eventValue The event values to create a url from
           * @return A url, based on the event, to use when loading data into the data table
           */
          onDataTableCreateURL: function onDataTableCreateURL(dataTable, eventName, eventValue)
          {
            return me.service.loadIncomingArtifactLinksURL(eventValue);
          },
          
          /**
           * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Actions collumn
           *
           * @method onDataTableRenderCellAction
           * @param {Object} dataTable
           * @param {Object} el
           * @param {Object} oRecord
           * @param {Object} oColumn
           * @param {Object} oData
           */
          onDataTableRenderCellAction: function onDataTableRenderCellAction(dataTable, el, oRecord, oColumn, oData) {
            var actions = [],
            data = oRecord.getData();
            // TODO: i18n
            actions.push('<a href="#" class="onActionGoToArtifact" title="Go to ' + data["sourceArtifact.label"] + '" tabindex="0">&nbsp;</a>');
            el.innerHTML = actions.join("");
          },

          /**
           * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Name collumn
           *
           * @method onDataTableRenderCellAction
           * @param {Object} dataTable
           * @param {Object} el
           * @param {Object} oRecord
           * @param {Object} oColumn
           * @param {Object} oData
           */
          onDataTableRenderCellName: function onDataTableRenderCellName(dataTable, el, oRecord, oColumn, oData) {
            var data = oRecord.getData();
            el.innerHTML = '<span class="' + me.getClassForContentType(data["sourceArtifact.contentType"]) + ' artifact-name" title="' + data["sourceArtifact.contentType"] + '">' + data["sourceArtifact.label"] + '</span>';
          },
          
          onActionGoToArtifact: function onActionGoToArtifact(data, datatable) {
            var connectorId = data["sourceArtifact.connectorId"],
            nodeId = data["sourceArtifact.nodeId"],
            artifactName = data["sourceArtifact.label"];
            
            var eventDescriptor = {activeNavigationTabIndex: 1, activeArtifactViewTabIndex: 0, connectorId: connectorId, nodeId: nodeId, vFolderId: '', label: artifactName, file: true};
            if(datatable.id.indexOf('links-widget')  != -1) {
              var url = Activiti.constants.URL_CONTEXT + "start#event=" + Activiti.util.eventDescriptorToState('updateArtifactView', eventDescriptor);
              window.open(url);
            } else {
              me.fireEvent(Activiti.event.updateArtifactView, eventDescriptor, null, true);
            }
          }
        },
        [ { event: Activiti.event.displayLinks, value: {} } ],
        incomingLinksElId,
        [ incomingLinksPaginationElId ],
        [
          { key:"name", label: "Name", sortable:true },
          { key:"sourceElementName", label: "Element", sortable:true },
          { key:"linkType",  label: "Type", sortable:true },
          { key:"action",  label: "Action", sortable:true }
        ],
        [
          { key:"id" },
          { key:"sourceArtifact.connectorId" },
          { key:"sourceArtifact.nodeId" },
          { key:"sourceArtifact.label" },
          { key:"sourceArtifact.contentType"},
          { key:"sourceElementName" },
          { key:"linkType" }
        ]
      );

      // Needed to load data and set up other events
      if (!Activiti.event.isInitEvent(Activiti.event.displayLinks)) {
        this.fireEvent(Activiti.event.displayLinks, {connectorId: this._connectorId, nodeId: this._nodeId}, null);
      }
      
      var addLinkButton = new YAHOO.widget.Button(addLinkElId, { label:"Add link", id:"addLinkButton" });
      addLinkButton.addListener("click", this.onClickAddLinkButton, null, this);
    },

    /**
     * This method is involed when the deleteArtifactLink method returns successfully. 
     * It fires a displayLinks event which will cause the links table to reload its data.
     */
    onDeleteArtifactLinkSuccess: function Links_onDeleteArtifactLinkSuccess(args) {
      this.fireEvent(Activiti.event.displayLinks, {connectorId: this._connectorId, nodeId: this._nodeId}, null);
    },

    /**
     * Click event listener for the "Add Link" button, instantiates a FileChooserDialog.
     * 
     * @param event {object} The event that was triggered
     * @param args {Array} The event values     
     */
    onClickAddLinkButton: function Links_onClickAddLinkButton(event, args)
    {
      return new Activiti.component.FileChooserDialog(this.id, "onAddLinkSubmit", true, this, false, true);
    },

    /**
     * Listener method for the submit button of the FileChooserDialog.
     *
     * @param obj {object} An object that contains the connector id and the id of the target artifact
     */
    onAddLinkSubmit: function Links_onAddLinkSubmit(obj) {
      this.service.createArtifactLink({"connectorId": this._connectorId, "nodeId": this._nodeId, "targetConnectorId": obj.connectorId, "targetNodeId": obj.nodeId});
    },

    /**
     * This method is called when the service method createArtifactLink returns and reloads 
     * the links data table so the newly added link becomes visible.
     *
     * @param args object that contains three attributes: config, json and serverResponse
     */
    onCreateArtifactLinkSuccess: function Links_onCreateArtifactLinkSuccess(args)
    {
      this.fireEvent(Activiti.event.displayLinks, {connectorId: this._connectorId, nodeId: this._nodeId}, null);
    },

    /**
     * Convenience method to retrieve css class attributes for content types.
     *
     * @param contentType the content type to retrieve the css class for
     * @return {string} the content type class name
     */
    getClassForContentType: function Links_getClassForContentType(contentType) {
      if(contentType === "image/png" || contentType === "image/gif" || contentType === "image/jpeg") {
        return "icon-img";
      } else if(contentType === "application/xml") {
        return "icon-code-red";
      } else if(contentType === "text/html") {
        return "icon-www";
      } else if(contentType === "text/plain") {
        return "icon-txt";
      } else if(contentType === "application/pdf") {
        return "icon-pdf";
      } else if(contentType === "application/json;charset=UTF-8") {
        return "icon-code-blue";
      } else if(contentType === "application/msword") {
        return "icon-doc";
      } else if(contentType === "application/powerpoint") {
        return "icon-ppt";
      } else if(contentType === "application/excel") {
        return "icon-xls";
      } else if(contentType === "application/javascript") {
        return "icon-code-blue";
      }
      // Use white page as default icon for all other content types
      return "icon-blank";
    }

  });
})();
