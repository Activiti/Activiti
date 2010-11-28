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
      // render the user configuration in the components DOM element
      var content = "";
      for(var repoConfig in response.json.userConfig) {
        var configClassName = response.json.userConfig[repoConfig].configClassName;
        var configs = response.json.userConfig[repoConfig].configs;
        content += '<div class="connector-type-div"><h2>' + this._availableConnectorConfigs[configClassName] + '</h2>';
        for(var config in configs) {
          content += '<span class="config-span highlightable"><span class="hidden">' + configClassName + '</span><ul><li><span id="' + this.id + '-name-label" class="attribute-label">Name:</span><span id="' + this.id + '-name-value" class="attribute-value">' + configs[config]["name"] + '</span></li><li><span id="' + this.id + '-id-label" class="attribute-label">ID:</span><span id="' + this.id + '-id-value" class="attribute-value">' + configs[config]["id"] + '</span></li>';
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
      
      // add an event listener to each config span so that it can be made editable by clicking it
      var configEls = Dom.getElementsByClassName("config-span", "span");
      for(var configEl in configEls) {
        if(configEls[configEl]) {
          YAHOO.util.Event.addListener(configEls[configEl], "click", this.onConfigElClick, configEls[configEl], this);
        }
      }
      
    },
    
    onConfigElClick: function Settings_onConfigElClick(event, configEl) {
      // Disable the editing of other configs while the current one is editable
      var configEls = Dom.getElementsByClassName("config-span", "span");
      Dom.removeClass(configEls, 'highlightable');
      Dom.addClass(configEl, 'highlight');
      for(var el in configEls) {
        if(configEls[el]) {
          YAHOO.util.Event.removeListener(configEls[el], "click", this.onConfigElClick);
        }
      }
      
      // create a form to wrap the config span
      var form = document.createElement("form");
      form.setAttribute("onsubmit", "javascript: return false;");
      Dom.insertAfter(form, configEl);
      configEl.parentNode.removeChild(configEl);
      form.appendChild(configEl);

      var configClassName = configEl.childNodes[0].innerHTML;
      configEl.childNodes[0].innerHTML = '<input type="hidden" value="' + configClassName + '"/>';

      var liEls = configEl.childNodes[1].childNodes;
      for(var liEl in liEls) {
        if(liEl && liEls[liEl].childNodes) {
          var tmp = liEls[liEl].childNodes[1].innerHTML;
          var name = liEls[liEl].childNodes[1].getAttribute('id').split('-')[1];
          liEls[liEl].childNodes[1].innerHTML = '<input type="text" name="' + name + '" value="' + tmp + '"/>';  
        }
      }
      
      var buttonsSpan = document.createElement('span');
      buttonsSpan.setAttribute('class', 'button-panel');
      buttonsSpan.innerHTML = '<span id="' + this.id + '-save-config" class="yui-button"><span class="first-child"><button type="button">Save</button></span></span><span id="' + this.id + '-cancel-config" class="yui-button"><span class="first-child"><button type="button">Cancel</button></span></span>';
      configEl.appendChild(buttonsSpan);
      
      var saveButton = new YAHOO.widget.Button(this.id + '-save-config', { label:"Save", id:"saveConfigButton" });
      saveButton.addListener("click", this.onClickSaveButton, form, this);
      
      var cancelButton = new YAHOO.widget.Button(this.id + '-cancel-config', { label:"Cancel", id:"cancelConfigButton" });
      cancelButton.addListener("click", this.onClickCancelButton, null, this);
    },

    onClickSaveButton: function Settings_onClickSaveButton(event, form) {
      var formEls = form.elements;
      var newConfig = {};
      newConfig.values = {};
      var configClassName = "";
      for(var el in formEls) {
        if(formEls[el].nodeName == "INPUT") {
          if(formEls[el].name == 'id') {
            newConfig.configurationId = formEls[el].value;
          }
          if(formEls[el].type == 'hidden') {
            newConfig.configurationClass = formEls[el].value;
          } else {
            newConfig.values[formEls[el].name] = formEls[el].value;  
          }
        }
      }
      this.service.saveRepositoryConnectorConfiguration(newConfig);
    },
    
    onClickCancelButton: function Settings_onClickCancelButton(event) {
      this.service.loadAvailableConnectorConfigs();
    },
    
    onSaveRepositoryConnectorConfigurationSuccess: function RepositoryService_Settings_onSaveRepositoryConnectorConfigurationSuccess(response) {
      Activiti.widget.PopupManager.displayMessage({
        text: "Successfully updated configuration"
      });
      this.service.loadAvailableConnectorConfigs();
    }

  });
})();
