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

/* Services */

function wireServices(angularModule) {
	// Add account service
	angularModule.factory('Account', ['$rootScope', '$http', '$q',
        function ($rootScope, $http, $q) {
			return {
				get: function() {
					var deferred = $q.defer();
			        $http.get(ACTIVITI.CONFIG.contextRoot + '/app/rest/account', {ignoreErrors: true})
			            .success(function (data, status, headers, config) {
			            	$rootScope.account = data;
			                deferred.resolve();
			            })
			            .error(function(data, status, headers, config) {
			              // Reject promise and broadcast login required event
			              deferred.reject(data);
			            });
			        
			        return deferred.promise;
				}
			};
		}
	]);
  
  // Add authentication factories
  angularModule.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService', '$q',
    function ($rootScope, $http, authService, $q) {
      return {
        authenticate: function() {
          var deferred = $q.defer();
          $http.get(ACTIVITI.CONFIG.contextRoot + '/app/rest/authenticate', {ignoreErrors: true, ignoreAuthModule: 'ignoreAuthModule'})
              .success(function (data, status, headers, config) {
                  $rootScope.account = data;
                  $rootScope.$broadcast('event:auth-authConfirmed');
                  
                  deferred.resolve();
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
                  ignoreAuthModule: 'ignoreAuthModule',
                  ignoreErrors: true
              }).success(function (data, status, headers, config) {
                  $rootScope.authenticationError = false;
                  
                  if(param.success){
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
                      authService.loginCancelled({isFromLogout: true});
                  });
          }
      };
  }]);
  
  // Add login controller
  angularModule.controller('LoginController', ['$scope', '$location', 'AuthenticationSharedService',
     function ($scope, $location, AuthenticationSharedService) {
         $scope.status = {
             loading: false
         };
         
         $scope.login = function () {
           $scope.status.loading = true;
           
             AuthenticationSharedService.login({
                 username: $scope.username,
                 password: $scope.password,
                 success: function () {
                   $scope.status.loading = false;
                 },
                 error: function() {
                   $scope.status.loading = false;
                 }
             });
         };
     }]);

  // Add logout controller
  angularModule.controller('LogoutController', ['$location', 'AuthenticationSharedService','$cookieStore',
     function ($location, AuthenticationSharedService, $cookieStore) {
         AuthenticationSharedService.logout();
     }]);
}

