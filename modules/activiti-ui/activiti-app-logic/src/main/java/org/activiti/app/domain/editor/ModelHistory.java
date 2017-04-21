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
package org.activiti.app.domain.editor;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="ACT_DE_MODEL_HISTORY")
public class ModelHistory extends AbstractModel {

	@Column(name="model_id")
	protected String modelId;
	
	@Column(name="removal_date")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date removalDate;
	
	public ModelHistory() {
		super();
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	
	public Date getRemovalDate() {
        return removalDate;
    }
	
	public void setRemovalDate(Date removalDate) {
        this.removalDate = removalDate;
    }
}
