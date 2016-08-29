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

var activitiApp = angular.module('activitiApp', [
    'http-auth-interceptor',
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'mgcrea.ngStrap',
    'ngAnimate',
    'ngFileUpload',
    'pascalprecht.translate',
    'ui.grid',
    'ui.grid.edit',
    'ui.grid.selection',
    'ui.grid.autoResize',
    'ui.grid.cellNav'
]);

var activitiModule = activitiApp;

activitiApp.config(['$provide', '$routeProvider', '$translateProvider', function ($provide, $routeProvider, $translateProvider) {

  var appName = 'idm';
  $provide.value('appName', appName);
  var appResourceRoot = ACTIVITI.CONFIG.webContextRoot + (ACTIVITI.CONFIG.webContextRoot ? '/' + appName + '/' : '');
  $provide.value('appResourceRoot', appResourceRoot);

  var authRouteResolver = ['$rootScope', 'AuthenticationSharedService', function($rootScope, AuthenticationSharedService) {

        if(!$rootScope.authenticated) {
          // Return auth-promise. On success, the promise resolves and user is assumed authenticated from now on. If
          // promise is rejected, route will not be followed (no unneeded HTTP-calls will be done, which case a 401 in the end, anyway)
          return AuthenticationSharedService.authenticate();
        } else {
          // Authentication done on rootscope, no need to call service again. Any unauthenticated access to REST will result in
          // a 401 and will redirect to login anyway. Done to prevent additional call to authenticate every route-change
          $rootScope.authenticated = true;
          return true;
        }
      }];

        $routeProvider
            .when('/user-mgmt', {
                controller: 'IdmUserMgmtController',
                templateUrl: appResourceRoot + 'views/idm-user-mgmt.html',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/system-group-mgmt', {
                controller: 'IdmSystemGroupMgmtController',
                templateUrl: appResourceRoot + 'views/idm-system-group-mgmt.html',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/group-mgmt', {
                controller: 'GroupMgmtController',
                templateUrl: appResourceRoot + 'views/idm-group-mgmt.html',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/profile', {
                controller: 'IdmProfileMgmtController',
                templateUrl: appResourceRoot + 'views/idm-profile-mgmt.html',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/logout', {
                templateUrl: appResourceRoot + 'views/empty.html',
                controller: 'LogoutController'
            })
            .otherwise({
                redirectTo: ACTIVITI.CONFIG.appDefaultRoute || '/profile'
            });

        // Initialize angular-translate
        $translateProvider.useStaticFilesLoader({
          prefix: appResourceRoot + 'i18n/',
          suffix: '.json'
        });

        $translateProvider.registerAvailableLanguageKeys(['en'], {
            'en_*': 'en',
            'en-*': 'en'
        });

  }])
    .run(['$rootScope', '$location', '$window', 'AuthenticationSharedService', '$translate', 'appResourceRoot', '$modal',
        function($rootScope, $location, $window, AuthenticationSharedService, $translate, appResourceRoot, $modal) {

          $rootScope.appResourceRoot = appResourceRoot;

            var fixedUrlPart = '/idm/';

            $rootScope.logout = function() {
                AuthenticationSharedService.logout();
            };

            var redirectToLogin = function(data) {
                var absUrl = $location.absUrl();
                var index = absUrl.indexOf(fixedUrlPart);
                var newUrl;
                if (data !== null && data !== undefined && data.isFromLogout !== undefined && data.isFromLogout === true) {
                    newUrl = absUrl.substring(0, index) + '/#login';
                    if (ACTIVITI.CONFIG.loginUrl) {
                        newUrl = ACTIVITI.CONFIG.loginUrl.replace("{url}", $location.absUrl());
                    }
                } else {
                    newUrl = absUrl.substring(0, index) + '/#login?redirectUrl=' + encodeURIComponent($location.absUrl());
                    if (ACTIVITI.CONFIG.loginUrl) {
                        newUrl = ACTIVITI.CONFIG.loginUrl.replace("{url}", encodeURIComponent($location.absUrl()));
                    }
                }
                $window.location.href = newUrl;
            };

            // Call when the 401 response is returned by the client
            $rootScope.$on('event:auth-loginRequired', function(rejection) {
                $rootScope.authenticated = false;
                $rootScope.authenticationChecked = true;

                redirectToLogin();
            });

            // Call when the user is authenticated
            $rootScope.$on('event:auth-authConfirmed', function() {
                $rootScope.authenticated = true;
                $rootScope.authenticationChecked = true;

                if($location.path() == '' || $location.path()=='#' || $location.path() == '/login') {
                    $location.path('/');
                }

            });

            // Call when the user logs in
            $rootScope.$on('event:auth-loginConfirmed', function() {
                AuthenticationSharedService.authenticate();
                $rootScope.common = {}; // clear it, cause we could login with a person with less capabailities
            });

            // Call when the user logs out
            $rootScope.$on('event:auth-loginCancelled', function(event, data) {
                $rootScope.authenticated = false;
                redirectToLogin(data);
            });

            // Call when login fails
            $rootScope.$on('event:auth-loginFailed', function() {
                $rootScope.addAlertPromise($translate('LOGIN.MESSAGES.ERROR.AUTHENTICATION'), 'error');
            });

            $rootScope.backToLanding = function() {
                var baseUrl = $location.absUrl();
                var index = baseUrl.indexOf(fixedUrlPart);
                if (index >= 0) {
                    baseUrl = baseUrl.substring(0, index);
                    baseUrl += '/';
                }
                $window.location.href = baseUrl;
            }
        }])
    .run(['$rootScope', '$timeout', '$translate', '$location', '$window', 'AuthenticationSharedService',
        function($rootScope, $timeout, $translate, $location, $window, AuthenticationSharedService) {

            var proposedLanguage = $translate.proposedLanguage();
            if (proposedLanguage !== 'de' && proposedLanguage !== 'en' && proposedLanguage !== 'es' && proposedLanguage !== 'fr'
                && proposedLanguage !== 'it' && proposedLanguage !== 'ja') {
                
                $translate.use('en');
            }
            
            // Common model (eg selected tenant id)
            $rootScope.common = {};

            $rootScope.webRootUrl = function() {
                return ACTIVITI.CONFIG.webContextRoot;
            };

            $rootScope.restRootUrl = function() {
                return ACTIVITI.CONFIG.contextRoot;
            };

            // Needed for auto-height
            $rootScope.window = {};
            var updateWindowSize = function() {
                $rootScope.window.width = $window.innerWidth;
                $rootScope.window.height  = $window.innerHeight;
            };

            // Window resize hook
            angular.element($window).bind('resize', function() {
                $rootScope.$apply(updateWindowSize());
            });

            $rootScope.$watch('window.forceRefresh', function(newValue) {
                if(newValue) {
                    $timeout(function() {
                        updateWindowSize();
                        $rootScope.window.forceRefresh = false;
                    });
                }
            });

            updateWindowSize();

            $rootScope.hasAdminCapability = function() {
                return AuthenticationSharedService.hasAdminCapability();
            };

            // Main navigation depends on the account being fetched
            $rootScope.$watch('account', function() {
                $rootScope.mainNavigation = [
                    {
                        id: 'userMgmt',
                        title: 'IDM.GENERAL.NAVIGATION.USER-MGMT',
                        path: '/user-mgmt',
                        isVisible: function() {
                            return AuthenticationSharedService.hasAdminCapability();
                        }
                    },
                    {
                        id: 'functionalGroupMgmt',
                        title: 'IDM.GENERAL.NAVIGATION.GROUP-MGMT',
                        path: '/group-mgmt',
                        isVisible: function() {
                            return AuthenticationSharedService.hasAdminCapability();
                        }
                    },
                    {
                        id: 'profile',
                        title: 'IDM.GENERAL.NAVIGATION.PROFILE',
                        path: '/profile',
                        isVisible: function() {
                            return true; // Visible for everyone
                        }
                    }
                ];


                /*
                 * Set the current main page, using the page object. If the page is already active,
                 * this is a no-op.
                 */
                $rootScope.setMainPage = function(mainPage) {
                    $rootScope.mainPage = mainPage;
                    $location.path($rootScope.mainPage.path);
                };

                /*
                 * Set the current main page, using the page ID. If the page is already active,
                 * this is a no-op.
                 */
                $rootScope.setMainPageById = function(mainPageId) {
                    for (var i=0; i<$rootScope.mainNavigation.length; i++) {
                        if (mainPageId == $rootScope.mainNavigation[i].id) {
                            $rootScope.mainPage = $rootScope.mainNavigation[i];
                            break;
                        }
                    }
                };
            });
        }
    ])
    .run(['$rootScope', '$timeout', '$translate', '$location', '$http', '$window',
        function($rootScope, $timeout, $translate, $location, $http, $window) {

            /* ALERTS */

            $rootScope.alerts = {
                queue: []
            };

            $rootScope.showAlert = function(alert) {
                if(alert.queue.length > 0) {
                    alert.current = alert.queue.shift();
                    // Start timout for message-pruning
                    alert.timeout = $timeout(function() {
                        if(alert.queue.length == 0) {
                            alert.current = undefined;
                            alert.timeout = undefined;
                        } else {
                            $rootScope.showAlert(alert);
                        }
                    }, (alert.current.type == 'error' ? 5000 : 1000));
                } else {
                    $rootScope.alerts.current = undefined;
                }
            };

            $rootScope.addAlert = function(message, type) {
                var newAlert = {message: message, type: type};
                if(!$rootScope.alerts.timeout) {
                    // Timeout for message queue is not running, start one
                    $rootScope.alerts.queue.push(newAlert);
                    $rootScope.showAlert($rootScope.alerts);
                } else {
                    $rootScope.alerts.queue.push(newAlert);
                }
            };

            $rootScope.dismissAlert = function() {
                if(!$rootScope.alerts.timeout) {
                    $rootScope.alerts.current = undefined;
                } else {
                    $timeout.cancel($rootScope.alerts.timeout);
                    $rootScope.alerts.timeout = undefined;
                    $rootScope.showAlert($rootScope.alerts);
                }
            };

            $rootScope.addAlertPromise = function(promise, type) {
                if(promise) {
                    promise.then(function(data) {
                        $rootScope.addAlert(data, type);
                    });
                }
            };
        }
    ])

    // Moment-JS date-formatting filter
    .filter('dateformat', function() {
        return function(date, format) {
            if (date) {
                if (format) {
                    return moment(date).format(format);
                } else {
                    return moment(date).calendar();
                }
            }
            return '';
        };
    });
