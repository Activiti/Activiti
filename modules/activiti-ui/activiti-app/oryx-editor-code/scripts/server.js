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

if(!ORYX) var ORYX = {};

if(!ORYX.CONFIG) ORYX.CONFIG = {};

/**
 * This file contains URI constants that may be used for XMLHTTPRequests.
 */

ORYX.CONFIG.ROOT_PATH =					"editor/"; //TODO: Remove last slash!!
ORYX.CONFIG.EXPLORER_PATH =				"explorer";
ORYX.CONFIG.LIBS_PATH =					"libs";

/**
 * Regular Config
 */	
ORYX.CONFIG.SERVER_HANDLER_ROOT = 			"service";
ORYX.CONFIG.SERVER_EDITOR_HANDLER =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor";
ORYX.CONFIG.SERVER_MODEL_HANDLER =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/model";
ORYX.CONFIG.STENCILSET_HANDLER = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_stencilset?embedsvg=true&url=true&namespace=";    
ORYX.CONFIG.STENCIL_SETS_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_stencilset";

ORYX.CONFIG.PLUGINS_CONFIG =				"editor-app/plugins.xml";
ORYX.CONFIG.SYNTAXCHECKER_URL =				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/syntaxchecker";
ORYX.CONFIG.DEPLOY_URL = 					ORYX.CONFIG.SERVER_HANDLER_ROOT + "/model/deploy";
ORYX.CONFIG.MODEL_LIST_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/models";
ORYX.CONFIG.FORM_FLOW_LIST_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/formflows";
ORYX.CONFIG.FORM_FLOW_IMAGE_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/formflow";
ORYX.CONFIG.FORM_LIST_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/forms";
ORYX.CONFIG.FORM_IMAGE_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/form";
ORYX.CONFIG.SUB_PROCESS_LIST_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/subprocesses";
ORYX.CONFIG.SUB_PROCESS_IMAGE_URL = 		ORYX.CONFIG.SERVER_HANDLER_ROOT + "/subprocess";
ORYX.CONFIG.TEST_SERVICE_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/service/";

ORYX.CONFIG.SERVICE_LIST_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/services";
ORYX.CONFIG.CONDITION_ELEMENT_LIST_URL = 	ORYX.CONFIG.SERVER_HANDLER_ROOT + "/conditionelements";
ORYX.CONFIG.VARIABLEDEF_ELEMENT_LIST_URL = 	ORYX.CONFIG.SERVER_HANDLER_ROOT + "/variabledefinitionelements";
ORYX.CONFIG.VALIDATOR_LIST_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/validators";

ORYX.CONFIG.SS_EXTENSIONS_FOLDER =			ORYX.CONFIG.ROOT_PATH + "stencilsets/extensions/";
ORYX.CONFIG.SS_EXTENSIONS_CONFIG =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_ssextensions";	
ORYX.CONFIG.ORYX_NEW_URL =					"/new";	
ORYX.CONFIG.BPMN_LAYOUTER =					ORYX.CONFIG.ROOT_PATH + "bpmnlayouter";

ORYX.CONFIG.EXPRESSION_METADATA_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/expression-metadata";
ORYX.CONFIG.DATASOURCE_METADATA_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/datasource-metadata";