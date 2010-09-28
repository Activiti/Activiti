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

// YOU ARE HERE: DATATABLE Upgrade

/* New Code, not working yet
        this.dataSource = new Activiti.widget.DataTable(this.id + "-task-list",
        this,
        [ { event: Activiti.event.selectTaskFilter, value: {} }],
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [ "Name", "Key", "Version", "Action"],
        [
            {key:"Name",label:"Name",sortable:true},
            {key:"Key",label:"Key",sortable:true},
            {key:"Version",label:"Version",sortable:true},
            {key:"Action",label:"Actions"}
        ];
      );
*/
        var columnDefs = [
            {key:"Name",label:"Name",sortable:true},
            {key:"Key",label:"Key",sortable:true},
            {key:"Version",label:"Version",sortable:true},
				{key:"Action",label:"Actions"}
        ];
        this.dataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("processesTable"));
        this.dataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
        this.dataSource.responseSchema = {
            fields: [{key:"Name"},
                    {key:"Key"},
                    {key:"Version"},
						  {key:"Action"}
						  
            ]
        };

        this.myDataTable = new YAHOO.widget.DataTable("processesContainer", columnDefs, this.dataSource,
                {sortedBy:{key:"Name",dir:"asc"}}
        );
			
         // Bind Ready Event
         var linkElements = Dom.getElementsByClassName("startProcess", "a");
         for (var i = 0; i < linkElements.length; i++) 
			{
				YAHOO.util.Event.addListener(linkElements[i], "click", this.onStartProcessClick)
         }
      },
      
      /**
       * Called when a process has been selected from the process list.
       * Will start new process based on the option.
       *
       * @method onStartProcessMenuClick
       * @param eventType The name of the event
       * @param eventArgs Event arguments
       */
      onStartProcessClick: function Processes_onStartProcessClick(e)
      {
         var id = this.href.split("?id=")[1]            
			if (id) 
         {
            new Activiti.widget.StartProcessInstanceForm(this.id + "-startProcessInstanceForm", id);
				YAHOO.util.Event.preventDefault(e)
         }
      }
      
   });
   
})();
