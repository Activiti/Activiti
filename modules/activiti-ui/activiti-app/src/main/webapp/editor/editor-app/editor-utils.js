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

var EDITOR = EDITOR || {};

EDITOR.UTIL = {

    getParameterByName: function (name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    },

    /**
     * Starts at the provided start element, and walks all preceding elements in the graph.
     * Each element is tested to have a certain property and, if it has, adds this property value
     * to the return result list.
     */
    collectPropertiesFromPrecedingElements: function (startElement, propertyType) {
        var visitedElements = [];
        var collectedProperties = [];
        EDITOR.UTIL._visitElementAndCollectProperty(startElement, propertyType, visitedElements, collectedProperties);
        return collectedProperties;
    },

    /**
     * Starts at the provided start element, and walks all preceding elements in the graph.
     * Each element is tested to be a specific stencil id and, if it has, adds the element
     * to the return result list.
     */
    collectElementsFromPrecedingElements: function (startElement, stencilId) {
        var visitedElements = [];
        var collectedElements = [];

        var incomingShapesIterator = startElement.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];
                if (visitedElements.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectElement(incomingShape, stencilId, visitedElements, collectedElements);
                }
            }
        }

        return collectedElements;
    },

    collectShapes: function (shapes, stencilId) {
        var collectedShapes = [];

        if (shapes && shapes.length > 0) {
            for (var i = 0; i < shapes.length; i++) {
                EDITOR.UTIL._visitShapeAndCollectShape(shapes[i], stencilId, collectedShapes);
            }
        }

        return collectedShapes;
    },

    collectSortedElementsFromPrecedingElements: function (startElement) {
        var visitedElements = [];
        var collectedElements = [];

        var incomingShapesIterator = startElement.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];
                if (visitedElements.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectAllElement(incomingShape, visitedElements, collectedElements);
                }
            }
        }

        return collectedElements.reverse();
    },

    _visitShapeAndCollectShape: function(shape, stencilId, collectedShapes) {

        if (shape.childShapes && shape.childShapes.length > 0) {
            for (var i = 0; i < shape.childShapes.length; i++) {
                EDITOR.UTIL._visitShapeAndCollectShape(shape.childShapes[i], stencilId, collectedShapes);
            }
        }

        if (shape.stencil && shape.stencil.id === stencilId) {
            collectedShapes.push(shape);
        }
    },


    _visitElementAndCollectProperty: function (element, propertyType, visitedElementsArray, collectedProperties) {

        visitedElementsArray.push(element.id);

        var property = element.properties[propertyType]
        if (property) {
            collectedProperties.push(property);
        }

        var incomingShapesIterator = element.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];
                if (visitedElementsArray.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectProperty(incomingShape, propertyType, visitedElementsArray, collectedProperties);
                }
            }
        }
        
        var childShapesIterator = element.getChildShapes();
        if (childShapesIterator) {
            for (var i = 0; i < childShapesIterator.length; i++) {
                var childShape = childShapesIterator[i];
                if (visitedElementsArray.indexOf(childShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectProperty(childShape, propertyType, visitedElementsArray, collectedProperties);
                }
            }
        }
    },

    _visitElementAndCollectElement: function (element, stencilId, visitedElementsArray, collectedElements) {

        visitedElementsArray.push(element.id);

        var elementStencilId = element.getStencil().id();
        if (elementStencilId && elementStencilId.indexOf(stencilId) >= 0) {
            collectedElements.push(element);
        }

        var incomingShapesIterator = element.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];
                if (visitedElementsArray.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectElement(incomingShape, stencilId, visitedElementsArray, collectedElements);
                }
            }
        }
        
        var childShapesIterator = element.getChildShapes();
        if (childShapesIterator) {
            for (var i = 0; i < childShapesIterator.length; i++) {
                var childShape = childShapesIterator[i];
                if (visitedElementsArray.indexOf(childShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectElement(childShape, stencilId, visitedElementsArray, collectedElements);
                }
            }
        }
    },

    _visitElementAndCollectAllElement: function (element, visitedElementsArray, collectedElements) {

        visitedElementsArray.push(element.id);

        var elementStencilId = element.getStencil().id();
        if (elementStencilId) {
            collectedElements.push(element);
        }

        var incomingShapesIterator = element.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];

                if (visitedElementsArray.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectAllElement(incomingShape, visitedElementsArray, collectedElements);
                }
            }
        }

        var childShapesIterator = element.getChildShapes();
        if (childShapesIterator) {
            for (var i = 0; i < childShapesIterator.length; i++) {
                var childShape = childShapesIterator[i];

                if (visitedElementsArray.indexOf(childShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectAllElement(childShape, visitedElementsArray, collectedElements);
                }
            }
        }

        var parent = element.getParentShape();
        if (parent) {
            try {
                var incomingParentShapesIterator = parent.getIncomingShapes();
                if (incomingParentShapesIterator) {
                    for (var i = 0; i < incomingParentShapesIterator.length; i++) {
                        var incomingParentShape = incomingParentShapesIterator[i];

                        if (visitedElementsArray.indexOf(incomingParentShape.id) < 0) {
                            EDITOR.UTIL._visitElementAndCollectAllElement(incomingParentShape, visitedElementsArray, collectedElements);
                        }
                    }
                }
            } catch(err) {
                // start of model reached
            }
        }
    },

    /**
     * Goes up the chain of parents of the provided element.
     * When the property is encountered, its value is immediately returned.
     * If the chain of parents is completely walked through, undefined is returned.
     */
    getPropertyFromParent: function (element, propertyType) {
        if (element.parent) {
            return EDITOR.UTIL._getPropertyFromParent(element.parent, propertyType);
        } else {
            return undefined;
        }

    },

    _getPropertyFromParent: function (parentElement, propertyType) {
        var property = parentElement.properties[propertyType];
        if (property) {
            return property;
        }

        if (parentElement.parent) {
            return EDITOR.UTIL._getPropertyFromParent(parentElement.parent, propertyType);
        } else {
            return undefined;
        }
    },

    /**
     * Finds last element of given shapes and traverses up the chain and returns elements of the given type
     *
     * @param allElements
     * @param propertyType
     *
     * @returns [Array] filteredSteps of given type
     */
    extractSortedStepsOfType: function (allElements, propertyType) {

        var filteredSteps = [];
        var sortedSteps = EDITOR.UTIL.extractSortedSteps(allElements);

        if (sortedSteps && sortedSteps.length > 0) {
            for (var i = 0; i < sortedSteps.length; i++) {
                if (sortedSteps[i].stencil && sortedSteps[i].stencil.id === propertyType) {
                    filteredSteps.push(sortedSteps[i]);
                }
            }
        }

        return filteredSteps;
    },

    extractSortedSteps: function(allElements) {

        var elementsMap = {};
        var sortedSteps = [];
        if (allElements && allElements.length > 0) {
            for (var i = 0; i < allElements.length; i++) {
                elementsMap[allElements[i].resourceId] = allElements[i];
            }
            EDITOR.UTIL.getOutgoingElements(allElements[0], elementsMap, sortedSteps, true);
        }
        return sortedSteps;
    },

    getOutgoingElements: function(element, elementsMap, sortedSteps, onlySteps) {

        if (element && elementsMap) {
            if (element.outgoing && element.outgoing.length > 0) {
                for (var i = 0; i < element.outgoing.length; i++) {
                    if (onlySteps && EDITOR.UTIL._elementIsStep(element)) {
                        element.id = element.resourceId;
                        sortedSteps.push(element);
                    } else if (!onlySteps) {
                        element.id = element.resourceId;
                        sortedSteps.push(element);
                    }
                    EDITOR.UTIL.getOutgoingElements(elementsMap[element.outgoing[i].resourceId], elementsMap, sortedSteps);
                }
            } else {
                if (onlySteps && EDITOR.UTIL._elementIsStep(element)) {
                    element.id = element.resourceId;
                    sortedSteps.push(element);
                } else if (!onlySteps) {
                    element.id = element.resourceId;
                    sortedSteps.push(element);
                }
            }
        }
    },

    _elementIsStep: function(element) {
        var isStep = false;
        if (element && element.stencil) {
            if (element.stencil.id != 'SequenceFlow') {
                isStep = true;
            }
        }
        return isStep;
    },



};