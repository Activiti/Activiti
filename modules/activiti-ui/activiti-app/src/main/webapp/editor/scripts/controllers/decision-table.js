/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
angular.module('activitiModeler')
    .controller('DecisionTableDetailsCtrl', ['$rootScope', '$scope', '$translate', '$http', '$location', '$routeParams','$modal', '$timeout', '$popover', 'DecisionTableService', 'uiGridConstants',
        function ($rootScope, $scope, $translate, $http, $location, $routeParams, $modal, $timeout, $popover, DecisionTableService, uiGridConstants) {

            var MIN_COLUMN_WIDTH = 200;

            $scope.decisionTableMode = 'read';

            // Initialize model
            $scope.model = {
                // Store the main model id, this points to the current version of a model,
                // even when we're showing history
                latestModelId: $routeParams.modelId,
                columnDefs: [],
                columnVariableIdMap: {}
            };

            $rootScope.currentDecisionTableRules = [];

            var variableUndefined = $translate.instant('DECISION-TABLE-EDITOR.EMPTY-MESSAGES.NO-VARIABLE-SELECTED');
            // helper for looking up variable id by col id
            $scope.getVariableNameByColumnId = function (colId) {

                if (!colId) {
                    return;
                }

                if ($scope.model.columnVariableIdMap[colId]) {
                    return $scope.model.columnVariableIdMap[colId];
                } else {
                    return variableUndefined;
                }
            };

            $scope.loadDecisionTable = function() {
                var url, decisionTableUrl;
                if ($routeParams.modelHistoryId) {
                    url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId + '/history/' + $routeParams.modelHistoryId;
                    decisionTableUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/decision-table-models/history/' + $routeParams.modelHistoryId;
                } else {
                    url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId;
                    decisionTableUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/decision-table-models/' + $routeParams.modelId;
                }

                $http({method: 'GET', url: url}).
                    success(function(data, status, headers, config) {
                        $scope.model.decisionTable = data;
                        $scope.model.decisionTableDownloadUrl = decisionTableUrl + '/export?version=' + Date.now();
                        $scope.loadVersions();

                    }).error(function(data, status, headers, config) {
                        $scope.returnToList();
                    });
            };

            $scope.useAsNewVersion = function() {
                _internalCreateModal({
                    template: 'views/popup/model-use-as-new-version.html',
                    scope: $scope
                }, $modal, $scope);
            };

            $scope.toggleFavorite = function() {
                $scope.model.favoritePending = true;

                var data = {
                    favorite: !$scope.model.decisionTable.favorite
                };

                $http({method: 'PUT', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.model.latestModelId, data: data}).
                    success(function(data, status, headers, config) {
                        $scope.model.favoritePending = false;
                        if ($scope.model.decisionTable.favorite) {
                            $scope.addAlertPromise($translate('DECISION-TABLE.ALERT.UN-FAVORITE-CONFIRM'), 'info');
                        } else {
                            $scope.addAlertPromise($translate('DECISION-TABLE.ALERT.FAVORITE-CONFIRM'), 'info');
                        }
                        $scope.model.decisionTable.favorite = !$scope.model.decisionTable.favorite;
                    }).error(function(data, status, headers, config) {
                        $scope.model.favoritePending = false;
                    });
            };


            $scope.loadVersions = function() {

                var params = {
                    includeLatestVersion: !$scope.model.decisionTable.latestVersion
                };

                $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.model.latestModelId + '/history', params: params}).
                    success(function(data, status, headers, config) {
                        if ($scope.model.decisionTable.latestVersion) {
                            if (!data.data) {
                                data.data = [];
                            }
                            data.data.unshift($scope.model.decisionTable);
                        }

                        $scope.model.versions = data;
                    });
            };

            $scope.showVersion = function(version) {
                if (version) {
                    if (version.latestVersion) {
                        $location.path("/decision-tables/" +  $scope.model.latestModelId);
                    } else {
                        // Show latest version, no history-suffix needed in URL
                        $location.path("/decision-tables/" +  $scope.model.latestModelId + "/history/" + version.id);
                    }
                }
            };

            $scope.returnToList = function() {
                $location.path("/decision-tables/");
            };

            $scope.editDecisionTable = function() {
                _internalCreateModal({
                    template: 'views/popup/model-edit.html',
                    scope: $scope
                }, $modal, $scope);
            };

            $scope.duplicateDecisionTable = function() {

                var modalInstance = _internalCreateModal({
                    template: 'views/popup/decision-table-duplicate.html?version=' + Date.now()
                }, $modal, $scope);

                modalInstance.$scope.originalModel = $scope.model;

                modalInstance.$scope.duplicateDecisionTableCallback = function(result) {
                    $rootScope.editorHistory = [];
                    $location.url("/decision-table-editor/" + encodeURIComponent(result.id));
                };
            };

            $scope.deleteDecisionTable = function() {
                _internalCreateModal({
                    template: 'views/popup/model-delete.html',
                    scope: $scope
                }, $modal, $scope);
            };

            $scope.shareDecisionTable = function() {
                _internalCreateModal({
                    template: 'views/popup/model-share.html',
                    scope: $scope
                }, $modal, $scope);
            };

            $scope.openEditor = function() {
                if ($scope.model.decisionTable) {
                    $location.path("/decision-table-editor/" + $scope.model.decisionTable.id);
                }
            };

            $scope.toggleHistory = function($event) {
                if (!$scope.historyState) {
                    var state = {};
                    $scope.historyState = state;

                    // Create popover
                    state.popover = $popover(angular.element($event.target), {
                        template: 'views/popover/history.html',
                        placement: 'bottom-right',
                        show: true,
                        scope: $scope,
                        container: 'body'
                    });

                    var destroy = function() {
                        state.popover.destroy();
                        delete $scope.historyState;
                    };

                    // When popup is hidden or scope is destroyed, hide popup
                    state.popover.$scope.$on('tooltip.hide', destroy);
                    $scope.$on('$destroy', destroy);
                }
            };

            var _loadDecisionTableDefinition = function (modelId, historyModelId) {
                DecisionTableService.fetchDecisionTableDetails(modelId, historyModelId).then( function (decisionTable) {

                    $rootScope.currentDecisionTable = decisionTable.decisionTableDefinition;
                    $rootScope.currentDecisionTable.id = decisionTable.id;
                    $rootScope.currentDecisionTable.name = decisionTable.name;
                    $rootScope.currentDecisionTable.description = decisionTable.description;

                    if ($rootScope.currentDecisionTable.inputExpressions) {
                        $rootScope.currentDecisionTable.inputExpressions.forEach( function (inputExpression) {
                            $scope.model.columnVariableIdMap[inputExpression.id] = inputExpression.variableId;
                        });
                    }

                    if ($rootScope.currentDecisionTable.outputExpressions) {
                        $rootScope.currentDecisionTable.outputExpressions.forEach( function (outputExpression) {
                            $scope.model.columnVariableIdMap[outputExpression.id] = outputExpression.variableId;
                        });
                    }

                    // initialize ui grid model
                    if ($rootScope.currentDecisionTable.rules && $rootScope.currentDecisionTable.rules.length > 0) {
                        Array.prototype.push.apply($rootScope.currentDecisionTableRules, $rootScope.currentDecisionTable.rules);
                    }

                    // get column definitions
                    $scope.getColumnDefinitions($rootScope.currentDecisionTable);
                });
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
                if ($scope.model.columnDefs) {
                    $scope.model.columnDefs.length = 0;
                }

                else {
                    $scope.model.columnDefs = [];
                }
                Array.prototype.push.apply($scope.model.columnDefs, newColumnDefs);

                $scope.gridApi.core.notifyDataChange( uiGridConstants.dataChange.ALL );
            };

            // config for grid
            $scope.gridOptions = {
                data: $rootScope.currentDecisionTableRules,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 2,
                enableColumnMenus: false,
                enableSorting: false,
                enableCellEditOnFocus: false,
                columnDefs: $scope.model.columnDefs,
                headerTemplate: 'views/templates/decision-table-header-template.html'
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;

                var cellTemplate = _rowHeaderTemplate();   // you could use your own template here
                $scope.gridApi.core.addRowHeaderColumn( { name: 'rowHeaderCol', displayName: '', width: 35, cellTemplate: cellTemplate} );

                // Load definition that will be rendered
                _loadDecisionTableDefinition($routeParams.modelId, $routeParams.modelHistoryId);
            };

            // Load model needed for favorites
            $scope.loadDecisionTable();


        }]);
