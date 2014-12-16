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
 * Task listeners
 */

var KisBpmFieldsCtrl = [ '$scope', '$modal', '$timeout', '$translate', function($scope, $modal, $timeout, $translate) {

    // Config for the modal window
    var opts = {
        template:  'kisbpm-app/configuration/properties/fields-popup.html',
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmFieldsPopupCtrl = [ '$scope', '$translate', function($scope, $translate) {

    // Put json representing form properties on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.fields !== undefined
        && $scope.property.value.fields !== null) {
        // Note that we clone the json object rather then setting it directly,
        // this to cope with the fact that the user can click the cancel button and no changes should have happened
        $scope.fields = angular.copy($scope.property.value.fields);
        
        for (var i = 0; i < $scope.fields.length; i++)
		{
			var field = $scope.fields[i];
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
        
    } else {
        $scope.fields = [];
    }

    // Array to contain selected properties (yes - we only can select one, but ng-grid isn't smart enough)
    $scope.selectedFields = [];

    // Config for grid
    $scope.gridOptions = {
        data: 'fields',
        enableRowReordering: true,
        headerRowHeight: 28,
        multiSelect: false,
        keepLastSelected : false,
        selectedItems: $scope.selectedFields,
        columnDefs: [{ field: 'name', displayName: $translate('PROPERTY.FIELDS.NAME') },
                     { field: 'implementation', displayName: $translate('PROPERTY.FIELDS.IMPLEMENTATION')}]
    };
    
    $scope.fieldDetailsChanged = function() {
    	if ($scope.selectedFields[0].stringValue != '')
    	{
    		$scope.selectedFields[0].implementation = $scope.selectedFields[0].stringValue;
    	}
    	else if ($scope.selectedFields[0].expression != '')
    	{
    		$scope.selectedFields[0].implementation = $scope.selectedFields[0].expression;
    	}
    	else if ($scope.selectedFields[0].string != '')
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
    	$scope.fields.push({ name : 'fieldName',
            implementation : '',
            stringValue : '',
            expression: '',
            string: ''});
    };

    // Click handler for remove button
    $scope.removeField = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.fields.indexOf($scope.selectedFields[0]);
            $scope.gridOptions.selectItem(index, false);
            $scope.fields.splice(index, 1);

            $scope.selectedFields.length = 0;
            if (index < $scope.fields.length) {
                $scope.gridOptions.selectItem(index + 1, true);
            } else if ($scope.fields.length > 0) {
                $scope.gridOptions.selectItem(index - 1, true);
            }
        }
    };

    // Click handler for up button
    $scope.moveFieldUp = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.fields.indexOf($scope.selectedFields[0]);
            if (index != 0) { // If it's the first, no moving up of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.fields[index];
                $scope.fields.splice(index, 1);
                $timeout(function(){
                	$scope.fields.splice(index + -1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for down button
    $scope.moveFieldDown = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.fields.indexOf($scope.selectedFields[0]);
            if (index != $scope.fields.length - 1) { // If it's the last element, no moving down of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.fields[index];
                $scope.fields.splice(index, 1);
                $timeout(function(){
                	$scope.fields.splice(index + 1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for save button
    $scope.save = function() {

        if ($scope.fields.length > 0) {
            $scope.property.value = {};
            $scope.property.value.fields = $scope.fields;
        } else {
            $scope.property.value = null;
        }

        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    $scope.cancel = function() {
      dialog.close();
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
        $scope.$hide();
    };
}];