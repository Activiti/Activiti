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
   * @param {String} htmlId The HTML id of the dom element
   * @return {Activiti.component.Processes} The new Activiti.component.Processes instance
   * @constructor
   */
  Activiti.component.Processes = function Processes_constructor(htmlId)
  {
    Activiti.component.Processes.superclass.constructor.call(this, "Activiti.component.Processes", htmlId);
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
      this.widgets.dataTable = new Activiti.widget.DataTable(
        this.id + "-processes",
        this,
        [ { event: Activiti.event.displayProcesses, value: {} } ],
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [
          { key:"name", label: this.msg("label.name"), sortable:true },
          { key:"key", label: this.msg("label.key"), sortable:true },
          { key:"version",  label: this.msg("label.version"), sortable:true },
          { key:"action", label: this.msg("label.action") }
        ]
      );

      // Needed to load data and set up other events
      if (!Activiti.event.isInitEvent(Activiti.event.displayProcesses))
      {
        this.fireEvent(Activiti.event.displayProcesses, {}, null);
      }

      // Display form if url
      var processId = Activiti.util.getQueryStringParameter("id");
      if (processId != null)
      {
        this.onActionShowProcessStartForm({ id: processId }, this.widgets.dataTable);
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
          data = oRecord.getData();
      if (data.startFormResourceKey != null) {
        actions.push('<a href="#" class="onActionStartProcessUsingForm" title="' + this.msg("action.startProcessUsingForm") + '" tabindex="0">&nbsp;</a>');
      }
      else {
        actions.push('<a href="#" class="onActionStartProcess" title="' + this.msg("action.startProcess") + '" tabindex="0">&nbsp;</a>');
      }
      if (data.diagramResourceName != null) {
        actions.push('<a href="#" class="onActionViewProcessDiagram" title="' + this.msg("action.viewProcessDiagram") + '" tabindex="0">&nbsp;</a>');
      }
      el.innerHTML = actions.join("");
    },

    /**
     * Called when the start process link has been clicked and will display a start form
     *
     * @method onActionStartProcessUsingForm
     * @param data {Object} The process definition data
     * @param datatable {Activiti.widget.DataTable} The data table in which the link was clicked
     */
    onActionStartProcessUsingForm: function Processes_onActionStartProcessUsingForm(data, datatable)
    {
      new Activiti.widget.StartProcessInstanceForm(this.id + "-startProcessInstanceForm", data.id);
    },

    /**
     * Called when the start process link has been clicked and will start a new process
     *
     * @method onActionStartProcess
     * @param data {Object} The process definition data
     * @param datatable {Activiti.widget.DataTable} The data table in which the link was clicked
     */
    onActionStartProcess: function Processes_onActionStartProcess(data, datatable)
    {
      this.services.processService.startProcessInstance(data.id, {});
    },

    /**
     * Called when a process start link has been clicked
     * Will start new process based on the element
     *
     * @method onActionViewProcessDiagram
     * @param data {Object} The process definition data
     * @param datatable {Activiti.widget.DataTable} The data table in which the link was clicked
     */
    onActionViewProcessDiagram: function Processes_onActionViewProcessDiagram(data, datatable)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + '/deployment/' + $html(data.deploymentId) + '/resource/' + $html(data.diagramResourceName);
      Activiti.widget.PopupManager.displayImage(url);
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
