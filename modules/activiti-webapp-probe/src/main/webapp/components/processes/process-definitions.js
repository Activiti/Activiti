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
 * Displays deployed processes, sends events to other components when process is clicked.
 *
 * Will:
 * - Display process with version
 * - Fire an Activiti.event.selectProcess event when a table is clicked so other components may use the table.
 *
 * @namespace Activiti
 * @class Activiti.component.Processes
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
   * Processes constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.Processes} The new component.Processes instance
   * @constructor
   */
  Activiti.component.Processes = function Processes_constructor(htmlId)
  {
    Activiti.component.Processes.superclass.constructor.call(this, "Activiti.component.Processes", htmlId);

    // Listen for events that interest us
    this.onEvent(Activiti.event.selectProcess, this.onSelectProcessEvent);

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
      // Attach event listeners on all table links
      var tableEls = Selector.query("li.table a", this.id);
      Event.addListener(tableEls, "click", this.onProcessClick, null, this);

      // Simulate click on first table if none was given from url
      if (!Activiti.event.isInitEvent(Activiti.event.selectProcess)) {
        if (tableEls.length > 0) {
          this.onProcessClick(null, tableEls[0]);
        }
      }
    },


    /**
     * Will fire a Activiti.event.selectProcess event so other components may display it
     *
     * @method onTableClick
     * @param e {object} The click event
     */
    onProcessClick: function Processes_onProcessClick(e, el)
    {
      // Get target, highlight the link and fire the event
      el = el ? el : Event.getTarget(e);
      var filter = Activiti.util.argumentStringToObject(el.getAttribute("rel"));

      this.fireEvent(Activiti.event.selectProcess, filter, e, true);
    },

    /**
     * Highlights the selected process.
     *
     * @method onSelectProcessEvent
     * @param event
     * @param args
     */
    onSelectProcessEvent: function TaskFilters_onSelectProcessEvent(event, args) {
      // Highlight the process
      var f = this.getEventValue(args);
      var filter = 'processId='+f.processId+'&diagram='+((f.diagram) ? f.diagram : '')

      filterEl = Selector.query("li.table a[rel=" + filter + "]", this.id, true),
      filterEls = Selector.query("li.table a", this.id);
      Activiti.util.toggleClass(filterEls, filterEl, "current");
    }

  });

})();
