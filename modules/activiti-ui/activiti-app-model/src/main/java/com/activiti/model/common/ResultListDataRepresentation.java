/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.model.common;

import java.util.List;



/**
 * @author Tijs Rademakers
 */
public class ResultListDataRepresentation {
	
	protected Integer size;
	protected Integer total;
	protected Integer start;
	protected List<? extends AbstractRepresentation> data;
	
	public ResultListDataRepresentation() {}
	
	public ResultListDataRepresentation(List<? extends AbstractRepresentation> data) {
		this.data = data;
		if (data != null) {
			size = data.size();
			total = data.size();
			start = 0;
		}
	}
	
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	public List<? extends AbstractRepresentation> getData() {
		return data;
	}
	public void setData(List<? extends AbstractRepresentation> data) {
		this.data = data;
	}	
}
