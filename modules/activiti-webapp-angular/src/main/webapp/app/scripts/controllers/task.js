'use strict';

angular.module('activitiApp')
    .controller('TaskCtrl', function ($scope, $http) {

        // Default settings
        $scope.loadingTasks = true;
        $scope.pageSize = 10;
        $scope.currentPage = 1;
        var noGroupId = '_activiti_invalid_group';
        var noGroupElement = { 'name' : '', 'id' : noGroupId};

        /*
         * Fetch tasks async when controller instantiates
         */
        $http.get('http://localhost:8080/activiti-webapp-rest2/service/runtime/tasks').
            success(function (data, status, headers, config) {
                showTasks(data);
            }).
            error(function (data, status, headers, config) {
                console.log('Couldn\'t fetch tasks : ' + status);
                $scope.loadingTasks = false;
            });

        // TODO: extract this in separate logic later!
        // TODO: remove hardcoded kermit dependency
        /*
         * Fetch user async when controller instantiates
         */
        $http.get('http://localhost:8080/activiti-webapp-rest2/service/user/kermit/groups').
            success(function (data, status, headers, config) {
                var groups = [];
                groups.push(noGroupElement);
                for (var i=0; i<data.data.length; i++) {
                    if (data.data[i].type == 'assignment') {
                        groups.push(data.data[i]);
                    }
                }

                if (groups.length > 0) {
                    $scope.groups = groups;
                    $scope.candidateGroup = groups[0];
                }

            }).
            error(function (data, status, headers, config) {
                console.log('Couldn\'t fetch user groups : ' + status);
            });

        /*
         * Task searching
         */
        $scope.search = function () {
            executeSearch();
        };

        /*
         * Task pagination
         */
        $scope.switchPage = function (page) {
            executeSearch(page);
        };

        /**
         * Builds the URL
         */
        var createSearchUrl = function (page) {
            var url = encodeURI('http://localhost:8080/activiti-webapp-rest2/service/runtime/tasks?size=' + $scope.pageSize);

            // name
            if ($scope.searchQuery && $scope.searchQuery.length > 0) {
                url = url + '&nameLike=%' + $scope.searchQuery + '%';
            }

            // pagination
            if (page) {
                url = url + '&start=' + ((page - 1) * $scope.pageSize);
            } else {
                url = url + '&start=0';
            }

            // Group
            if ($scope.candidateGroup && $scope.candidateGroup.id != noGroupId) {
                url = url + '&candidateGroup=' + $scope.candidateGroup.id;
            }

            url = encodeURI(url);
            console.log('Calling URL ' + url);
            return url;
        };

        /*
         * Execute the async search
         */
        var executeSearch = function (page) {
            $scope.loadingTasks = true;
            $http.get(createSearchUrl(page)).
                success(function (data, status, headers, config) {
                    showTasks(data);
                }).
                error(function (data, status, headers, config) {
                    console.log('Couldn\'t fetch tasks : ' + status);
                    $scope.loadingTasks = false;
                });
        };

        /*
         * Show the results from a rest call on the screen
         */
        var showTasks = function (data) {

            // Task data
            $scope.tasks = data.data;
            $scope.loadingTasks = false;

            var total = data.total;
            var pageSize = $scope.pageSize;
            var realSize = data.size;
            var start = data.start;

            console.log('Total = ' + total);
            console.log('size = ' + pageSize);
            console.log('Start = ' + start);

            // Pagination
            if (total > pageSize) {

                $scope.numberOfPages = Math.floor(total / pageSize);
                if (total % pageSize > 0) {
                    $scope.numberOfPages = $scope.numberOfPages + 1;
                }
                console.log('nrOfPages = ' + $scope.numberOfPages);

                $scope.currentPage = (Math.floor(start / pageSize)) + 1; // +1 for normal people numbering

            } else {

                $scope.numberOfPages = -1;
                $scope.currentPage = -1;

            }

            // Description above tasks
            if (total > 0) {
                $scope.indexOfFirstTask = start + 1;
                $scope.indexOfLastTask = start + realSize;
                $scope.totalNumberOfTasks = total;
            } else {
                $scope.totalNumberOfTasks = 0;
            }

            console.log('Current page is ' + $scope.currentPage);

        };

        /**
         * Used to determine whether to show the claim button or not
         */
        $scope.isUnclaimedTask = function() {
          return $scope.candidateGroup && $scope.candidateGroup.id != noGroupId;
        };

        /**
         * Claim a task
         */
        $scope.claimTask = function(task) {
            $scope.loadingTasks = true;

            $http.put('http://localhost:8080/activiti-webapp-rest2/service/task/' + task.id + "/claim").
                success(function (data, status, headers, config) {
                    // After a successful claim, simply refresh the task list with the current search params
                    executeSearch();
                }).
                error(function (data, status, headers, config) {
                    console.log('Couldn\'t claim task : ' + status);
                });

            $scope.loadingTasks = false;
        }

    })
;


