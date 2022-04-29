/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import org.activiti.bpmn.model.AssociationDirection;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Transaction;
import org.activiti.image.exception.ActivitiImageException;
import org.activiti.image.impl.icon.BusinessRuleTaskIconType;
import org.activiti.image.impl.icon.CompensateIconType;
import org.activiti.image.impl.icon.CompensateThrowIconType;
import org.activiti.image.impl.icon.ErrorIconType;
import org.activiti.image.impl.icon.ErrorThrowIconType;
import org.activiti.image.impl.icon.IconType;
import org.activiti.image.impl.icon.ManualTaskIconType;
import org.activiti.image.impl.icon.MessageIconType;
import org.activiti.image.impl.icon.ReceiveTaskIconType;
import org.activiti.image.impl.icon.ScriptTaskIconType;
import org.activiti.image.impl.icon.SendTaskIconType;
import org.activiti.image.impl.icon.ServiceTaskIconType;
import org.activiti.image.impl.icon.SignalIconType;
import org.activiti.image.impl.icon.SignalThrowIconType;
import org.activiti.image.impl.icon.TaskIconType;
import org.activiti.image.impl.icon.TimerIconType;
import org.activiti.image.impl.icon.UserTaskIconType;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Represents a canvas on which BPMN 2.0 constructs can be drawn.
 * <p>
 * @see org.activiti.image.impl.DefaultProcessDiagramGenerator
 */
public class DefaultProcessDiagramCanvas {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultProcessDiagramCanvas.class);

    public enum SHAPE_TYPE {
        Rectangle,
        Rhombus,
        Ellipse
    }

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
    protected static Color TASK_BOX_COLOR = new Color(249,
                                                      249,
                                                      249);
    protected static Color SUBPROCESS_BOX_COLOR = new Color(255,
                                                            255,
                                                            255);
    protected static Color EVENT_COLOR = new Color(255,
                                                   255,
                                                   255);
    protected static Color CONNECTION_COLOR = new Color(88,
                                                        88,
                                                        88);
    protected static Color CONDITIONAL_INDICATOR_COLOR = new Color(255,
                                                                   255,
                                                                   255);
    protected static Color HIGHLIGHT_CURRENT_COLOR = new Color(87,
        255,
        174);
    protected static Color HIGHLIGHT_COMPLETED_ACTIVITY_COLOR = new Color(51,
        153,
        255);
    protected static Color HIGHLIGHT_ERRORED_ACTIVITY_COLOR = new Color(255,
        55,
        87);
    protected static Color LABEL_COLOR = new Color(112,
                                                   146,
                                                   190);
    protected static Color TASK_BORDER_COLOR = new Color(187,
                                                         187,
                                                         187);
    protected static Color EVENT_BORDER_COLOR = new Color(88,
                                                          88,
                                                          88);
    protected static Color SUBPROCESS_BORDER_COLOR = new Color(0,
                                                               0,
                                                               0);

    // Fonts
    protected static Font LABEL_FONT = null;
    protected static Font ANNOTATION_FONT = null;

    // Strokes
    protected static Stroke THICK_TASK_BORDER_STROKE = new BasicStroke(4.0f);
    protected static Stroke GATEWAY_TYPE_STROKE = new BasicStroke(3.0f);
    protected static Stroke END_EVENT_STROKE = new BasicStroke(3.0f);
    protected static Stroke MULTI_INSTANCE_STROKE = new BasicStroke(1.3f);
    protected static Stroke EVENT_SUBPROCESS_STROKE = new BasicStroke(1.0f,
                                                                      BasicStroke.CAP_BUTT,
                                                                      BasicStroke.JOIN_MITER,
                                                                      1.0f,
                                                                      new float[]{1.0f},
                                                                      0.0f);
    protected static Stroke NON_INTERRUPTING_EVENT_STROKE = new BasicStroke(1.0f,
                                                                            BasicStroke.CAP_BUTT,
                                                                            BasicStroke.JOIN_MITER,
                                                                            1.0f,
                                                                            new float[]{4.0f, 3.0f},
                                                                            0.0f);
    protected static Stroke HIGHLIGHT_FLOW_STROKE = new BasicStroke(2.0f);
    protected static Stroke ANNOTATION_STROKE = new BasicStroke(2.0f);
    protected static Stroke ASSOCIATION_STROKE = new BasicStroke(2.0f,
                                                                 BasicStroke.CAP_BUTT,
                                                                 BasicStroke.JOIN_MITER,
                                                                 1.0f,
                                                                 new float[]{2.0f, 2.0f},
                                                                 0.0f);

    // icons
    protected static int ICON_PADDING = 5;
    protected static TaskIconType USERTASK_IMAGE;
    protected static TaskIconType SCRIPTTASK_IMAGE;
    protected static TaskIconType SERVICETASK_IMAGE;
    protected static TaskIconType RECEIVETASK_IMAGE;
    protected static TaskIconType SENDTASK_IMAGE;
    protected static TaskIconType MANUALTASK_IMAGE;
    protected static TaskIconType BUSINESS_RULE_TASK_IMAGE;

    protected static IconType TIMER_IMAGE;
    protected static IconType COMPENSATE_THROW_IMAGE;
    protected static IconType COMPENSATE_CATCH_IMAGE;
    protected static IconType ERROR_THROW_IMAGE;
    protected static IconType ERROR_CATCH_IMAGE;
    protected static IconType MESSAGE_CATCH_IMAGE;
    protected static IconType SIGNAL_CATCH_IMAGE;
    protected static IconType SIGNAL_THROW_IMAGE;

    protected int canvasWidth = -1;
    protected int canvasHeight = -1;
    protected int minX = -1;
    protected int minY = -1;
    protected ProcessDiagramSVGGraphics2D g;
    protected FontMetrics fontMetrics;
    protected boolean closed;
    protected String activityFontName = "Arial";
    protected String labelFontName = "Arial";
    protected String annotationFontName = "Arial";

    /**
     * Creates an empty canvas with given width and height.
     * <p>
     * Allows to specify minimal boundaries on the left and upper side of the
     * canvas. This is useful for diagrams that have white space there.
     * Everything beneath these minimum values will be cropped.
     * It's also possible to pass a specific font name and a class loader for the icon images.
     */
    public DefaultProcessDiagramCanvas(int width,
                                       int height,
                                       int minX,
                                       int minY,
                                       String activityFontName,
                                       String labelFontName,
                                       String annotationFontName) {

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

        initialize();
    }

    /**
     * Creates an empty canvas with given width and height.
     * <p>
     * Allows to specify minimal boundaries on the left and upper side of the
     * canvas. This is useful for diagrams that have white space there (eg
     * Signavio). Everything beneath these minimum values will be cropped.
     * @param minX Hint that will be used when generating the image. Parts that fall
     * below minX on the horizontal scale will be cropped.
     * @param minY Hint that will be used when generating the image. Parts that fall
     * below minX on the horizontal scale will be cropped.
     */
    public DefaultProcessDiagramCanvas(int width,
                                       int height,
                                       int minX,
                                       int minY) {
        this.canvasWidth = width;
        this.canvasHeight = height;
        this.minX = minX;
        this.minY = minY;

        initialize();
    }

    public void initialize() {
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS,
                                                   "svg",
                                                   null);

        // Create an instance of the SVG Generator.
        this.g = new ProcessDiagramSVGGraphics2D(document);

        this.g.setSVGCanvasSize(new Dimension(this.canvasWidth, this.canvasHeight));

        this.g.setBackground(new Color(255,
                                       255,
                                       255,
                                       0));
        this.g.clearRect(0,
                         0,
                         canvasWidth,
                         canvasHeight);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.black);

        Font font = new Font(activityFontName,
                             Font.BOLD,
                             FONT_SIZE);
        g.setFont(font);
        this.fontMetrics = g.getFontMetrics();

        LABEL_FONT = new Font(labelFontName,
                              Font.ITALIC,
                              10);
        ANNOTATION_FONT = new Font(annotationFontName,
                                   Font.PLAIN,
                                   FONT_SIZE);

        USERTASK_IMAGE = new UserTaskIconType();
        SCRIPTTASK_IMAGE = new ScriptTaskIconType();
        SERVICETASK_IMAGE = new ServiceTaskIconType();
        RECEIVETASK_IMAGE = new ReceiveTaskIconType();
        SENDTASK_IMAGE = new SendTaskIconType();
        MANUALTASK_IMAGE = new ManualTaskIconType();
        BUSINESS_RULE_TASK_IMAGE = new BusinessRuleTaskIconType();

        TIMER_IMAGE = new TimerIconType();
        COMPENSATE_THROW_IMAGE = new CompensateThrowIconType();
        COMPENSATE_CATCH_IMAGE = new CompensateIconType();
        ERROR_THROW_IMAGE = new ErrorThrowIconType();
        ERROR_CATCH_IMAGE = new ErrorIconType();
        MESSAGE_CATCH_IMAGE = new MessageIconType();
        SIGNAL_THROW_IMAGE = new SignalThrowIconType();
        SIGNAL_CATCH_IMAGE = new SignalIconType();
    }

    /**
     * Generates an image of what currently is drawn on the canvas.
     * <p>
     * Throws an {@link ActivitiImageException} when {@link #close()} is already
     * called.
     */
    public InputStream generateImage() {
        if (closed) {
            throw new ActivitiImageException("ProcessDiagramGenerator already closed");
        }

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Writer out;
            out = new OutputStreamWriter(stream,
                                         "UTF-8");
            g.stream(out,
                     true);
            return new ByteArrayInputStream(stream.toByteArray());
        } catch (UnsupportedEncodingException | SVGGraphics2DIOException e) {
            throw new ActivitiImageException("Error while generating process image",
                                             e);
        }
    }

    /**
     * Closes the canvas which dissallows further drawing and releases graphical
     * resources.
     */
    public void close() {
        g.dispose();
        closed = true;
    }

    public void drawNoneStartEvent(String id,
                                   GraphicInfo graphicInfo) {
        drawStartEvent(id,
                       graphicInfo,
                       null);
    }

    public void drawTimerStartEvent(String id,
                                    GraphicInfo graphicInfo) {
        drawStartEvent(id,
                       graphicInfo,
                       TIMER_IMAGE);
    }

    public void drawSignalStartEvent(String id,
                                     GraphicInfo graphicInfo) {
        drawStartEvent(id,
                       graphicInfo,
                       SIGNAL_CATCH_IMAGE);
    }

    public void drawMessageStartEvent(String id,
                                      GraphicInfo graphicInfo) {
        drawStartEvent(id,
                       graphicInfo,
                       MESSAGE_CATCH_IMAGE);
    }

    public void drawStartEvent(String id,
                               GraphicInfo graphicInfo,
                               IconType icon) {
        Paint originalPaint = g.getPaint();
        g.setPaint(EVENT_COLOR);
        Ellipse2D circle = new Ellipse2D.Double(graphicInfo.getX(),
                                                graphicInfo.getY(),
                                                graphicInfo.getWidth(),
                                                graphicInfo.getHeight());
        g.fill(circle);
        g.setPaint(EVENT_BORDER_COLOR);
        g.draw(circle);
        g.setPaint(originalPaint);

        // calculate coordinates to center image
        if (icon != null) {
            int imageX = (int) Math.round(graphicInfo.getX() + (graphicInfo.getWidth() / 2) - (icon.getWidth() / 2));
            int imageY = (int) Math.round(graphicInfo.getY() + (graphicInfo.getHeight() / 2) - (icon.getHeight() / 2));

            icon.drawIcon(imageX,
                          imageY,
                          ICON_PADDING,
                          g);
        }

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawNoneEndEvent(String id,
                                 String name,
                                 GraphicInfo graphicInfo) {
        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();
        g.setPaint(EVENT_COLOR);
        Ellipse2D circle = new Ellipse2D.Double(graphicInfo.getX(),
                                                graphicInfo.getY(),
                                                graphicInfo.getWidth(),
                                                graphicInfo.getHeight());
        g.fill(circle);
        g.setPaint(EVENT_BORDER_COLOR);
        g.setStroke(END_EVENT_STROKE);
        g.draw(circle);
        g.setStroke(originalStroke);
        g.setPaint(originalPaint);

        // set element's id
        g.setCurrentGroupId(id);

        drawLabel(name,
                  graphicInfo);
    }

    public void drawErrorEndEvent(String id,
                                  String name,
                                  GraphicInfo graphicInfo) {
        drawNoneEndEvent(id,
                         name,
                         graphicInfo);

        int imageX = (int) (graphicInfo.getX() + (graphicInfo.getWidth() / 4));
        int imageY = (int) (graphicInfo.getY() + (graphicInfo.getHeight() / 4));

        ERROR_THROW_IMAGE.drawIcon(imageX,
                                   imageY,
                                   ICON_PADDING,
                                   g);
    }

    public void drawErrorStartEvent(String id,
                                    GraphicInfo graphicInfo) {
        drawNoneStartEvent(id,
                           graphicInfo);

        int imageX = (int) (graphicInfo.getX() + (graphicInfo.getWidth() / 4));
        int imageY = (int) (graphicInfo.getY() + (graphicInfo.getHeight() / 4));

        ERROR_THROW_IMAGE.drawIcon(imageX,
                                   imageY,
                                   ICON_PADDING,
                                   g);
    }

    public void drawCatchingEvent(String id,
                                  GraphicInfo graphicInfo,
                                  boolean isInterrupting,
                                  IconType icon,
                                  String eventType) {

        // event circles
        Ellipse2D outerCircle = new Ellipse2D.Double(graphicInfo.getX(),
                                                     graphicInfo.getY(),
                                                     graphicInfo.getWidth(),
                                                     graphicInfo.getHeight());
        int innerCircleSize = 4;
        int innerCircleX = (int) graphicInfo.getX() + innerCircleSize;
        int innerCircleY = (int) graphicInfo.getY() + innerCircleSize;
        int innerCircleWidth = (int) graphicInfo.getWidth() - (2 * innerCircleSize);
        int innerCircleHeight = (int) graphicInfo.getHeight() - (2 * innerCircleSize);
        Ellipse2D innerCircle = new Ellipse2D.Double(innerCircleX,
                                                     innerCircleY,
                                                     innerCircleWidth,
                                                     innerCircleHeight);

        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();
        g.setPaint(EVENT_COLOR);
        g.fill(outerCircle);

        g.setPaint(EVENT_BORDER_COLOR);
        if (!isInterrupting) {
            g.setStroke(NON_INTERRUPTING_EVENT_STROKE);
        }
        g.draw(outerCircle);
        g.setStroke(originalStroke);
        g.setPaint(originalPaint);
        g.draw(innerCircle);

        if (icon != null) {
            // calculate coordinates to center image
            int imageX = (int) (graphicInfo.getX() + (graphicInfo.getWidth() / 2) - (icon.getWidth() / 2));
            int imageY = (int) (graphicInfo.getY() + (graphicInfo.getHeight() / 2) - (icon.getHeight() / 2));
            if ("timer".equals(eventType)) {
                // move image one pixel to center timer image
                imageX++;
                imageY++;
            }
            icon.drawIcon(imageX,
                          imageY,
                          ICON_PADDING,
                          g);
        }

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawCatchingCompensateEvent(String id,
                                            String name,
                                            GraphicInfo graphicInfo,
                                            boolean isInterrupting) {
        drawCatchingCompensateEvent(id,
                                    graphicInfo,
                                    isInterrupting);
        drawLabel(name,
                  graphicInfo);
    }

    public void drawCatchingCompensateEvent(String id,
                                            GraphicInfo graphicInfo,
                                            boolean isInterrupting) {

        drawCatchingEvent(id,
                          graphicInfo,
                          isInterrupting,
                          COMPENSATE_CATCH_IMAGE,
                          "compensate");
    }

    public void drawCatchingTimerEvent(String id,
                                       String name,
                                       GraphicInfo graphicInfo,
                                       boolean isInterrupting) {
        drawCatchingTimerEvent(id,
                               graphicInfo,
                               isInterrupting);
        drawLabel(name,
                  graphicInfo);
    }

    public void drawCatchingTimerEvent(String id,
                                       GraphicInfo graphicInfo,
                                       boolean isInterrupting) {
        drawCatchingEvent(id,
                          graphicInfo,
                          isInterrupting,
                          TIMER_IMAGE,
                          "timer");
    }

    public void drawCatchingErrorEvent(String id,
                                       String name,
                                       GraphicInfo graphicInfo,
                                       boolean isInterrupting) {
        drawCatchingErrorEvent(id,
                               graphicInfo,
                               isInterrupting);
        drawLabel(name,
                  graphicInfo);
    }

    public void drawCatchingErrorEvent(String id,
                                       GraphicInfo graphicInfo,
                                       boolean isInterrupting) {

        drawCatchingEvent(id,
                          graphicInfo,
                          isInterrupting,
                          ERROR_CATCH_IMAGE,
                          "error");
    }

    public void drawCatchingSignalEvent(String id,
                                        String name,
                                        GraphicInfo graphicInfo,
                                        boolean isInterrupting) {
        drawCatchingSignalEvent(id,
                                graphicInfo,
                                isInterrupting);
        drawLabel(name,
                  graphicInfo);
    }

    public void drawCatchingSignalEvent(String id,
                                        GraphicInfo graphicInfo,
                                        boolean isInterrupting) {
        drawCatchingEvent(id,
                          graphicInfo,
                          isInterrupting,
                          SIGNAL_CATCH_IMAGE,
                          "signal");
    }

    public void drawCatchingMessageEvent(String id,
                                         GraphicInfo graphicInfo,
                                         boolean isInterrupting) {

        drawCatchingEvent(id,
                          graphicInfo,
                          isInterrupting,
                          MESSAGE_CATCH_IMAGE,
                          "message");
    }

    public void drawCatchingMessageEvent(String id,
                                         String name,
                                         GraphicInfo graphicInfo,
                                         boolean isInterrupting) {
        drawCatchingEvent(id,
                          graphicInfo,
                          isInterrupting,
                          MESSAGE_CATCH_IMAGE,
                          "message");

        drawLabel(name,
                  graphicInfo);
    }

    public void drawThrowingCompensateEvent(String id,
                                            GraphicInfo graphicInfo) {
        drawCatchingEvent(id,
                          graphicInfo,
                          true,
                          COMPENSATE_THROW_IMAGE,
                          "compensate");
    }

    public void drawThrowingSignalEvent(String id,
                                        GraphicInfo graphicInfo) {
        drawCatchingEvent(id,
                          graphicInfo,
                          true,
                          SIGNAL_THROW_IMAGE,
                          "signal");
    }

    public void drawThrowingNoneEvent(String id,
                                      GraphicInfo graphicInfo) {
        drawCatchingEvent(id,
                          graphicInfo,
                          true,
                          null,
                          "none");
    }

    public void drawSequenceflow(int srcX,
                                 int srcY,
                                 int targetX,
                                 int targetY,
                                 boolean conditional) {
        drawSequenceflow(srcX,
                         srcY,
                         targetX,
                         targetY,
                         conditional,
                         false);
    }

    public void drawSequenceflow(int srcX,
                                 int srcY,
                                 int targetX,
                                 int targetY,
                                 boolean conditional,
                                 boolean highLighted) {
        Paint originalPaint = g.getPaint();
        if (highLighted) {
            g.setPaint(HIGHLIGHT_CURRENT_COLOR);
        }

        Line2D.Double line = new Line2D.Double(srcX,
                                               srcY,
                                               targetX,
                                               targetY);
        g.draw(line);
        drawArrowHead(line);

        if (conditional) {
            drawConditionalSequenceFlowIndicator(line);
        }

        if (highLighted) {
            g.setPaint(originalPaint);
        }
    }

    public void drawAssociation(int[] xPoints,
                                int[] yPoints,
                                AssociationDirection associationDirection,
                                boolean highLighted) {
        boolean conditional = false;
        boolean isDefault = false;
        drawConnection(xPoints,
                       yPoints,
                       conditional,
                       isDefault,
                       "association",
                       associationDirection,
                       highLighted);
    }

    public void drawSequenceflow(int[] xPoints,
                                 int[] yPoints,
                                 boolean conditional,
                                 boolean isDefault,
                                 boolean highLighted) {
        drawConnection(xPoints,
                       yPoints,
                       conditional,
                       isDefault,
                       "sequenceFlow",
                       AssociationDirection.ONE,
                       highLighted);
    }

    public void drawConnection(int[] xPoints,
                               int[] yPoints,
                               boolean conditional,
                               boolean isDefault,
                               String connectionType,
                               AssociationDirection associationDirection,
                               boolean highLighted) {

        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();

        g.setPaint(CONNECTION_COLOR);
        if ("association".equals(connectionType)) {
            g.setStroke(ASSOCIATION_STROKE);
        } else if (highLighted) {
            g.setPaint(HIGHLIGHT_COMPLETED_ACTIVITY_COLOR);
            g.setStroke(HIGHLIGHT_FLOW_STROKE);
        }

        for (int i = 1; i < xPoints.length; i++) {
            Integer sourceX = xPoints[i - 1];
            Integer sourceY = yPoints[i - 1];
            Integer targetX = xPoints[i];
            Integer targetY = yPoints[i];
            Line2D.Double line = new Line2D.Double(sourceX,
                                                   sourceY,
                                                   targetX,
                                                   targetY);
            g.draw(line);
        }

        if (isDefault) {
            Line2D.Double line = new Line2D.Double(xPoints[0],
                                                   yPoints[0],
                                                   xPoints[1],
                                                   yPoints[1]);
            drawDefaultSequenceFlowIndicator(line);
        }

        if (conditional) {
            Line2D.Double line = new Line2D.Double(xPoints[0],
                                                   yPoints[0],
                                                   xPoints[1],
                                                   yPoints[1]);
            drawConditionalSequenceFlowIndicator(line);
        }

        if (associationDirection.equals(AssociationDirection.ONE) || associationDirection.equals(AssociationDirection.BOTH)) {
            Line2D.Double line = new Line2D.Double(xPoints[xPoints.length - 2],
                                                   yPoints[xPoints.length - 2],
                                                   xPoints[xPoints.length - 1],
                                                   yPoints[xPoints.length - 1]);
            drawArrowHead(line);
        }
        if (associationDirection.equals(AssociationDirection.BOTH)) {
            Line2D.Double line = new Line2D.Double(xPoints[1],
                                                   yPoints[1],
                                                   xPoints[0],
                                                   yPoints[0]);
            drawArrowHead(line);
        }
        g.setPaint(originalPaint);
        g.setStroke(originalStroke);
    }

    public void drawSequenceflowWithoutArrow(int srcX,
                                             int srcY,
                                             int targetX,
                                             int targetY,
                                             boolean conditional) {
        drawSequenceflowWithoutArrow(srcX,
                                     srcY,
                                     targetX,
                                     targetY,
                                     conditional,
                                     false);
    }

    public void drawSequenceflowWithoutArrow(int srcX,
                                             int srcY,
                                             int targetX,
                                             int targetY,
                                             boolean conditional,
                                             boolean highLighted) {
        Paint originalPaint = g.getPaint();
        if (highLighted) {
            g.setPaint(HIGHLIGHT_CURRENT_COLOR);
        }

        Line2D.Double line = new Line2D.Double(srcX,
                                               srcY,
                                               targetX,
                                               targetY);
        g.draw(line);

        if (conditional) {
            drawConditionalSequenceFlowIndicator(line);
        }

        if (highLighted) {
            g.setPaint(originalPaint);
        }
    }

    public void drawArrowHead(Line2D.Double line) {
        int doubleArrowWidth = (int) (2 * ARROW_WIDTH);
        if (doubleArrowWidth == 0) {
            doubleArrowWidth = 2;
        }
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0,
                           0);
        int arrowHeadPoint = (int) (-ARROW_WIDTH);
        if (arrowHeadPoint == 0) {
            arrowHeadPoint = -1;
        }
        arrowHead.addPoint(arrowHeadPoint,
                           -doubleArrowWidth);
        arrowHeadPoint = (int) (ARROW_WIDTH);
        if (arrowHeadPoint == 0) {
            arrowHeadPoint = 1;
        }
        arrowHead.addPoint(arrowHeadPoint,
                           -doubleArrowWidth);

        AffineTransform transformation = new AffineTransform();
        transformation.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1,
                                  line.x2 - line.x1);
        transformation.translate(line.x2,
                                 line.y2);
        transformation.rotate((angle - Math.PI / 2d));

        AffineTransform originalTransformation = g.getTransform();
        g.setTransform(transformation);
        g.fill(arrowHead);
        g.setTransform(originalTransformation);
    }

    public void drawDefaultSequenceFlowIndicator(Line2D.Double line) {
        double length = DEFAULT_INDICATOR_WIDTH;
        double halfOfLength = length / 2;
        double f = 8;
        Line2D.Double defaultIndicator = new Line2D.Double(-halfOfLength,
                                                           0,
                                                           halfOfLength,
                                                           0);

        double angle = Math.atan2(line.y2 - line.y1,
                                  line.x2 - line.x1);
        double dx = f * Math.cos(angle);
        double dy = f * Math.sin(angle);
        double x1 = line.x1 + dx;
        double y1 = line.y1 + dy;

        AffineTransform transformation = new AffineTransform();
        transformation.setToIdentity();
        transformation.translate(x1,
                                 y1);
        transformation.rotate((angle - 3 * Math.PI / 4));

        AffineTransform originalTransformation = g.getTransform();
        g.setTransform(transformation);
        g.draw(defaultIndicator);

        g.setTransform(originalTransformation);
    }

    public void drawConditionalSequenceFlowIndicator(Line2D.Double line) {
        int horizontal = (int) (CONDITIONAL_INDICATOR_WIDTH * 0.7);
        int halfOfHorizontal = horizontal / 2;
        int halfOfVertical = CONDITIONAL_INDICATOR_WIDTH / 2;

        Polygon conditionalIndicator = new Polygon();
        conditionalIndicator.addPoint(0,
                                      0);
        conditionalIndicator.addPoint(-halfOfHorizontal,
                                      halfOfVertical);
        conditionalIndicator.addPoint(0,
                                      CONDITIONAL_INDICATOR_WIDTH);
        conditionalIndicator.addPoint(halfOfHorizontal,
                                      halfOfVertical);

        AffineTransform transformation = new AffineTransform();
        transformation.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1,
                                  line.x2 - line.x1);
        transformation.translate(line.x1,
                                 line.y1);
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

    public void drawTask(TaskIconType icon,
                         String id,
                         String name,
                         GraphicInfo graphicInfo) {
        drawTask(id,
                 name,
                 graphicInfo);

        icon.drawIcon((int) graphicInfo.getX(),
                      (int) graphicInfo.getY(),
                      ICON_PADDING,
                      g);
    }

    public void drawTask(String id,
                         String name,
                         GraphicInfo graphicInfo) {
        drawTask(id,
                 name,
                 graphicInfo,
                 false);
    }

    public void drawPoolOrLane(String id,
                               String name,
                               GraphicInfo graphicInfo) {
        int x = (int) graphicInfo.getX();
        int y = (int) graphicInfo.getY();
        int width = (int) graphicInfo.getWidth();
        int height = (int) graphicInfo.getHeight();
        g.drawRect(x,
                   y,
                   width,
                   height);

        // Add the name as text, vertical
        if (name != null && name.length() > 0) {
            // Include some padding
            int availableTextSpace = height - 6;

            // Create rotation for derived font
            AffineTransform transformation = new AffineTransform();
            transformation.setToIdentity();
            transformation.rotate(270 * Math.PI / 180);

            Font currentFont = g.getFont();
            Font theDerivedFont = currentFont.deriveFont(transformation);
            g.setFont(theDerivedFont);

            String truncated = fitTextToWidth(name,
                                              availableTextSpace);
            int realWidth = fontMetrics.stringWidth(truncated);

            g.drawString(truncated,
                         x + 2 + fontMetrics.getHeight(),
                         3 + y + availableTextSpace - (availableTextSpace - realWidth) / 2);
            g.setFont(currentFont);
        }

        // set element's id
        g.setCurrentGroupId(id);
    }

    protected void drawTask(String id,
                            String name,
                            GraphicInfo graphicInfo,
                            boolean thickBorder) {
        Paint originalPaint = g.getPaint();
        int x = (int) graphicInfo.getX();
        int y = (int) graphicInfo.getY();
        int width = (int) graphicInfo.getWidth();
        int height = (int) graphicInfo.getHeight();

        // Create a new gradient paint for every task box, gradient depends on x and y and is not relative
        g.setPaint(TASK_BOX_COLOR);

        int arcR = 6;
        if (thickBorder) {
            arcR = 3;
        }

        // shape
        RoundRectangle2D rect = new RoundRectangle2D.Double(x,
                                                            y,
                                                            width,
                                                            height,
                                                            arcR,
                                                            arcR);
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
            int boxX = x + width / 2 - boxWidth / 2;
            int boxY = y + height / 2 - boxHeight / 2 + ICON_PADDING + ICON_PADDING - 2 - 2;

            drawMultilineCentredText(name,
                                     boxX,
                                     boxY,
                                     boxWidth,
                                     boxHeight);
        }

        // set element's id
        g.setCurrentGroupId(id);
    }

    protected void drawMultilineCentredText(String text,
                                            int x,
                                            int y,
                                            int boxWidth,
                                            int boxHeight) {
        drawMultilineText(text,
                          x,
                          y,
                          boxWidth,
                          boxHeight,
                          true);
    }

    protected void drawMultilineAnnotationText(String text,
                                               int x,
                                               int y,
                                               int boxWidth,
                                               int boxHeight) {
        drawMultilineText(text,
                          x,
                          y,
                          boxWidth,
                          boxHeight,
                          false);
    }

    protected void drawMultilineText(String text,
                                     int x,
                                     int y,
                                     int boxWidth,
                                     int boxHeight,
                                     boolean centered) {
        // Create an attributed string based in input text
        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FONT,
                                      g.getFont());
        attributedString.addAttribute(TextAttribute.FOREGROUND,
                                      Color.black);

        AttributedCharacterIterator characterIterator = attributedString.getIterator();

        int currentHeight = 0;
        // Prepare a list of lines of text we'll be drawing
        List<TextLayout> layouts = new ArrayList<TextLayout>();
        String lastLine = null;

        LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator,
                                                           g.getFontRenderContext());

        TextLayout layout = null;
        while (measurer.getPosition() < characterIterator.getEndIndex() && currentHeight <= boxHeight) {

            int previousPosition = measurer.getPosition();

            // Request next layout
            layout = measurer.nextLayout(boxWidth);

            int height = ((Float) (layout.getDescent() + layout.getAscent() + layout.getLeading())).intValue();

            if (currentHeight + height > boxHeight) {
                // The line we're about to add should NOT be added anymore, append three dots to previous one instead
                // to indicate more text is truncated
                if (!layouts.isEmpty()) {
                    layouts.remove(layouts.size() - 1);

                    if (lastLine.length() >= 4) {
                        lastLine = lastLine.substring(0,
                                                      lastLine.length() - 4) + "...";
                    }
                    layouts.add(new TextLayout(lastLine,
                                               g.getFont(),
                                               g.getFontRenderContext()));
                } else {
                    // at least, draw one line
                    // even if text does not fit
                    // in order to avoid empty box
                    layouts.add(layout);
                    currentHeight += height;
                }
                break;
            } else {
                layouts.add(layout);
                lastLine = text.substring(previousPosition,
                                          measurer.getPosition());
                currentHeight += height;
            }
        }

        int currentY = y + (centered ? ((boxHeight - currentHeight) / 2) : 0);
        int currentX = 0;

        // Actually draw the lines
        for (TextLayout textLayout : layouts) {

            currentY += textLayout.getAscent();
            currentX = x + (centered ? ((boxWidth - ((Double) textLayout.getBounds().getWidth()).intValue()) / 2) : 0);

            textLayout.draw(g,
                            currentX,
                            currentY);
            currentY += textLayout.getDescent() + textLayout.getLeading();
        }
    }

    protected String fitTextToWidth(String original,
                                    int width) {
        String text = original;

        // remove length for "..."
        int maxWidth = width - 10;

        while (fontMetrics.stringWidth(text + "...") > maxWidth && text.length() > 0) {
            text = text.substring(0,
                                  text.length() - 1);
        }

        if (!text.equals(original)) {
            text = text + "...";
        }

        return text;
    }

    public void drawUserTask(String id,
                             String name,
                             GraphicInfo graphicInfo) {
        drawTask(USERTASK_IMAGE,
                 id,
                 name,
                 graphicInfo);
    }

    public void drawScriptTask(String id,
                               String name,
                               GraphicInfo graphicInfo) {
        drawTask(SCRIPTTASK_IMAGE,
                 id,
                 name,
                 graphicInfo);
    }

    public void drawServiceTask(String id,
                                String name,
                                GraphicInfo graphicInfo) {
        drawTask(SERVICETASK_IMAGE,
                 id,
                 name,
                 graphicInfo);
    }

    public void drawReceiveTask(String id,
                                String name,
                                GraphicInfo graphicInfo) {
        drawTask(RECEIVETASK_IMAGE,
                 id,
                 name,
                 graphicInfo);
    }

    public void drawSendTask(String id,
                             String name,
                             GraphicInfo graphicInfo) {
        drawTask(SENDTASK_IMAGE,
                 id,
                 name,
                 graphicInfo);
    }

    public void drawManualTask(String id,
                               String name,
                               GraphicInfo graphicInfo) {
        drawTask(MANUALTASK_IMAGE,
                 id,
                 name,
                 graphicInfo);
    }

    public void drawBusinessRuleTask(String id,
                                     String name,
                                     GraphicInfo graphicInfo) {
        drawTask(BUSINESS_RULE_TASK_IMAGE,
                 id,
                 name,
                 graphicInfo);
    }

    public void drawExpandedSubProcess(String id,
                                       String name,
                                       GraphicInfo graphicInfo,
                                       Class<?> type) {
        RoundRectangle2D rect = new RoundRectangle2D.Double(graphicInfo.getX(),
                                                            graphicInfo.getY(),
                                                            graphicInfo.getWidth(),
                                                            graphicInfo.getHeight(),
                                                            8,
                                                            8);

        if (type.equals(EventSubProcess.class)) {
            Stroke originalStroke = g.getStroke();
            g.setStroke(EVENT_SUBPROCESS_STROKE);
            g.draw(rect);
            g.setStroke(originalStroke);
        } else if (type.equals(Transaction.class)) {
            RoundRectangle2D outerRect = new RoundRectangle2D.Double(graphicInfo.getX()-3,
                    graphicInfo.getY()-3,
                    graphicInfo.getWidth()+6,
                    graphicInfo.getHeight()+6,
                    8,
                    8);

            Paint originalPaint = g.getPaint();
            g.setPaint(SUBPROCESS_BOX_COLOR);
            g.fill(outerRect);
            g.setPaint(SUBPROCESS_BORDER_COLOR);
            g.draw(outerRect);
            g.setPaint(SUBPROCESS_BOX_COLOR);
            g.fill(rect);
            g.setPaint(SUBPROCESS_BORDER_COLOR);
            g.draw(rect);
            g.setPaint(originalPaint);
        } else {
            Paint originalPaint = g.getPaint();
            g.setPaint(SUBPROCESS_BOX_COLOR);
            g.fill(rect);
            g.setPaint(SUBPROCESS_BORDER_COLOR);
            g.draw(rect);
            g.setPaint(originalPaint);
        }

        if (name != null && !name.isEmpty()) {
            String text = fitTextToWidth(name,
                                         (int) graphicInfo.getWidth());
            g.drawString(text,
                         (int) graphicInfo.getX() + 10,
                         (int) graphicInfo.getY() + 15);
        }

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawCollapsedSubProcess(String id,
                                        String name,
                                        GraphicInfo graphicInfo,
                                        Boolean isTriggeredByEvent) {
        drawCollapsedTask(id,
                          name,
                          graphicInfo,
                          false);
    }

    public void drawCollapsedCallActivity(String id,
                                          String name,
                                          GraphicInfo graphicInfo) {
        drawCollapsedTask(id,
                          name,
                          graphicInfo,
                          true);
    }

    protected void drawCollapsedTask(String id,
                                     String name,
                                     GraphicInfo graphicInfo,
                                     boolean thickBorder) {
        // The collapsed marker is now visualized separately
        drawTask(id,
                 name,
                 graphicInfo,
                 thickBorder);
    }

    public void drawCollapsedMarker(int x,
                                    int y,
                                    int width,
                                    int height) {
        // rectangle
        int rectangleWidth = MARKER_WIDTH;
        int rectangleHeight = MARKER_WIDTH;
        Rectangle rect = new Rectangle(x + (width - rectangleWidth) / 2,
                                       y + height - rectangleHeight - 3,
                                       rectangleWidth,
                                       rectangleHeight);
        g.draw(rect);

        // plus inside rectangle
        Line2D.Double line = new Line2D.Double(rect.getCenterX(),
                                               rect.getY() + 2,
                                               rect.getCenterX(),
                                               rect.getMaxY() - 2);
        g.draw(line);
        line = new Line2D.Double(rect.getMinX() + 2,
                                 rect.getCenterY(),
                                 rect.getMaxX() - 2,
                                 rect.getCenterY());
        g.draw(line);
    }

    public void drawActivityMarkers(int x,
                                    int y,
                                    int width,
                                    int height,
                                    boolean multiInstanceSequential,
                                    boolean multiInstanceParallel,
                                    boolean collapsed) {
        if (collapsed) {
            if (!multiInstanceSequential && !multiInstanceParallel) {
                drawCollapsedMarker(x,
                                    y,
                                    width,
                                    height);
            } else {
                drawCollapsedMarker(x - MARKER_WIDTH / 2 - 2,
                                    y,
                                    width,
                                    height);
                if (multiInstanceSequential) {
                    drawMultiInstanceMarker(true,
                                            x + MARKER_WIDTH / 2 + 2,
                                            y,
                                            width,
                                            height);
                } else {
                    drawMultiInstanceMarker(false,
                                            x + MARKER_WIDTH / 2 + 2,
                                            y,
                                            width,
                                            height);
                }
            }
        } else {
            if (multiInstanceSequential) {
                drawMultiInstanceMarker(true,
                                        x,
                                        y,
                                        width,
                                        height);
            } else if (multiInstanceParallel) {
                drawMultiInstanceMarker(false,
                                        x,
                                        y,
                                        width,
                                        height);
            }
        }
    }

    public void drawGateway(GraphicInfo graphicInfo) {
        Polygon rhombus = new Polygon();
        int x = (int) graphicInfo.getX();
        int y = (int) graphicInfo.getY();
        int width = (int) graphicInfo.getWidth();
        int height = (int) graphicInfo.getHeight();

        rhombus.addPoint(x,
                         y + (height / 2));
        rhombus.addPoint(x + (width / 2),
                         y + height);
        rhombus.addPoint(x + width,
                         y + (height / 2));
        rhombus.addPoint(x + (width / 2),
                         y);
        g.draw(rhombus);
    }

    public void drawGatewayHighLight(GraphicInfo graphicInfo, Color color) {
        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();
        g.setPaint(color);
        g.setStroke(THICK_TASK_BORDER_STROKE);

        drawGateway(graphicInfo);

        g.setPaint(originalPaint);
        g.setStroke(originalStroke);
    }

    public void drawGatewayHighLightCompleted(GraphicInfo graphicInfo) {
        drawGatewayHighLight(graphicInfo, HIGHLIGHT_COMPLETED_ACTIVITY_COLOR);
    }

    public void drawGatewayHighLightErrored(GraphicInfo graphicInfo) {
        drawGatewayHighLight(graphicInfo, HIGHLIGHT_ERRORED_ACTIVITY_COLOR);
    }

    public void drawParallelGateway(String id,
                                    GraphicInfo graphicInfo) {
        // rhombus
        drawGateway(graphicInfo);
        int x = (int) graphicInfo.getX();
        int y = (int) graphicInfo.getY();
        int width = (int) graphicInfo.getWidth();
        int height = (int) graphicInfo.getHeight();

        // plus inside rhombus
        Stroke orginalStroke = g.getStroke();
        g.setStroke(GATEWAY_TYPE_STROKE);
        Line2D.Double line = new Line2D.Double(x + 10,
                                               y + height / 2,
                                               x + width - 10,
                                               y + height / 2); // horizontal
        g.draw(line);
        line = new Line2D.Double(x + width / 2,
                                 y + height - 10,
                                 x + width / 2,
                                 y + 10); // vertical
        g.draw(line);
        g.setStroke(orginalStroke);

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawExclusiveGateway(String id,
                                     GraphicInfo graphicInfo) {
        // rhombus
        drawGateway(graphicInfo);
        int x = (int) graphicInfo.getX();
        int y = (int) graphicInfo.getY();
        int width = (int) graphicInfo.getWidth();
        int height = (int) graphicInfo.getHeight();

        int quarterWidth = width / 4;
        int quarterHeight = height / 4;

        // X inside rhombus
        Stroke orginalStroke = g.getStroke();
        g.setStroke(GATEWAY_TYPE_STROKE);
        Line2D.Double line = new Line2D.Double(x + quarterWidth + 3,
                                               y + quarterHeight + 3,
                                               x + 3 * quarterWidth - 3,
                                               y + 3 * quarterHeight - 3);
        g.draw(line);
        line = new Line2D.Double(x + quarterWidth + 3,
                                 y + 3 * quarterHeight - 3,
                                 x + 3 * quarterWidth - 3,
                                 y + quarterHeight + 3);
        g.draw(line);
        g.setStroke(orginalStroke);

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawInclusiveGateway(String id,
                                     GraphicInfo graphicInfo) {
        // rhombus
        drawGateway(graphicInfo);
        int x = (int) graphicInfo.getX();
        int y = (int) graphicInfo.getY();
        int width = (int) graphicInfo.getWidth();
        int height = (int) graphicInfo.getHeight();

        int diameter = width / 2;

        // circle inside rhombus
        Stroke orginalStroke = g.getStroke();
        g.setStroke(GATEWAY_TYPE_STROKE);
        Ellipse2D.Double circle = new Ellipse2D.Double(((width - diameter) / 2) + x,
                                                       ((height - diameter) / 2) + y,
                                                       diameter,
                                                       diameter);
        g.draw(circle);
        g.setStroke(orginalStroke);

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawEventBasedGateway(String id,
                                      GraphicInfo graphicInfo) {
        // rhombus
        drawGateway(graphicInfo);

        int x = (int) graphicInfo.getX();
        int y = (int) graphicInfo.getY();
        int width = (int) graphicInfo.getWidth();
        int height = (int) graphicInfo.getHeight();

        double scale = .6;

        GraphicInfo eventInfo = new GraphicInfo();
        eventInfo.setX(x + width * (1 - scale) / 2);
        eventInfo.setY(y + height * (1 - scale) / 2);
        eventInfo.setWidth(width * scale);
        eventInfo.setHeight(height * scale);
        drawCatchingEvent(null,
                          eventInfo,
                          true,
                          null,
                          "eventGateway");

        double r = width / 6.;

        // create pentagon (coords with respect to center)
        int topX = (int) (.95 * r); // top right corner
        int topY = (int) (-.31 * r);
        int bottomX = (int) (.59 * r); // bottom right corner
        int bottomY = (int) (.81 * r);

        int[] xPoints = new int[]{0, topX, bottomX, -bottomX, -topX};
        int[] yPoints = new int[]{-(int) r, topY, bottomY, bottomY, topY};
        Polygon pentagon = new Polygon(xPoints,
                                       yPoints,
                                       5);
        pentagon.translate(x + width / 2,
                           y + width / 2);

        // draw
        g.drawPolygon(pentagon);

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawMultiInstanceMarker(boolean sequential,
                                        int x,
                                        int y,
                                        int width,
                                        int height) {
        int rectangleWidth = MARKER_WIDTH;
        int rectangleHeight = MARKER_WIDTH;
        int lineX = x + (width - rectangleWidth) / 2;
        int lineY = y + height - rectangleHeight - 3;

        Stroke orginalStroke = g.getStroke();
        g.setStroke(MULTI_INSTANCE_STROKE);

        if (sequential) {
            g.draw(new Line2D.Double(lineX,
                                     lineY,
                                     lineX + rectangleWidth,
                                     lineY));
            g.draw(new Line2D.Double(lineX,
                                     lineY + rectangleHeight / 2,
                                     lineX + rectangleWidth,
                                     lineY + rectangleHeight / 2));
            g.draw(new Line2D.Double(lineX,
                                     lineY + rectangleHeight,
                                     lineX + rectangleWidth,
                                     lineY + rectangleHeight));
        } else {
            g.draw(new Line2D.Double(lineX,
                                     lineY,
                                     lineX,
                                     lineY + rectangleHeight));
            g.draw(new Line2D.Double(lineX + rectangleWidth / 2,
                                     lineY,
                                     lineX + rectangleWidth / 2,
                                     lineY + rectangleHeight));
            g.draw(new Line2D.Double(lineX + rectangleWidth,
                                     lineY,
                                     lineX + rectangleWidth,
                                     lineY + rectangleHeight));
        }

        g.setStroke(orginalStroke);
    }

    public void drawHighLightCurrent(GraphicInfo graphicInfo) {
        drawHighLight(graphicInfo, HIGHLIGHT_CURRENT_COLOR);
    }

    public void drawHighLightCompleted(GraphicInfo graphicInfo) {
        drawHighLight(graphicInfo, HIGHLIGHT_COMPLETED_ACTIVITY_COLOR);
    }

    public void drawHighLightErrored(GraphicInfo graphicInfo) {
        drawHighLight(graphicInfo, HIGHLIGHT_ERRORED_ACTIVITY_COLOR);
    }
    public void drawHighLight(GraphicInfo graphicInfo,
        Color color) {
        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();

        g.setPaint(color);
        g.setStroke(THICK_TASK_BORDER_STROKE);

        RoundRectangle2D rect = new RoundRectangle2D.Double((int) graphicInfo.getX(),
            (int) graphicInfo.getY(),
            (int) graphicInfo.getWidth(),
            (int) graphicInfo.getHeight(),
            6,
            6);
        g.draw(rect);

        g.setPaint(originalPaint);
        g.setStroke(originalStroke);
    }

    public void drawEventHighLight(GraphicInfo graphicInfo, Color color) {
        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();

        g.setPaint(color);
        g.setStroke(THICK_TASK_BORDER_STROKE);

        Ellipse2D circle = new Ellipse2D.Double((int) graphicInfo.getX(),
            (int) graphicInfo.getY(),
            (int) graphicInfo.getWidth(),
            (int) graphicInfo.getHeight());

        g.draw(circle);

        g.setPaint(originalPaint);
        g.setStroke(originalStroke);
    }

    public void drawEventHighLightCompleted(GraphicInfo graphicInfo) {
        drawEventHighLight(graphicInfo, HIGHLIGHT_COMPLETED_ACTIVITY_COLOR);
    }

    public void drawEventHighLightErrored(GraphicInfo graphicInfo) {
        drawEventHighLight(graphicInfo, HIGHLIGHT_ERRORED_ACTIVITY_COLOR);
    }

    public void drawTextAnnotation(String id,
                                   String text,
                                   GraphicInfo graphicInfo) {
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
        path.moveTo(x + lineLength,
                    y);
        path.lineTo(x,
                    y);
        path.lineTo(x,
                    y + height);
        path.lineTo(x + lineLength,
                    y + height);

        path.lineTo(x + lineLength,
                    y + height - 1);
        path.lineTo(x + 1,
                    y + height - 1);
        path.lineTo(x + 1,
                    y + 1);
        path.lineTo(x + lineLength,
                    y + 1);
        path.closePath();

        g.draw(path);

        int boxWidth = width - (2 * ANNOTATION_TEXT_PADDING);
        int boxHeight = height - (2 * ANNOTATION_TEXT_PADDING);
        int boxX = x + width / 2 - boxWidth / 2;
        int boxY = y + height / 2 - boxHeight / 2;

        if (text != null && !text.isEmpty()) {
            drawMultilineAnnotationText(text,
                                        boxX,
                                        boxY,
                                        boxWidth,
                                        boxHeight);
        }

        // restore originals
        g.setFont(originalFont);
        g.setStroke(originalStroke);

        // set element's id
        g.setCurrentGroupId(id);
    }

    public void drawLabel(String text,
                          GraphicInfo graphicInfo) {
        drawLabel(text,
                  graphicInfo,
                  true);
    }

    public void drawLabel(String text,
                          GraphicInfo graphicInfo,
                          boolean centered) {
        float interline = 1.0f;

        // text
        if (text != null && text.length() > 0) {
            Paint originalPaint = g.getPaint();
            Font originalFont = g.getFont();

            g.setPaint(LABEL_COLOR);
            g.setFont(LABEL_FONT);

            int wrapWidth = 100;
            int textY = (int) graphicInfo.getY();

            // TODO: use drawMultilineText()
            AttributedString as = new AttributedString(text);
            as.addAttribute(TextAttribute.FOREGROUND,
                            g.getPaint());
            as.addAttribute(TextAttribute.FONT,
                            g.getFont());
            AttributedCharacterIterator aci = as.getIterator();
            FontRenderContext frc = new FontRenderContext(null,
                                                          true,
                                                          false);
            LineBreakMeasurer lbm = new LineBreakMeasurer(aci,
                                                          frc);

            while (lbm.getPosition() < text.length()) {
                TextLayout tl = lbm.nextLayout(wrapWidth);
                textY += tl.getAscent();
                Rectangle2D bb = tl.getBounds();
                double tX = graphicInfo.getX();
                if (centered) {
                    tX += (int) (graphicInfo.getWidth() / 2 - bb.getWidth() / 2);
                }
                tl.draw(g,
                        (float) tX,
                        textY);
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
     */
    public List<GraphicInfo> connectionPerfectionizer(SHAPE_TYPE sourceShapeType,
                                                      SHAPE_TYPE targetShapeType,
                                                      GraphicInfo sourceGraphicInfo,
                                                      GraphicInfo targetGraphicInfo,
                                                      List<GraphicInfo> graphicInfoList) {
        Shape shapeFirst = createShape(sourceShapeType,
                                       sourceGraphicInfo);
        Shape shapeLast = createShape(targetShapeType,
                                      targetGraphicInfo);

        if (graphicInfoList != null && graphicInfoList.size() > 0) {
            GraphicInfo graphicInfoFirst = graphicInfoList.get(0);
            GraphicInfo graphicInfoLast = graphicInfoList.get(graphicInfoList.size() - 1);
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
                Line2D.Double lineFirst = new Line2D.Double(graphicInfoFirst.getX(),
                                                            graphicInfoFirst.getY(),
                                                            graphicInfoList.get(1).getX(),
                                                            graphicInfoList.get(1).getY());
                p = getIntersection(shapeFirst,
                                    lineFirst);
                if (p != null) {
                    graphicInfoFirst.setX(p.getX());
                    graphicInfoFirst.setY(p.getY());
                }
            }

            if (shapeLast != null) {
                Line2D.Double lineLast = new Line2D.Double(graphicInfoLast.getX(),
                                                           graphicInfoLast.getY(),
                                                           graphicInfoList.get(graphicInfoList.size() - 2).getX(),
                                                           graphicInfoList.get(graphicInfoList.size() - 2).getY());
                p = getIntersection(shapeLast,
                                    lineLast);
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
    private static Shape createShape(SHAPE_TYPE shapeType,
                                     GraphicInfo graphicInfo) {
        if (SHAPE_TYPE.Rectangle.equals(shapeType)) {
            // source is rectangle
            return new Rectangle2D.Double(graphicInfo.getX(),
                                          graphicInfo.getY(),
                                          graphicInfo.getWidth(),
                                          graphicInfo.getHeight());
        } else if (SHAPE_TYPE.Rhombus.equals(shapeType)) {
            // source is rhombus
            Path2D.Double rhombus = new Path2D.Double();
            rhombus.moveTo(graphicInfo.getX(),
                           graphicInfo.getY() + graphicInfo.getHeight() / 2);
            rhombus.lineTo(graphicInfo.getX() + graphicInfo.getWidth() / 2,
                           graphicInfo.getY() + graphicInfo.getHeight());
            rhombus.lineTo(graphicInfo.getX() + graphicInfo.getWidth(),
                           graphicInfo.getY() + graphicInfo.getHeight() / 2);
            rhombus.lineTo(graphicInfo.getX() + graphicInfo.getWidth() / 2,
                           graphicInfo.getY());
            rhombus.lineTo(graphicInfo.getX(),
                           graphicInfo.getY() + graphicInfo.getHeight() / 2);
            rhombus.closePath();
            return rhombus;
        } else if (SHAPE_TYPE.Ellipse.equals(shapeType)) {
            // source is ellipse
            return new Ellipse2D.Double(graphicInfo.getX(),
                                        graphicInfo.getY(),
                                        graphicInfo.getWidth(),
                                        graphicInfo.getHeight());
        }
        // unknown source element, just do not correct coordinates
        return null;
    }

    /**
     * This method returns intersection point of shape border and line.
     * @param shape
     * @param line
     * @return Point
     */
    private static Point getIntersection(Shape shape,
                                         Line2D.Double line) {
        if (shape instanceof Ellipse2D) {
            return getEllipseIntersection(shape,
                                          line);
        } else if (shape instanceof Rectangle2D || shape instanceof Path2D) {
            return getShapeIntersection(shape,
                                        line);
        } else {
            // something strange
            return null;
        }
    }

    /**
     * This method calculates ellipse intersection with line
     * @param shape Bounds of this shape used to calculate parameters of inscribed into this bounds ellipse.
     * @param line
     * @return Intersection point
     */
    private static Point getEllipseIntersection(Shape shape,
                                                Line2D.Double line) {
        double angle = Math.atan2(line.y2 - line.y1,
                                  line.x2 - line.x1);
        double x = shape.getBounds2D().getWidth() / 2 * Math.cos(angle) + shape.getBounds2D().getCenterX();
        double y = shape.getBounds2D().getHeight() / 2 * Math.sin(angle) + shape.getBounds2D().getCenterY();
        Point p = new Point();
        p.setLocation(x,
                      y);
        return p;
    }

    /**
     * This method calculates shape intersection with line.
     * @param shape
     * @param line
     * @return Intersection point
     */
    private static Point getShapeIntersection(Shape shape,
                                              Line2D.Double line) {
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
                    l = new Line2D.Double(pos[0],
                                          pos[1],
                                          coords[0],
                                          coords[1]);
                    if (line.intersectsLine(l)) {
                        return getLinesIntersection(line,
                                                    l);
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
    private static Point getLinesIntersection(Line2D a,
                                              Line2D b) {
        double d = (a.getX1() - a.getX2()) * (b.getY2() - b.getY1()) - (a.getY1() - a.getY2()) * (b.getX2() - b.getX1());
        double da = (a.getX1() - b.getX1()) * (b.getY2() - b.getY1()) - (a.getY1() - b.getY1()) * (b.getX2() - b.getX1());
        double ta = da / d;
        Point p = new Point();
        p.setLocation(a.getX1() + ta * (a.getX2() - a.getX1()),
                      a.getY1() + ta * (a.getY2() - a.getY1()));
        return p;
    }
}
