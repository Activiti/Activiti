'use strict';

angular.module('activitiApp')
    .controller('TaskCtrl', function ($scope, $http) {

        // Default settings
        $scope.loadingTasks = true;
        $scope.pageSize = 10;
        $scope.currentPage = 1;

        /* Fetch tasks async when controller instantiates */
        $http.get('http://localhost:8080/activiti-webapp-rest2/service/runtime/tasks').
            success(function (data, status, headers, config) {
                showTasks(data);
            }).
            error(function (data, status, headers, config) {
                console.log('Couldn\'t fetch tasks : ' + status);
                $scope.loadingTasks = false;
            });

        /* Task searching */
        $scope.search = function () {
            executeSearch();
        };

        /* Task pagination */
        $scope.switchPage = function (page) {
            executeSearch(page);
        };

        /** Builds the URL */
        var createUrl = function (page) {
            var url = encodeURI("http://localhost:8080/activiti-webapp-rest2/service/runtime/tasks?size=" + $scope.pageSize);

            // name
            if ($scope.searchQuery && $scope.searchQuery.length > 0) {
                url = url + "&nameLike=%" + $scope.searchQuery + "%";
            }

            // pagination
            if (page) {
                url = url + "&start=" + ((page - 1) * $scope.pageSize);
            } else {
                url = url + "&start=0";
            }

            url = encodeURI(url);
            console.log("Calling URL " + url);
            return url;
        };

        /* Execute the async search */
        var executeSearch = function (page) {
            $scope.loadingTasks = true;
            $http.get(createUrl(page)).
                success(function (data, status, headers, config) {
                    showTasks(data);
                }).
                error(function (data, status, headers, config) {
                    console.log('Couldn\'t fetch tasks : ' + status);
                    $scope.loadingTasks = false;
                });
        }

        /* Show the results from a rest call on the screen */
        var showTasks = function (data) {

            // Task data
            $scope.tasks = data.data;
            $scope.loadingTasks = false;

            // Pagination
            var total = data.total;
            var pageSize = $scope.pageSize;
            var start = data.start;

            console.log("Total = " + total);
            console.log("size = " + pageSize);
            console.log("Start = " + start);

            if (total > pageSize) {

                $scope.numberOfPages = Math.floor(total / pageSize);
                if (total % pageSize > 0) {
                    $scope.numberOfPages = $scope.numberOfPages + 1;
                }
                console.log("nrOfPages = " + $scope.numberOfPages);

                $scope.currentPage = (Math.floor(start / pageSize)) + 1; // +1 for normal people numbering

            } else {

                $scope.numberOfPages = -1;
                $scope.currentPage = -1;

            }

            console.log("Current page is " + $scope.currentPage);

        };

    })
;


