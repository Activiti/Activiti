/* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/**
* Activiti.component.Processes
*
* Controls the Process List
*
* @namespace Activiti
* @class Activiti.component.Processes
*/
  (function()
  {
    /**
    * Shortcuts
    */
    var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector, Event = YAHOO.util.Event, $html = Activiti.util.decodeHTML;
    
    /**
    * Processes constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Activiti.component.Processes} The new component.Navigation instance
    * @constructor
    */
    Activiti.component.Processes = function Processes_constructor(htmlId)
    {
      Activiti.component.Processes.superclass.constructor.call(this, "Activiti.component.Processes", htmlId);
      
      // Create new service instances and set this component to receive the callbacks
      this.services.processService = new Activiti.service.ProcessService(this);
            
      // Listen for events that interest this component
      this.onEvent(Activiti.event.processActionClick, this.onProcessActionClick);
                  
      return this;
    };
  
    YAHOO.extend(Activiti.component.Processes, Activiti.component.Base, 
    {
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */      
    onReady: function Processes_onReady()
    {  
      this.widgets.dataTable = new Activiti.widget.DataTable(
        this.id + "-processes",
        this,
        [
          { event: Activiti.event.displayProcesses, value: {} },
          { event: "linkClickEvent", subscribe: true, trigger: Activiti.event.processActionClick}
        ],
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [
          {key:"id", sortable:true},
          {key:"key",sortable:true},
          {key:"version",sortable:true},
          {key:"action"}
        ]
      );
      
      // Needed to load data and set up other events
      if (!Activiti.event.isInitEvent(Activiti.event.displayProcesses)) 
      {
        this.fireEvent(Activiti.event.displayProcesses, {}, null, true);
      }   
      
      // Display form if url
      var processId = Activiti.util.getQueryStringParameter("id");
      if (processId != null)
      {
        this.startProcess(processId);
      }
      
    },
    
    /**
    * Activiti.widget.DataTable-callback to construct the url to use to load data into the data table.
    *
    * @method onDataTableCreateURL
    * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
    * @param eventName The name of the event to create a url from
    * @param eventValue The event values to create a url from
    * @return A url, based on the event, to use when loading data into the data table
    */
    onDataTableCreateURL: function Processes_onDataTableCreateURL(dataTable, eventName, eventValue)
    {
      return this.services.processService.loadProcessDefinitionsURL(eventValue);
    },
    
    /**
     * 
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Actions row
     * 
     * @method onDataTableRenderCellAction
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */
    onDataTableRenderCellAction: function Processes_onDataTableRenderCellAssignee(dataTable, el, oRecord, oColumn, oData) {
      var actions = [],
        linkOpen = '<a href="#start?id=' + oRecord.getData().id + '" rel="' + oRecord.getData().id + '" class="processAction startProcess">',
        linkClose = "</a>";
      if (oRecord.getData().startFormResourceKey != null) {
        actions.push(linkOpen + this.msg("processes.startForm") + linkClose);
      }
      else {
        actions.push(linkOpen + this.msg("processes.start") + linkClose);
      }
      // TODO: Add Other Actions (View XML, View Image, etc.)
      //actions.push('<a href="#start?id=' + oRecord.getData().id + '" rel="' + oRecord.getData().id + '" class="processAction viewProcess">'+this.msg("processes.viewProcess")+'</a>');
      el.innerHTML = actions.join(" ");
    },
    
    /**
    * Called when a process start link has been clicked
    * Will start new process based on the element
    *
    * @method startProcess
    * @param eventType The name of the event
    * @param eventArgs Event arguments
    */
    startProcess: function Processes_onStartProcessClick(processId)
    {
      if (processId) 
      {
        new Activiti.widget.StartProcessInstanceForm(this.id + "-startProcessInstanceForm", processId);
      }
    },
    
    /**
     * 
     * Triggered when an action link on a process is clicked
     * 
     * @method
     */
    onProcessActionClick: function Processes_onProcessActionClick(event, args, me)
    {
      var el = args[1].e.target, 
        commonClasses = "processAction ",
        processId = el.rel;
      switch (el.className.replace(commonClasses, ""))
      {
        case "startProcess":
          me.startProcess(processId)
          break;
        case "viewProcess":
          break;
        case "viewXML":
          break;
        default:
      }
    },
    
    /**
     * Triggers every time the Processes table is reloaded
     * 
     * @method onDisplayProcesses
     * @param {Object} event
     * @param {Object} args
     */
    onDisplayProcesses: function Processes_onDisplayProcesses(event, args) 
    {
      this.widgets.dataTable.reload();
    }

  });

})();
