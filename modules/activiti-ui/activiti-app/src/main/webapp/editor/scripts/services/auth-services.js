/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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

