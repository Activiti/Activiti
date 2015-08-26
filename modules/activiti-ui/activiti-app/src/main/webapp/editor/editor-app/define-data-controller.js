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
/*
 * Controller for morph shape selection
 */

var KisBpmDefineDataCtrl = [ '$rootScope', '$scope', 'dialog', '$timeout', '$translate', function($rootScope, $scope, dialog, $timeout, $translate) {

    $scope.definedDataItems = [];
    $scope.selectedDataItems = [];
    
    // Config for grid
    $scope.gridOptions = {
        data: 'definedDataItems',
        enableRowSelection: true,
        headerRowHeight: 28,
        multiSelect: false,
        keepLastSelected : false,
        selectedItems: $scope.selectedDataItems,
        enableHorizontalScrollbar: 0,
        enableColumnMenus: false,
        enableSorting: false,
        columnDefs: [{ field: 'name', displayName: 'Name'},
            { field: 'value', displayName: 'Value'}]
    };
    
    // Click handler for add button
    $scope.addNewDataItem = function() {
    	$scope.definedDataItems.push({ name : '',
            value : ''});
    };

    // Click handler for remove button
    $scope.removeDataItem = function() {
        if ($scope.selectedDataItems.length > 0) {
            var index = $scope.definedDataItems.indexOf($scope.selectedDataItems[0]);
            $scope.gridOptions.selectItem(index, false);
            $scope.definedDataItems.splice(index, 1);

            $scope.selectedDataItems.length = 0;
            if (index < $scope.definedDataItems.length) {
                $scope.gridOptions.selectItem(index + 1, true);
            } else if ($scope.definedDataItems.length > 0) {
                $scope.gridOptions.selectItem(index - 1, true);
            }
        }
    };

    // Click handler for up button
    $scope.moveDataItemUp = function() {
        if ($scope.selectedParameters.length > 0) {
            var index = $scope.definedDataItems.indexOf($scope.selectedDataItems[0]);
            if (index != 0) { // If it's the first, no moving up of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.definedDataItems[index];
                $scope.definedDataItems.splice(index, 1);
                $timeout(function(){
                	$scope.definedDataItems.splice(index + -1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for down button
    $scope.moveDataItemDown = function() {
        if ($scope.selectedParameters.length > 0) {
            var index = $scope.definedDataItems.indexOf($scope.selectedDataItems[0]);
            if (index != $scope.definedDataItems.length - 1) { // If it's the last element, no moving down of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.definedDataItems[index];
                $scope.definedDataItems.splice(index, 1);
                $timeout(function(){
                	$scope.definedDataItems.splice(index + 1, 0, temp);
                }, 100);

            }
        }
    };
    
    $scope.save = function() {
      dialog.close();
    };

    $scope.cancel = function() {
      dialog.close();
    };

    // Close button handler
    $scope.close = function() {
        dialog.close();
    };

}];