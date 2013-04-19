'use strict';

angular.module('activitiApp')
    .controller('TaskCtrl', function ($scope, $http) {

        $scope.loadingTasks = true;

        /* Fetch tasks */
        $http.get('http://localhost:8080/activiti-webapp-rest2/service/runtime/tasks').
            success(function (data, status, headers, config) {
                showTasks(data);
            }).
            error(function (data, status, headers, config) {
                console.log('Couldn\'t fetch tasks : ' + status);
                $scope.loadingTasks = false;
            });

        /* Show the results from a rest call on the screen */
        var showTasks = function(data) {
            $scope.tasks = data.data;
            $scope.loadingTasks = false;
            console.log("Done loading");
        };

        /* Task searching */
        $scope.search = function() {
            $scope.loadingTasks = true;

            $http.get('http://localhost:8080/activiti-webapp-rest2/service/runtime/tasks?nameLike=%25' + $scope.searchQuery + '%25').
                success(function (data, status, headers, config) {
                    showTasks(data);
                }).
                error(function (data, status, headers, config) {
                    console.log('Couldn\'t fetch tasks : ' + status);
                    $scope.loadingTasks = false;
                });
        };

    })
;


