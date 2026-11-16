/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.runtime.api.impl;

import com.flipkart.zjsonpatch.Jackson3JsonPatch;
import org.activiti.engine.ActivitiIllegalArgumentException;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.JsonNode.OverwriteMode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class LenientJsonPatch {

    static final int MAX_ARRAY_INDEX = JsonNodeFactory.instance.getMaxElementIndexForInsert();

    private static final String END_OF_ARRAY = "-";

    JsonNode apply(JsonNode document, JsonNode patch) {
        Jackson3JsonPatch.validate(patch);
        JsonNode result = document.deepCopy();
        for (JsonNode operation : patch) {
            JsonPointer path = JsonPointer.compile(operation.get("path").asString());
            switch (operation.get("op").asString()) {
                case "add", "replace" -> result = set(result, path, operation.get("value"));
                case "remove" -> result = result.at(path).isMissingNode() ? result : applyOne(operation, result);
                case "move", "copy" -> {
                    if (!path.matches()) {
                        parentOf(result, path);
                    }
                    result = applyOne(operation, result);
                }
                default -> result = applyOne(operation, result);
            }
        }
        return result;
    }

    private JsonNode applyOne(JsonNode operation, JsonNode document) {
        return Jackson3JsonPatch.apply(JsonNodeFactory.instance.arrayNode().add(operation), document);
    }

    private JsonNode set(JsonNode root, JsonPointer path, JsonNode value) {
        if (path.matches()) {
            return value;
        }
        JsonPointer last = path.last();
        JsonNode parent = parentOf(root, path);
        if (parent instanceof ArrayNode array) {
            setElement(array, last, value, path);
        } else {
            ((ObjectNode) parent).set(last.getMatchingProperty(), value);
        }
        return root;
    }

    private JsonNode parentOf(JsonNode root, JsonPointer path) {
        JsonPointer parentPath = path.head();
        JsonNode parent = root.at(parentPath);
        if (parent.isContainer()) {
            return parent;
        }
        return isArrayPosition(path.last())
            ? root.withArray(parentPath, OverwriteMode.NULLS, true)
            : root.withObject(parentPath, OverwriteMode.NULLS, true);
    }

    private boolean isArrayPosition(JsonPointer segment) {
        return segment.getMatchingIndex() >= 0 || END_OF_ARRAY.equals(segment.getMatchingProperty());
    }

    private void setElement(ArrayNode array, JsonPointer last, JsonNode value, JsonPointer path) {
        if (END_OF_ARRAY.equals(last.getMatchingProperty())) {
            array.add(value);
            return;
        }
        int index = last.getMatchingIndex();
        if (index < 0) {
            throw new ActivitiIllegalArgumentException(
                String.format(
                    "Invalid array index '%s' in JsonPatch path '%s': expected a non-negative integer",
                    last.getMatchingProperty(),
                    path
                )
            );
        }
        if (index >= array.size() && index > MAX_ARRAY_INDEX) {
            throw new ActivitiIllegalArgumentException(
                String.format(
                    "Array index %d in JsonPatch path '%s' is out of range: the array has %d elements and cannot be extended beyond index %d",
                    index,
                    path,
                    array.size(),
                    MAX_ARRAY_INDEX
                )
            );
        }
        while (array.size() <= index) {
            array.addNull();
        }
        array.set(index, value);
    }
}
