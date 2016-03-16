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
var NORMAL_STROKE = 1;
var SEQUENCEFLOW_STROKE = 1.5;
var ASSOCIATION_STROKE = 2;
var TASK_STROKE = 1;
var TASK_HIGHLIGHT_STROKE = 2;
var CALL_ACTIVITY_STROKE = 2;
var ENDEVENT_STROKE = 3;

var COMPLETED_COLOR= "#2632aa";
var TEXT_COLOR= "#373e48";
var CURRENT_COLOR= "#017501";
var HOVER_COLOR= "#666666";
var ACTIVITY_STROKE_COLOR = "#bbbbbb";
var ACTIVITY_FILL_COLOR = "#f9f9f9";
var MAIN_STROKE_COLOR = "#585858";

var TEXT_PADDING = 3;
var ARROW_WIDTH = 4;
var MARKER_WIDTH = 12;

var TASK_FONT = {font: "11px Arial", opacity: 1, fill: Raphael.rgb(0, 0, 0)};

// icons
var ICON_SIZE = 16;
var ICON_PADDING = 4;

var INITIAL_CANVAS_WIDTH;
var INITIAL_CANVAS_HEIGHT;

var paper;
var viewBox;
var viewBoxWidth;
var viewBoxHeight;

var canvasWidth;
var canvasHeight;

var modelDiv = jQuery('#bpmnModel');
var modelId = modelDiv.attr('data-model-id');
var historyModelId = modelDiv.attr('data-history-id');
var processDefinitionId = modelDiv.attr('data-process-definition-id');
var modelType = modelDiv.attr('data-model-type');

// Support for custom background colors for activities
var customActivityColors = modelDiv.attr('data-activity-color-mapping');
if (customActivityColors !== null && customActivityColors !== undefined && customActivityColors.length > 0) {
    // Stored on the attribute as a string
    customActivityColors = JSON.parse(customActivityColors);
}

var customActivityToolTips = modelDiv.attr('data-activity-tooltips');
if (customActivityToolTips !== null && customActivityToolTips !== undefined && customActivityToolTips.length > 0) {
    // Stored on the attribute as a string
    customActivityToolTips = JSON.parse(customActivityToolTips);
}

// Support for custom opacity for activity backgrounds
var customActivityBackgroundOpacity = modelDiv.attr('data-activity-opacity');

var elementsAdded = new Array();
var elementsRemoved = new Array();

function _showTip(htmlNode, element)
{

    // Custom tooltip
    var documentation = undefined;
    if (customActivityToolTips) {
        if (customActivityToolTips[element.name]) {
            documentation = customActivityToolTips[element.name];
        } else if (customActivityToolTips[element.id]) {
            documentation = customActivityToolTips[element.id];
        } else {
            documentation = ''; // Show nothing if custom tool tips are enabled
        }
    }

    // Default tooltip, no custom tool tip set
    if (documentation === undefined) {
        var documentation = "";
        if (element.name && element.name.length > 0) {
            documentation += "<b>Name</b>: <i>" + element.name + "</i><br/><br/>";
        }

        if (element.properties) {
            for (var i = 0; i < element.properties.length; i++) {
                var propName = element.properties[i].name;
                if (element.properties[i].type && element.properties[i].type === 'list') {
                    documentation += '<b>' + propName + '</b>:<br/>';
                    for (var j = 0; j < element.properties[i].value.length; j++) {
                        documentation += '<i>' + element.properties[i].value[j] + '</i><br/>';
                    }
                }
                else {
                    documentation += '<b>' + propName + '</b>: <i>' + element.properties[i].value + '</i><br/>';
                }
            }
        }
    }

    var text = element.type + " ";
    if (element.name && element.name.length > 0)
    {
        text += element.name;
    }
    else
    {
        text += element.id;
    }

    htmlNode.qtip({
        content: {
            text: documentation,
            title: {
                text: text
            }
        },
        position: {
            my: 'top left',
            at: 'bottom center',
            viewport: jQuery('#bpmnModel')
        },
        hide: {
            fixed: true, delay: 500,
            event: 'click mouseleave'
        },
        style: {
            classes: 'ui-tooltip-kisbpm-bpmn'
        }
    });
}

function _addHoverLogic(element, type, defaultColor)
{
    var strokeColor = _bpmnGetColor(element, defaultColor);
    var topBodyRect = null;
    if (type === "rect")
    {
        topBodyRect = paper.rect(element.x, element.y, element.width, element.height);
    }
    else if (type === "circle")
    {
        var x = element.x + (element.width / 2);
        var y = element.y + (element.height / 2);
        topBodyRect = paper.circle(x, y, 15);
    }
    else if (type === "rhombus")
    {
        topBodyRect = paper.path("M" + element.x + " " + (element.y + (element.height / 2)) +
            "L" + (element.x + (element.width / 2)) + " " + (element.y + element.height) +
            "L" + (element.x + element.width) + " " + (element.y + (element.height / 2)) +
            "L" + (element.x + (element.width / 2)) + " " + element.y + "z"
        );
    }

    var opacity = 0;
    var fillColor = "#ffffff";
    if (jQuery.inArray(element.id, elementsAdded) >= 0)
    {
        opacity = 0.2;
        fillColor = "green";
    }

    if (jQuery.inArray(element.id, elementsRemoved) >= 0)
    {
        opacity = 0.2;
        fillColor = "red";
    }

    topBodyRect.attr({
        "opacity": opacity,
        "stroke" : "none",
        "fill" : fillColor
    });
    _showTip(jQuery(topBodyRect.node), element);

    topBodyRect.mouseover(function() {
        paper.getById(element.id).attr({"stroke":HOVER_COLOR});
    });

    topBodyRect.mouseout(function() {
        paper.getById(element.id).attr({"stroke":strokeColor});
    });
}

function _zoom(zoomIn)
{
    var tmpCanvasWidth, tmpCanvasHeight;
    if (zoomIn)
    {
        tmpCanvasWidth = canvasWidth * (1.0/0.90);
        tmpCanvasHeight = canvasHeight * (1.0/0.90);
    }
    else
    {
        tmpCanvasWidth = canvasWidth * (1.0/1.10);
        tmpCanvasHeight = canvasHeight * (1.0/1.10);
    }

    if (tmpCanvasWidth != canvasWidth || tmpCanvasHeight != canvasHeight)
    {
        canvasWidth = tmpCanvasWidth;
        canvasHeight = tmpCanvasHeight;
        paper.setSize(canvasWidth, canvasHeight);
    }
}

var modelUrl;

if (modelType == 'runtime') {
	if (historyModelId) {
    	modelUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/history/' + historyModelId + '/model-json';
	} else {
    	modelUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + modelId + '/model-json';
	}
} else if (modelType == 'design') {
	if (historyModelId) {
    	modelUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + modelId + '/history/' + historyModelId + '/model-json';
	} else {
    	modelUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + modelId + '/model-json';
	}
} else if (modelType == 'process-definition') {
    modelUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/process-definitions/' + processDefinitionId + '/model-json';
}

var request = jQuery.ajax({
    type: 'get',
    url: modelUrl + '?nocaching=' + new Date().getTime()
});

request.success(function(data, textStatus, jqXHR) {

    if ((!data.elements || data.elements.length == 0) && (!data.pools || data.pools.length == 0)) return;

    INITIAL_CANVAS_WIDTH = data.diagramWidth;
    
    if (modelType == 'design') {
    	INITIAL_CANVAS_WIDTH += 20;
    } else {
        INITIAL_CANVAS_WIDTH += 30;
    }
    
    INITIAL_CANVAS_HEIGHT = data.diagramHeight + 50;
    canvasWidth = INITIAL_CANVAS_WIDTH;
    canvasHeight = INITIAL_CANVAS_HEIGHT;
    viewBoxWidth = INITIAL_CANVAS_WIDTH;
    viewBoxHeight = INITIAL_CANVAS_HEIGHT;
    
    if (modelType == 'design') {
    	var headerBarHeight = 170;
    	var offsetY = 0;
    	if (jQuery(window).height() > (canvasHeight + headerBarHeight))
    	{
        	offsetY = (jQuery(window).height() - headerBarHeight - canvasHeight) / 2;
    	}

    	if (offsetY > 50) {
        	offsetY = 50;
    	}

    	jQuery('#bpmnModel').css('marginTop', offsetY);
    }

    jQuery('#bpmnModel').width(INITIAL_CANVAS_WIDTH);
    jQuery('#bpmnModel').height(INITIAL_CANVAS_HEIGHT);
    paper = Raphael(document.getElementById('bpmnModel'), canvasWidth, canvasHeight);
    paper.setViewBox(0, 0, viewBoxWidth, viewBoxHeight, false);
    paper.renderfix();

    if (data.pools)
    {
        for (var i = 0; i < data.pools.length; i++)
        {
            var pool = data.pools[i];
            _drawPool(pool);
        }
    }

    var modelElements = data.elements;
    for (var i = 0; i < modelElements.length; i++)
    {
        var element = modelElements[i];
        //try {
        var drawFunction = eval("_draw" + element.type);
        drawFunction(element);
        //} catch(err) {console.log(err);}
    }

    if (data.flows)
    {
        for (var i = 0; i < data.flows.length; i++)
        {
            var flow = data.flows[i];
            if (flow.type === 'sequenceFlow') {
                _drawFlow(flow);
            } else if (flow.type === 'association') {
                _drawAssociation(flow);
            }
        }
    }
});

request.error(function(jqXHR, textStatus, errorThrown) {
    alert("error");
});