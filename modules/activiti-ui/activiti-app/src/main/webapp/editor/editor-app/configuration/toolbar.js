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
'use strict';

var KISBPM = KISBPM || {};
KISBPM.TOOLBAR_CONFIG = {
    "items" : [
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.SAVE",
            "cssClass" : "editor-icon editor-icon-save",
            "action" : "KISBPM.TOOLBAR.ACTIONS.saveModel"
        },
        {
            "type" : "separator",
            "title" : "",
            "cssClass" : "toolbar-separator"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.CUT",
            "cssClass" : "editor-icon editor-icon-cut",
            "action" : "KISBPM.TOOLBAR.ACTIONS.cut",
            "enabled" : false,
            "enabledAction" : "element"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.COPY",
            "cssClass" : "editor-icon editor-icon-copy",
            "action" : "KISBPM.TOOLBAR.ACTIONS.copy",
            "enabled" : false,
            "enabledAction" : "element"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.PASTE",
            "cssClass" : "editor-icon editor-icon-paste",
            "action" : "KISBPM.TOOLBAR.ACTIONS.paste",
            "enabled" : false
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.DELETE",
            "cssClass" : "editor-icon editor-icon-delete",
            "action" : "KISBPM.TOOLBAR.ACTIONS.deleteItem",
            "enabled" : false,
            "enabledAction" : "element"
        },
        {
            "type" : "separator",
            "title" : "TOOLBAR.ACTION.SAVE",
            "cssClass" : "toolbar-separator"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.REDO",
            "cssClass" : "editor-icon editor-icon-redo",
            "action" : "KISBPM.TOOLBAR.ACTIONS.redo",
            "enabled" : false
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.UNDO",
            "cssClass" : "editor-icon editor-icon-undo",
            "action" : "KISBPM.TOOLBAR.ACTIONS.undo",
            "enabled" : false
        },
        {
            "type" : "separator",
            "title" : "TOOLBAR.ACTION.SAVE",
            "cssClass" : "toolbar-separator"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ALIGNVERTICAL",
            "cssClass" : "editor-icon editor-icon-align-vertical",
            "action" : "KISBPM.TOOLBAR.ACTIONS.alignVertical",
            "enabled" : false,
            "enabledAction" : "element",
            "disableInForm" : true,
            "minSelectionCount" : 2
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ALIGNHORIZONTAL",
            "cssClass" : "editor-icon editor-icon-align-horizontal",
            "action" : "KISBPM.TOOLBAR.ACTIONS.alignHorizontal",
            "enabledAction" : "element",
            "enabled" : false,
            "disableInForm" : true,
            "minSelectionCount" : 2
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.SAMESIZE",
            "cssClass" : "editor-icon editor-icon-same-size",
            "action" : "KISBPM.TOOLBAR.ACTIONS.sameSize",
            "enabledAction" : "element",
            "enabled" : false,
            "disableInForm" : true,
            "minSelectionCount" : 2
        },
        {
        	"type" : "separator",
        	"title" : "TOOLBAR.ACTION.SAVE",
        	"cssClass" : "toolbar-separator",
        	"disableInForm" : true
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMIN",
            "cssClass" : "editor-icon editor-icon-zoom-in",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomIn"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMOUT",
            "cssClass" : "editor-icon editor-icon-zoom-out",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomOut"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMACTUAL",
            "cssClass" : "editor-icon editor-icon-zoom-actual",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomActual"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMFIT",
            "cssClass" : "editor-icon editor-icon-zoom-fit",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomFit"
        },
        {
            "type" : "separator",
            "title" : "TOOLBAR.ACTION.SAVE",
            "cssClass" : "toolbar-separator",
            "disableInForm" : true
        },
    	{
            "type" : "button",
            "title" : "TOOLBAR.ACTION.BENDPOINT.ADD",
            "cssClass" : "editor-icon editor-icon-bendpoint-add",
            "action" : "KISBPM.TOOLBAR.ACTIONS.addBendPoint",
            "id" : "add-bendpoint-button",
            "disableInForm" : true
    	},
    	{
    	    "type" : "button",
    	    "title" : "TOOLBAR.ACTION.BENDPOINT.REMOVE",
    	    "cssClass" : "editor-icon editor-icon-bendpoint-remove",
    	    "action" : "KISBPM.TOOLBAR.ACTIONS.removeBendPoint",
    	    "id" : "remove-bendpoint-button",
    	    "disableInForm" : true
    	},
        {
            "type" : "separator",
            "title": "",
            "cssClass" : "toolbar-separator",
            "disableInForm" : true
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.HELP",
            "cssClass" : "glyphicon glyphicon-question-sign",
            "action" : "KISBPM.TOOLBAR.ACTIONS.help"
        }
    ],
    
    "secondaryItems" : []
};