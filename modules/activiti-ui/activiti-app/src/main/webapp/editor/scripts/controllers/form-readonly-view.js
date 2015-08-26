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
