/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

angular.module('activitiModeler')
    .controller('DecisionTableEditorController', ['$rootScope', '$scope', '$q', '$translate', '$http', '$timeout', '$location', '$modal', '$route', '$routeParams', 'DecisionTableService',
        'UtilityService', 'uiGridConstants', 'appResourceRoot',
        function ($rootScope, $scope, $q, $translate, $http, $timeout, $location, $modal, $route, $routeParams, DecisionTableService,
                  UtilityService, uiGridConstants, appResourceRoot) {

            var MIN_COLUMN_WIDTH = 200;

            // Export name to grid's scope
            $scope.appResourceRoot = appResourceRoot;

            // Model init
            $scope.status = {loading: true};
            $scope.model = {
                columnDefs: [],
                columnVariableIdMap: {}
            };

            $rootScope.decisionTableChanges = false;
            
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
                        $scope.currentDecisionTable.inputExpressions.splice(i, 1);
                        expressionPos = i;
                        break;
                    }
                    
                }

                // set updated column definitions
                $scope.getColumnDefinitions($scope.currentDecisionTable);

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
                    "   <div role=\"button\" tabindex=\"0\" class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" col-index=\"renderIndex\" title=\"TOOLTIP\" ng-click=\"grid.appScope.editInputExpression(col)\">" +
                    "       <div class=\"text-center\"><span>{{ col.displayName CUSTOM_FILTERS }}</span></div>" +
                    "       <div class=\"text-center\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\" style=\"text-decoration: underline;cursor:pointer\">[ {{ grid.appScope.getVariableNameByColumnId(col.name) }} ]</span></div>" +
                    "       <div tabindex=\"0\" ng-show=\"grid.appScope.enableRemoveInputExpression()\" class=\"ui-grid-column-menu-button\" style=\"margin-right: 10px\" ng-click=\"grid.appScope.removeInputExpression(col, $event)\"><i class=\"glyphicon glyphicon-trash\" style=\"font-size: 12px;\"></i></div>" +
                    "   </div>" +
                    "</div>";
            };

            var _getHeaderOutputExpressionCellTemplate = function () {
                return "" +
                    "<div role=\"columnheader\" ng-class=\"{ 'sortable': sortable }\" ui-grid-one-bind-aria-labelledby-grid=\"col.uid + '-header-text ' + col.uid + '-sortdir-text'\">" +
                    "   <div role=\"button\" tabindex=\"0\" class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" col-index=\"renderIndex\" title=\"TOOLTIP\" ng-click=\"grid.appScope.editOutputExpression(col)\">" +
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
                    "<div class=\"ui-grid-cell-contents\" ng-class=\"{ 'ui-grid-cell-contents-empty': !COL_FIELD }\" title=\"TOOLTIP\">" +
                    "   <span class=\"contents-value\">{{COL_FIELD}}</span>" +
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
        }]);

angular.module('activitiModeler')
    .controller('DecisionTableInputConditionEditorCtlr', ['$rootScope', '$scope', function ($rootScope, $scope) {
        var previousVariableId = $scope.model.selectedExpression.variableId;

        // condition input options
        $scope.popup = {
            selectedExpressionLabel: $scope.model.selectedExpression.label ? $scope.model.selectedExpression.label : '',
            selectedExpressionVariable: {id: previousVariableId}
        };

        $scope.save = function () {
        	$scope.model.selectedExpression.label = $scope.popup.selectedExpressionLabel;
        	
            if (previousVariableId !== $scope.popup.selectedExpressionVariable.id) {

                var newInputExpression = {
                    label: $scope.popup.selectedExpressionLabel,
                    variableId: $scope.popup.selectedExpressionVariable.id,
                    newVariable: $scope.popup.selectedExpressionNewVariable
                };

                $scope.updateInputExpression($scope.model.selectedColumn, newInputExpression);

                if ($scope.popup.selectedExpressionNewVariable) {
                    saveNewDefinedVariable();
                }

            } else {
                $scope.model.selectedColumn.displayName = $scope.model.selectedExpression.label;
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
    .controller('DecisionTableConclusionEditorCtrl', ['$rootScope', '$scope', '$q', '$translate', function ($rootScope, $scope, $q, $translate) {

        // condition input options
        $scope.popup = {
            selectedExpressionVariableType: '',
            selectedExpressionLabel: $scope.model.selectedExpression.label ? $scope.model.selectedExpression.label : '',
            selectedExpressionNewVariableType: $scope.availableVariableTypes[0]
        };

        $scope.popup.selectedExpressionNewVariableId = $scope.model.selectedExpression.variableId;
        $scope.popup.selectedExpressionNewVariableType = $scope.model.selectedExpression.type;

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
            $scope.model.selectedExpression.variableId = $scope.popup.selectedExpressionNewVariableId;
            $scope.model.selectedExpression.type = $scope.popup.selectedExpressionNewVariableType;
            
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
