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
   * Artifact constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.Artifact} The new component.Artifact instance
   * @constructor
   */
  Activiti.component.Artifact = function Artifact_constructor(htmlId)
  {
    Activiti.component.Artifact.superclass.constructor.call(this, "Activiti.component.Artifact", htmlId);
    // Create new service instances and set this component to receive the callbacks
    this.services.repositoryService = new Activiti.service.RepositoryService(this);
    // Listen for events that interest this component
    this.onEvent(Activiti.event.updateArtifactView, this.onUpdateArtifactView);
    this.onEvent(Activiti.event.clickFormEventButton, this.onClickFormEventButton);
    
    this._tabView = {};
    this._connectorId = "";
    this._repositoryNodeId = "";
    this._isRepositoryArtifact = false;
    this._name = "";
    this._activeTabIndex = 0;

    this._fileChooserDialog = {};
    this._linksDataTable = {};
    this._linksDataSource = {};
    this._backlinksDataTable = {};

    return this;
  };

  YAHOO.extend(Activiti.component.Artifact, Activiti.component.Base,
  {
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Artifact_onReady()
    {
      var size = parseInt(Dom.getStyle('content', 'width'), 10); 
      var left, main;
      var resize = new YAHOO.util.Resize('left', {
          handles: ['r']
        });
      left = Dom.get('left');
      main = Dom.get('main');
      resize.on('resize', function(ev) {
          var w = ev.width;
          Dom.setStyle(left, 'height', '');
          Dom.setStyle(main, 'width', (size - w - 37) + 'px');
        });
      
      
    },
    
    /**
     * This method is invoked when a node in the tree is selected or the active tab chages. 
     * It first checks whether the node is still the same and updates the active tab if 
     * needed. If a new node was selected in the tree, the current artifact view is removed
     * and the loadArtifact method is invoked with the new artifacts id.
     * 
     * @method onUpdateArtifactView
     * @param event {String} the name of the event that triggered this method invokation
     * @param args {array} an array of object literals
     */
    onUpdateArtifactView: function Artifact_onUpdateArtifactView(event, args) {
      
      var eventValue = args[1].value;
      
      this._connectorId = eventValue.connectorId;
      this._repositoryNodeId = eventValue.repositoryNodeId;
      this._isRepositoryArtifact = eventValue.isRepositoryArtifact;
      this._name = eventValue.name;
      this._activeTabIndex = eventValue.activeTabIndex;

      // get the header el of the content area
      var headerEl = Selector.query("h1", this.id, true);
      // determine whether the node is still the same
      if("header-" + eventValue.repositoryNodeId === headerEl.id) {
        // still the same node, if the tab view is instanciated, the tab selection should be updated
        if(this._tabView.set) {
          // Update active tab selection silently, without firing an event (last parameter 'true')
          this._tabView.set("activeTab", this._tabView.getTab(this._activeTabIndex), true);
        }
      } else {
        // a new node has been selected in the tree
        var tabViewHtml = YAHOO.util.Selector.query('div', 'artifact-div', true);
        // check whether an artifact was selected before. If yes, remove tabViewHtml and actions
        if(tabViewHtml) {
          var artifactDiv = document.getElementById('artifact-div');
          artifactDiv.removeChild(tabViewHtml);
          var optionsDiv = document.getElementById('options-div');
          optionsDiv.innerHTML = "";
          optionsDiv.removeAttribute("class");
        }
        // instantiate the tagging component
        new Activiti.component.TaggingComponent(this.id, {connectorId: eventValue.connectorId, repositoryNodeId: eventValue.repositoryNodeId, repositoryNodeLabel: eventValue.name}, "tags-div");
        // Check whether the selected node is a file node. If so, load its data
        if(eventValue.isRepositoryArtifact ) {
          this.services.repositoryService.loadArtifact(eventValue.connectorId, eventValue.repositoryNodeId);
        }
        // Update the heading that displays the name of the selected node
        headerEl.id = "header-" + eventValue.repositoryNodeId;
        headerEl.innerHTML = eventValue.name;
      }
    },
    
    /**
     * This method is invoked when an artifact is loaded successfully. It will draw a
     * yui tab view component to display the different content representations of the
     * artifact and create an options panel for links, downloads and actions.
     *
     * @method onLoadArtifactSuccess
     * @param response {object} The callback response
     * @param obj {object} Helper object
     */
    onLoadArtifactSuccess: function Artifact_RepositoryService_onLoadArtifactSuccess(response, obj)
    {
      var me = this;
      
      if(response.json.authenticationError) {
        return new Activiti.component.AuthenticationDialog(this.id, response.json.repoInError, response.json.authenticationError);
      }

      this._tabView = new YAHOO.widget.TabView(); 
      
      // Retrieve rest api response
      var artifactJson = response.json;
      // create a tab for each content representation
      for(var i = 0; i<artifactJson.contentRepresentations.length; i++) {
        var tab = new YAHOO.widget.Tab({ 
          label: artifactJson.contentRepresentations[i], 
          dataSrc: this.loadTabDataURL(artifactJson.connectorId, artifactJson.artifactId, artifactJson.contentRepresentations[i]), 
          cacheData: true
        });
        tab.addListener("contentChange", this.onTabDataLoaded);
        tab.loadHandler.success = function(response) {
          me.onLoadTabSuccess(this /* the tab */, response);
        };
        tab.loadHandler.failure = function(response) {
          me.onLoadTabFailure(this /* the tab */, response);
        };
        this._tabView.addTab(tab);
      }

      // Add links tab
      var linksTab = new YAHOO.widget.Tab({ 
        // TODO: i18n
        label: "Links", 
        // TODO: i18n
        content: '<div id="links-wrapper"><h3>Outgoing Links</h3><div id="links-div"></div><span id="addLink" class="yui-button"><span class="first-child"><button type="button">Add link</button></span></span></div><div id="backlinks-wrapper"><h3>Incoming Links</h3><div id="backlinks-div"></div></div>'
      });

      this._tabView.addTab(linksTab);
      this._tabView.appendTo('artifact-div');

      // instantiate a data source for the links data table
      this._linksDataSource = new YAHOO.util.XHRDataSource(Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-links?connectorId=" + encodeURIComponent(artifactJson.connectorId) + "&artifactId=" + encodeURIComponent(artifactJson.artifactId));
      this._linksDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;

      // TODO: i18n
      var linksColumnDefs = [
          {key: "Name", sortable:true},
          // {key: "Revision", sortable:true},
          {key: "Type"}
        ];

      // instantiate the links data table
      this._linksDataTable = new YAHOO.widget.DataTable("links-div", linksColumnDefs, this._linksDataSource, {scrollable:true});
      // parse the response of the request that loaded the links from the server and 
      // transform it into a format that is easily consumed by the YUI dataTable component
      this._linksDataTable.doBeforeLoadData = function (sRequest, oResponse, oPayload) {
          var jsonArray = oResponse.results;
          var rows = [];
          for(var i=0; i<jsonArray.length; i++) {
            var row = {
              Name: '<a class="openArtifactLink" href="#?connectorId=' + encodeURIComponent(jsonArray[i].artifact.targetConnectorId) + '&artifactId=' + encodeURIComponent(jsonArray[i].artifact.targetArtifactId) + '&artifactName=' + encodeURIComponent(jsonArray[i].artifact.label) + '">' + jsonArray[i].artifact.label + '</a>',
              Revision: jsonArray[i].artifact.targetArtifactRevision,
              Type: '<div class="artifact-type ' + me.getClassForContentType(jsonArray[i].artifact.targetContentType) + '">' + jsonArray[i].artifact.targetContentType + '</div>'
            };
            rows.push(row);
          }          
          oResponse.results = rows;
          return true;
      };   
      // make sure we add click listeners to the links once they are loaded and rendered in the data table
      this._linksDataTable.addListener('initEvent', function(e) {
          var linkElements = Dom.getElementsByClassName("openArtifactLink", "a");
          for (var i = 0; i < linkElements.length; i++) {
            YAHOO.util.Event.addListener(linkElements[i], "click", me.onArtifactLinkClick, me, true);
          }
        });

      var addLinkButton = new YAHOO.widget.Button("addLink", { label:"Add link", id:"addLinkButton" });
      addLinkButton.addListener("click", this.onClickAddLinkButton, null, this);

      // instantiate the datasource for the backlinks data table
      var backlinksDataSource = new YAHOO.util.XHRDataSource(Activiti.service.REST_PROXY_URI_RELATIVE + "incoming-artifact-links?connectorId=" + encodeURIComponent(artifactJson.connectorId) + "&artifactId=" + encodeURIComponent(artifactJson.artifactId));
      backlinksDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;

      // instantiate the links data table
      this._backlinksDataTable = new YAHOO.widget.DataTable("backlinks-div", linksColumnDefs, backlinksDataSource, {scrollable:true});
      // parse the response of the request that loaded the backlinks from the server and 
      // transform it into a format that is easily consumed by the YUI dataTable component
      this._backlinksDataTable.doBeforeLoadData = function (sRequest, oResponse, oPayload) {
          var jsonArray = oResponse.results;
          var rows = [];
          for(var i=0; i<jsonArray.length; i++) {
            var row = {
              Name: '<a class="openArtifactLink" href="#?connectorId=' + encodeURIComponent(jsonArray[i].artifact.sourceConnectorId) + '&artifactId=' + encodeURIComponent(jsonArray[i].artifact.sourceArtifactId) + '&artifactName=' + encodeURIComponent(jsonArray[i].artifact.label) + '">' + jsonArray[i].artifact.label + '</a>',
              Revision: jsonArray[i].artifact.sourceArtifactRevision,
              Type: '<div class="artifact-type ' + me.getClassForContentType(jsonArray[i].artifact.sourceContentType) + '">' + jsonArray[i].artifact.sourceContentType + '</div>'
            };
            rows.push(row);
          }          
          oResponse.results = rows;
          return true;
      };   
      // make sure we add click listeners to the links once they are loaded and rendered in the data table
      this._backlinksDataTable.addListener('initEvent', function(e) {
          var linkElements = Dom.getElementsByClassName("openArtifactLink", "a");
          for (var i = 0; i < linkElements.length; i++) {
            YAHOO.util.Event.addListener(linkElements[i], "click", me.onArtifactLinkClick, me, true);
          }
        });

      // replace the tabViews onActiveTabChange evnet handler with our own one
      this._tabView.unsubscribe("activeTabChange", this._tabView._onActiveTabChange);
      this._tabView.subscribe("activeTabChange", this.onActiveTabChange, null, this);

      // Select the active tab without firing an event (last parameter is 'silent=true')
      this._tabView.set("activeTab", this._tabView.getTab(this._activeTabIndex), true);

      // Get the options panel
      var optionsDiv = document.getElementById("options-div");

      // Add a dropdowns for actions, links and downloads
      if(artifactJson.actions.length > 0 || artifactJson.links.length > 0 || artifactJson.downloads.length > 0) {
        var actionsMenuItems = [];
        var actions = [];
        for(i = 0; i<artifactJson.actions.length; i++) {
          actions.push({ text: artifactJson.actions[i].label, value: {connectorId: artifactJson.connectorId, artifactId: artifactJson.artifactId, actionName: artifactJson.actions[i].name}, onclick: { fn: this.onExecuteActionClick } });
        }
        if(actions.length > 0) {
          actionsMenuItems.push(actions);
        }
        var links = [];
        for(i=0; i<artifactJson.links.length; i++) {
          links.push({ text: artifactJson.links[i].label, url: artifactJson.links[i].url, target: "_blank"});
        }
        if(links.length > 0) {
          actionsMenuItems.push(links);
        }
        var downloads = [];
        for(i=0; i<artifactJson.downloads.length; i++) {
          downloads.push({ text: artifactJson.downloads[i].label, url: artifactJson.downloads[i].url, target: "_blank"});
        }
        if(downloads.length > 0) {
          actionsMenuItems.push(downloads);
        }
        // TODO: i18n
        var optionsMenu = new YAHOO.widget.Button({ type: "menu", label: "Actions", name: "options", menu: actionsMenuItems, container: optionsDiv });
      }
      optionsDiv.setAttribute('class', 'active');
    },

    onLoadTabSuccess: function Artifact_onLoadTabSuccess(tab, response) {
      
      try {
        var responseJson = YAHOO.lang.JSON.parse(response.responseText);
        // parse response, create tab content and set it to the tab
        
        var tabContent;
        if(responseJson.renderInfo == "IMAGE") {
          tabContent = '<div class="artifact-image"><img id="' + responseJson.contentRepresentationId + '" src="' + Activiti.service.REST_PROXY_URI_RELATIVE + "content?connectorId=" + encodeURIComponent(responseJson.connectorId) + "&artifactId=" + encodeURIComponent(responseJson.artifactId) + "&contentRepresentationId=" + encodeURIComponent(responseJson.contentRepresentationId) + '" border=0></img></div>';
        } else if (responseJson.renderInfo == "HTML") {
          tabContent = '<div class="artifact-html"><iframe src ="' + Activiti.service.REST_PROXY_URI_RELATIVE + "content?connectorId=" + encodeURIComponent(responseJson.connectorId) + "&artifactId=" + encodeURIComponent(responseJson.artifactId) + "&contentRepresentationId=" + encodeURIComponent(responseJson.contentRepresentationId) + '"><p>Your browser does not support iframes.</p></iframe></div>';
        } else if (responseJson.renderInfo == "HTML_REFERENCE") {
          tabContent = '<div class="artifact-html-reference"><iframe src ="' + responseJson.url + '"><p>Your browser does not support iframes.</p></iframe></div>';
        } else if (responseJson.renderInfo == "BINARY") {
          // TODO: show some information but no content for binary
          tabContent = '<div class="artifact-binary"><p>No preview available...</p></div>';
        } else if (responseJson.renderInfo == "CODE") {
          tabContent = '<div class="artifact-code"><pre id="' + responseJson.contentRepresentationId + '" class="prettyprint" >' + responseJson.content + '</pre></div>';
        } else if (responseJson.renderInfo == "TEXT_PLAIN") {
          tabContent = '<div class="artifact-text-plain"><pre id="' + responseJson.contentRepresentationId + '">' + responseJson.content + '</pre></div>';
        }
        tab.set('content', tabContent);
      }
      catch (e) {
          alert("Invalid response for tab data");
      }
    },

    onLoadTabFailure: function Artifact_onLoadTabFailure(tab, response) {
      var responseJson = YAHOO.lang.JSON.parse(response.responseText);
      var tabContent = "<h3>Java Stack Trace:</h3>";
      for(var line in responseJson.callstack) {
        if( line == 1 || (responseJson.callstack[line].indexOf("org.activiti.cycle") != -1) || responseJson.callstack[line].indexOf("org.activiti.rest.api.cycle") != -1) {
          tabContent += "<span class=\"cycle-stack-trace-highlight\">" + responseJson.callstack[line] + "</span>";
        } else {
          tabContent += "<span class=\"cycle-stack-trace\">" + responseJson.callstack[line] + "</span>";
        }
      }
      tab.set('content', tabContent);
      Activiti.widget.PopupManager.displayError(responseJson.message);
    },

    onExecuteActionClick: function Artifact_onExecuteActionClick(e)
    {
      return new Activiti.widget.ExecuteArtifactActionForm(this.id + "-executeArtifactActionForm", this.value.connectorId, this.value.artifactId, this.value.actionName);
    },
    
    onTabDataLoaded: function Artifact_onTabDataLoaded()
    {
      prettyPrint();
    },

    loadTabDataURL: function Artifact_loadTabDataURL(connectorId, artifactId, representationId)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "content-representation?connectorId=" + encodeURIComponent(connectorId) + "&artifactId=" + encodeURIComponent(artifactId) + "&representationId=" + encodeURIComponent(representationId);
    },

    onActiveTabChange: function Artifact_onActiveTabChange(event)
    {
      var newActiveTabIndex = this._tabView.getTabIndex(event.newValue);
      this.fireEvent(Activiti.event.updateArtifactView, {"connectorId": this._connectorId, "repositoryNodeId": this._repositoryNodeId, "isRepositoryArtifact": this._isRepositoryArtifact, "name": this._name, "activeTabIndex": newActiveTabIndex}, null, true);
      YAHOO.util.Event.preventDefault(event);
    },
    
    onArtifactLinkClick: function Artifact_onArtifactLinkClick(event)
    {
      var params = event.target.href.split("?")[1].split("&");
      
      var connectorId = decodeURIComponent(params[0].split("=")[1]);
      var artifactId = decodeURIComponent(params[1].split("=")[1]);
      var artifactName = decodeURIComponent(params[2].split("=")[1]);

      this.fireEvent(Activiti.event.updateArtifactView, {"connectorId": connectorId, "repositoryNodeId": artifactId, "isRepositoryArtifact": true, "name": artifactName, "activeTabIndex": 0}, null, true);
      YAHOO.util.Event.preventDefault(event);
    },
    
    onClickFormEventButton: function Artifact_onClickFormEventButton(event, args)
    {
      return new Activiti.component.FileChooserDialog(this.id, args[1].value.callback, false, null, true, false);
    },
    
    onClickAddLinkButton: function Artifact_onClickAddLinkButton(event, args)
    {
      return new Activiti.component.FileChooserDialog(this.id, "onAddLinkSubmit", true, this, false, true);
    },

    onAddLinkSubmit: function Artifact_onAddLinkSubmit(obj) {
      this.services.repositoryService.createArtifactLink({"connectorId": this._connectorId, "artifactId": this._repositoryNodeId, "targetConnectorId": obj.connectorId,"targetArtifactId": obj.nodeId});
    },

    /**
     * This method is called when the service method createArtifactLink returns and reloads 
     * the links data table so the newly added link becomes visible.
     *
     * @param args object that contains three attributes: config, json and serverResponse
     */
    onCreateArtifactLinkSuccess: function Artifact_onCreateArtifactLinkSuccess(args)
    {
      var oCallback = {
          success : this._linksDataTable.onDataReturnInitializeTable,
          failure : this._linksDataTable.onDataReturnInitializeTable,
          scope : this._linksDataTable,
          argument: this._linksDataTable.getState() // data payload that will be returned to the callback function
      };
      this._linksDataSource.sendRequest("", oCallback);
    },
    
    /**
     * Convenience method to retrieve css class attributes for content types.
     *
     * @param contentType the content type to retrieve the css class for
     * @return {string} the content type class name
     */
    getClassForContentType: function Artifact_getClassForContentType(contentType) {
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
