package org.activiti.cycle.impl.connector.signavio.transform;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonTransformation {

	public JSONObject transform(JSONObject json) throws JSONException;
	
}
