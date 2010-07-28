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
		
		// onSelectDiv: function Artifact_onSelectDiv(event, args) {
		// 			var item;
		// 			for (item in event) {
		// 				alert(item + ": " + event[item]);
		// 			}
		// 		},
		
		onSelectTreeLabelEvent: function Artifact_onSelectTreeLabelEvent(event, args) {
			// get the tree node that was selected
			var node = args[1].value.node;
			// get the header el of the content area
			var headerEl = Selector.query("h1", this.id, true);
		
			if("header-" + node.data.id === headerEl.id) {
				// do nothing... the same node was clicked twice
			} else {

				var tabView = YAHOO.util.Selector.query('div', 'artifact-div', true);

				// check whether an artifact was selected before. If yes, remove the tabView
				if(tabView) {
					var artifactDiv = document.getElementById('artifact-div');
					artifactDiv.removeChild(tabView);
				}

				// Check whether the selected node is not a folder. If not, 
				// we can assume it is an artifact and load its data
				if(node.children.length === 0) {
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

			tabView.addTab( new YAHOO.widget.Tab({
				label: 'Image',
				content: '<div id="artifact-image"></div>',
				active: true
			}));

			tabView.addTab( new YAHOO.widget.Tab({
				label: 'Source',
				content: '<div id="artifact-source">\n<pre class="prettyprint lang-xml" >\n&lt;!DOCTYPE series PUBLIC "fibonacci numbers"&gt;\n&lt;series.root base="1" step="s(n-2) + s(n-1)">\n&lt;element i="0"&gt;1&lt;/element&gt;\n&lt;element i="1"&gt;1&lt;/element&gt;\n&lt;element i="2"&gt;2&lt;/element&gt;\n&lt;element i="3"&gt;3&lt;/element&gt;\n&lt;element i="4"&gt;5&lt;/element&gt;\n&lt;element i="5"&gt;8&lt;/element&gt;\n&lt;/series.root&gt;\n</pre></div>'
			}));

			tabView.appendTo('artifact-div');

	   	prettyPrint();

      // Retrieve rest api response
      var artifactJson = response.json;

			// display the image based on the UR we retrieved.
			var insertLocation = document.getElementById('artifact-image');
			var img = document.createElement('IMG');
			img.src = artifactJson.url;
			img.id = artifactJson.id;
			img.border = 0;
			insertLocation.appendChild(img);

    }

	});

})();









