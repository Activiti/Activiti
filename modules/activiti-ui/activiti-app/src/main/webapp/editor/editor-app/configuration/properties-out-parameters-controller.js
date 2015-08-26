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
 * Input parameters for call activity
 */

angular.module('activitiModeler').controller('KisBpmOutParametersCtrl',
    ['$scope', '$modal', '$timeout', '$translate', function ($scope, $modal, $timeout, $translate) {

        // Config for the modal window
        var opts = {
            template: 'editor-app/configuration/properties/out-parameters-popup.html?version=' + Date.now(),
            scope: $scope
        };

        // Open the dialog
        _internalCreateModal(opts, $modal, $scope);
    }]);

angular.module('activitiModeler').controller('KisBpmOutParametersPopupCtrl',
    ['$scope', '$q', '$translate', '$timeout', function ($scope, $q, $translate, $timeout) {

        // Put json representing form properties on scope
        if ($scope.property.value !== undefined && $scope.property.value !== null
            && $scope.property.value.outParameters !== undefined
            && $scope.property.value.outParameters !== null) {
            // Note that we clone the json object rather then setting it directly,
            // this to cope with the fact that the user can click the cancel button and no changes should have happened
            $scope.parameters = angular.copy($scope.property.value.outParameters);
        } else {
            $scope.parameters = [];
        }

        $scope.translationsRetrieved = false;
        $scope.labels = {};

        var sourcePromise = $translate('PROPERTY.PARAMETER.SOURCE');
        var sourceExpressionPromise = $translate('PROPERTY.PARAMETER.SOURCEEXPRESSION');
        var targetPromise = $translate('PROPERTY.PARAMETER.TARGET');

        $q.all([sourcePromise, sourceExpressionPromise, targetPromise]).then(function (results) {
            $scope.labels.sourceLabel = results[0];
            $scope.labels.sourceExpressionLabel = results[1];
            $scope.labels.targetLabel = results[2];
            $scope.translationsRetrieved = true;

            // Config for grid
            $scope.gridOptions = {
                data: $scope.parameters,
                headerRowHeight: 28,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: false,
                enableSorting: false,
                columnDefs: [{field: 'source', displayName: $scope.labels.sourceLabel},
                    {field: 'sourceExpression', displayName: $scope.labels.sourceExpressionLabel},
                    {field: 'target', displayName: $scope.labels.targetLabel}]
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    $scope.selectedParameter = row.entity;
                });
            };
        });

        // Click handler for add button
        $scope.addNewParameter = function () {
            var newParameter = {
                source: '',
                sourceExpression: '',
                target: ''};

            $scope.parameters.push(newParameter);
            $timeout(function () {
                $scope.gridApi.selection.toggleRowSelection(newParameter);
            });
        };

        // Click handler for remove button
        $scope.removeParameter = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.parameters.indexOf(selectedItems[0]);
                $scope.gridApi.selection.toggleRowSelection(selectedItems[0]);
                $scope.parameters.splice(index, 1);

                if ($scope.parameters.length == 0) {
                    $scope.selectedParameter = undefined;
                }

                $timeout(function () {
                    if ($scope.parameters.length > 0) {
                        $scope.gridApi.selection.toggleRowSelection($scope.parameters[0]);
                    }
                });
            }
        };

        // Click handler for up button
        $scope.moveParameterUp = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.parameters.indexOf(selectedItems[0]);
                if (index != 0) { // If it's the first, no moving up of course
                    var temp = $scope.parameters[index];
                    $scope.parameters.splice(index, 1);
                    $timeout(function () {
                        $scope.parameters.splice(index + -1, 0, temp);
                        $timeout(function () {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for down button
        $scope.moveParameterDown = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.parameters.indexOf(selectedItems[0]);
                if (index != $scope.parameters.length - 1) { // If it's the last element, no moving down of course
                    var temp = $scope.parameters[index];
                    $scope.parameters.splice(index, 1);
                    $timeout(function () {
                        $scope.parameters.splice(index + 1, 0, temp);
                        $timeout(function () {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for save button
        $scope.save = function () {

            if ($scope.parameters.length > 0) {
                $scope.property.value = {};
                $scope.property.value.outParameters = $scope.parameters;
            } else {
                $scope.property.value = null;
            }

            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        $scope.cancel = function () {
            $scope.close();
        };

        // Close button handler
        $scope.close = function () {
            $scope.property.mode = 'read';
            $scope.$hide();
        };

    }]);