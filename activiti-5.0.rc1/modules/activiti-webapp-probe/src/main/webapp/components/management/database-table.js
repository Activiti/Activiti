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
 * Activiti.component.DatabaseTable
 *
 * Displays the contents of the selected Database Table.
 *
 * @namespace Activiti
 * @class Activiti.component.DatabaseTable
 */
(function()
{
  /**
   * Short cuts
   */
  var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      Event = YAHOO.util.Event,
      Pagination = Activiti.util.Pagination,
      $html = Activiti.util.encodeHTML;

  /**
   * DatabaseTable constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.DatabaseTable} The new component.DatabaseTable instance
   * @constructor
   */
  Activiti.component.DatabaseTable = function DatabaseTable_constructor(htmlId)
  {
    Activiti.component.DatabaseTable.superclass.constructor.call(this, "Activiti.component.DatabaseTable", htmlId);
    this._currentTableName = null;
    this._metadata = {};
    this.widgets.dataTables = {};

    // Create new service instances and set this component to receive the callbacks
    this.services.managementService = new Activiti.service.ManagementService(this);

    // Listen for events that interest this component
    this.onEvent(Activiti.event.selectDatabaseTable, this.onSelectDatabaseTableEvent);

    return this;
  };

  YAHOO.extend(Activiti.component.DatabaseTable, Activiti.component.Base,
  {

    /**
     * The name of the currently selected database table
     */
    _currentTableName: null,

    /**
     * The database tables
     */
    _metadata: null,

    /**
     * Fired by YUI when parent element is available for scripting.
     * Template initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function DatabaseTable_onReady()
    {
    },

    /**
     * Changes the title
     * The data table will handle it self.
     *
     * @method onSelectDatabaseTableEvent
     * @param event
     * @param args
     */
    onSelectDatabaseTableEvent: function DatabaseTable_onSelectDatabaseTableEvent(event, args) {
      var filter = this.getEventValue(args);

      // If table already exists, then display it and load it
      if (this._currentTableName == filter.table) {
        // Table already displayed, do nothing since it will reload itself when it gets the event
      }
      else {
        // Hide previous table
        var currentContainerEl = Dom.get(this.id + "-" + this._currentTableName);
        if (currentContainerEl) {
          Dom.setStyle(currentContainerEl, "display", "none");
        }

        if (this._metadata[filter.table]) {
          // Table already exists (or is about to be created), just display it
          Dom.setStyle(this.id + "-" + filter.table, "display", "");
        }
        else {
          /**
           * Set metadata to true temporarily and load the actual metadata so we can create the database table.
           * Pass in a callback object so we know if the table shall be created or not in the callback.
           */
          this._metadata[filter.table] = true;
          this.services.managementService.loadTable(filter.table, {
            createTable: true,
            event: {
              event: event,
              value: filter
            }
          });
        }
        this._currentTableName = filter.table;
      }
    },

    /**
     * Uses the meta data to create a database table, if callback object is marked to do it.
     *
     * @method onLoadTableSuccess
     * @param response {Object} The server response
     * @param obj {Object} THe callback object
     */
    onLoadTableSuccess: function DatabaseTable_onLoadTableSuccess(response, obj) {
      var metadata = response.json;
      if (obj.createTable && this._metadata[metadata.tableName] == true) {
        // Replace the temporarily placed boolean with the metadata
        this._metadata[metadata.tableName] = metadata;

        // Create required dom elements
        var containersEl = Dom.get(this.id + "-containers");
        var containerEl = document.createElement("div");
        Activiti.util.setDomId(containerEl, this.id + "-" + metadata.tableName);
        containersEl.appendChild(containerEl);

        var paginatorEl = document.createElement("div");
        Activiti.util.setDomId(paginatorEl, this.id + "-" + metadata.tableName + "-paginator");
        containerEl.appendChild(paginatorEl);

        var headerEl = document.createElement("h1");
        Activiti.util.setDomId(headerEl, this.id + "-" + metadata.tableName + "-header");
        headerEl.innerHTML = $html(metadata.tableName);
        containerEl.appendChild(headerEl);

        var datatableEl = document.createElement("div");
        Activiti.util.setDomId(datatableEl, this.id + "-" + metadata.tableName + "-datatable");
        containerEl.appendChild(datatableEl);

        // Create a data table that supports pagination and browser back buttons that will call onDataTableCreateURL
        var columnIds = metadata.columnNames,
            columnDefinitions = [];
        for (var i = 0, il = columnIds.length; i < il; i++) {
          columnDefinitions.push({ key: columnIds[i], label: columnIds[i], sortable: true });
        }
        this.widgets.dataTables[metadata.tableName] = new Activiti.widget.DataTable(this.id + "-" + metadata.tableName + "-table",
          this,
          [ { event: Activiti.event.selectDatabaseTable, value: { table: metadata.tableName } } ],
          this.id + "-" + metadata.tableName + "-datatable",
          [ this.id + "-" + metadata.tableName + "-paginator" ],
          columnDefinitions,
          columnIds            
        );

        // Re-fire the event but without bookmarking it so the data will be loaded
        Activiti.event.fire(Activiti.event.selectDatabaseTable, obj.event.value, null, false);
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
    onDataTableCreateURL: function DatabaseTable_onDataTableCreateURL(dataTable, eventName, eventValue) {
      if (eventValue) {
        if (!eventValue[Pagination.SORT]) {
          // Set default sort order if not provided
          var columnNames = this._metadata[eventValue.table].columnNames;
          eventValue[Pagination.SORT] = columnNames.length > 0 ? columnNames[0] : null;
        }
        return this.services.managementService.loadTableDataURL(eventValue.table, eventValue);
      }
      else {
        return null;
      }
    }
  });

})();
