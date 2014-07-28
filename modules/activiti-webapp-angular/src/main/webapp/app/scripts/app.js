'use strict';

angular.module('activitiApp', ['ui', 'filters', 'ui.bootstrap'])

    // Temporary until we have a login page: always log in with kermit:kermit
    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.defaults.headers.common['Authorization'] = 'Basic a2VybWl0Omtlcm1pdA==';
    }])

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

// TODO: extract these in a separate filters.js?
angular.module('filters', [])
    .filter('makeRange', function () {
        return function (input) {
            var lowBound, highBound;
            switch (input.length) {
                case 1:
                    lowBound = 0;
                    highBound = parseInt(input[0]) - 1;
                    break;
                case 2:
                    lowBound = parseInt(input[0]);
                    highBound = parseInt(input[1]);
                    break;
                default:
                    return input;
            }
            var result = [];
            for (var i = lowBound; i <= highBound; i++)
                result.push(i);
            return result;
        };
    });
