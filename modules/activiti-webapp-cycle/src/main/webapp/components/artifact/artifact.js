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
    this.onEvent(Activiti.event.selectTreeLabel, this.onSelectTreeLabelEvent);
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
		
		onSelectTreeLabelEvent: function Artifact_onSelectTreeLabelEvent(event, args) {
			// get the tree node that was selected
			var node = args[1].value.node;
			// get the header el of the content area
			var headerEl = Selector.query("h1", this.id, true);
			if("header-" + node.data.id === headerEl.id) {
				// do nothing... the same node was clicked twice
			} else {
				var tabView = YAHOO.util.Selector.query('div', 'artifact-div', true);
				// check whether an artifact was selected before. If yes, remove tabView and actions
				if(tabView) {
					var artifactDiv = document.getElementById('artifact-div');
					artifactDiv.removeChild(tabView);
					var optionsDiv = document.getElementById('options-div');
					optionsDiv.innerHTML = "";
					optionsDiv.removeAttribute("class");
				}
				// Check whether the selected node is a file node. If so, 
				// we can load its data
				if(node.data.file) {
					this.services.repositoryService.loadArtifact(node.data.id);
				}
				// Update the heading that displays the name of the selected node
		  	headerEl.id = "header-" + node.data.id;
				headerEl.innerHTML = node.label;
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
			var tabView = new YAHOO.widget.TabView(); 
			// Retrieve rest api response
      var artifactJson = response.json;
      var firstTab = true;
			// Add a tab for each content representation from the JSON response
			for(var i = 0; i<artifactJson.contentViews.length; i++) {
				if(artifactJson.contentViews[i].type.indexOf("image") != -1) {
					tabView.addTab( new YAHOO.widget.Tab({
						label: 'Image',
						content: "<div id=\"artifact-image\"><img id=\"" + artifactJson.id + "\" src=\"" + artifactJson.contentViews[i].content + "\" border=0></img></div>",
						active: firstTab
					}));
					firstTab = false;
				}	else {
					tabView.addTab( new YAHOO.widget.Tab({
						label: artifactJson.contentViews[i].name,
						content: "<div id=\"artifact-source\">\n<pre class=\"prettyprint lang-" + artifactJson.contentViews[i].type + "\" >\n" + artifactJson.contentViews[i].content + "\n</pre></div>",
						active: firstTab
					}));
					firstTab = false;
				}			
			}
			tabView.appendTo('artifact-div');
	   	prettyPrint();

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
			YAHOO.util.Event.preventDefault(e)
			
			
		} 

	});

})();
