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

var KisBpmEventListenersCtrl = [ '$scope', '$modal', '$timeout', '$translate', function($scope, $modal, $timeout, $translate) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/event-listeners-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

//Need a separate controller for the modal window due to https://github.com/angular-ui/bootstrap/issues/259
// Will be fixed in a newer version of Angular UI
var KisBpmEventListenersPopupCtrl = [ '$scope', '$q', '$translate', function($scope, $q, $translate) {

    // Put json representing form properties on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.eventListeners !== undefined
        && $scope.property.value.eventListeners !== null) {
    	
    	if ($scope.property.value.eventListeners.constructor == String)
    	{
    		$scope.eventListeners = JSON.parse($scope.property.value.eventListeners);
    	}
    	else
    	{
    		// Note that we clone the json object rather then setting it directly,
            // this to cope with the fact that the user can click the cancel button and no changes should have happened
    		$scope.eventListeners = angular.copy($scope.property.value.eventListeners);
    	}
    	
    } else {
        $scope.eventListeners = [];
    }

    // Array to contain selected properties (yes - we only can select one, but ng-grid isn't smart enough)
    $scope.selectedListeners = [];
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
            data: 'eventListeners',
            enableRowReordering: true,
            headerRowHeight: 28,
            multiSelect: false,
            keepLastSelected : false,
            selectedItems: $scope.selectedListeners,
            afterSelectionChange: function (rowItem, event) {
            	
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
	            	
	            	if (!$scope.selectedListeners[0].events || $scope.selectedListeners[0].events.length == 0)
	                {
	                	$scope.selectedListeners[0].events = [{event: ''}];
	                }
            	}
            },
            columnDefs: [{ field: 'event', displayName: $scope.labels.eventLabel },
                { field: 'implementation', displayName: $scope.labels.implementationLabel }]
        };
    });
    
    // Click handler for + button after enum value
    $scope.addEventValue = function(index) {
        $scope.selectedListeners[0].events.splice(index + 1, 0, {event: ''});
    };

    // Click handler for - button after enum value
    $scope.removeEventValue = function(index) {
        $scope.selectedListeners[0].events.splice(index, 1);
        $scope.listenerDetailsChanged();
    };
    
    $scope.listenerDetailsChanged = function() {
    	var listener = $scope.selectedListeners[0];
    	if (listener.events)
    	{
    		var eventText = '';
    		for (var i = 0; i < listener.events.length; i++)
    		{
    			if (i > 0)
    			{
    				eventText += ", ";
    			}
    			eventText += listener.events[i].event;
    		}
    		$scope.selectedListeners[0].event = eventText;
    	}
    	
    	if (listener.rethrowEvent)
    	{
    		var implementationText = '';
    		if (listener.rethrowType && listener.rethrowType.length > 0)
    		{
    			if (listener.rethrowType === 'error' && listener.errorcode !== '')
	        	{
	        		implementationText = "Rethrow as error " + listener.errorcode;
	        	}
    			else if (listener.rethrowType === 'message' && listener.messagename !== '')
	        	{
	        		implementationText = "Rethrow as message " + listener.messagename;
	        	}
    			else if ((listener.rethrowType === 'signal' || listener.rethrowType === 'globalSignal') && listener.signalname !== '')
	        	{
	        		implementationText = "Rethrow as signal " + listener.signalname;
	        	}
    		}
    		$scope.selectedListeners[0].implementation = implementationText;
    	}
    	else
    	{
        	if ($scope.selectedListeners[0].className !== '')
        	{
        		$scope.selectedListeners[0].implementation = $scope.selectedListeners[0].className;
        	}
        	else if ($scope.selectedListeners[0].delegateExpression !== '')
        	{
        		$scope.selectedListeners[0].implementation = $scope.selectedListeners[0].delegateExpression;
        	}
        	else
        	{
        		$scope.selectedListeners[0].implementation = '';
        	}
    	}
    };

    // Click handler for add button
    $scope.addNewListener = function() {
        $scope.eventListeners.push({ event : '',
            implementation : '',
            className : '',
            delegateExpression: '',
            retrowEvent: false});
    };

    // Click handler for remove button
    $scope.removeListener = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.eventListeners.indexOf($scope.selectedListeners[0]);
            $scope.gridOptions.selectItem(index, false);
            $scope.eventListeners.splice(index, 1);

            $scope.selectedListeners.length = 0;
            if (index < $scope.eventListeners.length) {
                $scope.gridOptions.selectItem(index + 1, true);
            } else if ($scope.eventListeners.length > 0) {
                $scope.gridOptions.selectItem(index - 1, true);
            }
        }
    };

    // Click handler for up button
    $scope.moveListenerUp = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.eventListeners.indexOf($scope.selectedListeners[0]);
            if (index != 0) { // If it's the first, no moving up of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.eventListeners[index];
                $scope.eventListeners.splice(index, 1);
                $timeout(function(){
                    $scope.eventListeners.splice(index + -1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for down button
    $scope.moveListenerDown = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.eventListeners.indexOf($scope.selectedListeners[0]);
            if (index != $scope.eventListeners.length - 1) { // If it's the last element, no moving down of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.eventListeners[index];
                $scope.eventListeners.splice(index, 1);
                $timeout(function(){
                    $scope.eventListeners.splice(index + 1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for save button
    $scope.save = function() {

        if ($scope.eventListeners.length > 0) {
            $scope.property.value = {};
            $scope.property.value.eventListeners = $scope.eventListeners;
        } else {
            $scope.property.value = null;
        }

        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    $scope.cancel = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };

}];