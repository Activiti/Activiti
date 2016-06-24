/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

var DECISION_TABLE_TOOLBAR_CONFIG = {
    "items" : [
        {
            "type" : "button",
            "title" : "DECISION_TABLE_TOOLBAR.ACTION.SAVE",
            "cssClass" : "editor-icon editor-icon-save",
            "action" : "DECISION_TABLE_TOOLBAR.ACTIONS.saveModel",
            "disableOnReadonly": true
        }
    ],
    
    "secondaryItems" : [
		{
		    "type" : "button",
		    "title" : "DECISION_TABLE_TOOLBAR.ACTION.CLOSE",
		    "cssClass" : "glyphicon glyphicon-remove",
		    "action" : "DECISION_TABLE_TOOLBAR.ACTIONS.closeEditor"
		}
    ]
};