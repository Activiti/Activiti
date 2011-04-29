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
 * Activiti.component.DatabaseTables
 *
 * Displays table names that will send event to other components when clicked.
 *
 * Will:
 * - Display a count of how many rows a table has
 * - Fire an Activiti.event.selectDatabaseTable event when a table is clicked so other components may use the table.
 *
 * @namespace Activiti
 * @class Activiti.component.DatabaseTables
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
   * DatabaseTables constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.DatabaseTables} The new component.DatabaseTables instance
   * @constructor
   */
  Activiti.component.DatabaseTables = function DatabaseTables_constructor(htmlId)
  {
    Activiti.component.DatabaseTables.superclass.constructor.call(this, "Activiti.component.DatabaseTables", htmlId);

    // Listen for events that interest us
    this.onEvent(Activiti.event.selectDatabaseTable, this.onSelectDatabaseTableEvent);

    return this;
  };

  YAHOO.extend(Activiti.component.DatabaseTables, Activiti.component.Base,
  {

    /**
     * Fired by YUI when parent element is available for scripting.
     * Template initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function DatabaseTables_onReady()
    {
      // Attach event listeners on all table links
      var tableEls = Selector.query("li.table a", this.id);
      Event.addListener(tableEls, "click", this.onTableClick, null, this);

      // Simulate click on first table if none was given from url
      if (!Activiti.event.isInitEvent(Activiti.event.selectDatabaseTable)) {
        if (tableEls.length > 0) {
          this.onTableClick(null, tableEls[0]);
        }
      }
    },


    /**
     * Will fire a Activiti.event.selectDatabaseTable event so other components may display it
     *
     * @method onTableClick
     * @param e {object} The click event
     */
    onTableClick: function DatabaseTables_onTableClick(e, el)
    {
      // Get target, highlight the link and fire the event
      el = el ? el : Event.getTarget(e);
      var filter = Activiti.util.argumentStringToObject(el.getAttribute("rel"));
      this.fireEvent(Activiti.event.selectDatabaseTable, filter, e, true);
    },

    /**
     * Highlights the selected table.
     *
     * @method onSelectDatabaseTableEvent
     * @param event
     * @param args
     */
    onSelectDatabaseTableEvent: function TaskFilters_onSelectDatabaseTableEvent(event, args) {
      // Highlight the table
      var filter = Activiti.util.objectToArgumentString(this.getEventValue(args, Activiti.util.Pagination.list(), true)),
          filterEl = Selector.query("li.table a[rel=" + filter + "]", this.id, true),
          filterEls = Selector.query("li.table a", this.id);
      Activiti.util.toggleClass(filterEls, filterEl, "current");
    }

  });

})();
