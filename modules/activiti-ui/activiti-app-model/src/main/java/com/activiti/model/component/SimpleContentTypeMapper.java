/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.model.component;

import org.springframework.stereotype.Component;

import com.activiti.domain.runtime.RelatedContent;

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
