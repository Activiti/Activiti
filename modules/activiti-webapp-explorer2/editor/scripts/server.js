/**
 * Copyright (c) 2010
 * Signavio GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/
if(!ORYX) var ORYX = {};

if(!ORYX.CONFIG) ORYX.CONFIG = {};

/**
 * This file contains URI constants that may be used for XMLHTTPRequests.
 */

ORYX.CONFIG.ROOT_PATH =					"../editor/"; //TODO: Remove last slash!!
ORYX.CONFIG.EXPLORER_PATH =				"../explorer";
ORYX.CONFIG.LIBS_PATH =					"../libs";

/**
 * Regular Config
 */	
ORYX.CONFIG.SERVER_HANDLER_ROOT = 		"../service";
ORYX.CONFIG.SERVER_EDITOR_HANDLER =		ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor";
ORYX.CONFIG.SERVER_MODEL_HANDLER =		ORYX.CONFIG.SERVER_HANDLER_ROOT + "/model";
ORYX.CONFIG.STENCILSET_HANDLER = 		ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_stencilset?embedsvg=true&url=true&namespace=";    
ORYX.CONFIG.STENCIL_SETS_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_stencilset";

ORYX.CONFIG.PLUGINS_CONFIG =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor/plugins";
ORYX.CONFIG.SYNTAXCHECKER_URL =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/syntaxchecker";
ORYX.CONFIG.DEPLOY_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/model/deploy";
ORYX.CONFIG.MODEL_LIST_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/models";

ORYX.CONFIG.SS_EXTENSIONS_FOLDER =		ORYX.CONFIG.ROOT_PATH + "stencilsets/extensions/";
ORYX.CONFIG.SS_EXTENSIONS_CONFIG =		ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_ssextensions";	
ORYX.CONFIG.ORYX_NEW_URL =				"/new";	
ORYX.CONFIG.BPMN_LAYOUTER =				ORYX.CONFIG.ROOT_PATH + "bpmnlayouter";