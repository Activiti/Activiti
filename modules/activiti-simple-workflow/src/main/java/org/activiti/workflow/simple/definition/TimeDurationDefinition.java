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
package org.activiti.workflow.simple.definition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

public class TimeDurationDefinition {

	private Integer years;
	private Integer months;
	private Integer days;
	
	private Integer hours;
	private Integer minutes;
	private Integer seconds;
	
	public void setYears(Integer years) {
	  this.years = years;
  }
	
	@JsonSerialize(include=Inclusion.NON_NULL)
	public Integer getYears() {
	  return years;
  }
	
	public void setMonths(Integer months) {
	  this.months = months;
  }
	
	@JsonSerialize(include=Inclusion.NON_NULL)
	public Integer getMonths() {
	  return months;
  }
	
	public void setDays(Integer days) {
	  this.days = days;
  }
	
	@JsonSerialize(include=Inclusion.NON_NULL)
	public Integer getDays() {
	  return days;
  }
	
	public void setHours(Integer hours) {
	  this.hours = hours;
  }
	
	@JsonSerialize(include=Inclusion.NON_NULL)
	public Integer getHours() {
	  return hours;
  }
	
	@JsonSerialize(include=Inclusion.NON_NULL)
	public Integer getMinutes() {
	  return minutes;
  }
	
	public void setMinutes(Integer minutes) {
	  this.minutes = minutes;
  }
	
	@JsonSerialize(include=Inclusion.NON_NULL)
	public Integer getSeconds() {
	  return seconds;
  }
	
	public void setSeconds(Integer seconds) {
	  this.seconds = seconds;
  }
	
	public String toISO8601DurationString() {
		StringBuilder builder = new StringBuilder();
		boolean isDateDefined = years != null || months != null || days != null;
		boolean isTimeDefined = hours != null || minutes != null || seconds != null;
		
		if(isDateDefined || isTimeDefined) {
			builder.append("P");
			
			if(years != null && years > 0) {
				builder.append(years + "Y");
			}
			if(months != null && months > 0) {
				builder.append(months + "M");
			}
			if(days != null && days > 0) {
				builder.append(days + "D");
			}
			
			if(isTimeDefined) {
				builder.append("T");
				
				if(hours != null && hours > 0) {
					builder.append(hours + "H");
				}
				if(minutes != null && minutes > 0) {
					builder.append(minutes + "M");
				}
				if(seconds != null && seconds > 0) {
					builder.append(seconds + "S");
				}
			}
		}
		return builder.toString();
	}
}
