/*
 Copyright (c) 2019 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package org.activiti.engine.impl.util.json;

import java.util.HashMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class CookieTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void escapeShouldReturnInputValue() {
        //when
        String value = "foo";

        //then
        assertThat(Cookie.escape(value)).isEqualTo(value);
    }

    @Test
    public void escapeShouldEncodeAdditionSymbolToURLEncoding() {
        //when
        String value = "+foo";

        //then
        assertThat(Cookie.escape(value)).isEqualTo("%2bfoo");
    }

    @Test
    public void escapeShouldEncodePercentageSymbolToURLEncoding() {
        //when
        String value = "%foo";

        //then
        assertThat(Cookie.escape(value)).isEqualTo("%25foo");
    }

    @Test
    public void escapeShouldEncodeEqualsSymbol() {
        //when
        String value = "=foo";

        //then
        assertThat(Cookie.escape(value)).isEqualTo("%3dfoo");
    }

    @Test
    public void escapeShouldEncodeSemiColonSymbol() {
        //when
        String value = ";foo";

        //then
        assertThat(Cookie.escape(value)).isEqualTo("%3bfoo");
    }

    @Test
    public void toJSONObjectShouldConvertStringToJSONObject() {
        //when
        String value = "bar=2;expires=7/9/2019;secure";

        //then
        String retval = "{\"expires\":\"7/9/2019\",\"name\":\"bar\"," +
                "\"secure\":true,\"value\":\"2\"}";
        assertThat(Cookie.toJSONObject(value).toString()).isEqualTo(retval);
    }

    @Test
    public void toJSONObjectThrowsException() {
        //when
        String value = "bar=2;expires7/9/2019;secure";

        //then
        thrown.expect(JSONException.class);
        Cookie.toJSONObject(value);
    }

    @Test
    public void toStringShouldReturnStringOfJSONObject() {
        //when
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", "bar");
        hashMap.put("value", "2");
        hashMap.put("expires", "7/9/2019");
        hashMap.put("domain", "https://www.foo.bar");
        hashMap.put("path", "foo/bar/baz.txt");
        hashMap.put("secure", "true");

        //then
        assertThat(Cookie.toString(new JSONObject(hashMap))).isEqualTo(
                "bar=2;expires=7/9/2019;domain=" +
                        "https://www.foo.bar;path=foo/bar/baz.txt;secure");
    }

    @Test
    public void unescapeShouldReturnInputValue() {
        //when
        String value = "fo";

        //then
        assertThat(Cookie.unescape(value)).isEqualTo(value);
    }

    @Test
    public void unescapeShouldReplaceAdditionSymbolWithSpace() {
        //when
        String value = "foo+bar";

        //then
        assertThat(Cookie.unescape(value)).isEqualTo("foo bar");
    }

    @Test
    public void unescapeShouldReplacePercentageSymbolWithSingleCharacter() {
        //when
        String value = "foo%bar";

        //then
        assertThat(Cookie.unescape(value)).isEqualTo("fooºr");
    }
}
