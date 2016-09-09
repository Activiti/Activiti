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
package org.activiti.app.model.component;

import org.activiti.app.domain.runtime.RelatedContent;
import org.springframework.stereotype.Component;

/**
 * Mapper that converts mimetypes into simple types for convenience and grouping
 * common types together (eg. xls and xsls mimetypes will all be 'excel').
 * 
 * @author Frederik Heremans
 */
@Component
public class SimpleContentTypeMapper {

    public static final String TYPE_WORD = "word";
    public static final String TYPE_EXCEL = "excel";
    public static final String TYPE_POWERPOINT = "powerpoint";
    public static final String TYPE_PDF = "pdf";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_FOLDER = "folder";
    public static final String TYPE_GENERIC = "content";

    public static final String MIME_TYPE_DOC = "application/msword";
    public static final String MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIME_TYPE_GOOGLE_DOC = "application/vnd.google-apps.document";
    
    public static final String MIME_TYPE_XLS = "application/vnd.ms-excel";
    public static final String MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MIME_TYPE_GOOGLE_PRESENTATION = "application/vnd.google-apps.presentation";
    
    public static final String MIME_TYPE_GOOGLE_FOLDER = "application/vnd.google-apps.folder";
    public static final String MIME_TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    
    public static final String MIME_TYPE_GOOGLE_SHEET = "application/vnd.google-apps.spreadsheet";
    public static final String MIME_TYPE_PPT = "application/vnd.ms-powerpoint";
    public static final String MIME_TYPE_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    public static final String MIME_TYPE_PDF = "application/pdf";
    
    
    public static final String MIME_TYPE_IMAGE_PNG = "image/png";

    public static final String PREFIX_MIME_TYPE_IMAGE = "image/";

    public String getSimpleTypeForMimeType(String mimeType) {
        String result = null;
        if (mimeType != null) {
            if (MIME_TYPE_DOC.equals(mimeType) || MIME_TYPE_DOCX.equals(mimeType) || MIME_TYPE_GOOGLE_DOC.equals(mimeType)) {
                result = TYPE_WORD;
            } else if (MIME_TYPE_XLS.equals(mimeType) || MIME_TYPE_XLSX.equals(mimeType) || MIME_TYPE_GOOGLE_SHEET.equals(mimeType)) {
                result = TYPE_EXCEL;
            }
            if (MIME_TYPE_PPT.equals(mimeType) || MIME_TYPE_PPTX.equals(mimeType) || MIME_TYPE_GOOGLE_PRESENTATION.equals(mimeType)) {
                result = TYPE_POWERPOINT;
            } else if (MIME_TYPE_PDF.equals(mimeType)) {
                result = TYPE_PDF;
            } else if (MIME_TYPE_GOOGLE_DRAWING.equals(mimeType) || mimeType.startsWith(PREFIX_MIME_TYPE_IMAGE)) {
                result = TYPE_IMAGE;
            } else if(MIME_TYPE_GOOGLE_FOLDER.equals(mimeType)) {
                return TYPE_FOLDER;
            }
        }

        // Fallback to generic, when no type can be determined yet
        if (result == null) {
            result = TYPE_GENERIC;
        }
        return result;
    }

    public String getSimpleType(RelatedContent relatedContent) {
        return getSimpleTypeForMimeType(relatedContent.getMimeType());
    }
}
