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

var activitiApp = angular.module('activitiLanding', [
  'http-auth-interceptor',
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'mgcrea.ngStrap',
  'ngAnimate',
  'pascalprecht.translate'
]);

var activitiModule = activitiApp;
activitiApp
  // Initialize routes
  .config(['$provide', '$routeProvider', '$selectProvider', '$datepickerProvider', '$translateProvider', function ($provide, $routeProvider, $selectProvider, $datepickerProvider, $translateProvider) {

        var appName = '';
        $provide.value('appName', appName);

        var ctx = ACTIVITI.CONFIG.webContextRoot;
    var appResourceRoot = ctx + (ctx && ctx.charAt(ctx.length - 1) !== '/' ? '/' : '');
    $provide.value('appResourceRoot', appResourceRoot);

        // Override caret for bs-select directive
    angular.extend($selectProvider.defaults, {
        caretHtml: '&nbsp;<i class="icon icon-caret-down"></i>'
    });

    // Override carets for bs-datepicker directive
    angular.extend($datepickerProvider.defaults, {
        iconLeft: 'icon icon-caret-left',
        iconRight: 'icon icon-caret-right'
    });

    /*
     * Route resolver for all authenticated routes
     */
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

    /*
     * Route resolver for all unauthenticated routes
     */
    var unauthRouteResolver = ['$rootScope', function($rootScope) {
      $rootScope.authenticationChecked = true;
    }];

    $routeProvider
        .when('/login', {
            templateUrl: 'views/login.html',
            controller: 'LoginController',
            resolve: {
                verify: unauthRouteResolver
            }
        })
        .when('/', {
            templateUrl: 'views/landing.html',
            controller: 'LandingController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .otherwise({
            redirectTo: ACTIVITI.CONFIG.appDefaultRoute || '/'
        });
    
        // Initialize angular-translate
        $translateProvider.useStaticFilesLoader({
            prefix: './i18n/',
            suffix: '.json'
        })

        .registerAvailableLanguageKeys(['en'], {
            'en_*': 'en',
            'en-*': 'en'
        });


    }])
    .run(['$rootScope', function($rootScope) {
        $rootScope.$on( "$routeChangeStart", function(event, next, current) {
            if (next !== null && next !== undefined) {
                $rootScope.onLogin = next.templateUrl === 'views/login.html';
            }
        });
    }])
    .run(['$rootScope', '$timeout', '$translate', '$location', '$http', '$window', '$popover', 'appResourceRoot', 'RuntimeAppDefinitionService',
        function($rootScope, $timeout, $translate, $location, $http, $window, $popover, appResourceRoot, RuntimeAppDefinitionService) {


            $rootScope.appResourceRoot = appResourceRoot;

            // Alerts
        $rootScope.alerts = {
            queue: []
        };
        
        $rootScope.webRootUrl = function() {
            return ACTIVITI.CONFIG.webContextRoot;
        };
        
        $rootScope.restRootUrl = function() {
            return ACTIVITI.CONFIG.contextRoot;
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
     }])
     .run(['$rootScope', '$location', '$window', 'AuthenticationSharedService', '$translate', '$modal',
        function($rootScope, $location, $window, AuthenticationSharedService, $translate, $modal) {
         
        var proposedLanguage = $translate.proposedLanguage();
        if (proposedLanguage !== 'de' && proposedLanguage !== 'en' && proposedLanguage !== 'es' && proposedLanguage !== 'fr'
            && proposedLanguage !== 'it' && proposedLanguage !== 'ja') {
            
            $translate.use('en');
        }
         
        /* Auto-height */

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

        /* Capabilities */

        $rootScope.logout = function() {
            AuthenticationSharedService.logout();
        };

        // Call when the 401 response is returned by the client
        $rootScope.$on('event:auth-loginRequired', function(rejection) {
            $rootScope.authenticated = false;
            $rootScope.authenticationChecked = true;
            if (ACTIVITI.CONFIG.loginUrl) {
                $window.location.href = ACTIVITI.CONFIG.loginUrl.replace("{url}", $location.absUrl());
            }
            else {
                $location.path('/login').replace();
            }
        });

        // Call when the user is authenticated
        $rootScope.$on('event:auth-authConfirmed', function(event, data) {
        
            $rootScope.authenticated = true;
            $rootScope.authenticationChecked = true;

            var redirectUrl = $location.search().redirectUrl;
            if (redirectUrl !== null && redirectUrl !== undefined && redirectUrl.length > 0) {
                $window.location.href = redirectUrl;
            } else {
                var locationPath = $location.path();
                if (locationPath == '' || locationPath == '#' || locationPath == '/login'
                    || locationPath.indexOf('/account/activate/') >= 0 || locationPath.indexOf('/account/reset-password/') >= 0) {
                      
                    $location.path('/');
                }
            }
        });

        // Call when the user logs in
        $rootScope.$on('event:auth-loginConfirmed', function() {
            AuthenticationSharedService.authenticate();
        });

        // Call when the user logs out
        $rootScope.$on('event:auth-loginCancelled', function() {
            $rootScope.authenticated = false;
            $location.path('/login');
        });

        // Call when login fails
        $rootScope.$on('event:auth-loginFailed', function() {
            $rootScope.addAlertPromise($translate('LOGIN.MESSAGES.ERROR.AUTHENTICATION'), 'error'); 
        });

        $rootScope.backToLanding = function() {
            var baseUrl = $location.absUrl();
            var index = baseUrl.indexOf('/#');
            if (index >= 0) {
                baseUrl = baseUrl.substring(0, index);
                baseUrl += '/';
            }
            $window.location.href = baseUrl;
        };
}]);
