/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
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

/*
 * Sequence flow order controller
 */

var KisBpmSequenceFlowOrderCtrl = [ '$scope', '$modal', '$timeout', '$translate', function($scope, $modal, $timeout, $translate) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/sequenceflow-order-popup.html?version=' + Date.now(),
        scope: $scope
    };

    $modal(opts);
}];

var KisBpmSequenceFlowOrderPopupCtrl = ['$scope', '$translate', function($scope, $translate) {

    // Find the outgoing sequence flow of the current selected shape
    var outgoingSequenceFlow = [];
    var selectedShape = $scope.selectedShape;
    if (selectedShape) {
        var outgoingNodes = selectedShape.getOutgoingShapes();
        for (var i=0; i<outgoingNodes.length; i++) {
            if (outgoingNodes[i].getStencil().title() === 'Sequence flow') {
                var targetActivity = outgoingNodes[i].getTarget();
                // We need the resourceId of a sequence flow, not the id because that will change with every editor load
                outgoingSequenceFlow.push({
                    id : outgoingNodes[i].resourceId,
                    targetTitle : targetActivity.properties['oryx-name'],
                    targetType : targetActivity.getStencil().title()
                });
            }
        }
    } else {
        console.log('Programmatic error: no selected shape found');
    }

    // Now we can apply the order which was (possibly) previously saved
    var orderedOutgoingSequenceFlow = [];
    if ($scope.property.value && $scope.property.value.sequenceFlowOrder) {

        var sequenceFlowOrderList = $scope.property.value.sequenceFlowOrder;

        // Loop the list of sequence flow that was saved  in the json model and match them with the outgoing sequence flow found above
        for (var flowIndex=0; flowIndex < sequenceFlowOrderList.length; flowIndex++) {

            // find the sequence flow in the outgoing sequence flows.

            for (var outgoingFlowIndex=0; outgoingFlowIndex < outgoingSequenceFlow.length; outgoingFlowIndex++) {
                if (outgoingSequenceFlow[outgoingFlowIndex].id === sequenceFlowOrderList[flowIndex]) {
                    orderedOutgoingSequenceFlow.push(outgoingSequenceFlow[outgoingFlowIndex]);
                    outgoingSequenceFlow.splice(outgoingFlowIndex, 1);
                    break;
                }
            }
        }

        // Now all the matching sequence flow we're removed from the outgoing sequence flow list
        // We can simply apply the remaining ones (these are new vs. the time when the values were saved to the model)
        orderedOutgoingSequenceFlow = orderedOutgoingSequenceFlow.concat(outgoingSequenceFlow);

    } else {
        orderedOutgoingSequenceFlow = outgoingSequenceFlow;
    }

    // Now we can put it on the scope
    $scope.outgoingSequenceFlow = orderedOutgoingSequenceFlow;

    // Move up click handler
    $scope.moveUp = function(index) {
        var temp = $scope.outgoingSequenceFlow[index];
        $scope.outgoingSequenceFlow[index] = $scope.outgoingSequenceFlow[index - 1];
        $scope.outgoingSequenceFlow[index - 1] = temp;
    };

    // Move down click handler
    $scope.moveDown = function(index) {
        var temp = $scope.outgoingSequenceFlow[index];
        $scope.outgoingSequenceFlow[index] = $scope.outgoingSequenceFlow[index + 1];
        $scope.outgoingSequenceFlow[index + 1] = temp;
    };

    // Save click handler
    $scope.save = function() {
        if ($scope.outgoingSequenceFlow.length > 0) {
            $scope.property.value = {};
            $scope.property.value.sequenceFlowOrder = [];

            for (var flowIndex=0; flowIndex < $scope.outgoingSequenceFlow.length; flowIndex++) {
                $scope.property.value.sequenceFlowOrder.push($scope.outgoingSequenceFlow[flowIndex].id);
            }
        } else {
            $scope.property.value = null;
        }

        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    // Cancel click handler
    $scope.cancel = function() {
        $scope.close();
    };

    // Close button handler
    $scope.close = function() {
        $scope.property.mode = 'read';
        $scope.$hide();
    };

}];