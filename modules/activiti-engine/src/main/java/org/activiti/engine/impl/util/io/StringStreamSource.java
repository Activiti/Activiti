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
package org.activiti.engine.impl.util.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
<<<<<<< HEAD
=======
import java.io.UnsupportedEncodingException;
>>>>>>> upstream/master


/**
 * @author Tom Baeyens
 */
public class StringStreamSource implements StreamSource {
  
  String string;
<<<<<<< HEAD
  
  public StringStreamSource(String string) {
    this.string = string;
  }

  public InputStream getInputStream() {
    return new ByteArrayInputStream(string.getBytes());
=======
  String byteArrayEncoding="utf-8";
  
  public StringStreamSource(String string) {
	    this.string = string;
	  }

  public StringStreamSource(String string, String byteArrayEncoding) {
	    this.string = string;
	    this.byteArrayEncoding = byteArrayEncoding;
	  }

  public InputStream getInputStream() {
    try
	{
		return new ByteArrayInputStream(byteArrayEncoding == null ? string.getBytes() : string.getBytes(byteArrayEncoding));
	}
	catch (UnsupportedEncodingException e)
	{
		throw new RuntimeException(e);
	}
>>>>>>> upstream/master
  }

  public String toString() {
    return "String";
  }
}
