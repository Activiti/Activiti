package org.activiti.cycle.impl.connector.signavio.util;


public class MoviBuilder {
  
  private static final String MODEL_VIEWER = "modelviewer";
  private static final String MARKER_CCS = "{\"border\": \"2px solid blue\"}";
  private static final String ICON_RESOURCE = "http://bpt.hpi.uni-potsdam.de/pub/TWiki/OryxSkin/hpi.png";
  
//  private String init;
//  private String widgetModelviewer;
//  private String stencilsetStencilset;
//  private String stencilsetStencil;
//  private String modelShape;
//  private String modelNode;
//  private String modelEdge;
//  private String modelCanvas;
//  private String utilOverlay;
//  private String utilMarker;
//  private String utilAnnotation;
//  private String widgetModelnavigator;
//  private String utilShapeselect;
//  private String widgetToolbar;
//  private String widgetZoomslider;
//  private String widgetFullscreenviewer;
  
  private StringBuffer myJavaScript = new StringBuffer();

  public MoviBuilder(String modelToLoad) {
    // start javascript tag
    myJavaScript.append("<script type=\"text/javascript\">alert(\"executing javascript successfully\");");
    // declare variables
    myJavaScript.append("var " + MODEL_VIEWER + ";");
    myJavaScript.append(
      "MOVI.init(function() {" + MODEL_VIEWER + " = new MOVI.widget.ModelViewer(\"" + MODEL_VIEWER + "\"); " + MODEL_VIEWER + ".loadModel(\"" + modelToLoad + "\", { onSuccess: function(){} });},\"res/movi\",undefined,undefined);"
    );
  }
  
  public MoviBuilder createModelViewer() {
    String modelViewer = MODEL_VIEWER + " = new MOVI.widget.ModelViewer(\"" + MODEL_VIEWER + "\");";
    myJavaScript.append(modelViewer);
    
    return this;
  }
  
  public MoviBuilder loadModel(String pathToModel, String onSuccessCallbackFunction) {
    String loadModel = MODEL_VIEWER + ".loadModel(\"" + pathToModel + "\", { onSuccess: " + onSuccessCallbackFunction + " });" + "});";
    myJavaScript.append(loadModel);
    
    return this;
  }
  
  public MoviBuilder createModelNavigator() {
    // create modelNavigator
    return this;
  }
  
  public MoviBuilder createMarkerForShapeId(String markerVarName, String shapeId) {
    String marker = "var " + markerVarName + " = new MOVI.util.Marker(modelviewer.canvas.getShape(\"" + shapeId + "\")," + MARKER_CCS + ");";
    myJavaScript.append(marker);
    
    return this;
  }
  
  public MoviBuilder attachAnnotationToMarker(String markerVarName, String annotationVarName) {
    String annotation = "var " + annotationVarName + " = new MOVI.util.Annotation(" + markerVarName + ", \"&lt;p&gt;This is an annotation.&lt;/p&gt;\"); " + annotationVarName + ".show();";
    myJavaScript.append(annotation);
    
    return this;
  }
  
  public MoviBuilder attachIconToMarker(String markerVarName) {
    String icon = markerVarName + ".addIcon(\"northwest\", \"" + ICON_RESOURCE + "\");";
    myJavaScript.append(icon);
    
    return this;
  }
  
  @Override
  public String toString() {
    // end java script tag
    myJavaScript.append("</script>");
    return myJavaScript.toString();
  }
}
