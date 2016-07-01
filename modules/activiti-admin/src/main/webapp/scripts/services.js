/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Services */

activitiAdminApp.factory('Account', ['$resource',
    function ($resource) {
        return $resource('app/rest/account', {}, {
        });
    }]);

activitiAdminApp.factory('Password', ['$resource',
    function ($resource) {
        return $resource('app/rest/account/change-password', {}, {
        });
    }]);

activitiAdminApp.factory('Sessions', ['$resource',
    function ($resource) {
        return $resource('app/rest/account/sessions/:series', {}, {
            'get': { method: 'GET', isArray: true}
        });
    }]);

activitiAdminApp.factory('Metrics', ['$resource',
    function ($resource) {
        return $resource('/metrics/metrics', {}, {
            'get': { method: 'GET'}
        });
    }]);

activitiAdminApp.factory('LogsService', ['$resource',
    function ($resource) {
        return $resource('app/rest/logs', {}, {
            'findAll': { method: 'GET', isArray: true},
            'changeLevel':  { method: 'PUT'}
        });
    }]);

activitiAdminApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService', '$translate','$q',
    function ($rootScope, $http, authService, $translate, $q) {
        return {
            authenticate: function() {
                var deferred = $q.defer();

                $http.get('/app/rest/authenticate')
                    .success(function (data, status, headers, config) {
                        $rootScope.$broadcast('event:auth-authConfirmed');
                        deferred.resolve();
                    })
                    .error(function(data, status, headers, config) {
                      // Reject promise and broadcast login required event
                      $rootScope.$broadcast('event:auth-loginRequired');
                      deferred.reject();
                    });

                return deferred.promise;
            },
            login: function (param) {
                var data ="j_username=" + param.username +"&j_password=" + param.password +"&_spring_security_remember_me=" + param.rememberMe +"&submit=Login";
                $http.post('/app/authentication', data, {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    ignoreAuthModule: 'ignoreAuthModule',
                    ignoreErrors: true
                }).success(function (data, status, headers, config) {
                    $rootScope.authenticationError = false;
                    authService.loginConfirmed();
                    if(param.success){
                        param.success(data, status, headers, config);
                    }
                }).error(function (data, status, headers, config) {
                    $rootScope.authenticationError = true;

                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.AUTHENTICATION-ERROR'), 'error');

                    if(param.error){
                        param.error(data, status, headers, config);
                    }
                });
            },
            logout: function () {
            	$rootScope.authenticated = false;
                $rootScope.authenticationError = false;
                $http.get('/app/logout')
                    .success(function (data, status, headers, config) {
                        $rootScope.login = null;
                        authService.loginCancelled();
                    });
            }
        };
    }]);
