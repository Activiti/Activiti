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

angular.module('activitiModeler')
  .controller('FormReadonlyViewController', ['$rootScope', '$scope', '$translate', '$http', '$timeout', '$location', '$modal', '$routeParams', '$popover',
    function ($rootScope, $scope, $translate, $http, $timeout, $location, $modal, $routeParams, $popover) {

      // Main page (needed for visual indicator of current page)
      $rootScope.setMainPageById('forms');
      
      var guidSequence = 0;
      
      function setFieldDragDropAttributes (field, prefix) {
          if (!field._guid) {
              field._guid = prefix + guidSequence++;
          }
          
          if (!field._width) {
              field._width = 1;
          }
      }

      if ($routeParams.modelId) {

          var url;
          if ($routeParams.modelHistoryId) {
              url = ACTIVITI.CONFIG.contextRoot + '/app/rest/form-models/' + $routeParams.modelId
                  + '/history/' + $routeParams.modelHistoryId;
          } else {
              url = ACTIVITI.CONFIG.contextRoot + '/app/rest/form-models/' + $routeParams.modelId;
          }

          $http({method: 'GET', url: url}).
              success(function (response, status, headers, config) {
                  if (response.formDefinition.fields) {
                      for (var i = 0; i < response.formDefinition.fields.length; i++) {
                          var field = response.formDefinition.fields[i];
                          if (!field.params) {
                              field.params = {};
                          }
                          setFieldDragDropAttributes(field, 'savedField');
                      }

                      $scope.formElements = response.formDefinition.fields;
                  } else {
                      $scope.formElements = [];
                  }
            
                  $scope.formItems = $scope.formElements;
                  
                  $timeout(function () {
                      // Flip switch in timeout to start watching all form-related models
                      // after next digest cycle, to prevent first false-positive
                      $scope.formLoaded = true;
                  }, 200);
              }).
              error(function (response, status, headers, config) {
                  $scope.model.loading = false;
              });
          
      } else {
          $scope.formLoaded = true;
      }

}]);
