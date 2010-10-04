/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
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
package org.activiti.el.juel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.activiti.el.juel.tree.Tree;
import org.activiti.el.juel.tree.impl.Builder;


public abstract class TestCase extends junit.framework.TestCase {
	protected static final Builder BUILDER = new Builder(Builder.Feature.METHOD_INVOCATIONS);

	protected static final Tree parse(String expression) {
		return BUILDER.build(expression);
	}


	protected static byte[] serialize(Object value) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bout);
		out.writeObject(value);
		out.close();
		return bout.toByteArray();
	}
	
	protected static Object deserialize(byte[] bytes) throws Exception {
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		ObjectInput in = new ObjectInputStream(bin);
		return in.readObject();
	}
}
