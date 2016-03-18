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
var KISBPM = KISBPM || {};

/** Inspired by https://github.com/krasimir/EventBus/blob/master/src/EventBus.js */
KISBPM.eventBus = {

    /** Event fired when the editor is loaded and ready */
    EVENT_TYPE_EDITOR_READY: 'event-type-editor-ready',

    /** Event fired when a selection is made on the canvas. */
    EVENT_TYPE_SELECTION_CHANGE: 'event-type-selection-change',

    /** Event fired when a toolbar button has been clicked. */
    EVENT_TYPE_TOOLBAR_BUTTON_CLICKED: 'event-type-toolbar-button-clicked',

    /** Event fired when a stencil item is dropped on the canvas. */
    EVENT_TYPE_ITEM_DROPPED: 'event-type-item-dropped',

    /** Event fired when a property value is changed. */
    EVENT_TYPE_PROPERTY_VALUE_CHANGED: 'event-type-property-value-changed',

    /** Event fired on double click in canvas. */
    EVENT_TYPE_DOUBLE_CLICK: 'event-type-double-click',

    /** Event fired on a mouse out */
    EVENT_TYPE_MOUSE_OUT: 'event-type-mouse-out',

    /** Event fired on a mouse over */
    EVENT_TYPE_MOUSE_OVER: 'event-type-mouse-over',

    /** Event fired when a model is saved. */
    EVENT_TYPE_MODEL_SAVED: 'event-type-model-saved',
    
    /** Event fired when the quick menu buttons should be hidden. */
    EVENT_TYPE_HIDE_SHAPE_BUTTONS: 'event-type-hide-shape-buttons',
    
    /** Event fired when the validation popup should be shown. */
    EVENT_TYPE_SHOW_VALIDATION_POPUP: 'event-type-show-validation-popup',
    
    /** Event fired when a different process must be loaded. */
    EVENT_TYPE_NAVIGATE_TO_PROCESS: 'event-type-navigate-to-process',

    /** A mapping for storing the listeners*/
    listeners: {},

    /** The Oryx editor, which is stored locally to send events to */
    editor: null,

    /**
     * Add an event listener to the event bus, listening to the event with the provided type.
     * Type and callback are mandatory parameters.
     *
     * Provide scope parameter if it is important that the callback is executed
     * within a specific scope.
     */
    addListener: function (type, callback, scope) {

        // Add to the listeners map
        if (typeof this.listeners[type] !== "undefined") {
            this.listeners[type].push({scope: scope, callback: callback});
        } else {
            this.listeners[type] = [
                {scope: scope, callback: callback}
            ];
        }
    },

    /**
     * Removes the provided event listener.
     */
    removeListener: function (type, callback, scope) {
        if (typeof this.listeners[type] != "undefined") {
            var numOfCallbacks = this.listeners[type].length;
            var newArray = [];
            for (var i = 0; i < numOfCallbacks; i++) {
                var listener = this.listeners[type][i];
                if (listener.scope === scope && listener.callback === callback) {
                    // Do nothing, this is the listener and doesn't need to survive
                } else {
                    newArray.push(listener);
                }
            }
            this.listeners[type] = newArray;
        }
    },

    hasListener:function(type, callback, scope) {
        if(typeof this.listeners[type] != "undefined") {
            var numOfCallbacks = this.listeners[type].length;
            if(callback === undefined && scope === undefined){
                return numOfCallbacks > 0;
            }
            for(var i=0; i<numOfCallbacks; i++) {
                var listener = this.listeners[type][i];
                if(listener.scope == scope && listener.callback == callback) {
                    return true;
                }
            }
        }
        return false;
    },

    /**
     * Dispatch an event to all event listeners registered to that specific type.
     */
    dispatch:function(type, event) {
        if(typeof this.listeners[type] != "undefined") {
            var numOfCallbacks = this.listeners[type].length;
            for(var i=0; i<numOfCallbacks; i++) {
                var listener = this.listeners[type][i];
                if(listener && listener.callback) {
                    listener.callback.apply(listener.scope, [event]);
                }
            }
        }
    },

    dispatchOryxEvent: function(event, uiObject) {
        KISBPM.eventBus.editor.handleEvents(event, uiObject);
    }

};