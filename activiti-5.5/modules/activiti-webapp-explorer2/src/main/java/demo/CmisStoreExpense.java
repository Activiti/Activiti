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

package demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


/**
 * @author Joram Barrez
 */
public class CmisStoreExpense implements JavaDelegate {
  
  private static final String FOLDER = "activiti-demo";
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  
  public void execute(DelegateExecution execution) throws Exception {
    Session session = CmisUtil.createCmisSession("admin", "admin", "http://cmis.alfresco.com:80/service/cmis");

    // Get or create folder
    Folder folder = CmisUtil.getFolder(session, FOLDER);
    if (folder == null) {
      folder = CmisUtil.createFolder(session, session.getRootFolder(), FOLDER); 
    }
    
    // Create document
    byte[] pdfDoc = createPdfDocument(execution);
    
    // Push document to CMIS
    CmisUtil.createDocument(session, folder, "expense_" + DATE_FORMATTER.format(new Date()) + ".pdf", pdfDoc, "application/pdf");
  }
  
  protected byte[] createPdfDocument(DelegateExecution execution) throws IOException, COSVisitorException {
    PDDocument document = new PDDocument();
    PDPage page = new PDPage();
    document.addPage(page);
    PDFont font = PDType1Font.HELVETICA_BOLD;
    PDPageContentStream contentStream = new PDPageContentStream(document, page);
    contentStream.beginText();
    contentStream.setFont(font, 14);
    contentStream.moveTextPositionByAmount(100, 700);
    contentStream.drawString("Refund request by " + execution.getVariable("initiator"));
    contentStream.endText();
    contentStream.close();
    
    contentStream.beginText();
    contentStream.setFont(font, 12);
    contentStream.moveTextPositionByAmount(100, 650);
    contentStream.drawString("amount: " + execution.getVariable("amount") + " EUR");
    contentStream.endText();
    contentStream.close();
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    document.save(bos);
    document.close();
    return bos.toByteArray();
  }
  
}
