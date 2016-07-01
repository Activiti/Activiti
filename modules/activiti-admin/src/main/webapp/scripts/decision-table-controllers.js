/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

activitiAdminApp.controller('DecisionTableController', ['$scope', '$rootScope', '$http', '$timeout', '$location', '$routeParams', '$modal', '$translate', '$q', 'gridConstants',
    function ($scope, $rootScope, $http, $timeout, $location, $routeParams, $modal, $translate, $q, gridConstants) {
        $rootScope.navigation = {selection: 'decision-tables'};
        
        $scope.returnToList = function () {
            $location.path("/decision-tables");
        };

        $scope.showAllDecisionTables = function () {
            // Populate the process-filter with parentId
            $rootScope.filters.forced.processDefinitionFilter = {
                deploymentId: $scope.deployment.id
            };
            $location.path("/process-definitions");
        };

        $scope.showDecisionTable = function () {
            $modal.open({
                templateUrl: 'views/decision-table-popup.html',
                windowClass: 'modal modal-full-width',
                controller: 'ShowDecisionTablePopupCrtl',
                resolve: {
                    decisionTable: function () {
                        return $scope.decisionTable;
                    }
                }
            });
        };

        $scope.showDecisionAudit = function (decisionAudit) {
            if (decisionAudit && decisionAudit.getProperty('id')) {
                $location.path("/decision-audit/"+decisionAudit.getProperty('id'));
            }
        };

        $q.all([$translate('DECISION-AUDIT.HEADER.ID'),
                $translate('DECISION-AUDIT.HEADER.PROCESS-DEFINITION-ID'),
                $translate('DECISION-AUDIT.HEADER.PROCESS-INSTANCE-ID'),
                $translate('DECISION-AUDIT.HEADER.CREATED'),
                $translate('DECISION-AUDIT.HEADER.FAILED')])
            .then(function (headers) {

                $scope.gridDecisionAudits = {
                    data: 'decisionAudits.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected: false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.showDecisionAudit,
                    columnDefs: [
                        {field: 'id', displayName: headers[0]},
                        {field: 'processDefinitionId', displayName: headers[1]},
                        {field: 'processInstanceId', displayName: headers[2]},
                        {field: 'created', displayName: headers[3], cellTemplate: gridConstants.dateTemplate},
                        {field: 'decisionExecutionFailed', displayName: headers[4]}
                    ]
                };
            });

        $scope.executeWhenReady(function () {
            // Load deployment
            $http({method: 'GET', url: '/app/rest/activiti/decision-tables/' + $routeParams.decisionTableId}).
            success(function (data, status, headers, config) {
                $scope.decisionTable = data;

                // Load decision audits
                $http({
                    method: 'GET',
                    url: '/app/rest/activiti/decision-audits?decisionKey=' + data.key + '&dmnDeploymentId=' + data.deploymentId
                }).
                success(function (auditsData, status, headers, config) {
                    $scope.decisionAudits = auditsData;
                });
                
                $http({method: 'GET', url: '/app/rest/activiti/app?dmnDeploymentId=' + data.deploymentId}).
                success(function(appData, status, headers, config) {
                    $scope.appId = appData.id;
                });
                
            }).
            error(function (data, status, headers, config) {
                if (data && data.message) {
                    // Extract error-message
                    $rootScope.addAlert(data.message, 'error');
                } else {
                    // Use default error-message
                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                }
            });
        });

    }]);

activitiAdminApp.controller('ShowDecisionTablePopupCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'decisionTable', '$timeout', '$translate', 'uiGridConstants',
        function ($rootScope, $scope, $modalInstance, $http, decisionTable, $timeout, $translate, uiGridConstants) {

            var MIN_COLUMN_WIDTH = 200;

            $scope.status = {loading: false};

            $scope.cancel = function () {
                if (!$scope.status.loading) {
                    $modalInstance.dismiss('cancel');
                }
            };

            $scope.popup = {
                currentDecisionTableRules: [],
                columnDefs: [],
                columnVariableIdMap: {}
            };

            $scope.executeWhenReady(function () {
                // Load deployment
                $http({
                    method: 'GET',
                    url: '/app/rest/activiti/decision-tables/' + decisionTable.id + '/editorJson'
                }).
                success(function (data, status, headers, config) {
                    $scope.popup.currentDecisionTable = data;
                }).
                error(function (data, status, headers, config) {
                    if (data && data.message) {
                        // Extract error-message
                        $rootScope.addAlert(data.message, 'error');
                    } else {
                        // Use default error-message
                        $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                    }
                });
            });

            var variableUndefined = $translate.instant('DECISION-TABLE-EDITOR.EMPTY-MESSAGES.NO-VARIABLE-SELECTED');
            // helper for looking up variable id by col id
            $scope.getVariableNameByColumnId = function (colId) {

                if (!colId) {
                    return;
                }

                if ($scope.popup.columnVariableIdMap[colId]) {
                    return $scope.popup.columnVariableIdMap[colId];
                } else {
                    return variableUndefined;
                }
            };

            var _loadDecisionTableDefinition = function () {

                if ($scope.popup.currentDecisionTable.inputExpressions) {
                    $scope.popup.currentDecisionTable.inputExpressions.forEach(function (inputExpression) {
                        $scope.popup.columnVariableIdMap[inputExpression.id] = inputExpression.variableId;
                    });
                }

                if ($scope.popup.currentDecisionTable.outputExpressions) {
                    $scope.popup.currentDecisionTable.outputExpressions.forEach(function (outputExpression) {
                        $scope.popup.columnVariableIdMap[outputExpression.id] = outputExpression.variableId;
                    });
                }

                // initialize ui grid model
                if ($scope.popup.currentDecisionTable.rules && $scope.popup.currentDecisionTable.rules.length > 0) {
                    Array.prototype.push.apply($scope.popup.currentDecisionTableRules, $scope.popup.currentDecisionTable.rules);
                }

                // get column definitions
                $scope.getColumnDefinitions($scope.popup.currentDecisionTable);

            };

            // Custom UI grid template
            var _getHeaderInputExpressionCellTemplate = function () {
                return "" +
                    "<div role=\"columnheader\" ui-grid-one-bind-aria-labelledby-grid=\"col.uid + '-header-text ' + col.uid + '-sortdir-text'\">" +
                    "   <div class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" col-index=\"renderIndex\" title=\"TOOLTIP\">" +
                    "       <div class=\"text-center\"><span>{{ col.displayName CUSTOM_FILTERS }}</span></div>" +
                    "       <div class=\"text-center\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\" style=\"text-decoration: underline;\">[ {{ grid.appScope.getVariableNameByColumnId(col.name) }} ]</span></div>" +
                    "   </div>" +
                    "</div>";
            };

            var _getHeaderOutputExpressionCellTemplate = function () {
                return "" +
                    "<div role=\"columnheader\" ui-grid-one-bind-aria-labelledby-grid=\"col.uid + '-header-text ' + col.uid + '-sortdir-text'\">" +
                    "   <div class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" col-index=\"renderIndex\" title=\"TOOLTIP\">" +
                    "       <div class=\"text-center\"><span>{{ col.displayName CUSTOM_FILTERS }}</span></div>" +
                    "       <div class=\"text-center\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\" style=\"text-decoration: underline;\">[ {{ grid.appScope.getVariableNameByColumnId(col.name) }} ]</span></div>" +
                    "   </div>" +
                    "</div>";
            };

            var _rowHeaderTemplate = function () {
                return "<div class=\"ui-grid-disable-selection\"><div class=\"ui-grid-cell-contents text-center customRowHeader\">{{rowRenderIndex + 1}}</div></div>"
            };

            var _getCellTemplate = function (columnType) {
                var cellTemplate = "" +
                    "<div class=\"ui-grid-cell-contents\" ng-class=\"{ 'ui-grid-cell-contents-empty': !COL_FIELD }\" title=\"TOOLTIP\">" +
                    "   <span class=\"contents-value\">{{COL_FIELD}}</span>" +
                    "</div>";
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
                            cellTemplate: _getCellTemplate(),
                            enableHiding: false,
                            enableCellEditOnFocus: false,
                            minWidth: MIN_COLUMN_WIDTH
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
                            cellTemplate: _getCellTemplate(),
                            enableHiding: false,
                            enableCellEditOnFocus: false,
                            minWidth: MIN_COLUMN_WIDTH
                        });

                        expressionCounter++;
                    });
                }

                // merge models
                if ($scope.popup.columnDefs) {
                    $scope.popup.columnDefs.length = 0;
                }

                else {
                    $scope.popup.columnDefs = [];
                }
                Array.prototype.push.apply($scope.popup.columnDefs, newColumnDefs);

                $scope.popup.gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);
            };

            // config for grid
            $scope.popup.gridOptions = {
                data: $scope.popup.currentDecisionTableRules,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 2,
                enableColumnMenus: false,
                enableSorting: false,
                enableCellEditOnFocus: false,
                columnDefs: $scope.popup.columnDefs
                //headerTemplate: 'views/templates/decision-table-header-template.html'
            };

            $scope.popup.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.popup.gridApi = gridApi;

                var cellTemplate = _rowHeaderTemplate();   // you could use your own template here
                $scope.popup.gridApi.core.addRowHeaderColumn({name: 'rowHeaderCol', displayName: '', width: 35, cellTemplate: cellTemplate});

                // Load definition that will be rendered
                _loadDecisionTableDefinition();
            };
            

        }]);

