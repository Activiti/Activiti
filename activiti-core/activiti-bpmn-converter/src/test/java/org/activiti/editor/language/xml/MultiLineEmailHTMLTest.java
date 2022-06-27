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
package org.activiti.editor.language.xml;

import org.activiti.bpmn.model.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiLineEmailHTMLTest extends AbstractConverterTest{

    @Test
    public void convertXMLToModel() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        validateModel(bpmnModel);
    }

    private void validateModel(BpmnModel model) {

        Map<String, List<ExtensionElement>> extensionElements = new HashMap<>();
        extensionElements = model.getMainProcess().getFlowElement("userTask").getExtensionElements();
        List<ExtensionElement> emailTemplateEmailList = extensionElements.get("email-template");
        ExtensionElement extensionElement = emailTemplateEmailList.get(0);
        String elementText = extensionElement.getElementText();
        assertThat(elementText).isEqualTo(getEmailBody());
    }

    protected String getResource() {
        return "htmlEmailSplit.bpmn.xml";
    }

    private String getEmailBody(){
        return "<html>\n" +
            "<body style=\"background-color: #ffffff; padding: 0; margin: 0;\">\n" +
            "<table width=\"100%\" height=\"400\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0; padding:0\">\n" +
            "<tr height=\"20\"><td colspan=\"3\"></td></tr>\n" +
            "<tr>\n" +
            "<td width=\"10%\"></td>\n" +
            "<td width=\"80%\" valign=\"top\" >\n" +
            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n" +
            "<tr style=\"color:#ffffff; background-color: #000000;\" height=\"15\"><td colspan=\"3\"></td></tr>\n" +
            "<tr style=\"color:#ffffff; background-color: #000000;\">\n" +
            "<td width=\"15\"></td>\n" +
            "<td valign=\"center\"style=\"font-size: 16px;font-family: 'Open Sans', Helvetica, sans-serif; font-weight: bold; \">Tem uma nova tarefa no Flow</td>\n" +
            "<td width=\"15\"></td>\n" +
            "</tr>\n" +
            "<tr style=\"color:#ffffff; background-color: #000000;\" height=\"15\"><td colspan=\"3\"></td></tr>\n" +
            "<tr height=\"1\" style=\"background-color: #e8edf1;\"><td colspan=\"3\"></td></tr>\n" +
            "<tr colspan=\"3\" height=\"25\"><td></td></tr>\n" +
            "<tr>\n" +
            "<td width=\"15\"></td>\n" +
            "<td valign=\"top\" style=\"font-size: 14px;font-family: 'Open Sans', Helvetica, sans-serif;\">\n" +
            "<p>Processo: NAC - Declarações - Preencher declaração</p>\n" +
            "<p>ID da instância do processo: ${currentProcessInstanceId}</p>\n" +
            "<p>Tipo de declaração: ${tipo_declaracao_LABEL}</p>\n" +
            "<p>Ref.: ${referencia_concurso}</p>\n" +
            "<p>Papel no concurso: ${papel}</p>\n" +
            "<p>Tem uma nova tarefa no Flow para, como ${papel}, descarregar a declaração do tipo ${tipo_declaracao_LABEL} para o ${referencia_concurso}, validar e/ou corrigir os campos pré preenchidos e submeter o documento assinado.</p>\n" +
            "</td>\n" +
            "<td width=\"15\"></td>\n" +
            "</tr>\n" +
            "<tr height=\"30\"><td colspan=\"3\"></td></tr>\n" +
            "<tr>\n" +
            "<td width=\"15\"></td>\n" +
            "<td valign=\"top\">\n" +
            "<table style=\"background-color: #ffffff;\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n" +
            "<tr>\n" +
            "<td valign=\"middle\" style=\"font-size: 15px;font-family: 'Open Sans', Helvetica, sans-serif; text-align: left; font-weight: bold; color: #ffffff;\">\n" +
            "<a href=\"${taskDirectUrl}\" title=\"Open the task\" style=\"text-decoration: none; text-align: center;color: #ffffff; display: inline-block; width: 300px; padding: 10px 0px;background-color: #92d400; border-radius: 3px; -moz-border-radius: 3px; -webkit-border-radius: 3px;\">Clique aqui para assinar e submeter declaração</a>\n" +
            "</td>\n" +
            "</tr>\n" +
            "</table>\n" +
            "</td>\n" +
            "<td width=\"15\"></td>\n" +
            "</tr>\n" +
            "<tr height=\"15\"><td colspan=\"3\"></td></tr>\n" +
            "<tr height=\"1\" style=\"background-color: #e8edf1;\"><td colspan=\"3\"></td></tr>\n" +
            "<tr height=\"15\"><td colspan=\"3\"></td></tr>\n" +
            "<tr>\n" +
            "<td width=\"15\"></td>\n" +
            "<td style=\"font-size: 11px;font-family: 'Open Sans', Helvetica, sans-serif; color:#666666;\">\n" +
            "Este email foi enviado pelo <a href=\"${homeUrl}\" style=\"color: #666666;\">Flow</a>. Por favor, não responda a este email.\n" +
            "</td>\n" +
            "<td width=\"15\"></td>\n" +
            "</tr>\n" +
            "<tr height=\"25\"><td colspan=\"3\"></td></tr>\n" +
            "</table>\n" +
            "</td>\n" +
            "<td width=\"10%\"></td>\n" +
            "</tr>\n" +
            "</table>\n" +
            "</body>\n" +
            "</html>";
    }
}
