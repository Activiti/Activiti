/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

angular.module('activitiModeler')
    .controller('DecisionTableEditorController', ['$rootScope', '$scope', '$q', '$translate', '$http', '$timeout', '$location', '$modal', '$route', '$routeParams', 'DecisionTableService',
        'UtilityService', 'FormBuilderService', 'uiGridConstants', 'appResourceRoot', 'ProcessScopeService', 'EditorScopeService',
        function ($rootScope, $scope, $q, $translate, $http, $timeout, $location, $modal, $route, $routeParams, DecisionTableService,
                  UtilityService, FormBuilderService, uiGridConstants, appResourceRoot, ProcessScopeService, EditorScopeService) {

            var MIN_COLUMN_WIDTH = 200;

            ProcessScopeService.init(null, {
                success: function(){
                    $scope.availableFormFieldsForStep = ProcessScopeService.getFormFields();
                    $scope.availableVariablesForStep = ProcessScopeService.getVariables();
                }
            });

            // Export name to grid's scope
            $scope.appResourceRoot = appResourceRoot;

            $scope.errorsById = undefined;

            if ($rootScope.currentDecisionTableValidationErrors) {
                $rootScope.currentDecisionTableValidationErrors = undefined;
            }

            // Listen for validation changes
            $rootScope.$watch('currentDecisionTableValidationErrors', function(newValue, oldValue){
                if (newValue === undefined && oldValue === undefined) {
                    return;
                }
                var errorsById = null;
                var error;
                if (newValue) {
                    errorsById = {};

                    for (var i = 0; i < newValue.length; i++) {
                        error = newValue[i];
                        var id = error.id !== null ? error.id : "";
                        if (!errorsById[id]) {
                            errorsById[id] = [];
                        }
                        errorsById[id].push(error);
                    }
                }
                $scope.errorsById = errorsById;
            });


            if ($rootScope.account && $rootScope.account.type && $rootScope.account.type !== 'enterprise') {
                $location.path('/processes');
            }

            // Model init
            $scope.status = {loading: true};
            $scope.model = {
                columnDefs: [],
                columnVariableIdMap: {}
            };

            $rootScope.decisionTableChanges = false;
            $rootScope.parentReferenceId = EditorScopeService.getCurrentProcessModelId();

            var hitPolicies = ['FIRST','ANY'];
            $scope.hitPolicies = [];
            hitPolicies.forEach(function(id){
                $scope.hitPolicies.push({
                    id: id,
                    label: 'DECISION-TABLE.HIT-POLICIES.' + id
                });
            });

            $rootScope.currentDecisionTableRules = [];

            $scope.availableVariableTypes = ['string', 'number', 'boolean', 'date'];

            var columnIdCounter = 0;

            $scope.$on('$locationChangeStart', function (event, next, current) {
                var handleResponseFunction = function (discard) {
                    $scope.unsavedDecisionTableChangesModalInstance = undefined;
                    if (discard) {
                        $rootScope.ignoreChanges = true;
                        $location.url(next.substring(next.indexOf('/#') + 2));
                    } else {
                        $rootScope.ignoreChanges = false;
                        $rootScope.setMainPageById('decision-tables');
                    }
                };
                $scope.confirmNavigation(handleResponseFunction, event);
            });

            $scope.confirmNavigation = function (handleResponseFunction, event) {
                if (!$rootScope.ignoreChanges && $rootScope.decisionTableChanges) {

                    if (event) {
                        // Always prevent location from changing. We'll use a popup to determine the action we want to take
                        event.preventDefault();
                    }

                    $scope.handleResponseFunction = handleResponseFunction;
                    $scope.unsavedDecisionTableChangesModalInstance = _internalCreateModal({
                        template: 'editor-app/popups/unsaved-changes.html',
                        scope: $scope
                    }, $modal, $scope);
                } else {
                    // Clear marker
                    $rootScope.ignoreChanges = false;
                }

            };

            $scope.editInputExpression = function (column) {

                if (!column) {
                    return;
                }

                $scope.model.selectedColumn = column;
                var editTemplate = 'views/popup/decision-table-edit-input-expression.html';

                // get expression for selected column
                $scope.currentDecisionTable.inputExpressions.forEach(function (inputExpression) {
                    if (inputExpression.id === column.name) {
                        $scope.model.selectedExpression = inputExpression;
                    }
                });

                _internalCreateModal({
                    template: editTemplate,
                    scope: $scope
                }, $modal, $scope);
            };

            $scope.editOutputExpression = function (column) {

                if (!column) {
                    return;
                }

                $scope.model.selectedColumn = column;
                var editTemplate = 'views/popup/decision-table-edit-output-expression.html';

                $scope.currentDecisionTable.outputExpressions.forEach(function(outputExpression) {
                    if (outputExpression.id === column.name) {
                        $scope.model.selectedExpression = outputExpression;
                    }
                });

                _internalCreateModal({
                    template: editTemplate,
                    scope: $scope
                }, $modal, $scope);
            };

            $scope.addInputExpression = function (inputExpression, insertPos) {

                if (!$scope.currentDecisionTable.inputExpressions) {
                    $scope.currentDecisionTable.inputExpressions = [];
                }

                var newInputExpression;
                if (!inputExpression) {
                    newInputExpression = {id: _generateColumnId()};
                } else {
                    newInputExpression = {
                        id: _generateColumnId(),
                        label: inputExpression.label,
                        variableId: inputExpression.variableId,
                        type: inputExpression.type,
                        variableType: inputExpression.variableType,
                        newVariable : inputExpression.newVariable
                    };
                }

                // if no rules present add one
                if ($rootScope.currentDecisionTableRules.length === 0) {
                    $scope.addRule();
                }

                // add props to rule data
                $rootScope.currentDecisionTableRules.forEach(function (rowObject) {
                    rowObject[newInputExpression.id] = "";
                });

                // insert expression at position or just add
                if (insertPos !== undefined && insertPos !== -1) {
                    $scope.currentDecisionTable.inputExpressions.splice(insertPos, 0, newInputExpression);
                } else {
                    $scope.currentDecisionTable.inputExpressions.push(newInputExpression);
                }

                $scope.model.columnVariableIdMap[newInputExpression.id] = newInputExpression.variableId;

                // update column definitions off the source model
                $scope.getColumnDefinitions($scope.currentDecisionTable);

                // refresh grid
                $scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.COLUMN);

                if ($scope.errorsById !== undefined) {
                    DecisionTableService.validateDecisionTable();
                }
            };

            $scope.enableRemoveInputExpression = function(){
                return $scope.currentDecisionTable && $scope.currentDecisionTable.inputExpressions && $scope.currentDecisionTable.inputExpressions.length > 1;
            };

            $scope.removeInputExpression = function (column, event) {

                if (!column) {
                    return;
                }

                // remove props from data
                $rootScope.currentDecisionTableRules.forEach(function (rowObject) {
                    if (rowObject.hasOwnProperty(column.name)) {
                        delete rowObject[column.name];
                    }
                });
                delete $scope.model.columnVariableIdMap[column.name];

                var expressionPos = -1;
                // remove input expression from table
                for (var i = 0; i < $scope.currentDecisionTable.inputExpressions.length; i++) {
                    if ($scope.currentDecisionTable.inputExpressions[i].id === column.name) {
                        // in case the input expression defined a variable delete it
                        for (var j = 0; j < $scope.currentDecisionTable.executionVariables.length; j++) {
                            if ($scope.currentDecisionTable.executionVariables[j].processVariableName === $scope.currentDecisionTable.inputExpressions[i].variableId
                                    && $scope.currentDecisionTable.executionVariables[j].processVariableType === $scope.currentDecisionTable.inputExpressions[i].type) {
                                $scope.currentDecisionTable.executionVariables.splice(j, 1);
                                break;
                            }
                        }
                        
                        $scope.currentDecisionTable.inputExpressions.splice(i, 1);
                        expressionPos = i;
                        break;
                    }
                    
                }

                // set updated column definitions
                $scope.getColumnDefinitions($scope.currentDecisionTable);

                if ($scope.errorsById !== undefined) {
                    DecisionTableService.validateDecisionTable();
                }

                // prevent edit modal opening
                if (event) {
                    event.stopPropagation();
                }

                return expressionPos;
            };

            $scope.updateInputExpression = function (oldInputExpressionColumn, newInputExpression) {
                var deletedColumnIndex = $scope.removeInputExpression(oldInputExpressionColumn);
                $scope.addInputExpression(newInputExpression, deletedColumnIndex);
            };

            $scope.addOutputExpression = function (outputExpression, insertPos) {

                if (!$scope.currentDecisionTable.outputExpressions) {
                    $scope.currentDecisionTable.outputExpressions = [];
                }

                var newOutputExpression;
                if (!outputExpression) {
                    newOutputExpression = {id: _generateColumnId()};
                } else {
                    newOutputExpression = {
                        id: _generateColumnId(),
                        label: outputExpression.label,
                        variableId: outputExpression.variableId,
                        type: outputExpression.type,
                        variableType: outputExpression.variableType,
                        newVariable: outputExpression.newVariable
                    };
                }

                // if no rules present add one
                if ($rootScope.currentDecisionTableRules.length === 0) {
                    $scope.addRule();
                }

                // add props to rule data
                $rootScope.currentDecisionTableRules.forEach(function (rowObject) {
                    rowObject[newOutputExpression.id] = "";
                });

                if (insertPos !== undefined && insertPos !== -1) {
                    $scope.currentDecisionTable.outputExpressions.splice(insertPos, 0, newOutputExpression);
                } else {
                    $scope.currentDecisionTable.outputExpressions.push(newOutputExpression);
                }
                $scope.model.columnVariableIdMap[newOutputExpression.id] = newOutputExpression.variableId;


                // update column definitions off the source model
                $scope.getColumnDefinitions($scope.currentDecisionTable);

                // refresh grid
                $scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.COLUMN);

                if ($scope.errorsById !== undefined) {
                    DecisionTableService.validateDecisionTable();
                }
            };

            $scope.enableRemoveOutputExpression = function(){
                return $scope.currentDecisionTable && $scope.currentDecisionTable.outputExpressions && $scope.currentDecisionTable.outputExpressions.length > 1;
            };

            $scope.removeOutputExpression = function (column, event) {

                if (!column) {
                    return;
                }

                // remove props from data
                $rootScope.currentDecisionTableRules.forEach(function (rowObject) {
                    if (rowObject.hasOwnProperty(column.name)) {
                        delete rowObject[column.name];
                    }
                });
                delete $scope.model.columnVariableIdMap[column.name];

                var expressionPos = -1;
                for (var i = 0; i < $scope.currentDecisionTable.outputExpressions.length; i++) {
                    if ($scope.currentDecisionTable.outputExpressions[i].id === column.name) {
                        $scope.currentDecisionTable.outputExpressions.splice(i, 1);
                        expressionPos = i;
                        break;
                    }
                }
                $scope.getColumnDefinitions($scope.currentDecisionTable);

                if ($scope.errorsById !== undefined) {
                    DecisionTableService.validateDecisionTable();
                }

                // prevent edit modal opening
                if (event) {
                    event.stopPropagation();
                }

                return expressionPos;
            };

            // create rule row with unique id
            $scope.addRule = function () {
                $rootScope.currentDecisionTableRules.push({});
            };

            $scope.enableRemoveRule = function () {
                return $scope.model.selectedRule && $rootScope.currentDecisionTableRules.length > 1;
            };

            $scope.removeRule = function () {
                if (!$scope.model.selectedRule) {
                    return;
                }

                var index =  $rootScope.currentDecisionTableRules.indexOf($scope.model.selectedRule);
                if (index > -1) {
                    $rootScope.currentDecisionTableRules.splice(index, 1);
                }

                $scope.model.selectedRule = undefined;
            };

            $scope.enableMoveUpwards = function (selectedRule) {
                return selectedRule && $rootScope.currentDecisionTableRules.indexOf(selectedRule) !== 0;
            };

            $scope.moveRuleUpwards = function () {
                var index =  $rootScope.currentDecisionTableRules.indexOf($scope.model.selectedRule);
                if (index > -1) {
                    var row = $rootScope.currentDecisionTableRules[index];
                    $rootScope.currentDecisionTableRules.splice(index, 1);
                    $rootScope.currentDecisionTableRules.splice(index - 1, 0, row);
                }
            };

            $scope.enableMoveDownwards = function (selectedRule) {
                return selectedRule && $rootScope.currentDecisionTableRules.indexOf(selectedRule) !== ($rootScope.currentDecisionTableRules.length - 1);
            };

            $scope.moveRuleDownwards = function () {
                var index =  $rootScope.currentDecisionTableRules.indexOf($scope.model.selectedRule);
                if (index > -1) {
                    var row = $rootScope.currentDecisionTableRules[index];
                    $rootScope.currentDecisionTableRules.splice(index, 1);
                    $rootScope.currentDecisionTableRules.splice(index + 1, 0, row);
                }
            };

            $scope.hasErrorMessages = function(rowIndex, columnField, allErrors){
                if (allErrors) {
                    var id = rowIndex !== null ? rowIndex + ':' + columnField : columnField;
                    var errors = allErrors[id];
                    return errors && errors.length > 0;
                }
                return false;
            };

            $scope.showErrorMessage = function(rowIndex, columnField, event){
                if ($scope.errorsById) {
                    var id = rowIndex !== null ? rowIndex + ':' + columnField : columnField;
                    var errors = $scope.errorsById[id];
                    for (var i = 0; i < errors.length; i++) {
                        errors[i].defaultDescription = $translate.instant('DECISION-TABLE.VALIDATION.' + errors[i].problem.toUpperCase(), {
                            reference: errors[i].problemReference ? errors[i].problemReference : ''
                        });
                    }

                    $scope.validationErrors = errors;

                    _internalCreateModal({
                        backdrop: true,
                        keyboard: true,
                        template: 'views/popup/decision-table-validation-errors.html?version=' + Date.now(),
                        scope: $scope
                    }, $modal, $scope);

                    if (event) {
                        event.stopPropagation();
                    }
                }
            };


            $scope.editRuleExpression = function (row, column, event) {

                var columnId = column.name;
                var columnType;
                var expression;
                var expressions = $scope.currentDecisionTable.inputExpressions.concat($scope.currentDecisionTable.outputExpressions);
                var i = 0;
                for (var il = expressions.length; i < il; i++){
                    expression = expressions[i];
                    if (expression.id === columnId) {
                        columnType = expression.type;
                        break;
                    }
                }
                $scope.model.currentRuleExpression = {
                    inputExpression: i < $scope.currentDecisionTable.inputExpressions.length,
                    row: row,
                    columnDisplayName: column.displayName || (expression ? expression.variableId : '') ,
                    columnId: columnId,
                    columnType: columnType
                };

                var editTemplate = 'views/popup/decision-table-edit-rule-expression.html';
                _internalCreateModal({
                    template: editTemplate,
                    scope: $scope
                }, $modal, $scope);

                // prevent default inline edit mode
                event.stopPropagation();
            };

            $scope.convertVariableType = function (variableType) {

                var gridType = 'string';

                if (!variableType || variableType !== '') {

                    switch (variableType) {

                        case "integer":
                            gridType = 'number';
                            break;

                        case "number":
                            gridType = 'number';
                            break;

                        case "date":
                            gridType = 'date';
                            break;

                        case "boolean":
                            gridType = 'boolean';
                            break;
                    }
                }
                return gridType;
            };

            $scope.convertFormfieldType = function (formFieldType) {

                var gridType = 'string';

                if (!formFieldType || formFieldType !== '') {

                    switch (formFieldType) {

                        case "number":
                            gridType = 'number';
                            break;

                        case "integer":
                            gridType = 'number';
                            break;

                        case "amount":
                            gridType = 'number';
                            break;

                        case "date":
                            gridType = 'date';
                            break;

                        case "boolean":
                            gridType = 'boolean';
                            break;
                    }
                }
                return gridType;
            };


            // helper for looking up variable id by col id
            $scope.getVariableNameByColumnId = function (colId) {

                if (!colId) {
                    return;
                }

                if ($scope.model.columnVariableIdMap[colId]) {
                    return $scope.model.columnVariableIdMap[colId];
                } else {
                    return $translate.instant('DECISION-TABLE-EDITOR.EMPTY-MESSAGES.NO-VARIABLE-SELECTED');
                }
            };


            var _loadDecisionTableDefinition = function (modelId) {

                DecisionTableService.fetchDecisionTableDetails(modelId).then( function (decisionTable) {

                    $rootScope.currentDecisionTable = decisionTable.decisionTableDefinition;
                    $rootScope.currentDecisionTable.id = decisionTable.id;
                    $rootScope.currentDecisionTable.key = decisionTable.decisionTableDefinition.key;
                    $rootScope.currentDecisionTable.name = decisionTable.name;
                    $rootScope.currentDecisionTable.description = decisionTable.description;
                    $rootScope.currentDecisionTable.referenceId = decisionTable.referenceId;
                    $rootScope.currentDecisionTable.isEmbeddedTable = decisionTable.isEmbeddedTable;
                    
                    // decision table model to used in save dialog
                    $rootScope.currentDecisionTableModel = {
                        id: decisionTable.id,
                        name: decisionTable.name,
                        key: decisionTable.decisionTableDefinition.key,
                        description: decisionTable.description
                    };

                    if (!$rootScope.currentDecisionTable.hitIndicator) {
                        $rootScope.currentDecisionTable.hitIndicator = hitPolicies[0];
                    }
                    _initializeDecisionTableGrid($rootScope.currentDecisionTable);

                    $timeout(function () {
                        // Flip switch in timeout to start watching all decision-related models
                        // after next digest cycle, to prevent first false-positive
                        $scope.status.loading = false;
                        $rootScope.decisionTableChanges = false;
                    }, 200);

                });
            };

            var _initializeDecisionTableGrid = function (decisionTable) {

                // initialize ui grid model
                if (decisionTable.rules && decisionTable.rules.length > 0) {
                    Array.prototype.push.apply($rootScope.currentDecisionTableRules, decisionTable.rules);
                }

                // if no input condition present; add one
                if (!decisionTable.inputExpressions || decisionTable.inputExpressions.length === 0) {
                    $scope.addInputExpression();
                } else {
                    // initialize map
                    decisionTable.inputExpressions.forEach( function (inputExpression) {
                        $scope.model.columnVariableIdMap[inputExpression.id] = inputExpression.variableId;

                        // set counter to max value
                        var expressionId = 0;
                        try {
                            expressionId = parseInt(inputExpression.id);
                        } catch(e){}
                        if (expressionId > columnIdCounter) {
                            columnIdCounter = expressionId;
                        }
                    });
                }

                // if no output conclusion present; add one
                if (!decisionTable.outputExpressions || decisionTable.outputExpressions.length === 0) {
                    $scope.addOutputExpression();
                } else {
                    // initialize map
                    decisionTable.outputExpressions.forEach( function (outputExpression) {
                        $scope.model.columnVariableIdMap[outputExpression.id] = outputExpression.variableId;

                        // set counter to max value
                        var expressionId = 0;
                        try {
                            expressionId = parseInt(outputExpression.id);
                        } catch(e){}
                        if (expressionId > columnIdCounter) {
                            columnIdCounter = expressionId;
                        }
                    });
                }

                // get column definitions
                $scope.getColumnDefinitions(decisionTable);
            };

            // Custom UI grid template
            var _getHeaderInputExpressionCellTemplate = function () {
                return "" +
                    "<div role=\"columnheader\" ng-class=\"{ 'sortable': sortable }\" ui-grid-one-bind-aria-labelledby-grid=\"col.uid + '-header-text ' + col.uid + '-sortdir-text'\">" +
                    "   <div role=\"button\" tabindex=\"0\" class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" ng-class=\"{ 'ui-grid-cell-contents-has-error': grid.appScope.hasErrorMessages(null, col.field, grid.appScope.errorsById) }\"col-index=\"renderIndex\" title=\"TOOLTIP\" ng-click=\"grid.appScope.editInputExpression(col)\">" +
                    "       <span tabindex=\"0\" class=\"show-error-icon\"  ng-click=\"grid.appScope.showErrorMessage(null, col.field, $event)\"><img ng-src=\"{{ appResourceRoot + 'editor-app/images/bpmn-error.png' }}\"/></span>" +
                    "       <div class=\"text-center\"><span>{{ col.displayName CUSTOM_FILTERS }}</span></div>" +
                    "       <div class=\"text-center\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\" style=\"text-decoration: underline;cursor:pointer\">[ {{ grid.appScope.getVariableNameByColumnId(col.name) }} ]</span></div>" +
                    "       <div tabindex=\"0\" ng-show=\"grid.appScope.enableRemoveInputExpression()\" class=\"ui-grid-column-menu-button\" style=\"margin-right: 10px\" ng-click=\"grid.appScope.removeInputExpression(col, $event)\"><i class=\"glyphicon glyphicon-trash\" style=\"font-size: 12px;\"></i></div>" +
                    "   </div>" +
                    "</div>";
            };

            var _getHeaderOutputExpressionCellTemplate = function () {
                return "" +
                    "<div role=\"columnheader\" ng-class=\"{ 'sortable': sortable }\" ui-grid-one-bind-aria-labelledby-grid=\"col.uid + '-header-text ' + col.uid + '-sortdir-text'\">" +
                    "   <div role=\"button\" tabindex=\"0\" class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" ng-class=\"{ 'ui-grid-cell-contents-has-error': grid.appScope.hasErrorMessages(null, col.field, grid.appScope.errorsById) }\" col-index=\"renderIndex\" title=\"TOOLTIP\" ng-click=\"grid.appScope.editOutputExpression(col)\">" +
                    "       <span tabindex=\"0\" class=\"show-error-icon\"  ng-click=\"grid.appScope.showErrorMessage(null, col.field, $event)\"><img ng-src=\"{{ appResourceRoot + 'editor-app/images/bpmn-error.png' }}\"/></span>" +
                    "       <div class=\"text-center\"><span>{{ col.displayName CUSTOM_FILTERS }}</span></div>" +
                    "       <div class=\"text-center\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\" style=\"text-decoration: underline;cursor:pointer\">[ {{ grid.appScope.getVariableNameByColumnId(col.name) }} ]</span></div>" +
                    "       <div tabindex=\"0\" ng-show=\"grid.appScope.enableRemoveOutputExpression()\" class=\"ui-grid-column-menu-button\" style=\"margin-right: 10px\" ng-click=\"grid.appScope.removeOutputExpression(col, $event)\"><i class=\"glyphicon glyphicon-trash\" style=\"font-size: 12px;\"></i></div>" +
                    "   </div>" +
                    "</div>";
            };

            var _rowHeaderTemplate = function () {
                return "<div class=\"ui-grid-disable-selection\"><div class=\"ui-grid-cell-contents text-center customRowHeader\">{{rowRenderIndex + 1}}</div></div>"
            };

            var _getCellTemplate = function (columnType) {

                var editableCellTemplate = "" +
                    "<div class=\"ui-grid-cell-contents\" ng-class=\"{ 'ui-grid-cell-contents-empty': !COL_FIELD, 'ui-grid-cell-contents-has-error': grid.appScope.hasErrorMessages(rowRenderIndex, col.field, grid.appScope.errorsById) }\" title=\"TOOLTIP\">" +
                    "   <span class=\"show-error-icon\" ng-click=\"grid.appScope.showErrorMessage(rowRenderIndex, col.field, $event)\"><img ng-src=\"{{ appResourceRoot + 'editor-app/images/bpmn-error.png' }}\" /></span>" +
                    "   <span class=\"contents-value\">{{COL_FIELD}}</span>" +
                    "   <span class=\"edit-icon\" ng-click=\"grid.appScope.editRuleExpression(row.entity, col, $event)\"><i class=\"glyphicon glyphicon-edit\" style=\"cursor:pointer;font-size: 14px;margin-right: 4px\"></i></span>" +
                    "</div>";
                var cellTemplate;
                switch (columnType) {

                    case "date":
                        cellTemplate = editableCellTemplate;
                        break;

                    case "number":
                        cellTemplate = editableCellTemplate;
                        break;

                    case "boolean":
                        cellTemplate = editableCellTemplate;
                        break;

                    case "string":
                        cellTemplate = editableCellTemplate;
                        break;

                    default:
                        cellTemplate = "<div class=\"ui-grid-cell-contents\" title=\"TOOLTIP\">{{COL_FIELD}}</div>";
                }

                return cellTemplate;

            };

            // create UI grid column definitions based on input / output expression
            $scope.getColumnDefinitions = function (decisionTable) {

                if (!decisionTable) {
                    return;
                }

                var expressionCounter = 0;
                var newColumnDefs = [];

                // input expression column defs
                if (decisionTable.inputExpressions && decisionTable.inputExpressions.length > 0) {

                    decisionTable.inputExpressions.forEach(function (inputExpression) {

                        newColumnDefs.push({
                            name: inputExpression.id,
                            displayName: inputExpression.label ? inputExpression.label : "",
                            field: inputExpression.id,
                            type: 'string',
                            headerCellClass: 'header-expression header-input-expression',
                            headerCellTemplate: _getHeaderInputExpressionCellTemplate(),
                            cellClass: 'cell-expression cell-input-expression',
                            cellTemplate: _getCellTemplate(inputExpression.type),
                            enableHiding: false,
                            enableCellEditOnFocus: false,
                            minWidth: MIN_COLUMN_WIDTH,
                            cellEditableCondition: function($scope){
                                // check if column has been mapped to variable
                                if ($scope.grid.appScope.model.columnVariableIdMap[inputExpression.id]) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });

                        expressionCounter++;
                    });
                }

                // output expression column defs
                if (decisionTable.outputExpressions && decisionTable.outputExpressions.length > 0) {

                    decisionTable.outputExpressions.forEach(function (outputExpression) {

                        newColumnDefs.push({
                            name: outputExpression.id,
                            displayName: outputExpression.label ? outputExpression.label : "",
                            field: outputExpression.id,
                            type: 'string',
                            headerCellTemplate: _getHeaderOutputExpressionCellTemplate(),
                            headerCellClass: 'header-expression header-output-expression',
                            cellClass: 'cell-expression cell-output-expression',
                            cellTemplate: _getCellTemplate(outputExpression.type),
                            enableHiding: false,
                            enableCellEditOnFocus: false,
                            minWidth: MIN_COLUMN_WIDTH,
                            cellEditableCondition: function($scope){
                                // check if column has been mapped to variable
                                if ($scope.grid.appScope.model.columnVariableIdMap[outputExpression.id]) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });

                        expressionCounter++;
                    });
                }

                // merge models
                $scope.model.columnDefs.length = 0;
                Array.prototype.push.apply($scope.model.columnDefs, newColumnDefs);

                $scope.gridApi.core.notifyDataChange( uiGridConstants.dataChange.ALL );
            };

            // config for grid
            $scope.gridOptions = {
                data: $rootScope.currentDecisionTableRules,
                enableRowHeaderSelection: true,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 2,
                enableColumnMenus: true,
                enableSorting: false,
                enableCellEditOnFocus: true,
                columnDefs: $scope.model.columnDefs,
                headerTemplate: 'views/templates/decision-table-header-template.html'
            };

            // register UI grid API
            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    if (row.isSelected) {
                        $scope.model.selectedRule = row.entity;
                    } else {
                        delete $scope.model.selectedRule;
                    }
                });
                gridApi.edit.on.afterCellEdit($scope, function(row, col, newValue, oldValue){
                    if ($scope.errorsById !== undefined && newValue !== oldValue) {
                        DecisionTableService.validateDecisionTable();
                    }
                });
                var cellTemplate = _rowHeaderTemplate();   // you could use your own template here
                $scope.gridApi.core.addRowHeaderColumn( { name: 'rowHeaderCol', displayName: '', width: 35, cellTemplate: cellTemplate} );
            };

            // fetch table from service and populate model
            _loadDecisionTableDefinition($routeParams.modelId);

            $scope.changeDetector = function(){
                $rootScope.decisionTableChanges = true;
            };
            $rootScope.$watch('currentDecisionTable', $scope.changeDetector, true);
            $rootScope.$watch('currentDecisionTableRules', $scope.changeDetector, true);

            var _generateColumnId = function () {
                columnIdCounter++;
                return "" + columnIdCounter;
            };

            $scope.getSelectedVariable = function(variableId){
                if ($scope.availableFormFieldsForStep && $scope.availableFormFieldsForStep.length > 0) {
                    for (var i = 0; i < $scope.availableFormFieldsForStep.length; i++) {
                        if (variableId === $scope.availableFormFieldsForStep[i].id) {
                            return [$scope.availableFormFieldsForStep[i], 'formfield'];
                        }
                    }
                }
                if ($scope.availableVariablesForStep && $scope.availableVariablesForStep.length > 0) {
                    for (var i = 0; i < $scope.availableVariablesForStep.length; i++) {
                        if (variableId === $scope.availableVariablesForStep[i].processVariableName) {
                            return [{
                                id: $scope.availableVariablesForStep[i].processVariableName,
                                name: $scope.availableVariablesForStep[i].processVariableName,
                                type: $scope.availableVariablesForStep[i].processVariableType
                            }, 'variable'];
                        }
                    }
                }
                return [{
                    id: variableId,
                    name: null,
                    type: null
                }, 'value'];
            };

            $scope.getSelectedVariableModel = function(variableId){
                var searchedVariable;

                $scope.availableFormFieldsForStep.forEach(function(currentVariable){
                    if(currentVariable.id === variableId ){
                        currentVariable.variableType = 'formfield';
                        searchedVariable = currentVariable;
                    }
                });

                if(!searchedVariable){
                    $scope.availableVariablesForStep.forEach(function(currentVariable){
                        if(currentVariable.id === variableId ){
                            searchedVariable = {
                                id: currentVariable.processVariableName,
                                name: currentVariable.processVariableName,
                                type: currentVariable.processVariableType,
                                variableType: 'variable'
                            };
                        }
                    });
                }

                if(!searchedVariable) {
                    $scope.currentDecisionTable.executionVariables.forEach(function(currentVariable){
                        if(currentVariable.processVariableName === variableId ){
                            searchedVariable = {
                                id: currentVariable.processVariableName,
                                name: currentVariable.processVariableName,
                                type: currentVariable.processVariableType,
                                variableType: 'variable'
                            };
                        }
                    });
                }

                return searchedVariable;
            };

            $scope.isEmbeddedTable = function () {
                return $rootScope.currentDecisionTable.isEmbeddedTable;
            };

            $scope.getVariablesAndTablesVariables = function () {
                var convertProcessVariables = convertVariableToFitInSelectBoxDirective($scope.availableVariablesForStep);
                var convertTableVariables = convertVariableToFitInSelectBoxDirective($scope.currentDecisionTable.executionVariables);
                return convertProcessVariables.concat(convertTableVariables).uniq();
            }

            function convertVariableToFitInSelectBoxDirective(variables) {
                var selectVariables = [];
                variables.forEach(function (currentVariable) {
                    var variable = {
                        id: currentVariable.processVariableName,
                        name: currentVariable.processVariableName,
                        type: currentVariable.processVariableType
                    };

                    selectVariables.push(variable);
                });

                return selectVariables ? selectVariables : [];
            }
        }]);

angular.module('activitiModeler')
    .controller('DecisionTableInputConditionEditorCtlr', ['$rootScope', '$scope', function ($rootScope, $scope) {
        var previousVariableId = $scope.model.selectedExpression.variableId,
            defaultVariableType = 'variable';

        // condition input options
        $scope.popup = {
            selectedExpressionNewVariable: (!$scope.model.selectedExpression.variableId && !$scope.isEmbeddedTable()) ? true : false,
            selectedExpressionVariableType: $scope.model.selectedExpression.variableType ? $scope.model.selectedExpression.variableType : defaultVariableType,
            selectedExpressionLabel: $scope.model.selectedExpression.label ? $scope.model.selectedExpression.label : '',
            selectedExpressionVariable: previousVariableId ? $scope.getSelectedVariableModel(previousVariableId) : undefined
        };

        $scope.tableVariables = $scope.getVariablesAndTablesVariables();

        $scope.save = function () {
            if (previousVariableId !== $scope.popup.selectedExpressionVariable.id) {

                if ($scope.popup.selectedExpressionVariableType === 'formfield') {
                    $scope.model.selectedExpression.type = $scope.convertFormfieldType($scope.popup.selectedExpressionVariable.type);
                } else if ($scope.popup.selectedExpressionVariableType === 'variable') {
                    $scope.model.selectedExpression.type = $scope.convertVariableType($scope.popup.selectedExpressionVariable.type);
                }

                var newInputExpression = {
                    label: $scope.popup.selectedExpressionLabel,
                    variableId: $scope.popup.selectedExpressionVariable.id,
                    type: $scope.model.selectedExpression.type,
                    variableType: $scope.popup.selectedExpressionVariableType,
                    newVariable: $scope.popup.selectedExpressionNewVariable
                };

                $scope.updateInputExpression($scope.model.selectedColumn, newInputExpression);

                if ($scope.popup.selectedExpressionNewVariable) {
                    saveNewDefinedVariable();
                }

            } else {
                $scope.model.selectedColumn.displayName = $scope.popup.selectedExpressionLabel;
            }

            $scope.close();
        };

        function saveNewDefinedVariable() {
            var newVariable = {
                processVariableName: $scope.popup.selectedExpressionVariable.id,
                processVariableType: $scope.model.selectedExpression.type
            };

            if ($scope.currentDecisionTable.executionVariables.indexOf(newVariable)) {
                $scope.currentDecisionTable.executionVariables.push(newVariable);
            }
        }

        $scope.setExpressionVariableType = function (variableType) {
            $scope.popup.selectedExpressionVariable = null;
            $scope.popup.selectedExpressionVariableType = variableType;
        };

        $scope.setNewVariable = function (value) {
            $scope.popup.selectedExpressionNewVariable = value;
            if (value) {
                $scope.setExpressionVariableType('variable');
            }
        };

        $scope.close = function () {
            $scope.$hide();
        };

        // Cancel button handler
        $scope.cancel = function () {
            $scope.close();
        };
    }]);

angular.module('activitiModeler')
    .controller('DecisionTableConclusionEditorCtlr', ['$rootScope', '$scope', '$q', '$translate', function ($rootScope, $scope, $q, $translate) {

        // condition input options
        $scope.popup = {
            selectedExpressionVariableType: $scope.model.selectedExpression.variableType || '',
            selectedExpressionLabel: $scope.model.selectedExpression.label || '',
            selectedExpressionNewVariable: $scope.model.selectedExpression.newVariable === true,
            selectedExpressionNewVariableType: $scope.availableVariableTypes[0]
        };

        if ($scope.popup.selectedExpressionNewVariable) {
            $scope.popup.selectedExpressionNewVariableId = $scope.model.selectedExpression.variableId;
            $scope.popup.selectedExpressionNewVariableType = $scope.model.selectedExpression.type;
        }
        else {
            var expressionAndType = $scope.getSelectedVariable($scope.model.selectedExpression.variableId);
            if (expressionAndType[1] !== 'value') {
                $scope.popup.selectedExpressionVariable = expressionAndType[0];
                $scope.popup.selectedExpressionVariableType = expressionAndType[1];
            }
            else {
                // The variable or form field doesn't exist anymore,
                $scope.popup.selectedExpressionVariable = null;
                $scope.popup.selectedExpressionVariableType = null;
            }
        }

        // make copy of variable id and type to see if full update is needed
        var variableIdCopy = angular.copy($scope.model.selectedExpression.variableId);
        var newVariableIdCopy = angular.copy($scope.model.selectedExpressionNewVariableId);
        var newVariableTypeCopy = angular.copy($scope.model.selectedExpressionNewVariableType);

        // Cancel button handler
        $scope.cancel = function() {
            $scope.close();
        };

        // Saving the edited input
        $scope.save = function() {
            if ($scope.popup.selectedExpressionNewVariable) {
                $scope.model.selectedExpression.variableId = $scope.popup.selectedExpressionNewVariableId;
                $scope.model.selectedExpression.type = $scope.popup.selectedExpressionNewVariableType;
                $scope.model.selectedExpression.variableType = 'variable';
            }
            else if ($scope.popup.selectedExpressionVariableType === 'formfield' || !$scope.popup.selectedExpressionVariableType) {
                $scope.model.selectedExpression.variableId = $scope.popup.selectedExpressionVariable.id;
                $scope.model.selectedExpression.type = $scope.convertFormfieldType($scope.popup.selectedExpressionVariable.type || 'formfield');
                $scope.model.selectedExpression.variableType = 'formfield';
            } else if ($scope.popup.selectedExpressionVariableType === 'variable') {
                $scope.model.selectedExpression.variableId = $scope.popup.selectedExpressionVariable.id;
                $scope.model.selectedExpression.type = $scope.convertVariableType($scope.popup.selectedExpressionVariable.type);
                $scope.model.selectedExpression.variableType = 'variable';
            }
            $scope.model.selectedExpression.newVariable = $scope.popup.selectedExpressionNewVariable;
            $scope.model.selectedExpression.label = $scope.popup.selectedExpressionLabel;

            if (variableIdCopy !== $scope.model.selectedExpression.variableId || newVariableIdCopy !== $scope.model.selectedExpressionNewVariableId || newVariableTypeCopy !== $scope.model.selectedExpressionNewVariableType) {

                // remove current column
                var deletedColumnIndex = $scope.removeOutputExpression($scope.model.selectedColumn);

                // add new conclusion
                $scope.addOutputExpression(
                    {
                        label: $scope.model.selectedExpression.label,
                        variableId: $scope.model.selectedExpression.variableId,
                        type: $scope.model.selectedExpression.type,
                        variableType: $scope.model.selectedExpression.variableType,
                        newVariable: $scope.model.selectedExpression.newVariable
                    },
                    deletedColumnIndex
                );
            } else {
                $scope.model.selectedColumn.displayName = $scope.model.selectedExpression.label;
            }

            $scope.close();
        };

        $scope.close = function () {
            $scope.$hide();
        };

    }]);

angular.module('activitiModeler')
    .controller('DecisionTableRuleExpressionEditorCtlr', ['$rootScope', '$scope', '$q', '$translate', '$filter', 'DecisionTableService', function ($rootScope, $scope, $q, $translate, $filter, DecisionTableService) {

        var columnType = $scope.model.currentRuleExpression.columnType;
        var inputExpression = $scope.model.currentRuleExpression.inputExpression;

        var MVEL_DATE_FORMAT = "yyyy-MM-dd";
        var MOMENT_DATE_FORMAT = "YYYY-MM-DD";

        $scope.invalidExpressionMessage = false;

        var operatorsByType = {
            'date': ['==', '!=', '>', '<', '<=', '>=', '__isEmpty__', '__isNotEmpty__'],
            'number': ['==', '!=', '>', '<', '<=', '>=', '__isEmpty__', '__isNotEmpty__'],
            'boolean': ['==', '!=', '__isEmpty__', '__isNotEmpty__'],
            'string': ['==', '!=', '.startsWith', '.endsWith', '.contains', '__isEmpty__', '__isNotEmpty__']
        };

        $scope.formFieldsByType = {
            'date': ['date'],
            'number': ['integer', 'amount'],
            'boolean': ['boolean'],
            'string': ['text', 'multi-line-text', 'dropdown', 'radio-buttons', 'typeahead']
        };

        $scope.variablesByType = {
            'date': ['date'],
            'number': ['integer', 'number'],
            'boolean': ['boolean'],
            'string': ['string']
        };

        $scope.additionalValueTypes = {
            'date': ['date_execution'],
            'number': [],
            'boolean': [],
            'string': []
        };

        $scope.availableOperatorsByType = {};

        $scope.tableVariables = $scope.getVariablesAndTablesVariables();

        for (var type in operatorsByType) {
            var operators = operatorsByType[type];
            var operatorDescriptors = [];
            operators.forEach(function(operator){
                operatorDescriptors.push({ id: operator, label: 'DECISION-TABLE-EDITOR.OPERATOR.' + type.toUpperCase() + '.' + operator});
            });
            $scope.availableOperatorsByType[type] = operatorDescriptors;
        }
        $scope.getOperators = function(columnType) {
            return $scope.availableOperatorsByType[columnType];
        };

        $scope.availableDateCalculations = [
            {key: '', label: 'DECISION-TABLE-EDITOR.CALCULATION.DATE.NONE'},
            {key: 'fn_addDate', label: 'DECISION-TABLE-EDITOR.CALCULATION.DATE.ADD'},
            {key: 'fn_subtractDate', label: 'DECISION-TABLE-EDITOR.CALCULATION.DATE.SUBTRACT'}
        ];

        $scope.availableNumberCalculations = [
            {key: '',  label: 'DECISION-TABLE-EDITOR.CALCULATION.NUMBER.NONE'},
            {key: '+', label: 'DECISION-TABLE-EDITOR.CALCULATION.NUMBER.ADD'},
            {key: '-', label: 'DECISION-TABLE-EDITOR.CALCULATION.NUMBER.SUBTRACT'},
            {key: '/', label: 'DECISION-TABLE-EDITOR.CALCULATION.NUMBER.DIVIDE'},
            {key: '*', label: 'DECISION-TABLE-EDITOR.CALCULATION.NUMBER.MULTIPLY'}
        ];

        $scope.availableBooleanValues = [
            {id: 'true', label: 'true'},
            {id: 'false', label: 'false'}
        ];

        var mapEmptyOperatorKeyword = {
            '__isEmpty__': '== empty',
            '__isNotEmpty__': '!= empty'
        };

        // initialization
        $scope.popup = {
            value: { type: 'value' },
            variable: { reference: null },
            formfield: { reference: null },
            selectedExpressionVariableType: 'value',
            columnType: $scope.model.currentRuleExpression.columnType,
            inputExpression: inputExpression,
            selectedOperator: null,
            date: { type: 'fixed', calculation: { method: null } },
            number: { type: 'fixed', static: null, calculation: { method: null } },
            boolean: { type:'fixed', static: null},
            string: { type:'fixed', static: ''}
        };

        var _generateDisplayValueCalculationDate = function (calculationDate, dateValue) {

            if (!calculationDate.method || !calculationDate.method.key) {
                return dateValue;
            }

            var displayValue = calculationDate.method.key;
            displayValue += '(';
            displayValue += dateValue;

            displayValue += ',';
            if (calculationDate.years) {
                displayValue += calculationDate.years;
            }
            else {
                displayValue += 0;
            }

            displayValue += ',';
            if (calculationDate.months) {
                displayValue += calculationDate.months;
            }
            else {
                displayValue += 0;
            }

            displayValue += ',';
            if (calculationDate.days) {
                displayValue += calculationDate.days;
            }
            else {
                displayValue += 0;
            }

            displayValue += ')';
            return displayValue;
        };

        var _generateDisplayValueCalculationNumber = function (calculation, value) {

            if (!calculation.method || !calculation.method.key) {
                return value;
            }

            var displayValue = '';
            displayValue += value;
            displayValue += ' ' + calculation.method.key + ' ';

            if (calculation.param1) {
                displayValue += calculation.param1;
            }
            else {
                displayValue += 0;
            }
            return displayValue;
        };

        var indexDateCalculationParamMap = {
            '1': 'years',
            '2': 'months',
            '3': 'days'
        };

        var setDateValue = function(str) {
            var res = str.match(/^([a-zA-Z0-9_]+)\((.*)\)$/);
            if (res && res.length >= 2) {
                var dateFn = res[1];
                if (dateFn === 'fn_date' && res.length === 3 && res[2]) {
                    $scope.popup.value.type = 'value';
                    var d = res[2].replace(/"/g, "");
                    $scope.popup.date.static = moment(d, MOMENT_DATE_FORMAT).toDate();
                }
                else if (dateFn === 'fn_now') {
                    $scope.popup.value.type = 'date_execution';
                }
            }
            else {
                var expressionAndType = $scope.getSelectedVariable(str);
                $scope.popup.value.type = expressionAndType[1];
                if (expressionAndType[1] !== 'value') {
                    $scope.popup[expressionAndType[1]].reference = expressionAndType[0];
                }
            }
        };

        var setDateRollParams = function(calculation, dateParams){
            if (dateParams) {
                var params = dateParams.split(',');
                for (var i = 0; i < params.length && i < 4; i++) {
                    if (i === 0) {
                        setDateValue(params[i].trim())
                    }
                    else {
                        try {
                            calculation[indexDateCalculationParamMap[i]] = parseInt(params[i].trim());
                        }
                        catch(e){}
                    }

                }
            }
        };

        var setNumberValue = function(str) {
            var numberValue = '';
            try {
                numberValue = parseFloat(str);
            }
            catch(e) {
            }
            if (!isNaN(numberValue)) {
                $scope.popup.value.type = 'value';
                $scope.popup.date.type = 'fixed';
                $scope.popup.number.static = numberValue;
            }
            else {
                var expressionAndType = $scope.getSelectedVariable(str);
                if (expressionAndType[1] === 'value') {
                    $scope.invalidExpressionMessage = true;
                    $scope.popup.value.type = null;
                }
                else {
                    $scope.popup.value.type = expressionAndType[1];
                    $scope.popup[expressionAndType[1]].reference = expressionAndType[0];
                }
            }
        };

        var _initValue = function (displayValue, columnType) {

            var expressionAndType;

            displayValue = displayValue.trim();

            if (!displayValue) {
                if (inputExpression && $scope.availableOperatorsByType[columnType] && $scope.availableOperatorsByType[columnType].length) {
                    $scope.selectedOperator = $scope.availableOperatorsByType[columnType][0].id;
                }
            }
            else if (columnType === 'date') {
                var res = displayValue.match(/^([a-zA-Z0-9_]+)\((.*)\)$/);
                if (res && res.length >= 2) {
                    var dateFn = res[1];
                    if ($scope.availableDateCalculations.filter(function(c){ return c.key === dateFn }).length > 0) {
                        $scope.popup.date.calculation = {
                            method: { key: dateFn }
                        };
                        if (res.length > 2) {
                            setDateRollParams($scope.popup.date.calculation, res[2]);
                        }
                        return;
                    }
                }
                if (!$scope.popup.date.calculation.method) {
                    $scope.popup.date.calculation.method = { key: '' };
                }
                setDateValue(displayValue);
            }
            else if (columnType === 'number') {
                var res = displayValue.match(/^([a-zA-Z0-9_]+)\s*([^a-z^A-Z^0-9^_^\s]+)\s*([0-9\.]+)$/);
                if (res && res.length === 4) {
                    var numberFn = res[2];
                    if ($scope.availableNumberCalculations.filter(function(c){ return c.key === numberFn }).length > 0) {
                        var param1 = '';
                        $scope.popup.number.calculation = {
                            method: { key: numberFn }
                        };
                        try {
                            param1 = parseFloat(res[3]);
                            $scope.popup.number.calculation.param1 = param1;
                        }
                        catch(e){
                            $scope.invalidExpressionMessage = true;
                        }
                        setNumberValue(res[1]);
                        return;
                    }
                }
                if (!$scope.popup.number.calculation.method) {
                    $scope.popup.number.calculation.method = { key: '' };
                }
                setNumberValue(displayValue);
            }
            else if (columnType === 'boolean') {
                if (displayValue.toLowerCase() === 'true' || displayValue.toLowerCase() === 'false') {
                    $scope.popup.value.type = 'value';
                    $scope.popup.boolean.type = 'fixed';
                    $scope.popup.boolean.static = displayValue;
                }
                else {
                    expressionAndType = $scope.getSelectedVariable(displayValue);
                    $scope.popup.value.type = expressionAndType[1];
                    if (expressionAndType[1] !== 'value') {
                        $scope.popup[expressionAndType[1]].reference = expressionAndType[0];
                    }
                }
            }
            else if (columnType === 'string') {
                var res = displayValue.match(/^'(.*)'$/);
                if (res) {
                    displayValue = res.length > 1 ? res[1] : '';
                }
                else {
                    res = displayValue.match(/^"(.*)"$/);
                    if (res) {
                        displayValue = res.length > 1 ? res[1] : '';
                    }
                }
                if (res) {
                    $scope.popup.value.type = 'value';
                    $scope.popup.string.type = 'fixed';
                    $scope.popup.string.static = displayValue;
                }
                else {
                    expressionAndType = $scope.getSelectedVariable(displayValue);
                    $scope.popup.value.type = expressionAndType[1];
                    if (expressionAndType[1] !== 'value') {
                        $scope.popup[expressionAndType[1]].reference = expressionAndType[0];
                    }
                }
            }
        };

        // parse the display value
        var parseExpression = function() {
            var displayValue = $scope.model.currentRuleExpression.row[$scope.model.currentRuleExpression.columnId] || '';
            displayValue = displayValue.trim();
            if (displayValue !== '') {
                if (inputExpression) {
                    // input expression
                    if (displayValue.indexOf("==") === 0 ||
                        displayValue.indexOf("!=") === 0 ||
                        displayValue.indexOf(">=") === 0 ||
                        displayValue.indexOf("<=") === 0) {
                        $scope.popup.selectedOperator = displayValue.substring(0, 2);
                        displayValue = displayValue.substring(2);
                        // check if we are comparing against the empty keyword
                        if ($scope.popup.selectedOperator.indexOf("==") === 0 && displayValue.trim().indexOf('empty') === 0) {
                            $scope.popup.selectedOperator = '__isEmpty__';
                        }
                        else if ($scope.popup.selectedOperator.indexOf("!=") === 0 && displayValue.trim().indexOf('empty') === 0) {
                            $scope.popup.selectedOperator = '__isNotEmpty__';
                        }
                        else {
                            _initValue(displayValue, columnType);
                        }
                    } else if (displayValue.indexOf("<") === 0 || displayValue.indexOf(">") === 0) {
                        $scope.popup.selectedOperator = displayValue.substring(0, 1);
                        displayValue = displayValue.substring(1);
                        _initValue(displayValue, columnType);
                    }
                    else {
                        var res = displayValue.match(/^(\.[a-zA-Z0-9_]+)\(([^\\]*)\)$/);
                        if (res && res.length > 1) {
                            // it was an instance method call .method()
                            $scope.popup.selectedOperator = res[1];
                            if ($scope.popup.selectedOperator && operatorsByType[columnType].indexOf($scope.popup.selectedOperator) === -1) {
                                $scope.invalidExpressionMessage = true;
                            }
                            else {
                                _initValue(res.length > 2 ? res[2] : '', columnType);
                            }
                        }
                        else {
                            $scope.invalidExpressionMessage = true;
                        }
                    }
                }
                else {
                    // output expression
                    _initValue(displayValue, columnType);
                }
            } else if (inputExpression && $scope.availableOperatorsByType[columnType] && $scope.availableOperatorsByType[columnType].length ) {
                $scope.popup.selectedOperator = $scope.availableOperatorsByType[columnType][0].id;
            }
        };
        parseExpression();

        // Cancel button handler
        $scope.cancel = function() {
            $scope.close();
        };

        var surroundValue = function(value, operator){
            if (operator && operator.length > 1 && operator[0] === '.') {
                return '(' + (value || '') + ')';
            }
            else if (operator && operator.length) {
                return ' ' + (value || '');
            }
            return value;
        };

        var saveDateExpression = function() {
            var displayValue = '';
            var operator = $scope.popup.selectedOperator;

            if (operator && mapEmptyOperatorKeyword[operator]) {
                displayValue = mapEmptyOperatorKeyword[operator];
            }
            else {
                if (inputExpression) {
                    displayValue += operator;
                }

                var dateValue;
                if ($scope.popup.value.type === 'value') {
                    if ($scope.popup.date.static) {
                        dateValue = 'fn_date("' + $filter('date')($scope.popup.date.static, MVEL_DATE_FORMAT) + '")';
                    }
                    else {
                        dateValue = '';
                    }
                }
                else if ($scope.popup.value.type === 'date_execution') {
                    dateValue = 'fn_now()';
                }
                else if ($scope.popup[$scope.popup.value.type].reference) {
                    dateValue = $scope.popup[$scope.popup.value.type].reference.id + '';
                }
                displayValue += surroundValue(_generateDisplayValueCalculationDate($scope.popup.date.calculation, dateValue), operator);
            }

            $scope.model.currentRuleExpression.row[$scope.model.currentRuleExpression.columnId] = displayValue;
        };

        var saveNumberExpression = function() {
            var displayValue = '';
            var operator = $scope.popup.selectedOperator;

            if (operator && mapEmptyOperatorKeyword[operator]) {
                displayValue = mapEmptyOperatorKeyword[operator];
            }
            else {
                if (inputExpression) {
                    displayValue += operator;
                }

                var dateValue;
                if ($scope.popup.value.type === 'value') {
                    try {
                        dateValue = parseFloat($scope.popup.number.static);
                    }
                    catch (e) {
                    }
                }
                else if ($scope.popup[$scope.popup.value.type].reference) {
                    dateValue = $scope.popup[$scope.popup.value.type].reference.id + '';
                }
                displayValue += surroundValue(_generateDisplayValueCalculationNumber($scope.popup.number.calculation, dateValue), operator);
            }

            $scope.model.currentRuleExpression.row[$scope.model.currentRuleExpression.columnId] = displayValue;
        };

        var saveBooleanExpression = function() {
            var displayValue = '';
            var operator = $scope.popup.selectedOperator;

            if (operator && mapEmptyOperatorKeyword[operator]) {
                displayValue = mapEmptyOperatorKeyword[operator];
            }
            else {
                if (inputExpression) {
                    displayValue = operator;
                }
                if ($scope.popup.value.type === 'value') {
                    displayValue += surroundValue($scope.popup.boolean.static + '', operator);
                }
                else if ($scope.popup[$scope.popup.value.type].reference) {
                    displayValue += surroundValue($scope.popup[$scope.popup.value.type].reference.id + '', operator);
                }
            }
            $scope.model.currentRuleExpression.row[$scope.model.currentRuleExpression.columnId] = displayValue;

        };

        var saveStringExpression = function() {
            var displayValue = '';
            var operator = $scope.popup.selectedOperator;
            if (operator && mapEmptyOperatorKeyword[operator]) {
                displayValue = mapEmptyOperatorKeyword[operator];
            }
            else {
                if (inputExpression) {
                    displayValue = operator;
                }

                if ($scope.popup.value.type === 'value') {
                    displayValue += surroundValue('"' + $scope.popup.string.static + '"' , operator);
                }
                else if ($scope.popup[$scope.popup.value.type].reference) {
                    displayValue += surroundValue($scope.popup[$scope.popup.value.type].reference.id + '', operator);
                }
            }
            $scope.model.currentRuleExpression.row[$scope.model.currentRuleExpression.columnId] = displayValue;
        };


        // Saving the edited input
        $scope.save = function() {
            if (inputExpression && !$scope.popup.selectedOperator) {
                $scope.model.currentRuleExpression.row[$scope.model.currentRuleExpression.columnId] = '';
            }
            else {
                var columnType = $scope.model.currentRuleExpression.columnType;
                if (columnType === 'date') {
                    saveDateExpression();
                } else if (columnType === 'number') {
                    saveNumberExpression();
                } else if (columnType === 'boolean') {
                    saveBooleanExpression();
                } else if (columnType === 'string') {
                    saveStringExpression();
                }
            }
            $scope.close();
            if ($scope.errorsById !== undefined) {
                DecisionTableService.validateDecisionTable();
            }
        };

        $scope.close = function () {
            $scope.$hide();
        };


        $scope.closeDatePopup = function() {
            jQuery("#date").blur();
        };

        $scope.clearDate = function() {
            $scope.popup.date.static = undefined;
            jQuery("#date").blur();
        };

        $scope.selectToday = function(field, callback) {
            var today = new Date();
            today = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0);
            field = today;
            if (callback) {
                callback(field);
            }
            jQuery("#date").blur();
        };

    }]);


angular.module('activitiModeler')
    .controller('ValidateDecisionTableModelCtrl', ['$rootScope', '$scope', '$translate', '$timeout', function ($rootScope, $scope, $translate, $timeout) {

        // Config for grid
        $scope.gridOptions = {
            data: $scope.validationErrors,
            headerRowHeight: 28,
            enableHorizontalScrollbar: 0,
            enableColumnMenus: false,
            enableSorting: false,
            columnDefs: [
                {field: 'defaultDescription', displayName: $translate.instant('MODEL.VALIDATION.ERRORS.DESCRIPTION')},
                {field: 'warning', displayName: $translate.instant('MODEL.VALIDATION.ERRORS.WARNING'), width: 100},
                {field: 'validatorSetName', displayName: $translate.instant('MODEL.VALIDATION.ERRORS.SET'), width: 200}
            ]
        };

        $scope.ok = function () {
            $scope.$hide();
        };

        // Close button handler
        $scope.close = function () {
            $scope.$hide();
        };
    }
    ]);
