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
package org.activiti.editor.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;

/**
 * @author Tijs Rademakers
 */
public class ImageStreamSource extends StreamResource {

	private static final long serialVersionUID = 1L;

	public ImageStreamSource(StreamSource streamSource, Application application) {
		super(streamSource, null, application);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String filename = "myfilename-" + df.format(new Date()) + ".png";
		setFilename(filename);
		setCacheTime(0l);
	}

}
