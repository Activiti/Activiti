package org.activiti.cycle.impl.transform.signavio;

import org.activiti.cycle.impl.transform.JsonTransformation;
import org.json.JSONException;
import org.json.JSONObject;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;
import org.oryxeditor.server.diagram.JSONBuilder;

public abstract class OryxTransformation implements JsonTransformation {

	public JSONObject transform(JSONObject json) throws JSONException {
		Diagram diagram = DiagramBuilder.parseJson(json);
		diagram = transform(diagram);
		return JSONBuilder.parseModel(diagram);
	}

	public abstract Diagram transform(Diagram diagram);

}
