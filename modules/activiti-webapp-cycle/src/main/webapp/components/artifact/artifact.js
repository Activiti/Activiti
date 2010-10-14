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
		
		this._tabView = {};
		this._repositoryNodeId = "";
		this._isRepositoryArtifact = false;
		this._name = "";
		this._activeTabIndex = 0;

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

		},
		
		onUpdateArtifactView: function Artifact_onUpdateArtifactView(event, args) {
			
			this._repositoryNodeId = args[1].value.repositoryNodeId;
			this._isRepositoryArtifact = args[1].value.isRepositoryArtifact;
			this._name = args[1].value.name;
			this._activeTabIndex = args[1].value.activeTabIndex;

			// get the header el of the content area
			var headerEl = Selector.query("h1", this.id, true);
			if("header-" + args[1].value.repositoryNodeId === headerEl.id) {
				// still the same node, check whether the tab should change

				if(this._tabView.set) {
					// Update active tav selection silently, without firing an event (last parameter is 'silent=true')
					this._tabView.set("activeTab", this._tabView.getTab(this._activeTabIndex), true);
				}

			} else {
				var tabViewHtml = YAHOO.util.Selector.query('div', 'artifact-div', true);
				// check whether an artifact was selected before. If yes, remove tabViewHtml and actions
				if(tabViewHtml) {
					var artifactDiv = document.getElementById('artifact-div');
					artifactDiv.removeChild(tabViewHtml);
					var optionsDiv = document.getElementById('options-div');
					optionsDiv.innerHTML = "";
					optionsDiv.removeAttribute("class");
				}
				// Check whether the selected node is a file node. If so, 
				// we can load its data
				if(args[1].value.isRepositoryArtifact && (args[1].value.isRepositoryArtifact != "undefined") ) {
					this.services.repositoryService.loadArtifact(args[1].value.repositoryNodeId);
				}
				// Update the heading that displays the name of the selected node
		  	headerEl.id = "header-" + args[1].value.repositoryNodeId;
				headerEl.innerHTML = args[1].value.name;
			}
		},
		
		/**
     * Will display the artifact
     *
     * @method onLoadArtifactSuccess
     * @param response {object} The callback response
     * @param obj {object} Helper object
     */
    onLoadArtifactSuccess: function RepoTree_RepositoryService_onLoadArtifactSuccess(response, obj)
    {
	
			this._tabView = new YAHOO.widget.TabView(); 
			
			// Retrieve rest api response
      var artifactJson = response.json;

			for(var i = 0; i<artifactJson.contentRepresentations.length; i++) {
				var tab = new YAHOO.widget.Tab({ 
					label: artifactJson.contentRepresentations[i], 
					dataSrc: this.loadTabDataURL(artifactJson.id, artifactJson.contentRepresentations[i]), 
					cacheData: true
				});
				tab.addListener("contentChange", this.onTabDataLoaded);				
				this._tabView.addTab(tab);
			}

			this._tabView.appendTo('artifact-div');

			// replace the tabViews onActiveTabCHange evnet handler with our own one
			this._tabView.unsubscribe("activeTabChange", this._tabView._onActiveTabChange);
			this._tabView.subscribe("activeTabChange", this.onActiveTabChange, null, this);

			// Select the active tab without firing an event (last parameter is 'silent=true')
			this._tabView.set("activeTab", this._tabView.getTab(this._activeTabIndex), true);

			// Create the options panel
			var optionsDiv = document.getElementById("options-div");//YAHOO.util.Selector.query('div', 'artifact-div', true);

			// Add a dropdown for the actions			
			if(artifactJson.actions.length > 0) {
				var actionsDiv = document.createElement("div");
				actionsDiv.setAttribute('id', "artifact-actions");
				actionsDiv.appendChild(document.createTextNode("Actions: "));
				var actionsDropdown = document.createElement("select");
				actionsDropdown.setAttribute('name', "Actions");
				
				var option = document.createElement("option");
				option.appendChild(document.createTextNode("choose an action..."));
				actionsDropdown.appendChild(option);
				
				for(var i = 0; i<artifactJson.actions.length; i++) {
					option = document.createElement("option");
					option.setAttribute('value', artifactJson.id + "#TOKEN#" + artifactJson.actions[i].name);
					option.appendChild(document.createTextNode(artifactJson.actions[i].label));
		  		actionsDropdown.appendChild(option);
					YAHOO.util.Event.addListener(option, "click", this.onExecuteActionClick);
				}
				actionsDiv.appendChild(actionsDropdown);
				optionsDiv.appendChild(actionsDiv);
			}
			if(artifactJson.links.length > 0) {
				var linksDiv = document.createElement("div");
				linksDiv.setAttribute('id', "artifact-links");
				linksDiv.appendChild(document.createTextNode("Links: "));
				for(var i=0; i<artifactJson.links.length; i++) {
					var link = document.createElement("a");
					link.setAttribute('href', artifactJson.links[i].url);
					link.setAttribute('title', artifactJson.links[i].label);
					link.setAttribute('target', "blank");
					link.appendChild(document.createTextNode(artifactJson.links[i].label));
					linksDiv.appendChild(link);
					if(i < (artifactJson.links.length - 1)) {
						linksDiv.appendChild(document.createTextNode(" | "));
					}
				}
				optionsDiv.appendChild(linksDiv);
			}
			// Add download links if available
			if(artifactJson.downloads.length > 0) {
				var downloadsDiv = document.createElement("div");
				downloadsDiv.setAttribute('id', "artifact-downloads");
				downloadsDiv.appendChild(document.createTextNode("Downloads: "));
				for(var i=0; i<artifactJson.downloads.length; i++) {
					var link = document.createElement("a");
					link.setAttribute('href', artifactJson.downloads[i].url);
					link.setAttribute('title', artifactJson.downloads[i].name + " (" + artifactJson.downloads[i].type + ")");
					link.setAttribute('target', "blank");
					link.appendChild(document.createTextNode(artifactJson.downloads[i].name));
					downloadsDiv.appendChild(link);
					if(i < (artifactJson.downloads.length -1)) {
						downloadsDiv.appendChild(document.createTextNode(" | "));
					}
				}
				optionsDiv.appendChild(downloadsDiv);
			}
			var clearDiv = document.createElement('div');
			clearDiv.setAttribute('style', 'clear: both');
			optionsDiv.appendChild(clearDiv);
			optionsDiv.setAttribute('class', 'active');
    },

		onExecuteActionClick: function Artifact_onExecuteActionClick(e)
		{
			var artifactId = this.value.split("#TOKEN#")[0];
			var actionName = this.value.split("#TOKEN#")[1];
			
			new Activiti.widget.ExecuteArtifactActionForm(this.id + "-executeArtifactActionForm", artifactId, actionName);
			YAHOO.util.Event.preventDefault(e);
		},
		
		onTabDataLoaded: function Artifact_onTabDataLoaded()
		{
			prettyPrint();
		},

		loadTabDataURL: function Artifact_loadTabDataURL(artifactId, representationId)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "content-representation?artifactId=" + encodeURIComponent(artifactId) + "&representationId=" + encodeURIComponent(representationId) + "&restProxyUri=" + encodeURIComponent(Activiti.service.REST_PROXY_URI_RELATIVE);
    },

		onActiveTabChange: function Artifact_onActiveTabChange(event)
		{
			var newActiveTabIndex = this._tabView.getTabIndex(event.newValue);
			this.fireEvent(Activiti.event.updateArtifactView, {"repositoryNodeId": this._repositoryNodeId, "isRepositoryArtifact": this._isRepositoryArtifact, "name": this._name, "activeTabIndex": newActiveTabIndex}, null, true);
			YAHOO.util.Event.preventDefault(event);
		},

	});

})();
