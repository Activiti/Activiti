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
activitiApp.controller('AddCommentController', ['$scope', '$location', 'AuthenticationSharedService', '$timeout',
    function ($scope, $location, AuthenticationSharedService, $timeout) {

      $scope.popupModel = {
          newComment: '',
          added: false
      };

      $scope.createComment = function() {
          $scope.popupModel.added = true;
          $scope.$emit("new-comment-confirm", $scope.popupModel.newComment);

          // Popover directive doesn't expose hiding from parent scopes, no other way
          if($scope.$$childHead) {
              $scope.$$childHead.$hide();
          }
      };

      $scope.$on('tooltip.hide', function() {
        if($scope.popupModel.added) {
            $scope.popupModel.added = false;
        } else {
            $scope.$emit("new-comment-cancelled", $scope.popupModel.newComment);
        }
      });
    }]
);