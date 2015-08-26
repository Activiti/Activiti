/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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




