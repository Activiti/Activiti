'use strict';

angular.module('activitiApp', ['ui'])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/main.html',
                controller: 'MainCtrl'
            })
            .when('/tasks', {
                templateUrl: 'views/tasks.html',
                controller: 'TaskCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    }]);
