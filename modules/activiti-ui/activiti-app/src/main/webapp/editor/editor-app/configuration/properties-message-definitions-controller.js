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
 * Execution listeners
 */

angular.module('activitiModeler').controller('ActivitiMessageDefinitionsCtrl', ['$scope', '$modal', function ($scope, $modal) {

    // Config for the modal window
    var opts = {
        template: 'editor-app/configuration/properties/message-definitions-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    _internalCreateModal(opts, $modal, $scope);
}]);

//Need a separate controller for the modal window due to https://github.com/angular-ui/bootstrap/issues/259
// Will be fixed in a newer version of Angular UI
angular.module('activitiModeler').controller('ActivitiMessageDefinitionsPopupCtrl',
    ['$scope', '$q', '$translate', '$timeout', function ($scope, $q, $translate, $timeout) {

        // Put json representing mesage definitions on scope
        if ($scope.property.value !== undefined && $scope.property.value !== null && $scope.property.value.length > 0) {

            if ($scope.property.value.constructor == String) {
                $scope.messageDefinitions = JSON.parse($scope.property.value);
            }
            else {
                // Note that we clone the json object rather then setting it directly,
                // this to cope with the fact that the user can click the cancel button and no changes should have happened
                $scope.messageDefinitions = angular.copy($scope.property.value);
            }

        } else {
            $scope.messageDefinitions = [];
        }

        // Array to contain selected mesage definitions (yes - we only can select one, but ng-grid isn't smart enough)
        $scope.selectedMessageDefinition = undefined;
        $scope.translationsRetrieved = false;

        $scope.labels = {};

        var idPromise = $translate('PROPERTY.MESSAGEDEFINITIONS.ID');
        var namePromise = $translate('PROPERTY.MESSAGEDEFINITIONS.NAME');

        $q.all([idPromise, namePromise]).then(function (results) {

            $scope.labels.idLabel = results[0];
            $scope.labels.nameLabel = results[1];
            $scope.translationsRetrieved = true;

            // Config for grid
            $scope.gridOptions = {
                data: $scope.messageDefinitions,
                headerRowHeight: 28,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: false,
                enableSorting: false,
                columnDefs: [
                    {field: 'id', displayName: $scope.labels.idLabel},
                    {field: 'name', displayName: $scope.labels.nameLabel}]
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    $scope.selectedMessageDefinition = row.entity;
                });
            };
        });

        // Click handler for add button
        $scope.addNewMessageDefinition = function () {
            var newMessageDefinition = {id: '', name: ''};

            $scope.messageDefinitions.push(newMessageDefinition);
            $timeout(function () {
                $scope.gridApi.selection.toggleRowSelection(newMessageDefinition);
            });
        };

        // Click handler for remove button
        $scope.removeMessageDefinition = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.messageDefinitions.indexOf(selectedItems[0]);
                $scope.gridApi.selection.toggleRowSelection(selectedItems[0]);
                $scope.messageDefinitions.splice(index, 1);

                if ($scope.messageDefinitions.length == 0) {
                    $scope.selectedMesageDefinition = undefined;
                }

                $timeout(function () {
                    if ($scope.messageDefinitions.length > 0) {
                        $scope.gridApi.selection.toggleRowSelection($scope.messageDefinitions[0]);
                    }
                });
            }
        };

        // Click handler for save button
        $scope.save = function () {

            if ($scope.messageDefinitions.length > 0) {
                $scope.property.value = $scope.messageDefinitions;
            } else {
                $scope.property.value = null;
            }

            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        $scope.cancel = function () {
            $scope.property.mode = 'read';
            $scope.$hide();
        };

        // Close button handler
        $scope.close = function () {
            $scope.property.mode = 'read';
            $scope.$hide();
        };

    }]);