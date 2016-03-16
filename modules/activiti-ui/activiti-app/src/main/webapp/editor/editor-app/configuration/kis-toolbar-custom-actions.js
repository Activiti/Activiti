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

// Create custom functions for the KIS-editor
KISBPM.TOOLBAR.ACTIONS.closeEditor =  function(services) {
	services.$location.path("/processes");
};

KISBPM.TOOLBAR.ACTIONS.navigateToProcess = function(processId) {
    var navigateEvent = {
        type: KISBPM.eventBus.EVENT_TYPE_NAVIGATE_TO_PROCESS,
        processId: processId
    };
    KISBPM.eventBus.dispatch(KISBPM.eventBus.EVENT_TYPE_NAVIGATE_TO_PROCESS, navigateEvent);
},

// Add custom buttons 
KISBPM.TOOLBAR_CONFIG.secondaryItems.push( 
	{
        "type" : "button",
        "title" : "Close",
        "cssClass" : "glyphicon glyphicon-remove",
        "action" : "KISBPM.TOOLBAR.ACTIONS.closeEditor"
    }
);




