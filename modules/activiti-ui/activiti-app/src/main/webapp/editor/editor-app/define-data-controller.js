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