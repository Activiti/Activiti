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
 * Due date
 */
'use strict';

angular.module('activitiModeler').controller('BpmnEditorDueDateCtrl', [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        template: 'editor-app/configuration/properties/duedate-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    _internalCreateModal(opts, $modal, $scope);
}]);

angular.module('activitiModeler').controller('BpmnEditorDueDatePopupCtrl',
    [ '$rootScope', '$scope', '$translate', function($rootScope, $scope, $translate) {

    // Put json representing assignment on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null) {
    
        if ($scope.property.value.duedate !== undefined && $scope.property.value.duedate !== null) {
            $scope.popup = {'duedate': $scope.property.value.duedate};
        
        } else if ($scope.property.value.duedateExpression !== undefined && $scope.property.value.duedateExpression !== null) {
            $scope.popup = {'duedateExpression': $scope.property.value.duedateExpression};
            
        } else {
            $scope.popup = {'duedateExpression': $scope.property.value};
        }
    
    } else {
        $scope.popup = {};
    }
    
    $scope.taskDueDateOptions = [
        {id: "none", title: $translate.instant('PROPERTY.DUEDATE.TASK-DUE-DATE-OPTIONS.NO-DUEDATE')},
        {id: "expression", title: $translate.instant('PROPERTY.DUEDATE.TASK-DUE-DATE-OPTIONS.EXPRESSION')},
        {id: "static", title: $translate.instant('PROPERTY.DUEDATE.TASK-DUE-DATE-OPTIONS.STATIC')},
        {id: "field", title: $translate.instant('PROPERTY.DUEDATE.TASK-DUE-DATE-OPTIONS.FIELD')}
    ];
    
    if (!$scope.popup.duedate && !$scope.popup.duedateExpression) {
        // Default, first time opening the popup
        $scope.popup.selectedDueDateOption = $scope.taskDueDateOptions[0].id;
        
    } else if (!$scope.popup.duedate) {
        $scope.popup.selectedDueDateOption = $scope.taskDueDateOptions[1].id;
        
    } else {
        
        if ($scope.popup.duedate.fixed) {
            $scope.popup.selectedDueDateOption = $scope.taskDueDateOptions[2].id;
            
        } else if ($scope.popup.duedate.field) {
            $scope.popup.selectedDueDateOption = $scope.taskDueDateOptions[3].id;
        
        } else {
            $scope.popup.selectedDueDateOption = $scope.taskDueDateOptions[0].id;
        }
    }
    
    $scope.dueDateOptionChanged = function() {
        if ($scope.popup.selectedDueDateOption === 'expression') {
            $scope.popup.duedate = undefined;
        
        } else if ($scope.popup.selectedDueDateOption === 'none') {
            $scope.popup.duedate = undefined;
            $scope.popup.duedateExpression = undefined;
        
        } else if ($scope.popup.selectedDueDateOption === 'static') {
            $scope.popup.duedate = {'fixed': {}};
            $scope.popup.duedateExpression = undefined;
        
        } else if ($scope.popup.selectedDueDateOption === 'field') {
            $scope.popup.duedate = {'field': {}};
            $scope.popup.duedateExpression = undefined;
        }
    };
    
    $scope.setAddCalculationType = function() {
        $scope.popup.duedate.field.taskDueDateCalculationType = 'add';
    };
    
    $scope.setSubtractCalculationType = function() {
        $scope.popup.duedate.field.taskDueDateCalculationType = 'subtract';
    };

    $scope.allSteps = EDITOR.UTIL.collectSortedElementsFromPrecedingElements($scope.selectedShape);

    $scope.save = function () {
        $scope.property.value = {};
        if ($scope.popup.duedate) {
            $scope.property.value.duedate = $scope.popup.duedate;
            
        } else if ($scope.popup.duedateExpression) {
            $scope.property.value.duedateExpression = $scope.popup.duedateExpression;
        
        } else {
            $scope.property.value = '';
        }
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };
}]);
