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
package org.activiti.cycle.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author christian.lipphardt@camunda.com
 */
public class IoUtils {
	
	private static final int BUFFERSIZE = 4096;
	
	public static byte[] readBytes(InputStream in) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("InputStream cannot be null");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copyBytes(in, out);
		return out.toByteArray();
	}

	public static int copyBytes(InputStream in, OutputStream out) throws IOException {
		if (in == null || out == null) {
			throw new IllegalArgumentException("In/OutStream cannot be null");
		}

		int total = 0;
		byte[] buffer = new byte[BUFFERSIZE];
		for (int bytesRead; (bytesRead = in.read(buffer)) != -1;) {
			out.write(buffer, 0, bytesRead);
			total += bytesRead;
		}
		return total;
	}
	
	public static String readText(InputStream in) throws IOException {
		return new String(readBytes(in));
	}
}
