/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
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
'use strict';

var KISBPM = KISBPM || {};
KISBPM.PROPERTY_CONFIG =
{
    "string": {
        "readModeTemplateUrl": "editor-app/configuration/properties/default-value-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/string-property-write-mode-template.html"
    },
    "boolean": {
        "templateUrl": "editor-app/configuration/properties/boolean-property-template.html"
    },
    "text" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/default-value-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/text-property-write-template.html"
    },
    "kisbpm-multiinstance" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/default-value-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/multiinstance-property-write-template.html"
    },
    "oryx-formproperties-complex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/form-properties-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/form-properties-write-template.html"
    },
    "oryx-executionlisteners-multiplecomplex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/execution-listeners-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/execution-listeners-write-template.html"
    },
    "oryx-tasklisteners-multiplecomplex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/task-listeners-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/task-listeners-write-template.html"
    },
    "oryx-eventlisteners-multiplecomplex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/event-listeners-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/event-listeners-write-template.html"
    },
    "oryx-usertaskassignment-complex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/assignment-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/assignment-write-template.html"
    },
    "oryx-servicetaskfields-complex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/fields-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/fields-write-template.html"
    },
    "oryx-callactivityinparameters-complex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/in-parameters-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/in-parameters-write-template.html"
    },
    "oryx-callactivityoutparameters-complex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/out-parameters-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/out-parameters-write-template.html"
    },
    "oryx-subprocessreference-complex": {
        "readModeTemplateUrl": "editor-app/configuration/properties/subprocess-reference-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/subprocess-reference-write-template.html"
    },
    "oryx-sequencefloworder-complex" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/sequenceflow-order-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/sequenceflow-order-write-template.html"
    },
    "oryx-conditionsequenceflow-complex" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/condition-expression-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/condition-expression-write-template.html"
    },
    "oryx-signaldefinitions-multiplecomplex" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/signal-definitions-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/signal-definitions-write-template.html"
    },
    "oryx-signalref-string" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/default-value-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/signal-property-write-template.html"
    },
    "oryx-messagedefinitions-multiplecomplex" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/message-definitions-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/message-definitions-write-template.html"
    },
    "oryx-messageref-string" : {
        "readModeTemplateUrl": "editor-app/configuration/properties/default-value-display-template.html",
        "writeModeTemplateUrl": "editor-app/configuration/properties/message-property-write-template.html"
    }
};
