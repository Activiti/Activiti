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

/*
 * Execution listeners
 */

var KisBpmExecutionListenersCtrl = [ '$scope', '$modal', '$timeout', '$translate', function($scope, $modal, $timeout, $translate) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/execution-listeners-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmExecutionListenersPopupCtrl = [ '$scope', '$q', '$translate', function($scope, $q, $translate) {

    // Put json representing form properties on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.executionListeners !== undefined
        && $scope.property.value.executionListeners !== null) {
        
        if ($scope.property.value.executionListeners.constructor == String)
        {
            $scope.executionListeners = JSON.parse($scope.property.value.executionListeners);
        }
        else
        {
            // Note that we clone the json object rather then setting it directly,
            // this to cope with the fact that the user can click the cancel button and no changes should have happened
            $scope.executionListeners = angular.copy($scope.property.value.executionListeners);
        }
        
        for (var i = 0; i < $scope.executionListeners.length; i++)
        {
            var executionListener = $scope.executionListeners[i];
            if (executionListener.className !== undefined && executionListener.className !== '')
            {
                executionListener.implementation = executionListener.className;
            }
            else if (executionListener.expression !== undefined && executionListener.expression !== '')
            {
                executionListener.implementation = executionListener.expression;
            }
            else if (executionListener.delegateExpression !== undefined && executionListener.delegateExpression !== '')
            {
                executionListener.implementation = executionListener.delegateExpression;
            }
        }
    } else {
        $scope.executionListeners = [];
    }

    // Array to contain selected properties (yes - we only can select one, but ng-grid isn't smart enough)
    $scope.selectedListeners = [];
    $scope.selectedFields = [];
    $scope.translationsRetrieved = false;
    
    $scope.labels = {};
    
    var eventPromise = $translate('PROPERTY.EXECUTIONLISTENERS.EVENT');
    var implementationPromise = $translate('PROPERTY.EXECUTIONLISTENERS.FIELDS.IMPLEMENTATION');
    var namePromise = $translate('PROPERTY.EXECUTIONLISTENERS.FIELDS.NAME');
    
    $q.all([eventPromise, implementationPromise, namePromise]).then(function(results) { 
        $scope.labels.eventLabel = results[0];
        $scope.labels.implementationLabel = results[1];
        $scope.labels.nameLabel = results[2];
        $scope.translationsRetrieved = true;
        
        // Config for grid
        $scope.gridOptions = {
            data: 'executionListeners',
            enableRowReordering: true,
            headerRowHeight: 28,
            multiSelect: false,
            keepLastSelected : false,
            selectedItems: $scope.selectedListeners,
            afterSelectionChange: function (rowItem, event) {
                $scope.selectedFields.length = 0;
                if ($scope.selectedListeners.length > 0)
                {
                    var fields = $scope.selectedListeners[0].fields;
                    if (fields !== undefined && fields !== null)
                    {
                        for (var i = 0; i < fields.length; i++)
                        {
                            var field = fields[i];
                            if (field.stringValue !== undefined && field.stringValue !== '')
                            {
                                field.implementation = field.stringValue;
                            }
                            else if (field.expression !== undefined && field.expression !== '')
                            {
                                field.implementation = field.expression;
                            }
                            else if (field.string !== undefined && field.string !== '')
                            {
                                field.implementation = field.string;
                            }
                        }
                    }
                }
            },
            columnDefs: [{ field: 'event', displayName: $scope.labels.eventLabel },
                { field: 'implementation', displayName: $scope.labels.implementationLabel }]
        };
        
        // Config for field grid
        $scope.gridFieldOptions = {
            data: 'selectedListeners[0].fields',
            enableRowReordering: true,
            headerRowHeight: 28,
            multiSelect: false,
            keepLastSelected : false,
            selectedItems: $scope.selectedFields,
            columnDefs: [{ field: 'name', displayName: $scope.labels.nameLabel },
                { field: 'implementation', displayName: $scope.labels.implementationLabel}]
        };
    });
    
    $scope.listenerDetailsChanged = function() {
        if ($scope.selectedListeners[0].className !== '')
        {
            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].className;
        }
        else if ($scope.selectedListeners[0].expression !== '')
        {
            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].expression;
        }
        else if ($scope.selectedListeners[0].delegateExpression !== '')
        {
            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].delegateExpression;
        }
        else
        {
            $scope.selectedListeners[0].implementation = '';
        }
    };

    // Click handler for add button
    $scope.addNewListener = function() {
        $scope.executionListeners.push({ event : 'start',
            implementation : '',
            className : '',
            expression: '',
            delegateExpression: ''});
    };

    // Click handler for remove button
    $scope.removeListener = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.executionListeners.indexOf($scope.selectedListeners[0]);
            $scope.gridOptions.selectItem(index, false);
            $scope.executionListeners.splice(index, 1);

            $scope.selectedListeners.length = 0;
            if (index < $scope.executionListeners.length) {
                $scope.gridOptions.selectItem(index + 1, true);
            } else if ($scope.executionListeners.length > 0) {
                $scope.gridOptions.selectItem(index - 1, true);
            }
        }
    };

    // Click handler for up button
    $scope.moveListenerUp = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.executionListeners.indexOf($scope.selectedListeners[0]);
            if (index != 0) { // If it's the first, no moving up of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.executionListeners[index];
                $scope.executionListeners.splice(index, 1);
                $timeout(function(){
                    $scope.executionListeners.splice(index + -1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for down button
    $scope.moveListenerDown = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.executionListeners.indexOf($scope.selectedListeners[0]);
            if (index != $scope.executionListeners.length - 1) { // If it's the last element, no moving down of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.executionListeners[index];
                $scope.executionListeners.splice(index, 1);
                $timeout(function(){
                    $scope.executionListeners.splice(index + 1, 0, temp);
                }, 100);

            }
        }
    };
    
    $scope.fieldDetailsChanged = function() {
        if ($scope.selectedFields[0].stringValue !== '')
        {
            $scope.selectedFields[0].implementation = $scope.selectedFields[0].stringValue;
        }
        else if ($scope.selectedFields[0].expression !== '')
        {
            $scope.selectedFields[0].implementation = $scope.selectedFields[0].expression;
        }
        else if ($scope.selectedFields[0].string !== '')
        {
            $scope.selectedFields[0].implementation = $scope.selectedFields[0].string;
        }
        else
        {
            $scope.selectedFields[0].implementation = '';
        }
    };

    // Click handler for add button
    $scope.addNewField = function() {
        if ($scope.selectedListeners.length > 0)
        {
            if ($scope.selectedListeners[0].fields == undefined)
            {
                $scope.selectedListeners[0].fields = [];
            }
            $scope.selectedListeners[0].fields.push({ name : 'fieldName',
                implementation : '',
                stringValue : '',
                expression: '',
                string: ''});
        }
    };

    // Click handler for remove button
    $scope.removeField = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.selectedListeners[0].fields.indexOf($scope.selectedFields[0]);
            $scope.gridFieldOptions.selectItem(index, false);
            $scope.selectedListeners[0].fields.splice(index, 1);

            $scope.selectedFields.length = 0;
            if (index < $scope.selectedListeners[0].fields.length) {
                $scope.gridFieldOptions.selectItem(index + 1, true);
            } else if ($scope.selectedListeners[0].fields.length > 0) {
                $scope.gridFieldOptions.selectItem(index - 1, true);
            }
        }
    };

    // Click handler for up button
    $scope.moveFieldUp = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.selectedListeners[0].fields.indexOf($scope.selectedFields[0]);
            if (index != 0) { // If it's the first, no moving up of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.selectedListeners[0].fields[index];
                $scope.selectedListeners[0].fields.splice(index, 1);
                $timeout(function(){
                    $scope.selectedListeners[0].fields.splice(index + -1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for down button
    $scope.moveFieldDown = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.selectedListeners[0].fields.indexOf($scope.selectedFields[0]);
            if (index != $scope.selectedListeners[0].fields.length - 1) { // If it's the last element, no moving down of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.selectedListeners[0].fields[index];
                $scope.selectedListeners[0].fields.splice(index, 1);
                $timeout(function(){
                    $scope.selectedListeners[0].fields.splice(index + 1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for save button
    $scope.save = function() {

        if ($scope.executionListeners.length > 0) {
            $scope.property.value = {};
            $scope.property.value.executionListeners = $scope.executionListeners;
        } else {
            $scope.property.value = null;
        }

        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    $scope.cancel = function() {
        $scope.$hide();
        $scope.property.mode = 'read';
    };

    // Close button handler
    $scope.close = function() {
        $scope.$hide();
        $scope.property.mode = 'read';
    };

}];