/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('DecisionTablesController', ['$rootScope', '$scope', '$http', '$timeout', '$location', '$translate', '$q', '$modal', 'gridConstants',
    function ($rootScope, $scope, $http, $timeout, $location, $translate, $q, $modal, gridConstants) {

        $rootScope.navigation = {selection: 'decision-tables'};
        
        $scope.filter = {};
        $scope.decisionTablesData = {};

        // Array to contain selected properties (yes - we only can select one, but ng-grid isn't smart enough)
        $scope.selectedDecisionTables = [];

        var filterConfig = {
            url: '/app/rest/activiti/decision-tables',
            method: 'GET',
            success: function (data, status, headers, config) {
                $scope.decisionTablesData = data;
            },
            error: function (data, status, headers, config) {
                if (data && data.message) {
                    // Extract error-message
                    $rootScope.addAlert(data.message, 'error');
                } else {
                    // Use default error-message
                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                }
            },

            sortObjects: [
                {name: 'DECISION-TABLES.SORT.ID', id: 'id'},
                {name: 'DECISION-TABLES.SORT.NAME', id: 'name'}
            ],

            supportedProperties: [
                {id: 'nameLike', name: 'DECISION-TABLES.FILTER.NAME', showByDefault: true},
                {id: 'keyLike', name: 'DECISION-TABLES.FILTER.KEY', showByDefault: true},
                {id: 'tenantIdLike', name: 'DECISION-TABLES.FILTER.TENANTID', showByDefault: true}
            ]
        };

        if ($rootScope.filters && $rootScope.filters.decisionTableFilter) {
            // Reuse the existing filter
            $scope.filter = $rootScope.filters.decisionTableFilter;
            $scope.filter.config = filterConfig;
        } else {
            $scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
            $rootScope.filters.decisionTableFilter = $scope.filter;
        }

        $scope.decisionTableSelected = function (decisionTable) {
            if (decisionTable && decisionTable.getProperty('id')) {
                $location.path('/decision-table/' + decisionTable.getProperty('id'));
            }
        };

        $q.all([$translate('DECISION-TABLES.HEADER.ID'),
                $translate('DECISION-TABLES.HEADER.NAME'),
                $translate('DECISION-TABLES.HEADER.KEY'),
                $translate('DECISION-TABLES.HEADER.VERSION'),
                $translate('DECISION-TABLES.HEADER.TENANT-ID')])
            .then(function (headers) {
                // Config for grid
                $scope.gridDecisionTables = {
                    data: 'decisionTablesData.data',
                    enableRowReordering: true,
                    multiSelect: false,
                    keepLastSelected: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.decisionTableSelected,
                    columnDefs: [
                        {field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'name', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'key', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'version', displayName: headers[3], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'tenantId', displayName: headers[4], cellTemplate: gridConstants.defaultTemplate}]
                };
            });

        $scope.executeWhenReady(function () {
            $scope.filter.refresh();
        });

    }]);
