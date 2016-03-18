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

angular.module('activitiModeler').controller('ActivitiSignalRefCtrl', [ '$scope', function($scope) {

    // Find the parent shape on which the signal definitions are defined
    var signalDefinitionsProperty = undefined;
    var parent = $scope.selectedShape;
    while (parent !== null && parent !== undefined && signalDefinitionsProperty === undefined) {
        if (parent.properties && parent.properties['oryx-signaldefinitions']) {
            signalDefinitionsProperty = parent.properties['oryx-signaldefinitions'];
        } else {
            parent = parent.parent;
        }
    }

    try {
        signalDefinitionsProperty = JSON.parse(signalDefinitionsProperty);
        if (typeof signalDefinitionsProperty == 'string') {
            signalDefinitionsProperty = JSON.parse(signalDefinitionsProperty);
        }
    } catch (err) {
        // Do nothing here, just to be sure we try-catch it
    }

    $scope.signalDefinitions = signalDefinitionsProperty;


    $scope.signalChanged = function() {
    	$scope.updatePropertyInModel($scope.property);
    };
}]);