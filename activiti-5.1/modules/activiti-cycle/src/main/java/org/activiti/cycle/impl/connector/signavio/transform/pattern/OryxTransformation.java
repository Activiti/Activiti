package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import java.util.logging.Logger;

import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformation;
import org.json.JSONException;
import org.json.JSONObject;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;
import org.oryxeditor.server.diagram.JSONBuilder;

public abstract class OryxTransformation implements JsonTransformation {

  protected Logger log = Logger.getLogger(this.getClass().getName());
  
	public JSONObject transform(JSONObject json) throws JSONException {
		Diagram diagram = DiagramBuilder.parseJson(json);
		diagram = transform(diagram);
		return JSONBuilder.parseModel(diagram);
	}

	public abstract Diagram transform(Diagram diagram);

}
