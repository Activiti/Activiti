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
 * Form Properties
 */

angular.module('activitiModeler').controller('KisBpmFormPropertiesCtrl',
    ['$scope', '$modal', '$timeout', '$translate', function ($scope, $modal, $timeout, $translate) {

        // Config for the modal window
        var opts = {
            template: 'editor-app/configuration/properties/form-properties-popup.html?version=' + Date.now(),
            scope: $scope
        };

        // Open the dialog
        _internalCreateModal(opts, $modal, $scope);
    }]);

angular.module('activitiModeler').controller('KisBpmFormPropertiesPopupCtrl',
    ['$scope', '$q', '$translate', '$timeout', function ($scope, $q, $translate, $timeout) {

        // Put json representing form properties on scope
        if ($scope.property.value !== undefined && $scope.property.value !== null
            && $scope.property.value.formProperties !== undefined
            && $scope.property.value.formProperties !== null) {
            // Note that we clone the json object rather then setting it directly,
            // this to cope with the fact that the user can click the cancel button and no changes should have happended
            $scope.formProperties = angular.copy($scope.property.value.formProperties);
        } else {
            $scope.formProperties = [];
        }

        $scope.translationsRetrieved = false;

        $scope.labels = {};

        var idPromise = $translate('PROPERTY.FORMPROPERTIES.ID');
        var namePromise = $translate('PROPERTY.FORMPROPERTIES.NAME');
        var typePromise = $translate('PROPERTY.FORMPROPERTIES.TYPE');

        $q.all([idPromise, namePromise, typePromise]).then(function (results) {
            $scope.labels.idLabel = results[0];
            $scope.labels.nameLabel = results[1];
            $scope.labels.typeLabel = results[2];
            $scope.translationsRetrieved = true;

            // Config for grid
            $scope.gridOptions = {
                data: $scope.formProperties,
                headerRowHeight: 28,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: false,
                enableSorting: false,
                columnDefs: [{field: 'id', displayName: $scope.labels.idLabel},
                    {field: 'name', displayName: $scope.labels.nameLabel},
                    {field: 'type', displayName: $scope.labels.typeLabel}]
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    $scope.selectedProperty = row.entity;
                });
            };
        });

        // Handler for when the value of the type dropdown changes
        $scope.propertyTypeChanged = function () {

            // Check date. If date, show date pattern
            if ($scope.selectedProperty.type === 'date') {
                $scope.selectedProperty.datePattern = 'MM-dd-yyyy hh:mm';
            } else {
                delete $scope.selectedProperty.datePattern;
            }

            // Check enum. If enum, show list of options
            if ($scope.selectedProperty.type === 'enum') {
                $scope.selectedProperty.enumValues = [{value: 'value 1'}, {value: 'value 2'}];
            } else {
                delete $scope.selectedProperty.enumValues;
            }
        };

        // Click handler for + button after enum value
        var valueIndex = 3;
        $scope.addEnumValue = function (index) {
            $scope.selectedProperty.enumValues.splice(index + 1, 0, {value: 'value ' + valueIndex++});
        };

        // Click handler for - button after enum value
        $scope.removeEnumValue = function (index) {
            $scope.selectedProperty.enumValues.splice(index, 1);
        };

        // Click handler for add button
        var propertyIndex = 1;
        $scope.addNewProperty = function () {
            var newProperty = {
                id: 'new_property_' + propertyIndex++,
                name: '',
                type: 'string',
                readable: true,
                writable: true
            };

            $scope.formProperties.push(newProperty);

            $timeout(function () {
                $scope.gridApi.selection.toggleRowSelection(newProperty);
            });
        };

        // Click handler for remove button
        $scope.removeProperty = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.formProperties.indexOf(selectedItems[0]);
                $scope.gridApi.selection.toggleRowSelection(selectedItems[0]);
                $scope.formProperties.splice(index, 1);

                if ($scope.formProperties.length == 0) {
                    $scope.selectedProperty = undefined;
                }

                $timeout(function() {
                    if ($scope.formProperties.length > 0) {
                        $scope.gridApi.selection.toggleRowSelection($scope.formProperties[0]);
                    }
                });
            }
        };

        // Click handler for up button
        $scope.movePropertyUp = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.formProperties.indexOf(selectedItems[0]);
                if (index != 0) { // If it's the first, no moving up of course
                    var temp = $scope.formProperties[index];
                    $scope.formProperties.splice(index, 1);
                    $timeout(function(){
                        $scope.formProperties.splice(index + -1, 0, temp);
                        $timeout(function() {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for down button
        $scope.movePropertyDown = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.formProperties.indexOf(selectedItems[0]);
                if (index != $scope.formProperties.length - 1) { // If it's the last element, no moving down of course
                    var temp = $scope.formProperties[index];
                    $scope.formProperties.splice(index, 1);
                    $timeout(function(){
                        $scope.formProperties.splice(index + 1, 0, temp);
                        $timeout(function() {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for save button
        $scope.save = function () {

            if ($scope.formProperties.length > 0) {
                $scope.property.value = {};
                $scope.property.value.formProperties = $scope.formProperties;
            } else {
                $scope.property.value = null;
            }

            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        $scope.cancel = function () {
            $scope.$hide();
            $scope.property.mode = 'read';
        };

        // Close button handler
        $scope.close = function () {
            $scope.$hide();
            $scope.property.mode = 'read';
        };

    }])
;