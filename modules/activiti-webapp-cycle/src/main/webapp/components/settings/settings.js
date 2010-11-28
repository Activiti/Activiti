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
   * Settings constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.Settings} The new component.Settings instance
   * @constructor
   */
  Activiti.component.Settings = function Settings_constructor(htmlId)
  {
    Activiti.component.Settings.superclass.constructor.call(this, "Activiti.component.Settings", htmlId);
    // Create new service instances and set this component to receive the callbacks
    this.service = new Activiti.service.RepositoryService(this);
    
    // Listen for events that interest this component
    // this.onEvent(Activiti.event.updateArtifactView, this.onUpdateArtifactView);

    this._availableConnectorConfigs = {};
    
    return this;
  };

  YAHOO.extend(Activiti.component.Settings, Activiti.component.Base,
  {
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Settings_onReady()
    {
      this.service.loadAvailableConnectorConfigs();
    },
    
    onLoadAvailableConnectorConfigsSuccess: function RepositoryService_Settings_onLoadAvailableConnectorConfigsSuccess(response) {
      this._availableConnectorConfigs = response.json.configs;
      this.service.loadUserConfig();
    },
    
    onLoadUserConfigSuccess: function RepositoryService_Settings_onLoadUserConfigSuccess(response) {

      var el = document.getElementById(this.id);
      var content = "";
      for(var repoConfig in response.json.userConfig) {
        var configClassName = response.json.userConfig[repoConfig].configClassName;
        var configs = response.json.userConfig[repoConfig].configs;
        content += '<div class="connector-type-div"><h2>' + this._availableConnectorConfigs[configClassName] + '</h2>';
        for(var config in configs) {
          content += '<span class="config-span"><ul><li><span id="' + this.id + '-name-label" class="attribute-label">Name:</span><span id="' + this.id + '-name-value" class="attribute-value">' + configs[config]["name"] + '</span></li><li><span id="' + this.id + '-id-label" class="attribute-label">ID:</span><span id="' + this.id + '-id-value" class="attribute-value">' + configs[config]["id"] + '</span></li>';
          for (attr in configs[config]) {
            if(configs[config].hasOwnProperty(attr) && attr != "name" && attr != "id") {
              content += '<li><span id="' + this.id + '-' + attr + '-label" class="attribute-label">' + attr + ':</span><span id="' + this.id + '-' + attr + '-value" class="attribute-value">' + configs[config][attr] + "</span></li>";
            }
          }
          content += '</ul></span>';
        }
        content += '<div style="clear:both"></div></div>';
      }
      el.innerHTML = content;
      
      var configEls = Dom.getElementsByClassName("config-span", "span");
      for(var configEl in configEls) {
        YAHOO.util.Event.addListener(configEls[configEl], "click", this.onConfigElClick, configEls[configEl], true);
      }
      
    },
    
    onConfigElClick: function Settings_onConfigElClick(event, configEl) {
      // TODO
    }

  });
})();
