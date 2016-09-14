/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

/**
 * @license HTTP Auth Interceptor Module for AngularJS
 * (c) 2012 Witold Szczerba
 * License: MIT
 */
(function () {
    angular.module('http-auth-interceptor', ['http-auth-interceptor-buffer'])

        .factory('authService', ['$rootScope','httpBuffer', function($rootScope, httpBuffer) {
            return {
                /**
                 * Call this function to indicate that authentication was successfull and trigger a
                 * retry of all deferred requests.
                 * @param data an optional argument to pass on to $broadcast which may be useful for
                 * example if you need to pass through details of the user that was logged in
                 */
                loginConfirmed: function(data, configUpdater) {
                    var updater = configUpdater || function(config) {return config;};
                    $rootScope.$broadcast('event:auth-loginConfirmed', data);
                    httpBuffer.retryAll(updater);
                },

                /**
                 * Call this function to indicate that authentication should not proceed.
                 * All deferred requests will be abandoned or rejected (if reason is provided).
                 * @param data an optional argument to pass on to $broadcast.
                 * @param reason if provided, the requests are rejected; abandoned otherwise.
                 */
                loginCancelled: function(data, reason) {
                    httpBuffer.rejectAll(reason);
                    $rootScope.$broadcast('event:auth-loginCancelled', data);
                }
            };
        }])

        /**
         * $http interceptor.
         * On 401 response (without 'ignoreAuthModule' option) stores the request
         * and broadcasts 'event:angular-auth-loginRequired'.
         */
        .config(['$httpProvider', function($httpProvider) {
            var interceptor = ['$rootScope', '$q', 'httpBuffer', '$translate', function($rootScope, $q, httpBuffer, $translate) {
                return {
                    responseError: function(response) {
                        if (response.status === 401 && !response.config.ignoreAuthModule) {
                            var deferred = $q.defer();
                            httpBuffer.append(response.config, deferred);
                            $rootScope.$broadcast('event:auth-loginRequired', response);
                            return deferred.promise;
                        } else if (response.status === 401) {
                            $rootScope.invalidCredentials = true;
                        }

                        if(response.status >= 500 && response.status <= 599 && response.config && !response.config.ignoreErrors) {
                            if(response.data && response.data.messageKey) {
                                // Use message key to translate error
                                $rootScope.addAlertPromise($translate(response.data.messageKey), 'error');
                            } else if(response.data && response.data.message) {
                                // Extract error-message
                                $rootScope.addAlert(response.data.message, 'error');
                            } else {
                                // Use default error-message
                                $rootScope.addAlertPromise($translate('GENERAL.ERROR.GENERIC'), 'error');
                            }
                        }

                        // otherwise, default behaviour
                        return $q.reject(response);
                    }
                };

            }];

            $httpProvider.interceptors.push(interceptor);
        }]);

    /**
     * Private module, a utility, required internally by 'http-auth-interceptor'.
     */
    angular.module('http-auth-interceptor-buffer', [])

        .factory('httpBuffer', ['$injector', function($injector) {
            /** Holds all the requests, so they can be re-requested in future. */
            var buffer = [];

            /** Service initialized later because of circular dependency problem. */
            var $http;

            function retryHttpRequest(config, deferred) {
                function successCallback(response) {
                    deferred.resolve(response);
                }
                function errorCallback(response) {
                    deferred.reject(response);
                }
                $http = $http || $injector.get('$http');
                $http(config).then(successCallback, errorCallback);
            }

            return {
                /**
                 * Appends HTTP request configuration object with deferred response attached to buffer.
                 */
                append: function(config, deferred) {
                    buffer.push({
                        config: config,
                        deferred: deferred
                    });
                },

                /**
                 * Abandon or reject (if reason provided) all the buffered requests.
                 */
                rejectAll: function(reason) {
                    if (reason) {
                        for (var i = 0; i < buffer.length; ++i) {
                            buffer[i].deferred.reject(reason);
                        }
                    }
                    buffer = [];
                },

                /**
                 * Retries all the buffered requests clears the buffer.
                 */
                retryAll: function(updater) {
                    for (var i = 0; i < buffer.length; ++i) {
                        retryHttpRequest(updater(buffer[i].config), buffer[i].deferred);
                    }
                    buffer = [];
                }
            };
        }]);
})();
/* Services */

// Add authentication factories
activitiApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService', '$q', '$location', '$window',
    function ($rootScope, $http, authService, $q, $location, $window) {
      return {
        authenticate: function() {
          var deferred = $q.defer();
          $http.get(ACTIVITI.CONFIG.contextRoot + '/app/rest/authenticate', {ignoreErrors: true, ignoreAuthModule: 'ignoreAuthModule'})
              .success(function (data, status, headers, config) {
              
                  var authUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/account';
                  if (ACTIVITI.CONFIG.integrationProfile) {
                      authUrl += '?includeApps=true';
                  }
                  
                  $http.get(authUrl)
                      .success(function (data, status, headers, config) {
                          $rootScope.account = data;
                          $rootScope.invalidCredentials = false;
                          $rootScope.$broadcast('event:auth-authConfirmed');

                          deferred.resolve();
                      })
                      .error(function(data, status, headers, config) {
                          // Reject promise and broadcast login required event
                          deferred.reject(data);
                          $rootScope.$broadcast('event:auth-loginRequired');
                      });
              })
              .error(function(data, status, headers, config) {
                  // Reject promise and broadcast login required event
                  deferred.reject(data);
                  $rootScope.$broadcast('event:auth-loginRequired');
              });

          return deferred.promise;
        },
          
        login: function (param) {
            var data ="j_username=" + encodeURIComponent(param.username) +"&j_password=" + encodeURIComponent(param.password) +"&_spring_security_remember_me=true&submit=Login";
            $http.post(ACTIVITI.CONFIG.contextRoot + '/app/authentication', data, {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                ignoreAuthModule: 'ignoreAuthModule'
                
            }).success(function (data, status, headers, config) {
                $rootScope.authenticationError = false;

                if (param.success){
                    param.success(data, status, headers, config);
                }
                authService.loginConfirmed();
                
            }).error(function (data, status, headers, config) {
                $rootScope.$broadcast('event:auth-loginFailed');
                if(param.error){
                    param.error(data, status, headers, config);
                }
            });
        },
        logout: function () {
            $rootScope.authenticated = false;
            $rootScope.authenticationError = false;
            $http.get(ACTIVITI.CONFIG.contextRoot + '/app/logout')
                .success(function (data, status, headers, config) {
                    $rootScope.login = null;
                    $rootScope.authenticated = false;
                    authService.loginCancelled({isFromLogout: true});
                });
        },

        hasAdminCapability: function() {
            if ($rootScope.account && $rootScope.account.groups) {
                for (var groupIndex = 0; groupIndex < $rootScope.account.groups.length; groupIndex++) {
                    var group = $rootScope.account.groups[groupIndex];
                    if (group.type !== null && group.type !== undefined && group.type.toLowerCase() === 'security-role') {
                        return group.id !== null && group.id !== undefined && group.id.toLowerCase() === 'role_admin';
                    }
                }
            }
            return false;
        }

      };
    }]);
