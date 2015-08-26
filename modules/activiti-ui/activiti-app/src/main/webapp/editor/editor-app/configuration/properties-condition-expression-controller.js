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
 * Condition expression
 */

angular.module('activitiModeler').controller('KisBpmConditionExpressionCtrl', [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        template: 'editor-app/configuration/properties/condition-expression-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    _internalCreateModal(opts, $modal, $scope);
}]);

angular.module('activitiModeler').controller('KisBpmConditionExpressionPopupCtrl',
    [ '$rootScope', '$scope', '$translate', 'FormBuilderService', function($rootScope, $scope, $translate, FormBuilderService) {
    	
    // Put json representing assignment on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.expression !== undefined
        && $scope.property.value.expression !== null) {

        $scope.expression = $scope.property.value.expression;

    } else if ($scope.property.value !== undefined && $scope.property.value !== null) {
        $scope.expression = {type: 'static', staticValue: $scope.property.value};
        
    } else {
        $scope.expression = {};
    }

    $scope.save = function() {
        $scope.property.value = {expression: $scope.expression};
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };

}]);