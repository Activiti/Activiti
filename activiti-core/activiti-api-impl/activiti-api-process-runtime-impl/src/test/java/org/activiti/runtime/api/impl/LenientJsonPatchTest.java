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

import static org.activiti.runtime.api.impl.LenientJsonPatch.MAX_ARRAY_INDEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flipkart.zjsonpatch.InvalidJsonPatchException;
import com.flipkart.zjsonpatch.JsonPatchTestFailedException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.exc.JsonNodeException;
import tools.jackson.databind.json.JsonMapper;

class LenientJsonPatchTest {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private final LenientJsonPatch jsonPatch = new LenientJsonPatch();

    @Test
    void should_overwriteElement_when_writingExistingIndex() {
        assertThat(apply("{'d':['A','B']}", "[{'op':'add','path':'/d/1','value':'X'}]")).isEqualTo(
            json("{'d':['A','X']}")
        );
    }

    @Test
    void should_overwriteElement_when_replacingExistingIndex() {
        assertThat(apply("{'d':['A','B']}", "[{'op':'replace','path':'/d/1','value':'X'}]")).isEqualTo(
            json("{'d':['A','X']}")
        );
    }

    @Test
    void should_fillGapsWithNull_when_writingIndexBeyondArraySize() {
        assertThat(apply("{'d':[]}", "[{'op':'add','path':'/d/3','value':'X'}]")).isEqualTo(
            json("{'d':[null,null,null,'X']}")
        );
    }

    @Test
    void should_fillGapsWithNull_when_indexIsAtTheLimit() {
        JsonNode result = apply("{'d':[]}", "[{'op':'add','path':'/d/" + MAX_ARRAY_INDEX + "','value':'X'}]");

        assertThat(result.get("d").size()).isEqualTo(MAX_ARRAY_INDEX + 1);
        assertThat(result.get("d").get(0).isNull()).isTrue();
        assertThat(result.get("d").get(MAX_ARRAY_INDEX).asString()).isEqualTo("X");
    }

    @Test
    void should_fail_when_indexIsBeyondTheLimit() {
        int index = MAX_ARRAY_INDEX + 1;

        assertThatThrownBy(() -> apply("{'d':[]}", "[{'op':'add','path':'/d/" + index + "','value':'X'}]"))
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(
                "Array index " +
                    index +
                    " in JsonPatch path '/d/" +
                    index +
                    "' is out of range: the array has 0 elements and cannot be extended beyond index " +
                    MAX_ARRAY_INDEX
            );
    }

    @Test
    void should_fail_when_indexIsNegative() {
        assertThatThrownBy(() -> apply("{'d':[]}", "[{'op':'add','path':'/d/-1','value':'X'}]"))
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage("Invalid array index '-1' in JsonPatch path '/d/-1': expected a non-negative integer");
    }

    @Test
    void should_appendElement_when_usingEndOfArrayToken() {
        assertThat(apply("{'d':['A']}", "[{'op':'add','path':'/d/-','value':'X'}]")).isEqualTo(json("{'d':['A','X']}"));
    }

    @Test
    void should_createArray_when_usingEndOfArrayTokenOnMissingParent() {
        assertThat(apply("{}", "[{'op':'add','path':'/d/-','value':'X'}]")).isEqualTo(json("{'d':['X']}"));
    }

    @Test
    void should_createArrayOfObjects_when_numericIntermediateSegmentPointsToMissingContainer() {
        assertThat(apply("{}", "[{'op':'add','path':'/items/0/name','value':'X'}]")).isEqualTo(
            json("{'items':[{'name':'X'}]}")
        );
    }

    @Test
    void should_treatNumericSegmentAsKey_when_parentIsObject() {
        assertThat(apply("{'byId':{'7':'SEVEN'}}", "[{'op':'add','path':'/byId/3','value':'THREE'}]")).isEqualTo(
            json("{'byId':{'7':'SEVEN','3':'THREE'}}")
        );
    }

    @Test
    void should_setValue_when_replacingMissingObjectKey() {
        assertThat(apply("{}", "[{'op':'replace','path':'/person/name','value':'X'}]")).isEqualTo(
            json("{'person':{'name':'X'}}")
        );
    }

    @Test
    void should_applyOperationsInOrder_when_patchWritesSeveralIndexes() {
        assertThat(
            apply("{}", "[{'op':'add','path':'/d/0','value':'X'},{'op':'add','path':'/d/1','value':'Y'}]")
        ).isEqualTo(json("{'d':['X','Y']}"));
    }

    @Test
    void should_removeElementAndShiftFollowingOnes_when_removingArrayElementByIndex() {
        assertThat(apply("{'d':['A','B','C']}", "[{'op':'remove','path':'/d/1'}]")).isEqualTo(json("{'d':['A','C']}"));
    }

    @Test
    void should_ignoreRemoval_when_pathDoesNotExist() {
        assertThat(
            apply("{'d':['A','B','C']}", "[{'op':'remove','path':'/missing'},{'op':'remove','path':'/d/7'}]")
        ).isEqualTo(json("{'d':['A','B','C']}"));
    }

    @Test
    void should_fail_when_testOperationOnArrayIndexDoesNotMatch() {
        assertThatThrownBy(() ->
            apply("{'d':['A','B','C']}", "[{'op':'test','path':'/d/1','value':'Z'}]")
        ).isInstanceOf(JsonPatchTestFailedException.class);
    }

    @Test
    void should_createMissingParent_when_copyingToMissingPath() {
        assertThat(apply("{'a':'X'}", "[{'op':'copy','from':'/a','path':'/b/c'}]")).isEqualTo(
            json("{'a':'X','b':{'c':'X'}}")
        );
    }

    @Test
    void should_fail_when_patchEntryHasNoPath() {
        assertThatThrownBy(() -> apply("{}", "[{'op':'add','value':'X'}]"))
            .isInstanceOf(InvalidJsonPatchException.class)
            .hasMessageContaining("missing 'path'");
    }

    @Test
    void should_failInsteadOfOverwritingScalar_when_intermediateSegmentIsScalar() {
        assertThatThrownBy(() -> apply("{'d':'scalar'}", "[{'op':'add','path':'/d/0','value':'X'}]"))
            .isInstanceOf(JsonNodeException.class)
            .hasMessageContaining("Cannot replace `JsonNode` of type `tools.jackson.databind.node.StringNode`")
            .hasMessageContaining("\"/d\"");
    }

    @Test
    void should_notModifyInputDocument() {
        JsonNode document = json("{'d':['A']}");

        jsonPatch.apply(document, json("[{'op':'add','path':'/d/3','value':'X'}]"));

        assertThat(document).isEqualTo(json("{'d':['A']}"));
    }

    private JsonNode apply(String document, String patch) {
        return jsonPatch.apply(json(document), json(patch));
    }

    private static JsonNode json(String json) {
        return JSON_MAPPER.readTree(json.replace('\'', '"'));
    }
}
