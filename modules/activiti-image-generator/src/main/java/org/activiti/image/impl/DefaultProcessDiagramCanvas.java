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

package org.activiti.image.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.activiti.bpmn.model.AssociationDirection;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.image.exception.ActivitiImageException;
import org.activiti.image.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a canvas on which BPMN 2.0 constructs can be drawn.
 * 
 * Some of the icons used are licensed under a Creative Commons Attribution 2.5
 * License, see http://www.famfamfam.com/lab/icons/silk/
 * 
 * @see org.activiti.engine.impl.bpmn.diagram.DefaultProcessDiagramGenerator
 * @author Joram Barrez
 */
public class DefaultProcessDiagramCanvas {

  protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultProcessDiagramCanvas.class);
  public enum SHAPE_TYPE {Rectangle, Rhombus, Ellipse}

  // Predefined sized
  protected static final int ARROW_WIDTH = 5;
  protected static final int CONDITIONAL_INDICATOR_WIDTH = 16;
  protected static final int DEFAULT_INDICATOR_WIDTH = 10;
  protected static final int MARKER_WIDTH = 12;
  protected static final int FONT_SIZE = 11;
  protected static final int FONT_SPACING = 2;
  protected static final int TEXT_PADDING = 3;
  protected static final int ANNOTATION_TEXT_PADDING = 7;
  protected static final int LINE_HEIGHT = FONT_SIZE + FONT_SPACING;
  

  // Colors
  protected static Color TASK_BOX_COLOR = new Color(249, 249, 249);
  protected static Color SUBPROCESS_BOX_COLOR = new Color(255, 255, 255);
  protected static Color EVENT_COLOR = new Color(255, 255, 255);
  protected static Color CONNECTION_COLOR = new Color(88, 88, 88);
  protected static Color CONDITIONAL_INDICATOR_COLOR = new Color(255, 255, 255);
  protected static Color HIGHLIGHT_COLOR = Color.RED;
  protected static Color LABEL_COLOR = new Color(112, 146, 190);
  protected static Color TASK_BORDER_COLOR = new Color(187, 187, 187);
  protected static Color EVENT_BORDER_COLOR = new Color(88, 88, 88);
  protected static Color SUBPROCESS_BORDER_COLOR = new Color(0, 0, 0);
  
  // Fonts
  protected static Font LABEL_FONT = null;
  protected static Font ANNOTATION_FONT = null;

  // Strokes
  protected static Stroke THICK_TASK_BORDER_STROKE = new BasicStroke(3.0f);
  protected static Stroke GATEWAY_TYPE_STROKE = new BasicStroke(3.0f);
  protected static Stroke END_EVENT_STROKE = new BasicStroke(3.0f);
  protected static Stroke MULTI_INSTANCE_STROKE = new BasicStroke(1.3f);
  protected static Stroke EVENT_SUBPROCESS_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f,  new float[] { 1.0f }, 0.0f);
  protected static Stroke NON_INTERRUPTING_EVENT_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f,  new float[] { 4.0f, 3.0f }, 0.0f);
  protected static Stroke HIGHLIGHT_FLOW_STROKE = new BasicStroke(1.3f);
  protected static Stroke ANNOTATION_STROKE = new BasicStroke(2.0f);
  protected static Stroke ASSOCIATION_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f,  new float[] { 2.0f, 2.0f }, 0.0f);

  // icons
  protected static int ICON_PADDING = 5;
  protected static BufferedImage USERTASK_IMAGE;
  protected static BufferedImage SCRIPTTASK_IMAGE;
  protected static BufferedImage SERVICETASK_IMAGE;
  protected static BufferedImage RECEIVETASK_IMAGE;
  protected static BufferedImage SENDTASK_IMAGE;
  protected static BufferedImage MANUALTASK_IMAGE;
  protected static BufferedImage BUSINESS_RULE_TASK_IMAGE;
  protected static BufferedImage SHELL_TASK_IMAGE;
  protected static BufferedImage MULE_TASK_IMAGE;
  protected static BufferedImage CAMEL_TASK_IMAGE;
  
  protected static BufferedImage TIMER_IMAGE;
  protected static BufferedImage COMPENSATE_THROW_IMAGE;
  protected static BufferedImage COMPENSATE_CATCH_IMAGE;
  protected static BufferedImage ERROR_THROW_IMAGE;
  protected static BufferedImage ERROR_CATCH_IMAGE;
  protected static BufferedImage MESSAGE_THROW_IMAGE;
  protected static BufferedImage MESSAGE_CATCH_IMAGE;
  protected static BufferedImage SIGNAL_CATCH_IMAGE;
  protected static BufferedImage SIGNAL_THROW_IMAGE;

  protected int canvasWidth = -1;
  protected int canvasHeight = -1;
  protected int minX = -1;
  protected int minY = -1;
  protected BufferedImage processDiagram;
  protected Graphics2D g;
  protected FontMetrics fontMetrics;
  protected boolean closed;
  protected ClassLoader customClassLoader;
  protected String activityFontName = "Arial";
  protected String labelFontName = "Arial";
  protected String annotationFontName = "Arial";
  
  /**
   * Creates an empty canvas with given width and height.
   * 
   * Allows to specify minimal boundaries on the left and upper side of the
   * canvas. This is useful for diagrams that have white space there.
   * Everything beneath these minimum values will be cropped. 
   * It's also possible to pass a specific font name and a class loader for the icon images.
   * 
   */
  public DefaultProcessDiagramCanvas(int width, int height, int minX, int minY, String imageType, 
      String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader) {
    
    this.canvasWidth = width;
    this.canvasHeight = height;
    this.minX = minX;
    this.minY = minY;
    if (activityFontName != null) {
      this.activityFontName = activityFontName;
    }
    if (labelFontName != null) {
      this.labelFontName = labelFontName;
    }
    if (annotationFontName != null) {
        this.annotationFontName = annotationFontName;
    }
    this.customClassLoader = customClassLoader;
    
    initialize(imageType);
  }

  /**
   * Creates an empty canvas with given width and height.
   * 
   * Allows to specify minimal boundaries on the left and upper side of the
   * canvas. This is useful for diagrams that have white space there (eg
   * Signavio). Everything beneath these minimum values will be cropped.
   * 
   * @param minX
   *          Hint that will be used when generating the image. Parts that fall
   *          below minX on the horizontal scale will be cropped.
   * @param minY
   *          Hint that will be used when generating the image. Parts that fall
   *          below minX on the horizontal scale will be cropped.
   */
  public DefaultProcessDiagramCanvas(int width, int height, int minX, int minY, String imageType) {
    this.canvasWidth = width;
    this.canvasHeight = height;
    this.minX = minX;
    this.minY = minY;
    
    initialize(imageType);
  }
  
  public void initialize(String imageType) {
    if ("png".equalsIgnoreCase(imageType)) {
      this.processDiagram = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
    } else {
      this.processDiagram = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
    }
    
    this.g = processDiagram.createGraphics();
    if ("png".equalsIgnoreCase(imageType) == false) {
      this.g.setBackground(new Color(255, 255, 255, 0));
      this.g.clearRect(0, 0, canvasWidth, canvasHeight);
    }

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setPaint(Color.black);
    
    Font font = new Font(activityFontName, Font.BOLD, FONT_SIZE);
    g.setFont(font);
    this.fontMetrics = g.getFontMetrics();

    LABEL_FONT = new Font(labelFontName, Font.ITALIC, 10);
    ANNOTATION_FONT = new Font(annotationFontName, Font.PLAIN, FONT_SIZE);
    
    try {
      USERTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/userTask.png", customClassLoader));
      SCRIPTTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/scriptTask.png", customClassLoader));
      SERVICETASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/serviceTask.png", customClassLoader));
      RECEIVETASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/receiveTask.png", customClassLoader));
      SENDTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/sendTask.png", customClassLoader));
      MANUALTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/manualTask.png", customClassLoader));
      BUSINESS_RULE_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/businessRuleTask.png", customClassLoader));
      SHELL_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/shellTask.png", customClassLoader));
      CAMEL_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/camelTask.png", customClassLoader));
      MULE_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/muleTask.png", customClassLoader));
      
      TIMER_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/timer.png", customClassLoader));
      COMPENSATE_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/compensate-throw.png", customClassLoader));
      COMPENSATE_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/compensate.png", customClassLoader));
      ERROR_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/error-throw.png", customClassLoader));
      ERROR_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/error.png", customClassLoader));
      MESSAGE_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/message-throw.png", customClassLoader));
      MESSAGE_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/message.png", customClassLoader));
      SIGNAL_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/signal-throw.png", customClassLoader));
      SIGNAL_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/signal.png", customClassLoader));
    } catch (IOException e) {
      LOGGER.warn("Could not load image for process diagram creation: {}", e.getMessage());
    }
  }

  /**
   * Generates an image of what currently is drawn on the canvas.
   * 
   * Throws an {@link ActivitiException} when {@link #close()} is already
   * called.
   */
  public InputStream generateImage(String imageType) {
    if (closed) {
      throw new ActivitiImageException("ProcessDiagramGenerator already closed");
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      ImageIO.write(processDiagram, imageType, out);
      
    } catch (IOException e) {
      throw new ActivitiImageException("Error while generating process image", e);
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch(IOException ignore) {
        // Exception is silently ignored
      }
    }
    return new ByteArrayInputStream(out.toByteArray());
  }
  
  /**
   * Generates an image of what currently is drawn on the canvas.
   * 
   * Throws an {@link ActivitiException} when {@link #close()} is already
   * called.
   */
  public BufferedImage generateBufferedImage(String imageType) {
    if (closed) {
      throw new ActivitiImageException("ProcessDiagramGenerator already closed");
    }

    // Try to remove white space
    minX = (minX <= 5) ? 5 : minX;
    minY = (minY <= 5) ? 5 : minY;
    BufferedImage imageToSerialize = processDiagram;
    if (minX >= 0 && minY >= 0) {
      imageToSerialize = processDiagram.getSubimage(minX - 5, minY - 5, canvasWidth - minX + 5, canvasHeight - minY + 5);
    }
    return imageToSerialize;
  }

  /**
   * Closes the canvas which dissallows further drawing and releases graphical
   * resources.
   */
  public void close() {
    g.dispose();
    closed = true;
  }

  public void drawNoneStartEvent(GraphicInfo graphicInfo) {
    drawStartEvent(graphicInfo, null, 1.0);
  }

  public void drawTimerStartEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawStartEvent(graphicInfo, TIMER_IMAGE, scaleFactor);
  }
  
  public void drawSignalStartEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawStartEvent(graphicInfo, SIGNAL_CATCH_IMAGE, scaleFactor);
  }
  
  public void drawMessageStartEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawStartEvent(graphicInfo, MESSAGE_CATCH_IMAGE, scaleFactor);
  }

  public void drawStartEvent(GraphicInfo graphicInfo, BufferedImage image, double scaleFactor) {
    Paint originalPaint = g.getPaint();
    g.setPaint(EVENT_COLOR);
    Ellipse2D circle = new Ellipse2D.Double(graphicInfo.getX(), graphicInfo.getY(), 
        graphicInfo.getWidth(), graphicInfo.getHeight());
    g.fill(circle);
    g.setPaint(EVENT_BORDER_COLOR);
    g.draw(circle);
    g.setPaint(originalPaint);
    if (image != null) {
      // calculate coordinates to center image
      int imageX = (int) Math.round(graphicInfo.getX() + (graphicInfo.getWidth() / 2) - (image.getWidth() / 2 * scaleFactor));
      int imageY = (int) Math.round(graphicInfo.getY() + (graphicInfo.getHeight() / 2) - (image.getHeight() / 2 * scaleFactor));  
      g.drawImage(image, imageX, imageY,
          (int) (image.getWidth() / scaleFactor), (int) (image.getHeight() / scaleFactor), null);
    }

  }

  public void drawNoneEndEvent(GraphicInfo graphicInfo, double scaleFactor) {
    Paint originalPaint = g.getPaint();
    Stroke originalStroke = g.getStroke();
    g.setPaint(EVENT_COLOR);
    Ellipse2D circle = new Ellipse2D.Double(graphicInfo.getX(), graphicInfo.getY(), 
        graphicInfo.getWidth(), graphicInfo.getHeight());
    g.fill(circle);
    g.setPaint(EVENT_BORDER_COLOR);
    if (scaleFactor == 1.0) {
      g.setStroke(END_EVENT_STROKE);
    } else {
      g.setStroke(new BasicStroke(2.0f));
    }
    g.draw(circle);
    g.setStroke(originalStroke);
    g.setPaint(originalPaint);
  }

  public void drawErrorEndEvent(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawErrorEndEvent(graphicInfo, scaleFactor);
    if (scaleFactor == 1.0) {
      drawLabel(name, graphicInfo);
    }
  }
  
  public void drawErrorEndEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawNoneEndEvent(graphicInfo, scaleFactor);
    g.drawImage(ERROR_THROW_IMAGE, (int) (graphicInfo.getX() + (graphicInfo.getWidth() / 4)), 
        (int) (graphicInfo.getY() + (graphicInfo.getHeight() / 4)), 
        (int) (ERROR_THROW_IMAGE.getWidth() / scaleFactor), 
        (int) (ERROR_THROW_IMAGE.getHeight() / scaleFactor), null);
  }
  
  public void drawErrorStartEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawNoneStartEvent(graphicInfo);
    g.drawImage(ERROR_CATCH_IMAGE, (int) (graphicInfo.getX() + (graphicInfo.getWidth() / 4)), 
        (int) (graphicInfo.getY() + (graphicInfo.getHeight() / 4)), 
        (int) (ERROR_CATCH_IMAGE.getWidth() / scaleFactor), 
        (int) (ERROR_CATCH_IMAGE.getHeight() / scaleFactor), null);
  }

  public void drawCatchingEvent(GraphicInfo graphicInfo, boolean isInterrupting, 
      BufferedImage image, String eventType, double scaleFactor) {
    
    // event circles
    Ellipse2D outerCircle = new Ellipse2D.Double(graphicInfo.getX(), graphicInfo.getY(), 
        graphicInfo.getWidth(), graphicInfo.getHeight());
    int innerCircleSize = (int) (4 / scaleFactor);
    if (innerCircleSize == 0) {
      innerCircleSize = 1;
    }
    int innerCircleX = (int) graphicInfo.getX() + innerCircleSize;
    int innerCircleY = (int) graphicInfo.getY() + innerCircleSize;
    int innerCircleWidth = (int) graphicInfo.getWidth() - (2 * innerCircleSize);
    int innerCircleHeight = (int) graphicInfo.getHeight() - (2 * innerCircleSize);
    Ellipse2D innerCircle = new Ellipse2D.Double(innerCircleX, innerCircleY, innerCircleWidth, innerCircleHeight);

    Paint originalPaint = g.getPaint();
    Stroke originalStroke = g.getStroke();
    g.setPaint(EVENT_COLOR);
    g.fill(outerCircle);

    g.setPaint(EVENT_BORDER_COLOR);
    if (isInterrupting == false) 
      g.setStroke(NON_INTERRUPTING_EVENT_STROKE);
    g.draw(outerCircle);
    g.setStroke(originalStroke);
    g.setPaint(originalPaint);
    g.draw(innerCircle);

    if (image != null) {
      // calculate coordinates to center image
      int imageX = (int) (graphicInfo.getX() + (graphicInfo.getWidth() / 2) - (image.getWidth() / 2 * scaleFactor));
      int imageY = (int) (graphicInfo.getY() + (graphicInfo.getHeight() / 2) - (image.getHeight() / 2 * scaleFactor));  
      if (scaleFactor == 1.0 && "timer".equals(eventType)) {
        // move image one pixel to center timer image
        imageX++;
        imageY++;
      }
      g.drawImage(image, imageX, imageY, (int) (image.getWidth() / scaleFactor), 
          (int) (image.getHeight() / scaleFactor), null);
    }
  }

  public void drawCatchingCompensateEvent(String name, GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingCompensateEvent(graphicInfo, isInterrupting, scaleFactor);
    drawLabel(name, graphicInfo);
  }

  public void drawCatchingCompensateEvent(GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingEvent(graphicInfo, isInterrupting, COMPENSATE_CATCH_IMAGE, "compensate", scaleFactor);
  }

  public void drawCatchingTimerEvent(String name, GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingTimerEvent(graphicInfo, isInterrupting, scaleFactor);
    drawLabel(name, graphicInfo);
  }

  public void drawCatchingTimerEvent(GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingEvent(graphicInfo, isInterrupting, TIMER_IMAGE, "timer", scaleFactor);
  }

  public void drawCatchingErrorEvent(String name, GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingErrorEvent(graphicInfo, isInterrupting, scaleFactor);
    drawLabel(name, graphicInfo);
  }

  public void drawCatchingErrorEvent(GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingEvent(graphicInfo, isInterrupting, ERROR_CATCH_IMAGE, "error", scaleFactor);
  }

  public void drawCatchingSignalEvent(String name, GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingSignalEvent(graphicInfo, isInterrupting, scaleFactor);
    drawLabel(name, graphicInfo);
  }

  public void drawCatchingSignalEvent(GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingEvent(graphicInfo, isInterrupting, SIGNAL_CATCH_IMAGE, "signal", scaleFactor);
  }
  
  public void drawCatchingMessageEvent(GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingEvent(graphicInfo, isInterrupting, MESSAGE_CATCH_IMAGE, "message", scaleFactor);
  }

  public void drawCatchingMessageEvent(String name, GraphicInfo graphicInfo, boolean isInterrupting, double scaleFactor) {
    drawCatchingEvent(graphicInfo, isInterrupting, MESSAGE_CATCH_IMAGE, "message", scaleFactor);
    drawLabel(name, graphicInfo);
  }
  
  public void drawThrowingCompensateEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawCatchingEvent(graphicInfo, true, COMPENSATE_THROW_IMAGE, "compensate", scaleFactor);
  }

  public void drawThrowingSignalEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawCatchingEvent(graphicInfo, true, SIGNAL_THROW_IMAGE, "signal", scaleFactor);
  }
  
  public void drawThrowingNoneEvent(GraphicInfo graphicInfo, double scaleFactor) {
    drawCatchingEvent(graphicInfo, true, null, "none", scaleFactor);
  }

  public void drawSequenceflow(int srcX, int srcY, int targetX, int targetY, boolean conditional, double scaleFactor) {
    drawSequenceflow(srcX, srcY, targetX, targetY, conditional, false, scaleFactor);
  }
  
  public void drawSequenceflow(int srcX, int srcY, int targetX, int targetY, boolean conditional, boolean highLighted, double scaleFactor) {
    Paint originalPaint = g.getPaint();
    if (highLighted)
      g.setPaint(HIGHLIGHT_COLOR);

    Line2D.Double line = new Line2D.Double(srcX, srcY, targetX, targetY);
    g.draw(line);
    drawArrowHead(line, scaleFactor);

    if (conditional) {
      drawConditionalSequenceFlowIndicator(line, scaleFactor);
    }

    if (highLighted)
      g.setPaint(originalPaint);
  }

  public void drawAssociation(int[] xPoints, int[] yPoints, AssociationDirection associationDirection, boolean highLighted, double scaleFactor) {
    boolean conditional = false, isDefault = false;
    drawConnection(xPoints, yPoints, conditional, isDefault, "association", associationDirection, highLighted, scaleFactor);
  }

  public void drawSequenceflow(int[] xPoints, int[] yPoints, boolean conditional, boolean isDefault, boolean highLighted, double scaleFactor) {
	  drawConnection(xPoints, yPoints, conditional, isDefault, "sequenceFlow", AssociationDirection.ONE, highLighted, scaleFactor);
  }
  
  public void drawConnection(int[] xPoints, int[] yPoints, boolean conditional, boolean isDefault, String connectionType, 
      AssociationDirection associationDirection, boolean highLighted, double scaleFactor) {
    
    Paint originalPaint = g.getPaint();
    Stroke originalStroke = g.getStroke();

    g.setPaint(CONNECTION_COLOR);
    if (connectionType.equals("association")) {
      g.setStroke(ASSOCIATION_STROKE);
    } else if (highLighted) {
      g.setPaint(HIGHLIGHT_COLOR);
      g.setStroke(HIGHLIGHT_FLOW_STROKE);
    }

    for (int i=1; i<xPoints.length; i++) {
      Integer sourceX = xPoints[i - 1];
      Integer sourceY = yPoints[i - 1];
      Integer targetX = xPoints[i];
      Integer targetY = yPoints[i];
      Line2D.Double line = new Line2D.Double(sourceX, sourceY, targetX, targetY);
      g.draw(line);
    }
  
    if (isDefault){
      Line2D.Double line = new Line2D.Double(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
      drawDefaultSequenceFlowIndicator(line, scaleFactor);
    }

    if (conditional) {
      Line2D.Double line = new Line2D.Double(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
      drawConditionalSequenceFlowIndicator(line, scaleFactor);
    }
  
    if (associationDirection.equals(AssociationDirection.ONE) || associationDirection.equals(AssociationDirection.BOTH)) {
      Line2D.Double line = new Line2D.Double(xPoints[xPoints.length-2], yPoints[xPoints.length-2], xPoints[xPoints.length-1], yPoints[xPoints.length-1]);
      drawArrowHead(line, scaleFactor);
    }
    if (associationDirection.equals(AssociationDirection.BOTH)) {
      Line2D.Double line = new Line2D.Double(xPoints[1], yPoints[1], xPoints[0], yPoints[0]);
      drawArrowHead(line, scaleFactor);
    }
    g.setPaint(originalPaint);
    g.setStroke(originalStroke);
  }

  public void drawSequenceflowWithoutArrow(int srcX, int srcY, int targetX, int targetY, boolean conditional, double scaleFactor) {
    drawSequenceflowWithoutArrow(srcX, srcY, targetX, targetY, conditional, false, scaleFactor);
  }

  public void drawSequenceflowWithoutArrow(int srcX, int srcY, int targetX, int targetY, boolean conditional, boolean highLighted, double scaleFactor) {
    Paint originalPaint = g.getPaint();
    if (highLighted)
      g.setPaint(HIGHLIGHT_COLOR);

    Line2D.Double line = new Line2D.Double(srcX, srcY, targetX, targetY);
    g.draw(line);

    if (conditional) {
      drawConditionalSequenceFlowIndicator(line, scaleFactor);
    }

    if (highLighted)
      g.setPaint(originalPaint);
  }

  public void drawArrowHead(Line2D.Double line, double scaleFactor) {
    int doubleArrowWidth = (int) (2 * ARROW_WIDTH / scaleFactor);
    if (doubleArrowWidth == 0) {
      doubleArrowWidth = 2;
    }
    Polygon arrowHead = new Polygon();
    arrowHead.addPoint(0, 0);
    int arrowHeadPoint = (int) (-ARROW_WIDTH / scaleFactor);
    if (arrowHeadPoint == 0) {
      arrowHeadPoint = -1;
    }
    arrowHead.addPoint(arrowHeadPoint, -doubleArrowWidth);
    arrowHeadPoint = (int) (ARROW_WIDTH / scaleFactor);
    if (arrowHeadPoint == 0) {
      arrowHeadPoint = 1;
    }
    arrowHead.addPoint(arrowHeadPoint, -doubleArrowWidth);

    AffineTransform transformation = new AffineTransform();
    transformation.setToIdentity();
    double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
    transformation.translate(line.x2, line.y2);
    transformation.rotate((angle - Math.PI / 2d));

    AffineTransform originalTransformation = g.getTransform();
    g.setTransform(transformation);
    g.fill(arrowHead);
    g.setTransform(originalTransformation);
  }

  public void drawDefaultSequenceFlowIndicator(Line2D.Double line, double scaleFactor) {
    double length = DEFAULT_INDICATOR_WIDTH / scaleFactor, halfOfLength = length/2, f = 8;
    Line2D.Double defaultIndicator = new Line2D.Double(-halfOfLength, 0, halfOfLength, 0);

    double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
    double dx = f * Math.cos(angle), dy = f * Math.sin(angle),
	       x1 = line.x1 + dx, y1 = line.y1 + dy;

    AffineTransform transformation = new AffineTransform();
    transformation.setToIdentity();
    transformation.translate(x1, y1);
    transformation.rotate((angle - 3 * Math.PI / 4));

    AffineTransform originalTransformation = g.getTransform();
    g.setTransform(transformation);
    g.draw(defaultIndicator);

    g.setTransform(originalTransformation);
  }

  public void drawConditionalSequenceFlowIndicator(Line2D.Double line, double scaleFactor) {
    if (scaleFactor > 1.0) return;
    int horizontal = (int) (CONDITIONAL_INDICATOR_WIDTH * 0.7);
    int halfOfHorizontal = horizontal / 2;
    int halfOfVertical = CONDITIONAL_INDICATOR_WIDTH / 2;

    Polygon conditionalIndicator = new Polygon();
    conditionalIndicator.addPoint(0, 0);
    conditionalIndicator.addPoint(-halfOfHorizontal, halfOfVertical);
    conditionalIndicator.addPoint(0, CONDITIONAL_INDICATOR_WIDTH);
    conditionalIndicator.addPoint(halfOfHorizontal, halfOfVertical);

    AffineTransform transformation = new AffineTransform();
    transformation.setToIdentity();
    double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
    transformation.translate(line.x1, line.y1);
    transformation.rotate((angle - Math.PI / 2d));

    AffineTransform originalTransformation = g.getTransform();
    g.setTransform(transformation);
    g.draw(conditionalIndicator);

    Paint originalPaint = g.getPaint();
    g.setPaint(CONDITIONAL_INDICATOR_COLOR);
    g.fill(conditionalIndicator);

    g.setPaint(originalPaint);
    g.setTransform(originalTransformation);
  }

  public void drawTask(BufferedImage icon, String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(name, graphicInfo);
    g.drawImage(icon, (int) (graphicInfo.getX() + ICON_PADDING / scaleFactor), 
        (int) (graphicInfo.getY() + ICON_PADDING / scaleFactor), 
        (int) (icon.getWidth() / scaleFactor), (int) (icon.getHeight() / scaleFactor), null);
  }

  public void drawTask(String name, GraphicInfo graphicInfo) {
    drawTask(name, graphicInfo, false);
  }
  
  public void drawPoolOrLane(String name, GraphicInfo graphicInfo) {
    int x = (int) graphicInfo.getX();
    int y = (int) graphicInfo.getY();
    int width = (int) graphicInfo.getWidth();
    int height = (int) graphicInfo.getHeight();
    g.drawRect(x, y, width, height);
    
    // Add the name as text, vertical
    if(name != null && name.length() > 0) {
      // Include some padding
      int availableTextSpace = height - 6;

      // Create rotation for derived font
      AffineTransform transformation = new AffineTransform();
      transformation.setToIdentity();
      transformation.rotate(270 * Math.PI/180);

      Font currentFont = g.getFont();
      Font theDerivedFont = currentFont.deriveFont(transformation);
      g.setFont(theDerivedFont);
      
      String truncated = fitTextToWidth(name, availableTextSpace);
      int realWidth = fontMetrics.stringWidth(truncated);
      
      g.drawString(truncated, x + 2 + fontMetrics.getHeight(), 3 + y + availableTextSpace - (availableTextSpace - realWidth) / 2);
      g.setFont(currentFont);
    }
  }

  protected void drawTask(String name, GraphicInfo graphicInfo, boolean thickBorder) {
    Paint originalPaint = g.getPaint();
    int x = (int) graphicInfo.getX();
    int y = (int) graphicInfo.getY();
    int width = (int) graphicInfo.getWidth();
    int height = (int) graphicInfo.getHeight();
    
    // Create a new gradient paint for every task box, gradient depends on x and y and is not relative
    g.setPaint(TASK_BOX_COLOR);

    int arcR = 6;
    if (thickBorder)
    	arcR = 3;
    
    // shape
    RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcR, arcR);
    g.fill(rect);
    g.setPaint(TASK_BORDER_COLOR);

    if (thickBorder) {
      Stroke originalStroke = g.getStroke();
      g.setStroke(THICK_TASK_BORDER_STROKE);
      g.draw(rect);
      g.setStroke(originalStroke);
    } else {
      g.draw(rect);
    }

    g.setPaint(originalPaint);
    // text
    if (name != null && name.length() > 0) {
      int boxWidth = width - (2 * TEXT_PADDING);
      int boxHeight = height - 16 - ICON_PADDING - ICON_PADDING - MARKER_WIDTH - 2 - 2;
      int boxX = x + width/2 - boxWidth/2;
      int boxY = y + height/2 - boxHeight/2 + ICON_PADDING + ICON_PADDING - 2 - 2;
      
      drawMultilineCentredText(name, boxX, boxY, boxWidth, boxHeight);
    }
  }
  
  protected void drawMultilineCentredText(String text, int x, int y, int boxWidth, int boxHeight) {
    drawMultilineText(text, x, y, boxWidth, boxHeight, true);
  }

  protected void drawMultilineAnnotationText(String text, int x, int y, int boxWidth, int boxHeight) {
	  drawMultilineText(text, x, y, boxWidth, boxHeight, false);
  }
  
  protected void drawMultilineText(String text, int x, int y, int boxWidth, int boxHeight, boolean centered) {
    // Create an attributed string based in input text
    AttributedString attributedString = new AttributedString(text);
    attributedString.addAttribute(TextAttribute.FONT, g.getFont());
    attributedString.addAttribute(TextAttribute.FOREGROUND, Color.black);
    
    AttributedCharacterIterator characterIterator = attributedString.getIterator();
    
    int currentHeight = 0;
    // Prepare a list of lines of text we'll be drawing
    List<TextLayout> layouts = new ArrayList<TextLayout>();
    String lastLine = null;
    
    LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator, g.getFontRenderContext());
    
    TextLayout layout = null;
    while (measurer.getPosition() < characterIterator.getEndIndex() && currentHeight <= boxHeight) {
       
      int previousPosition = measurer.getPosition();
      
      // Request next layout
      layout = measurer.nextLayout(boxWidth);
      
      int height = ((Float)(layout.getDescent() + layout.getAscent() + layout.getLeading())).intValue();
      
      if(currentHeight + height > boxHeight) {
        // The line we're about to add should NOT be added anymore, append three dots to previous one instead
        // to indicate more text is truncated
        if (!layouts.isEmpty()) {
          layouts.remove(layouts.size() - 1);
          
          if(lastLine.length() >= 4) {
            lastLine = lastLine.substring(0, lastLine.length() - 4) + "...";
          }
          layouts.add(new TextLayout(lastLine, g.getFont(), g.getFontRenderContext()));
        }
        break;
      } else {
        layouts.add(layout);
        lastLine = text.substring(previousPosition, measurer.getPosition());
        currentHeight += height;
      }
    }
    
    
    int currentY = y + (centered ? ((boxHeight - currentHeight) /2) : 0);
    int currentX = 0;
    
    // Actually draw the lines
    for(TextLayout textLayout : layouts) {
      
      currentY += textLayout.getAscent();
      currentX = x + (centered ? ((boxWidth - ((Double)textLayout.getBounds().getWidth()).intValue()) /2) : 0);
      
      textLayout.draw(g, currentX, currentY);
      currentY += textLayout.getDescent() + textLayout.getLeading();
    }
    
  }
  

  protected String fitTextToWidth(String original, int width) {
    String text = original;

    // remove length for "..."
    int maxWidth = width - 10;

    while (fontMetrics.stringWidth(text + "...") > maxWidth && text.length() > 0) {
      text = text.substring(0, text.length() - 1);
    }

    if (!text.equals(original)) {
      text = text + "...";
    }

    return text;
  }

  public void drawUserTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(USERTASK_IMAGE, name, graphicInfo, scaleFactor);
  }

  public void drawScriptTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(SCRIPTTASK_IMAGE, name, graphicInfo, scaleFactor);
  }

  public void drawServiceTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(SERVICETASK_IMAGE, name, graphicInfo, scaleFactor);
  }

  public void drawReceiveTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(RECEIVETASK_IMAGE, name, graphicInfo, scaleFactor);
  }

  public void drawSendTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(SENDTASK_IMAGE, name, graphicInfo, scaleFactor);
  }

  public void drawManualTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(MANUALTASK_IMAGE, name, graphicInfo, scaleFactor);
  }
  
  public void drawBusinessRuleTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(BUSINESS_RULE_TASK_IMAGE, name, graphicInfo, scaleFactor);
  }
  
  public void drawCamelTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(CAMEL_TASK_IMAGE, name, graphicInfo, scaleFactor);
  }
  
  public void drawMuleTask(String name, GraphicInfo graphicInfo, double scaleFactor) {
    drawTask(MULE_TASK_IMAGE, name, graphicInfo, scaleFactor);
  }

  public void drawExpandedSubProcess(String name, GraphicInfo graphicInfo, Boolean isTriggeredByEvent, double scaleFactor) {
    RoundRectangle2D rect = new RoundRectangle2D.Double(graphicInfo.getX(), graphicInfo.getY(), 
        graphicInfo.getWidth(), graphicInfo.getHeight(), 8, 8);
    
    // Use different stroke (dashed)
    if (isTriggeredByEvent) {
      Stroke originalStroke = g.getStroke();
      g.setStroke(EVENT_SUBPROCESS_STROKE);
      g.draw(rect);
      g.setStroke(originalStroke);
    } else {
      Paint originalPaint = g.getPaint();
      g.setPaint(SUBPROCESS_BOX_COLOR);
      g.fill(rect);
      g.setPaint(SUBPROCESS_BORDER_COLOR);
      g.draw(rect);
      g.setPaint(originalPaint);
    }

    if (scaleFactor == 1.0 && name != null && !name.isEmpty()) {
      String text = fitTextToWidth(name, (int) graphicInfo.getWidth());
      g.drawString(text, (int) graphicInfo.getX() + 10, (int) graphicInfo.getY() + 15);
    }
  }

  public void drawCollapsedSubProcess(String name, GraphicInfo graphicInfo, Boolean isTriggeredByEvent) {
    drawCollapsedTask(name, graphicInfo, false);
  }

  public void drawCollapsedCallActivity(String name, GraphicInfo graphicInfo) {
    drawCollapsedTask(name, graphicInfo, true);
  }

  protected void drawCollapsedTask(String name, GraphicInfo graphicInfo, boolean thickBorder) {
    // The collapsed marker is now visualized separately
    drawTask(name, graphicInfo, thickBorder);
  }

  public void drawCollapsedMarker(int x, int y, int width, int height) {
    // rectangle
    int rectangleWidth = MARKER_WIDTH;
    int rectangleHeight = MARKER_WIDTH;
    Rectangle rect = new Rectangle(x + (width - rectangleWidth) / 2, y + height - rectangleHeight - 3, rectangleWidth, rectangleHeight);
    g.draw(rect);

    // plus inside rectangle
    Line2D.Double line = new Line2D.Double(rect.getCenterX(), rect.getY() + 2, rect.getCenterX(), rect.getMaxY() - 2);
    g.draw(line);
    line = new Line2D.Double(rect.getMinX() + 2, rect.getCenterY(), rect.getMaxX() - 2, rect.getCenterY());
    g.draw(line);
  }

  public void drawActivityMarkers(int x, int y, int width, int height, boolean multiInstanceSequential, boolean multiInstanceParallel, boolean collapsed) {
    if (collapsed) {
      if (!multiInstanceSequential && !multiInstanceParallel) {
        drawCollapsedMarker(x, y, width, height);
      } else {
        drawCollapsedMarker(x - MARKER_WIDTH / 2 - 2, y, width, height);
        if (multiInstanceSequential) {
          drawMultiInstanceMarker(true, x + MARKER_WIDTH / 2 + 2, y, width, height);
        } else {
          drawMultiInstanceMarker(false, x + MARKER_WIDTH / 2 + 2, y, width, height);
        }
      }
    } else {
      if (multiInstanceSequential) {
        drawMultiInstanceMarker(true, x, y, width, height);
      } else if (multiInstanceParallel) {
        drawMultiInstanceMarker(false, x, y, width, height);
      }
    }
  }

  public void drawGateway(GraphicInfo graphicInfo) {
    Polygon rhombus = new Polygon();
    int x = (int) graphicInfo.getX();
    int y = (int) graphicInfo.getY();
    int width = (int) graphicInfo.getWidth();
    int height = (int) graphicInfo.getHeight();
    
    rhombus.addPoint(x, y + (height / 2));
    rhombus.addPoint(x + (width / 2), y + height);
    rhombus.addPoint(x + width, y + (height / 2));
    rhombus.addPoint(x + (width / 2), y);
    g.draw(rhombus);
  }

  public void drawParallelGateway(GraphicInfo graphicInfo, double scaleFactor) {
    // rhombus
    drawGateway(graphicInfo);
    int x = (int) graphicInfo.getX();
    int y = (int) graphicInfo.getY();
    int width = (int) graphicInfo.getWidth();
    int height = (int) graphicInfo.getHeight();

    if (scaleFactor == 1.0) {
      // plus inside rhombus
      Stroke orginalStroke = g.getStroke();
      g.setStroke(GATEWAY_TYPE_STROKE);
      Line2D.Double line = new Line2D.Double(x + 10, y + height / 2, x + width - 10, y + height / 2); // horizontal
      g.draw(line);
      line = new Line2D.Double(x + width / 2, y + height - 10, x + width / 2, y + 10); // vertical
      g.draw(line);
      g.setStroke(orginalStroke);
    }
  }

  public void drawExclusiveGateway(GraphicInfo graphicInfo, double scaleFactor) {
    // rhombus
    drawGateway(graphicInfo);
    int x = (int) graphicInfo.getX();
    int y = (int) graphicInfo.getY();
    int width = (int) graphicInfo.getWidth();
    int height = (int) graphicInfo.getHeight();

    int quarterWidth = width / 4;
    int quarterHeight = height / 4;

    if (scaleFactor == 1.0) {
      // X inside rhombus
      Stroke orginalStroke = g.getStroke();
      g.setStroke(GATEWAY_TYPE_STROKE);
      Line2D.Double line = new Line2D.Double(x + quarterWidth + 3, y + quarterHeight + 3, x + 3 * quarterWidth - 3, y + 3 * quarterHeight - 3);
      g.draw(line);
      line = new Line2D.Double(x + quarterWidth + 3, y + 3 * quarterHeight - 3, x + 3 * quarterWidth - 3, y + quarterHeight + 3);
      g.draw(line);
      g.setStroke(orginalStroke);
    }
  }

  public void drawInclusiveGateway(GraphicInfo graphicInfo, double scaleFactor) {
    // rhombus
    drawGateway(graphicInfo);
    int x = (int) graphicInfo.getX();
    int y = (int) graphicInfo.getY();
    int width = (int) graphicInfo.getWidth();
    int height = (int) graphicInfo.getHeight();

    int diameter = width / 2;

    if (scaleFactor == 1.0) {
      // circle inside rhombus
      Stroke orginalStroke = g.getStroke();
      g.setStroke(GATEWAY_TYPE_STROKE);
      Ellipse2D.Double circle = new Ellipse2D.Double(((width - diameter) / 2) + x, ((height - diameter) / 2) + y, diameter, diameter);
      g.draw(circle);
      g.setStroke(orginalStroke);
    }
  }
  
  public void drawEventBasedGateway(GraphicInfo graphicInfo, double scaleFactor) {
    // rhombus
    drawGateway(graphicInfo);
    
    if (scaleFactor == 1.0) {
      int x = (int) graphicInfo.getX();
      int y = (int) graphicInfo.getY();
      int width = (int) graphicInfo.getWidth();
      int height = (int) graphicInfo.getHeight();
      
      double scale = .6;
      
      GraphicInfo eventInfo = new GraphicInfo();
      eventInfo.setX(x + width*(1-scale)/2);
      eventInfo.setY(y + height*(1-scale)/2);
      eventInfo.setWidth(width*scale);
      eventInfo.setHeight(height*scale);
      drawCatchingEvent(eventInfo, true, null, "eventGateway", scaleFactor);
      
      double r = width / 6.;
      
      // create pentagon (coords with respect to center)
      int topX = (int)(.95 * r); // top right corner
      int topY = (int)(-.31 * r);
      int bottomX = (int)(.59 * r); // bottom right corner
      int bottomY = (int)(.81 * r);
      
      int[] xPoints = new int[]{ 0, topX, bottomX, -bottomX, -topX };
      int[] yPoints = new int[]{ -(int)r, topY, bottomY, bottomY, topY };
      Polygon pentagon = new Polygon(xPoints, yPoints, 5);
      pentagon.translate(x+width/2, y+width/2);
  
      // draw
      g.drawPolygon(pentagon);
    }
  }

  public void drawMultiInstanceMarker(boolean sequential, int x, int y, int width, int height) {
    int rectangleWidth = MARKER_WIDTH;
    int rectangleHeight = MARKER_WIDTH;
    int lineX = x + (width - rectangleWidth) / 2;
    int lineY = y + height - rectangleHeight - 3;

    Stroke orginalStroke = g.getStroke();
    g.setStroke(MULTI_INSTANCE_STROKE);

    if (sequential) {
      g.draw(new Line2D.Double(lineX, lineY, lineX + rectangleWidth, lineY));
      g.draw(new Line2D.Double(lineX, lineY + rectangleHeight / 2, lineX + rectangleWidth, lineY + rectangleHeight / 2));
      g.draw(new Line2D.Double(lineX, lineY + rectangleHeight, lineX + rectangleWidth, lineY + rectangleHeight));
    } else {
      g.draw(new Line2D.Double(lineX, lineY, lineX, lineY + rectangleHeight));
      g.draw(new Line2D.Double(lineX + rectangleWidth / 2, lineY, lineX + rectangleWidth / 2, lineY + rectangleHeight));
      g.draw(new Line2D.Double(lineX + rectangleWidth, lineY, lineX + rectangleWidth, lineY + rectangleHeight));
    }

    g.setStroke(orginalStroke);
  }

  public void drawHighLight(int x, int y, int width, int height) {
    Paint originalPaint = g.getPaint();
    Stroke originalStroke = g.getStroke();

    g.setPaint(HIGHLIGHT_COLOR);
    g.setStroke(THICK_TASK_BORDER_STROKE);

    RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 20, 20);
    g.draw(rect);

    g.setPaint(originalPaint);
    g.setStroke(originalStroke);
  }

  public void drawTextAnnotation(String text, GraphicInfo graphicInfo) {
    int x = (int) graphicInfo.getX();
    int y = (int) graphicInfo.getY();
    int width = (int) graphicInfo.getWidth();
    int height = (int) graphicInfo.getHeight();
    
	  Font originalFont = g.getFont();
	  Stroke originalStroke = g.getStroke();
	  
	  g.setFont(ANNOTATION_FONT);
	  
	  Path2D path = new Path2D.Double();
	  x += .5;
	  int lineLength = 18;
	  path.moveTo(x + lineLength, y);
	  path.lineTo(x, y);
	  path.lineTo(x, y + height);
	  path.lineTo(x + lineLength, y + height);
	  
	  path.lineTo(x + lineLength, y + height -1);
	  path.lineTo(x + 1, y + height -1);
	  path.lineTo(x + 1, y + 1);
	  path.lineTo(x + lineLength, y + 1);
	  path.closePath();
	  
	  g.draw(path);
	  
	  int boxWidth = width - (2 * ANNOTATION_TEXT_PADDING);
    int boxHeight = height - (2 * ANNOTATION_TEXT_PADDING);
    int boxX = x + width/2 - boxWidth/2;
    int boxY = y + height/2 - boxHeight/2;
    
    if (text != null && text.isEmpty() == false) {
      drawMultilineAnnotationText(text, boxX, boxY, boxWidth, boxHeight);
    }
	  
	  // restore originals
    g.setFont(originalFont);
    g.setStroke(originalStroke);
  }
  
  public void drawLabel(String text, GraphicInfo graphicInfo){
	  drawLabel(text, graphicInfo, true);
  }
  public void drawLabel(String text, GraphicInfo graphicInfo, boolean centered){
	float interline = 1.0f;
	
    // text
    if (text != null && text.length()>0) {
      Paint originalPaint = g.getPaint();
      Font originalFont = g.getFont();

      g.setPaint(LABEL_COLOR);
      g.setFont(LABEL_FONT);

      int wrapWidth = 100;
      int textY = (int) graphicInfo.getY();
      
      // TODO: use drawMultilineText()
      AttributedString as = new AttributedString(text);
      as.addAttribute(TextAttribute.FOREGROUND, g.getPaint());
      as.addAttribute(TextAttribute.FONT, g.getFont());
      AttributedCharacterIterator aci = as.getIterator();
      FontRenderContext frc = new FontRenderContext(null, true, false);
      LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
      
      while (lbm.getPosition() < text.length()) {
    	  TextLayout tl = lbm.nextLayout(wrapWidth);
    	  textY += tl.getAscent();
    	  Rectangle2D bb = tl.getBounds();
    	  double tX = graphicInfo.getX();
    	  if (centered) {
    	  	tX += (int) (graphicInfo.getWidth() / 2 - bb.getWidth() / 2);
    	  }
    	  tl.draw(g, (float) tX, textY);
    	  textY += tl.getDescent() + tl.getLeading() + (interline - 1.0f) * tl.getAscent();
      }
  
      // restore originals
      g.setFont(originalFont);
      g.setPaint(originalPaint);
    }
  }

  /**
   * This method makes coordinates of connection flow better.
   * @param sourceShapeType
   * @param targetShapeType
   * @param sourceGraphicInfo
   * @param targetGraphicInfo
   * @param graphicInfoList
   * 
   */
  public List<GraphicInfo> connectionPerfectionizer(SHAPE_TYPE sourceShapeType, SHAPE_TYPE targetShapeType, GraphicInfo sourceGraphicInfo, GraphicInfo targetGraphicInfo, List<GraphicInfo> graphicInfoList) {
    Shape shapeFirst = createShape(sourceShapeType, sourceGraphicInfo);
    Shape shapeLast = createShape(targetShapeType, targetGraphicInfo);

    if (graphicInfoList != null && graphicInfoList.size() > 0) {
      GraphicInfo graphicInfoFirst = graphicInfoList.get(0);
      GraphicInfo graphicInfoLast = graphicInfoList.get(graphicInfoList.size()-1);
      if (shapeFirst != null) {
        graphicInfoFirst.setX(shapeFirst.getBounds2D().getCenterX());
        graphicInfoFirst.setY(shapeFirst.getBounds2D().getCenterY());
      }
      if (shapeLast != null) {
        graphicInfoLast.setX(shapeLast.getBounds2D().getCenterX());
        graphicInfoLast.setY(shapeLast.getBounds2D().getCenterY());
      }
  
      Point p = null;
      
      if (shapeFirst != null) {
        Line2D.Double lineFirst = new Line2D.Double(graphicInfoFirst.getX(), graphicInfoFirst.getY(), graphicInfoList.get(1).getX(), graphicInfoList.get(1).getY());
        p = getIntersection(shapeFirst, lineFirst);
        if (p != null) {
          graphicInfoFirst.setX(p.getX());
          graphicInfoFirst.setY(p.getY());
        }
      }
  
      if (shapeLast != null) {
        Line2D.Double lineLast = new Line2D.Double(graphicInfoLast.getX(), graphicInfoLast.getY(), graphicInfoList.get(graphicInfoList.size()-2).getX(), graphicInfoList.get(graphicInfoList.size()-2).getY());
        p = getIntersection(shapeLast, lineLast);
        if (p != null) {
          graphicInfoLast.setX(p.getX());
          graphicInfoLast.setY(p.getY());
        }
      }
    }

    return graphicInfoList;
  }

  /**
   * This method creates shape by type and coordinates.
   * @param shapeType
   * @param graphicInfo
   * @return Shape
   */
  private static Shape createShape(SHAPE_TYPE shapeType, GraphicInfo graphicInfo) {
    if (SHAPE_TYPE.Rectangle.equals(shapeType)) {
      // source is rectangle
      return new Rectangle2D.Double(graphicInfo.getX(), graphicInfo.getY(), graphicInfo.getWidth(), graphicInfo.getHeight());       
    } else if (SHAPE_TYPE.Rhombus.equals(shapeType)) {
      // source is rhombus
      Path2D.Double rhombus = new Path2D.Double();
      rhombus.moveTo(graphicInfo.getX(), graphicInfo.getY() + graphicInfo.getHeight() / 2);
      rhombus.lineTo(graphicInfo.getX() + graphicInfo.getWidth() / 2, graphicInfo.getY() + graphicInfo.getHeight());
      rhombus.lineTo(graphicInfo.getX() + graphicInfo.getWidth(), graphicInfo.getY() + graphicInfo.getHeight() / 2);
      rhombus.lineTo(graphicInfo.getX() + graphicInfo.getWidth() / 2, graphicInfo.getY());
      rhombus.lineTo(graphicInfo.getX(), graphicInfo.getY() + graphicInfo.getHeight() / 2);
      rhombus.closePath();
      return rhombus;
    } else if (SHAPE_TYPE.Ellipse.equals(shapeType)) {
      // source is ellipse
      return new Ellipse2D.Double(graphicInfo.getX(), graphicInfo.getY(), graphicInfo.getWidth(), graphicInfo.getHeight());
    } else {
      // unknown source element, just do not correct coordinates
    }
    return null;
  }

  /**
   * This method returns intersection point of shape border and line.
   * 
   * @param shape
   * @param line
   * @return Point
   */
  private static Point getIntersection(Shape shape, Line2D.Double line) {
    if (shape instanceof Ellipse2D) {
      return getEllipseIntersection(shape, line);
    } else if (shape instanceof Rectangle2D || shape instanceof Path2D) {
      return getShapeIntersection(shape, line);
    } else {
      // something strange
      return null;
    }
  }

    /**
     * This method calculates ellipse intersection with line
     * @param shape
     *                  Bounds of this shape used to calculate parameters of inscribed into this bounds ellipse.
     * @param line
     * @return Intersection point
     */
  private static Point getEllipseIntersection(Shape shape, Line2D.Double line) {
    double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
    double x = shape.getBounds2D().getWidth()/2 * Math.cos(angle) + shape.getBounds2D().getCenterX();
    double y = shape.getBounds2D().getHeight()/2 * Math.sin(angle) + shape.getBounds2D().getCenterY();
    Point p = new Point();
    p.setLocation(x, y);
    return p;
  }

  /**
   * This method calculates shape intersection with line.
   * 
   * @param shape
   * @param line
   * @return Intersection point
   */
  private static Point getShapeIntersection(Shape shape, Line2D.Double line) {
    PathIterator it = shape.getPathIterator(null);
    double[] coords = new double[6];
    double[] pos = new double[2];
    Line2D.Double l = new Line2D.Double();
    while (!it.isDone()) {
      int type = it.currentSegment(coords);
      switch (type) {
      case PathIterator.SEG_MOVETO:
        pos[0] = coords[0];
        pos[1] = coords[1];
        break;
      case PathIterator.SEG_LINETO:
        l = new Line2D.Double(pos[0], pos[1], coords[0], coords[1]);
        if (line.intersectsLine(l)) {
          return getLinesIntersection(line, l);
        }
        pos[0] = coords[0];
        pos[1] = coords[1];
        break;
      case PathIterator.SEG_CLOSE:
        break;
      default:
        // whatever
      }
      it.next();
    }
    return null;
  }

  /**
   * This method calculates intersections of two lines.
   * @param a Line 1
   * @param b Line 2
   * @return Intersection point
   */
  private static Point getLinesIntersection(Line2D a, Line2D b) {
    double d  = (a.getX1()-a.getX2())*(b.getY2()-b.getY1()) - (a.getY1()-a.getY2())*(b.getX2()-b.getX1());
    double da = (a.getX1()-b.getX1())*(b.getY2()-b.getY1()) - (a.getY1()-b.getY1())*(b.getX2()-b.getX1());
    // double db = (a.getX1()-a.getX2())*(a.getY1()-b.getY1()) - (a.getY1()-a.getY2())*(a.getX1()-b.getX1());
    double ta = da/d;
    // double tb = db/d;
    Point p = new Point();
    p.setLocation(a.getX1()+ta*(a.getX2()-a.getX1()), a.getY1()+ta*(a.getY2()-a.getY1()));
    return p;
  }
}
