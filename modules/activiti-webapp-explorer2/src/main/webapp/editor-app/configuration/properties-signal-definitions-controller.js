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

angular.module('activitiModeler').controller('ActivitiSignalDefinitionsCtrl', ['$scope', '$modal', function ($scope, $modal) {

    // Config for the modal window
    var opts = {
        template: 'editor-app/configuration/properties/signal-definitions-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}]);

//Need a separate controller for the modal window due to https://github.com/angular-ui/bootstrap/issues/259
// Will be fixed in a newer version of Angular UI
angular.module('activitiModeler').controller('ActivitiSignalDefinitionsPopupCtrl',
    ['$scope', '$q', '$translate', '$timeout', function ($scope, $q, $translate, $timeout) {

        // Put json representing signal definitions on scope
        if ($scope.property.value !== undefined && $scope.property.value !== null && $scope.property.value.length > 0) {

            if ($scope.property.value.constructor == String) {
                $scope.signalDefinitions = JSON.parse($scope.property.value);
            }
            else {
                // Note that we clone the json object rather then setting it directly,
                // this to cope with the fact that the user can click the cancel button and no changes should have happened
                $scope.signalDefinitions = angular.copy($scope.property.value);
            }

        } else {
            $scope.signalDefinitions = [];
        }

        // Array to contain selected signal definitions (yes - we only can select one, but ng-grid isn't smart enough)
        $scope.selectedSignals = [];
        $scope.translationsRetrieved = false;

        $scope.labels = {};

        var idPromise = $translate('PROPERTY.SIGNALDEFINITIONS.ID');
        var namePromise = $translate('PROPERTY.SIGNALDEFINITIONS.NAME');
        var scopePromise = $translate('PROPERTY.SIGNALDEFINITIONS.SCOPE');

        $q.all([idPromise, namePromise, scopePromise]).then(function (results) {

            $scope.labels.idLabel = results[0];
            $scope.labels.nameLabel = results[1];
            $scope.labels.scopeLabel = results[2];
            $scope.translationsRetrieved = true;

            // Config for grid
            $scope.gridOptions = {
                data: 'signalDefinitions',
                headerRowHeight: 28,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                keepLastSelected : false,
                selectedItems: $scope.selectedSignals,
                columnDefs: [
                    {field: 'id', displayName: $scope.labels.idLabel},
                    {field: 'name', displayName: $scope.labels.nameLabel},
                    {field: 'scope', displayName: $scope.labels.scopeLabel}]
            };
        });

        // Click handler for add button
        $scope.addNewSignalDefinition = function () {
            var newSignalDefinition = {id: '', name: '', scope: 'global'};

            $scope.signalDefinitions.push(newSignalDefinition);
            $timeout(function () {
            	$scope.gridOptions.selectItem($scope.signalDefinitions.length - 1, true);
            });
        };

        // Click handler for remove button
        $scope.removeSignalDefinition = function () {
            if ($scope.selectedSignals && $scope.selectedSignals.length > 0) {
            	var index = $scope.signalDefinitions.indexOf($scope.selectedSignals[0]);
                $scope.gridOptions.selectItem(index, false);
                $scope.signalDefinitions.splice(index, 1);

                $scope.selectedSignals.length = 0;
                if (index < $scope.signalDefinitions.length) {
                    $scope.gridOptions.selectItem(index + 1, true);
                } else if ($scope.signalDefinitions.length > 0) {
                    $scope.gridOptions.selectItem(index - 1, true);
                }
            }
        };

        // Click handler for save button
        $scope.save = function () {

            if ($scope.signalDefinitions.length > 0) {
                $scope.property.value = $scope.signalDefinitions;
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