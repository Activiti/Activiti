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
        // Check whether the selected node is a file node. If so, load its data
        if(eventValue.isRepositoryArtifact ) {
          this.services.repositoryService.loadArtifact(eventValue.connectorId, eventValue.repositoryNodeId);
        } else {
          this.services.repositoryService.loadTagsByArtifact(eventValue.connectorId, eventValue.repositoryNodeId);
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
      
      // remove folder tags from UI
      var tagsDiv = document.getElementById("tags-div");
      if(tagsDiv) {
        tagsDiv.innerHTML = "";
      }
      
      this._tabView = new YAHOO.widget.TabView(); 
      
      // Retrieve rest api response
      var artifactJson = response.json;

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
        this._tabView.addTab(tab);
      }

      // Add artifact links tab
			var linksTab = new YAHOO.widget.Tab({ 
				label: "Links", 
				dataSrc: Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-links?connectorId=" + encodeURIComponent(artifactJson.connectorId) + "&artifactId=" + encodeURIComponent(artifactJson.artifactId),
				cacheData: true
			});

			linksTab.loadHandler.success = function(response) {
				me.onLodLinksSuccess(this /* the tab */, response);
			};

			this._tabView.addTab(linksTab);

      this._tabView.appendTo('artifact-div');

      // replace the tabViews onActiveTabCHange evnet handler with our own one
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

    onLoadTagsByArtifactSuccess: function Artifact_RepositoryService_onLoadTagsByArtifactSuccess(response, obj) {
      var tagsDiv = document.getElementById("tags-div");
      if(!tagsDiv) {
        // create tags div
        tagsDiv = document.createElement("div");
        tagsDiv.setAttribute('id', 'tags-div');
        // retrieve the header
        var headerEl = Selector.query("h1", this.id, true);
        // insert tagsDiv after the header
        Dom.insertAfter(tagsDiv, headerEl);
      }
      
      if(response.json.tags && response.json.tags.length > 0) {
        tagsDiv.innerHTML = "Tags: ";
      } else {
        tagsDiv.innerHTML = "";
      }
      
      for(tag in response.json.tags) {
        var tagSpan = document.createElement("span");
        tagSpan.innerHTML = response.json.tags[tag].alias;
        tagsDiv.appendChild(tagSpan);
      }
      
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

    onLodLinksSuccess: function Artifact_onLodLinksSuccess(tab, response) {
      try{
        var responseJson = YAHOO.lang.JSON.parse(response.responseText);

        tab.set('content', '<div id="linksTable"></div><span id="addLink" class="yui-button"><span class="first-child"><button type="button">Add link</button></span></span>');

        // TODO: i18n
        var linksColumnDefs = [
            {key:"Name", sortable:true},
            {key:"Revision", sortable:true},
            {key:"Type"}
          ];
        
        var rows = [];
        for(var i=0; i<responseJson.length; i++) {
          var typeClass;
          if(responseJson[i].artifact.targetContentType === "image/png" || responseJson[i].artifact.targetContentType === "image/gif" || responseJson[i].artifact.targetContentType === "image/jpeg") {
            typeClass = "icon-img";
          } else if(responseJson[i].artifact.targetContentType === "application/xml") {
            typeClass = "icon-code-red";
          } else if(responseJson[i].artifact.targetContentType === "text/html") {
            typeClass = "icon-www";
          } else if(responseJson[i].artifact.targetContentType === "text/plain") {
            typeClass = "icon-txt";
          } else if(responseJson[i].artifact.targetContentType === "application/pdf") {
            typeClass = "icon-pdf";
          } else if(responseJson[i].artifact.targetContentType === "application/json;charset=UTF-8") {
            typeClass = "icon-code-blue";
          } else if(responseJson[i].artifact.targetContentType === "application/msword") {
            typeClass = "icon-doc";
          } else if(responseJson[i].artifact.targetContentType === "application/powerpoint") {
            typeClass = "icon-ppt";
          } else if(responseJson[i].artifact.targetContentType === "application/excel") {
            typeClass = "icon-xls";
          } else if(responseJson[i].artifact.targetContentType === "application/javascript") {
            typeClass = "icon-code-blue";
          } else {
            // Use white page as default icon for all other content types
            typeClass = "icon-blank";
          }
          var row = {Name: '<a class="openArtifactLink" href="#?connectorId=' + encodeURIComponent(responseJson[i].artifact.targetConnectorId) + '&artifactId=' + encodeURIComponent(responseJson[i].artifact.targetArtifactId) + '&artifactName=' + encodeURIComponent(responseJson[i].artifact.label) + '">' + responseJson[i].artifact.label + '</a>', Revision: responseJson[i].artifact.targetArtifactRevision, Type: '<div class="artifact-type ' + typeClass + '">' + responseJson[i].artifact.targetContentType + '</div>' };
          rows.push(row);
        }
        var linksDataSource = new YAHOO.util.LocalDataSource(rows);

        linksDataSource.responseSchema = {
            fields: ["Name","Revision","Type"]
          };

        this._linksDataTable = new YAHOO.widget.DataTable("linksTable", linksColumnDefs, linksDataSource, {scrollable:true});

        var linkElements = Dom.getElementsByClassName("openArtifactLink", "a");
        for (var i = 0; i < linkElements.length; i++) {
				  YAHOO.util.Event.addListener(linkElements[i], "click", this.onArtifactLinkClick, this, true);
        }

        var addLinkButton = new YAHOO.widget.Button("addLink", { label:"Add link", id:"addLinkButton" });
        
        addLinkButton.addListener("click", this.onClickAddLinkButton, null, this);
      }
      catch (e) {
          alert("Invalid response for tab data");
      }
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
     * the links-tab so the new row in the links-table becomes visible.
     *
     * @param args object that contains three attributes: config, json and serverResponse
     */
    onCreateArtifactLinkSuccess: function Artifact_onCreateArtifactLinkSuccess(args)
    {
      this._tabView.getTab(this._activeTabIndex).set( 'cacheData', false );
      this._tabView.selectTab( this._activeTabIndex );
      this._tabView.getTab(this._activeTabIndex).set( 'cacheData', true );
    }

  });

})();
