/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.test.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.image.exception.ActivitiImageException;
import org.activiti.image.impl.DefaultProcessDiagramCanvas;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class DefaultProcessDiagramCanvasTest {

    private DefaultProcessDiagramCanvas canvas;

    @BeforeEach
    void setUp() {
        canvas = new DefaultProcessDiagramCanvas(500, 500, 0, 0, "Arial", "Arial", "Arial");
    }

    @Test
    void testInitializeCanvas() {
        assertThat(canvas).isNotNull();
    }

    @Test
    void testGenerateImage() throws Exception {
        InputStream imageStream = canvas.generateImage();
        assertThat(imageStream).isNotNull();

        SVGOMDocument svgDocument = parseSvg(imageStream);
        assertThat(svgDocument.getDocumentElement().getTagName()).isEqualTo("svg");
    }

    @Test
    void testGenerateImageAfterCloseThrowsException() {
        canvas.close();

        assertThatThrownBy(canvas::generateImage)
            .isInstanceOf(ActivitiImageException.class)
            .hasMessageContaining("ProcessDiagramGenerator already closed");
    }

    @Test
    void testDrawNoneStartEventGeneratesCorrectSvg() throws Exception {
        GraphicInfo graphicInfo = new GraphicInfo();
        graphicInfo.setX(100);
        graphicInfo.setY(100);
        graphicInfo.setWidth(50);
        graphicInfo.setHeight(50);

        canvas.drawNoneStartEvent("startEvent1", "Start Event", graphicInfo);

        InputStream svgStream = canvas.generateImage();
        SVGOMDocument svgDocument = parseSvg(svgStream);

        Element circleElement = (Element) svgDocument.getElementsByTagName("circle").item(0);
        assertThat(circleElement).isNotNull();
        assertThat(circleElement.getAttribute("cx")).isEqualTo("125");
        assertThat(circleElement.getAttribute("cy")).isEqualTo("125");
        assertThat(circleElement.getAttribute("r")).isEqualTo("25");
    }

    @Test
    void testDrawLabelGeneratesCorrectSvg() throws Exception {
        GraphicInfo graphicInfo = new GraphicInfo();
        graphicInfo.setX(150);
        graphicInfo.setY(150);
        graphicInfo.setWidth(100);
        graphicInfo.setHeight(50);

        canvas.drawLabel("Test Label", graphicInfo);

        InputStream svgStream = canvas.generateImage();
        SVGOMDocument svgDocument = parseSvg(svgStream);

        Element groupElement = (Element) svgDocument.getElementsByTagName("g").item(0);
        assertThat(groupElement).isNotNull();

        assertThat(groupElement.getChildNodes().getLength()).isGreaterThan(0);

        Element childElement = (Element) groupElement.getChildNodes().item(0);
        assertThat(childElement).isNotNull();
        assertThat(childElement.getTagName()).isEqualTo("g");
    }

    private SVGOMDocument parseSvg(InputStream svgStream) throws Exception {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        return (SVGOMDocument) factory.createDocument(null, svgStream);
    }
}
