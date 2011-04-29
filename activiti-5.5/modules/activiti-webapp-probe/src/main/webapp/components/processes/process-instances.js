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
* Activiti.component.ProcessInstances
*
* Displays a list of Process Instances
*
*
* @namespace Activiti
* @class Activiti.component.ProcessInstances
*/
(function()
{
  /**
  * Shortcuts
  */
  var Dom = YAHOO.util.Dom,
    Selector = YAHOO.util.Selector,
    Event = YAHOO.util.Event,
    $html = Activiti.util.decodeHTML;

  /**
  * ProcessInstances constructor.
  *
  * @param {String} htmlId The HTML id of the parent element
  * @return {Activiti.component.ProcessInstances} The new component.ProcessInstances instance
  * @constructor
  */
  Activiti.component.ProcessInstances = function ProcessInstances_constructor(htmlId)
  {
    Activiti.component.ProcessInstances.superclass.constructor.call(this, "Activiti.component.ProcessInstances", htmlId);
    this.services.processService = new Activiti.service.ProcessService(this);

    this.onEvent(Activiti.event.selectProcess, this.onSelectProcessEvent);

    return this;
  };

  YAHOO.extend(Activiti.component.ProcessInstances, Activiti.component.Base,
  {

    _currentProcessId : null,
    _hasDiagram : null,
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function ProcessInstances_onReady()
    {
      this.widgets.dataTable = new Activiti.widget.DataTable(
            this.id + "-processInstances",
            this,
            [
              { event: Activiti.event.selectProcess, value: {} }
            ],
            this.id + "-datatable",
            [ this.id + "-paginator" ],
            [
              { key: "id", label: this.msg("label.id"), sortable:true },
              { key: "businessKey", label: this.msg("label.businessKey"), sortable:true },
              { key: "startTime", label: this.msg("label.startTime"), sortable:true },
              { key: "action", label: this.msg("label.actions") }

            ]
          );
    },

    onSelectProcessEvent: function DatabaseTable_onSelectProcessEvent(event, args) {
      var filter = this.getEventValue(args);
      _currentProcessId = filter.processId;
      _hasDiagram = filter.diagram;
    },



    /**
     * Activiti.widget.ProcessInstances-callback to construct the url to use to load data into the data table.
     *
     * @method onDataTableCreateURL
     * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
     * @param eventName The name of the event to create a url from
     * @param eventValue The event values to create a url from
     * @return A url, based on the event, to use when loading data into the data table
     */
    onDataTableCreateURL: function ProcessInstances_onDataTableCreateURL(dataTable, eventName, eventValue)
    {
      return this.services.processService.loadProcessInstancesURL(_currentProcessId, eventValue);
    },

    /**
     *
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Action row
     *
     * @method onDataTableRenderCellAction
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */
    onDataTableRenderCellAction: function ProcessInstances_onDataTableRenderCellAction(dataTable, el, oRecord, oColumn, oData) {
      var data = oRecord.getData();

      if (_hasDiagram) {
        Activiti.widget.createCellButton(this, el, this.msg("action.display"),
            "view-process-diagram", this.onViewProcessDiagram, data, dataTable);

      }
    },

    /**
     * Displays process instance diagram with current activities highlighted.
     *
     * @method onViewProcessDiagram
     */
    onViewProcessDiagram: function Processes_onViewProcessDiagram(data, datatable)
    {
      
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + '/processInstance/' + $html(data.id) + '/diagram?noCache=' + (new Date().getTime());
      Activiti.widget.PopupManager.displayImage(url);
    },
    /**
     *
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the StartTime row
     *
     * This funtion formats the time according to a property string prior to display.
     *
     * @method onDataTableRenderCellStartTime
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */
    onDataTableRenderCellStartTime: function Processes_onDataTableRenderCellStartTime(dataTable, el, oRecord, oColumn, oData)
    {
      el.innerHTML = Activiti.thirdparty.dateFormat(Activiti.thirdparty.fromISO8601(oRecord.getData().startTime), this.msg("Activiti.date-format.default"));
    }

  });

})();