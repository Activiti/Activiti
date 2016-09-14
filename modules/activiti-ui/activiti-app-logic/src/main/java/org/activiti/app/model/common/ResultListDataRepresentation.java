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
package org.activiti.app.model.common;

import java.util.List;



/**
 * @author Tijs Rademakers
 */
public class ResultListDataRepresentation {
	
	protected Integer size;
	protected Long total;
	protected Integer start;
	protected List<? extends Object> data;
	
	public ResultListDataRepresentation() {}
	
	public ResultListDataRepresentation(List<? extends Object> data) {
		this.data = data;
		if (data != null) {
			size = data.size();
			total = Long.valueOf(data.size());
			start = 0;
		}
	}
	
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	public List<? extends Object> getData() {
		return data;
	}
	public void setData(List<? extends Object> data) {
		this.data = data;
	}	
}
