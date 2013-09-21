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
package org.activiti.bpmn.converter.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saeid Mirzaei
 */

public  class CommaSplitter {
	public static int isInsideExpression(int i, String st) {
		if ( i>0 && st.substring(0, i-1).indexOf("${") != -1 && i < st.length() && st.indexOf("}", i) != -1)
			return st.indexOf("}", i);
		else
			return -1;		
	}
	
	
	// split the given spring using commas if they are not inside an expression
	 public static List<String> splitCommas(String st) {
	    	List<String> result = new ArrayList<String>();
	    	int insideOffset=0; 
	    	int j=0;
	    	
	    	boolean shouldContinue;
	    	do {
	    	   j = st.indexOf(",", insideOffset);
	    	   j = j==-1?st.length():j;
	    	   int isInside = isInsideExpression(j, st);
	    	   if (isInside != -1) {
	    		   insideOffset = isInside; 
	    		   shouldContinue = insideOffset < st.length();
	    		   continue;
	    	   }
	    	   insideOffset=0;    	   
	    		   
	    	   String subStr = st.substring(0, j);
	    	   if (!subStr.isEmpty())
	    	      result.add(subStr);
	    	   shouldContinue = j < st.length();
	    	   if (j < st.length())
	    	      st = st.substring(j+1);
	    		
	    	} while (shouldContinue);	    	
	    	return result;
	    }
}
