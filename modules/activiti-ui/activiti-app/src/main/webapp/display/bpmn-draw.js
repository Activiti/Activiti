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
function _bpmnGetColor(element, defaultColor) 
{
    var strokeColor;
    if(element.current) {
        strokeColor = CURRENT_COLOR;
    } else if(element.completed) {
        strokeColor = COMPLETED_COLOR;
    } else {
        strokeColor = defaultColor;
    }
    return strokeColor;
}

function _drawPool(pool)
{
	var rect = paper.rect(pool.x, pool.y, pool.width, pool.height);

	rect.attr({"stroke-width": 1,
		"stroke": "#000000",
		"fill": "white"
 	});
	
	if (pool.name)
	{
		var poolName = paper.text(pool.x + 14, pool.y + (pool.height / 2), pool.name).attr({
	        "text-anchor" : "middle",
	        "font-family" : "Arial",
	        "font-size" : "12",
	        "fill" : "#000000"
	  	});
		
		poolName.transform("r270");
	}
	
	if (pool.lanes)
	{
		for (var i = 0; i < pool.lanes.length; i++)
		{
			var lane = pool.lanes[i];
			_drawLane(lane);
		}
	}
}

function _drawLane(lane)
{
	var rect = paper.rect(lane.x, lane.y, lane.width, lane.height);

	rect.attr({"stroke-width": 1,
		"stroke": "#000000",
		"fill": "white"
 	});
	
	if (lane.name)
	{
		var laneName = paper.text(lane.x + 10, lane.y + (lane.height / 2), lane.name).attr({
	        "text-anchor" : "middle",
	        "font-family" : "Arial",
	        "font-size" : "12",
	        "fill" : "#000000"
	  	});
		
		laneName.transform("r270");
	}
}

function _drawSubProcess(element)
{
	var rect = paper.rect(element.x, element.y, element.width, element.height, 4);

	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	rect.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "white"
 	});
}

function _drawTransaction(element)
{
	var rect = paper.rect(element.x, element.y, element.width, element.height, 4);

	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	rect.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "white"
 	});
	
	var borderRect = paper.rect(element.x + 2, element.y + 2, element.width - 4, element.height -4, 4);
	
	borderRect.attr({"stroke-width": 1,
		"stroke": "black",
		"fill": "none"
 	});
}

function _drawEventSubProcess(element)
{
	var rect = paper.rect(element.x, element.y, element.width, element.height, 4);
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	rect.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"stroke-dasharray": ".",
		"fill": "white"
 	});
}

function _drawStartEvent(element)
{
	var startEvent = _drawEvent(element, NORMAL_STROKE, 15);
	startEvent.click(function() {
		_zoom(true);
	});
	_addHoverLogic(element, "circle", MAIN_STROKE_COLOR);
}

function _drawEndEvent(element)
{
	var endEvent = _drawEvent(element, ENDEVENT_STROKE, 14);
	endEvent.click(function() {
		_zoom(false);
	});
	_addHoverLogic(element, "circle", MAIN_STROKE_COLOR);
}

function _drawEvent(element, strokeWidth, radius)
{
	var x = element.x + (element.width / 2);
	var y = element.y + (element.height / 2);

	var circle = paper.circle(x, y, radius);

	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);

    // Fill
    var eventFillColor = _determineCustomFillColor(element, "#ffffff");

    // Opacity
    var eventOpacity = 1.0;
    if (customActivityBackgroundOpacity) {
        eventOpacity = customActivityBackgroundOpacity;
    }

	circle.attr({"stroke-width": strokeWidth,
		"stroke": strokeColor,
		"fill": eventFillColor,
        "fill-opacity": eventOpacity
 	});

	circle.id = element.id;
	
	_drawEventIcon(paper, element);
	
	return circle;
}

function _drawServiceTask(element)
{
	_drawTask(element);
	if (element.taskType === "mail")
	{
		_drawSendTaskIcon(paper, element.x + 4, element.y + 4);
	}
	else if (element.taskType === "camel")
	{
		_drawCamelTaskIcon(paper, element.x + 4, element.y + 4);
	}
	else if (element.taskType === "mule")
	{
		_drawMuleTaskIcon(paper, element.x + 4, element.y + 4);
	}
    else if (element.taskType === "alfresco_publish")
    {
        _drawAlfrescoPublishTaskIcon(paper, element.x + 4, element.y + 4);
    }
    else if (element.taskType === "rest_call")
    {
        _drawRestCallTaskIcon(paper, element.x + 4, element.y + 4);
    }
	else if (element.stencilIconId)
	{
		paper.image("../service/stencilitem/" + element.stencilIconId + "/icon", element.x + 4, element.y + 4, 16, 16);
	}
	else
	{
		_drawServiceTaskIcon(paper, element.x + 4, element.y + 4);
	}
	_addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawCallActivity(element)
{
    var width = element.width - (CALL_ACTIVITY_STROKE / 2);
    var height = element.height - (CALL_ACTIVITY_STROKE / 2);

    var rect = paper.rect(element.x, element.y, width, height, 4);
  
    var strokeColor = _bpmnGetColor(element, ACTIVITY_STROKE_COLOR);

    // Fill
    var callActivityFillColor = _determineCustomFillColor(element, ACTIVITY_FILL_COLOR);

    // Opacity
    var callActivityOpacity = 1.0;
    if (customActivityBackgroundOpacity) {
        callActivityOpacity = customActivityBackgroundOpacity;
    }

    rect.attr({"stroke-width": CALL_ACTIVITY_STROKE,
        "stroke": strokeColor,
        "fill": callActivityFillColor,
        "fill-opacity": callActivityOpacity
    });
  
    rect.id = element.id;
  
    if (element.name) {
        this._drawMultilineText(element.name, element.x, element.y, element.width, element.height, "middle", "middle", 11);
    }
    _addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawScriptTask(element)
{
	_drawTask(element);
	_drawScriptTaskIcon(paper, element.x + 4, element.y + 4);
	_addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawUserTask(element)
{
	_drawTask(element);
	_drawUserTaskIcon(paper, element.x + 4, element.y + 4);
	_addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawBusinessRuleTask(element)
{
	_drawTask(element);
	_drawBusinessRuleTaskIcon(paper, element.x + 4, element.y + 4);
	_addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawManualTask(element)
{
	_drawTask(element);
	_drawManualTaskIcon(paper, element.x + 4, element.y + 4);
	_addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawSendTask(element)
{
    _drawTask(element);
    _drawSendTaskIcon(paper, element.x + 4, element.y + 4);
    _addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawReceiveTask(element)
{
	_drawTask(element);
	_drawReceiveTaskIcon(paper, element.x, element.y);
	_addHoverLogic(element, "rect", ACTIVITY_STROKE_COLOR);
}

function _drawTask(element)
{
    var rectAttrs = {};
    
    // Stroke
    var strokeColor = _bpmnGetColor(element, ACTIVITY_STROKE_COLOR);
    rectAttrs['stroke'] = strokeColor;
    
    var strokeWidth;
    if (strokeColor === ACTIVITY_STROKE_COLOR) {
        strokeWidth = TASK_STROKE;
    } else {
        strokeWidth = TASK_HIGHLIGHT_STROKE;
    }
    
	var width = element.width - (strokeWidth / 2);
	var height = element.height - (strokeWidth / 2);

	var rect = paper.rect(element.x, element.y, width, height, 4);
    rectAttrs['stroke-width'] = strokeWidth;

    // Fill
	var fillColor = _determineCustomFillColor(element, ACTIVITY_FILL_COLOR);
    rectAttrs['fill'] = fillColor;

    // Opacity
    if (customActivityBackgroundOpacity) {
        rectAttrs['fill-opacity'] = customActivityBackgroundOpacity;
    }

	rect.attr(rectAttrs);
	rect.id = element.id;
	
	if (element.name) {
		this._drawMultilineText(element.name, element.x, element.y, element.width, element.height, "middle", "middle", 11);
	}
}

function _drawExclusiveGateway(element)
{
	_drawGateway(element);
	var quarterWidth = element.width / 4;
	var quarterHeight = element.height / 4;
	
	var iks = paper.path(
		"M" + (element.x + quarterWidth + 3) + " " + (element.y + quarterHeight + 3) + 
		"L" + (element.x + 3 * quarterWidth - 3) + " " + (element.y + 3 * quarterHeight - 3) + 
		"M" + (element.x + quarterWidth + 3) + " " + (element.y + 3 * quarterHeight - 3) + 
		"L" + (element.x + 3 * quarterWidth - 3) + " " + (element.y + quarterHeight + 3)
	);
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);

    // Fill
    var gatewayFillColor = _determineCustomFillColor(element, ACTIVITY_FILL_COLOR);

    // Opacity
    var gatewayOpacity = 1.0;
    if (customActivityBackgroundOpacity) {
        gatewayOpacity = customActivityBackgroundOpacity;
    }


    iks.attr({"stroke-width": 3, "stroke": strokeColor, "fill": gatewayFillColor, "fill-opacity": gatewayOpacity});
	
	_addHoverLogic(element, "rhombus", MAIN_STROKE_COLOR);
}

function _drawParallelGateway(element)
{
	_drawGateway(element);
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	var path1 = paper.path("M 6.75,16 L 25.75,16 M 16,6.75 L 16,25.75");

    // Fill
    var gatewayFillColor = _determineCustomFillColor(element, ACTIVITY_FILL_COLOR);

    // Opacity
    var gatewayOpacity = 1.0;
    if (customActivityBackgroundOpacity) {
        gatewayOpacity = customActivityBackgroundOpacity;
    }

	path1.attr({
		"stroke-width": 3, 
		"stroke": strokeColor,
		"fill": gatewayFillColor,
        "fill-opacity": gatewayOpacity
	});
	
	path1.transform("T" + (element.x + 4) + "," + (element.y + 4));
	
	_addHoverLogic(element, "rhombus", MAIN_STROKE_COLOR);
}

function _drawInclusiveGateway(element)
{
	_drawGateway(element);
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	var circle1 = paper.circle(element.x + (element.width / 2), element.y + (element.height / 2), 9.75);

    // Fill
    var gatewayFillColor = _determineCustomFillColor(element, ACTIVITY_FILL_COLOR);

    // Opacity
    var gatewayOpacity = 1.0;
    if (customActivityBackgroundOpacity) {
        gatewayOpacity = customActivityBackgroundOpacity;
    }

	circle1.attr({
		"stroke-width": 2.5, 
		"stroke": strokeColor,
		"fill": gatewayFillColor,
        "fill-opacity": gatewayOpacity
	});
	
	_addHoverLogic(element, "rhombus", MAIN_STROKE_COLOR);
}

function _drawEventGateway(element)
{
	_drawGateway(element);
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	var circle1 = paper.circle(element.x + (element.width / 2), element.y + (element.height / 2), 10.4);

    // Fill
    var gatewayFillColor = _determineCustomFillColor(element, ACTIVITY_FILL_COLOR);

    // Opacity
    var gatewayOpacity = 1.0;
    if (customActivityBackgroundOpacity) {
        gatewayOpacity = customActivityBackgroundOpacity;
    }

	circle1.attr({
		"stroke-width": 0.5, 
		"stroke": strokeColor,
		"fill": gatewayFillColor,
        "fill-opacity": gatewayOpacity
    });
	
	var circle2 = paper.circle(element.x + (element.width / 2), element.y + (element.height / 2), 11.7);
	circle2.attr({
		"stroke-width": 0.5, 
		"stroke": strokeColor,
        "fill": gatewayFillColor,
        "fill-opacity": gatewayOpacity
	});
	
	var path1 = paper.path("M 20.327514,22.344972 L 11.259248,22.344216 L 8.4577203,13.719549 L 15.794545,8.389969 L 23.130481,13.720774 L 20.327514,22.344972 z");
	path1.attr({
		"stroke-width": 1.39999998, 
		"stroke": strokeColor,
        "fill": gatewayFillColor,
        "fill-opacity": gatewayOpacity,
		"stroke-linejoin": "bevel"
	});
	
	path1.transform("T" + (element.x + 4) + "," + (element.y + 4));
	
	_addHoverLogic(element, "rhombus", MAIN_STROKE_COLOR);
}

function _drawGateway(element)
{
    var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
    
	var rhombus = paper.path("M" + element.x + " " + (element.y + (element.height / 2)) + 
		"L" + (element.x + (element.width / 2)) + " " + (element.y + element.height) + 
		"L" + (element.x + element.width) + " " + (element.y + (element.height / 2)) +
		"L" + (element.x + (element.width / 2)) + " " + element.y + "z"
	);

    // Fill
    var gatewayFillColor = _determineCustomFillColor(element, ACTIVITY_FILL_COLOR);

    // Opacity
    var gatewayOpacity = 1.0;
    if (customActivityBackgroundOpacity) {
        gatewayOpacity = customActivityBackgroundOpacity;
    }

	rhombus.attr("stroke-width", 2);
	rhombus.attr("stroke", strokeColor);
	rhombus.attr("fill", gatewayFillColor);
    rhombus.attr("fill-opacity", gatewayOpacity);
	
	rhombus.id = element.id;
	
	return rhombus;
}

function _drawBoundaryEvent(element)
{
	var x = element.x + (element.width / 2);
	var y = element.y + (element.height / 2);

	var circle = paper.circle(x, y, 15);
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);

	circle.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "white"
 	});
	
	var innerCircle = paper.circle(x, y, 12);
	
	innerCircle.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "none"
 	});
	
	_drawEventIcon(paper, element);
	_addHoverLogic(element, "circle", MAIN_STROKE_COLOR);
	
	circle.id = element.id;
	innerCircle.id = element.id + "_inner";
}

function _drawIntermediateCatchEvent(element)
{
	var x = element.x + (element.width / 2);
	var y = element.y + (element.height / 2);

	var circle = paper.circle(x, y, 15);
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);

	circle.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "white"
 	});
	
	var innerCircle = paper.circle(x, y, 12);
	
	innerCircle.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "none"
 	});
	
	_drawEventIcon(paper, element);
	_addHoverLogic(element, "circle", MAIN_STROKE_COLOR);
	
	circle.id = element.id;
	innerCircle.id = element.id + "_inner";
}

function _drawThrowEvent(element)
{
	var x = element.x + (element.width / 2);
	var y = element.y + (element.height / 2);

	var circle = paper.circle(x, y, 15);

	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	circle.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "white"
 	});
	
	var innerCircle = paper.circle(x, y, 12);
	
	innerCircle.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "none"
 	});
	
	_drawEventIcon(paper, element);
	_addHoverLogic(element, "circle", MAIN_STROKE_COLOR);
	
	circle.id = element.id;
	innerCircle.id = element.id + "_inner";
}

function _drawMultilineText(text, x, y, boxWidth, boxHeight, horizontalAnchor, verticalAnchor, fontSize) 
{
	if (!text || text == "")
	{
		return;
	}
	
	var textBoxX, textBoxY;
    var width = boxWidth - (2 * TEXT_PADDING);
    
    if (horizontalAnchor === "middle")
    {
    	textBoxX = x + (boxWidth / 2);
    }
    else if (horizontalAnchor === "start")
    {
    	textBoxX = x;
    }
    
    textBoxY = y + (boxHeight / 2);
    
 	var t = paper.text(textBoxX + TEXT_PADDING, textBoxY + TEXT_PADDING).attr({
        "text-anchor" : horizontalAnchor,
        "font-family" : "Arial",
        "font-size" : fontSize,
        "fill" : "#373e48"
  	});
  	
    var abc = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    t.attr({
        "text" : abc
    });
    var letterWidth = t.getBBox().width / abc.length;
	   
    t.attr({
        "text" : text
    });
    var removedLineBreaks = text.split("\n");
    var x = 0, s = [];
    for (var r = 0; r < removedLineBreaks.length; r++)
    {
  	    var words = removedLineBreaks[r].split(" ");
  	    for ( var i = 0; i < words.length; i++) {
  
  	        var l = words[i].length;
  	        if (x + (l * letterWidth) > width) {
  	            s.push("\n");
  	            x = 0;
  	        }
  	        x += l * letterWidth;
  	        s.push(words[i] + " ");
  	    }
	  	s.push("\n");
        x = 0;
    }
    t.attr({
    	"text" : s.join("")
    });
    
    if (verticalAnchor && verticalAnchor === "top")
    {
    	t.attr({"y": y + (t.getBBox().height / 2)});
    }
}

function _drawTextAnnotation(element)
{
	var path1 = paper.path("M20,1 L1,1 L1,50 L20,50");
	path1.attr({
		"stroke": "#585858",
		"fill": "none"
 	});
	
	var annotation = paper.set();
	annotation.push(path1);

	annotation.transform("T" + element.x + "," + element.y);
	
	if (element.text) {
		this._drawMultilineText(element.text, element.x + 2, element.y, element.width, element.height, "start", "middle", 11);
	}
}
 
function _drawFlow(flow){
	
	var polyline = new Polyline(flow.id, flow.waypoints, SEQUENCEFLOW_STROKE, paper);
	
	var strokeColor = _bpmnGetColor(flow, MAIN_STROKE_COLOR);
	
	polyline.element = paper.path(polyline.path);
	polyline.element.attr({"stroke-width":SEQUENCEFLOW_STROKE});
	polyline.element.attr({"stroke":strokeColor});
	
	polyline.element.id = flow.id;
	
	var lastLineIndex = polyline.getLinesCount() - 1;
	var line = polyline.getLine(lastLineIndex);
	
	if (line == undefined) return;
	
	if (flow.type == "connection" && flow.conditions)
	{
		var middleX = (line.x1 + line.x2) / 2;
		var middleY = (line.y1 + line.y2) / 2;
		var image = paper.image("../editor/images/condition-flow.png", middleX - 8, middleY - 8, 16, 16);
	}
	
	var polylineInvisible = new Polyline(flow.id, flow.waypoints, SEQUENCEFLOW_STROKE, paper);
	
	polylineInvisible.element = paper.path(polyline.path);
	polylineInvisible.element.attr({
			"opacity": 0,
			"stroke-width": 8,
            "stroke" : "#000000"
	});
	
	_showTip(jQuery(polylineInvisible.element.node), flow);
	
	polylineInvisible.element.mouseover(function() {
		paper.getById(polyline.element.id).attr({"stroke":"blue"});
	});
	
	polylineInvisible.element.mouseout(function() {
		paper.getById(polyline.element.id).attr({"stroke":"#585858"});
	});
	
	_drawArrowHead(line);
}

function _drawAssociation(flow){
	
	var polyline = new Polyline(flow.id, flow.waypoints, ASSOCIATION_STROKE, paper);
	
	polyline.element = paper.path(polyline.path);
	polyline.element.attr({"stroke-width": ASSOCIATION_STROKE});
	polyline.element.attr({"stroke-dasharray": ". "});
	polyline.element.attr({"stroke":"#585858"});
	
	polyline.element.id = flow.id;
	
	var polylineInvisible = new Polyline(flow.id, flow.waypoints, ASSOCIATION_STROKE, paper);
	
	polylineInvisible.element = paper.path(polyline.path);
	polylineInvisible.element.attr({
			"opacity": 0,
			"stroke-width": 8,
            "stroke" : "#000000"
	});
	
	_showTip(jQuery(polylineInvisible.element.node), flow);
	
	polylineInvisible.element.mouseover(function() {
		paper.getById(polyline.element.id).attr({"stroke":"blue"});
	});
	
	polylineInvisible.element.mouseout(function() {
		paper.getById(polyline.element.id).attr({"stroke":"#585858"});
	});
}

function _drawArrowHead(line, connectionType) 
{
	var doubleArrowWidth = 2 * ARROW_WIDTH;
	
	var arrowHead = paper.path("M0 0L-" + (ARROW_WIDTH / 2 + .5) + " -" + doubleArrowWidth + "L" + (ARROW_WIDTH/2 + .5) + " -" + doubleArrowWidth + "z");
	
	// anti smoothing
	if (this.strokeWidth%2 == 1)
		line.x2 += .5, line.y2 += .5;
	
	arrowHead.transform("t" + line.x2 + "," + line.y2 + "");
	arrowHead.transform("...r" + Raphael.deg(line.angle - Math.PI / 2) + " " + 0 + " " + 0);
	
	arrowHead.attr("fill", "#585858");
		
	arrowHead.attr("stroke-width", SEQUENCEFLOW_STROKE);
	arrowHead.attr("stroke", "#585858");
	
	return arrowHead;
}

function _determineCustomFillColor(element, defaultColor) {

    var color;

    // By name
    if (customActivityColors && customActivityColors[element.name]) {
        color = customActivityColors[element.name];
    }

    if (color !== null && color !== undefined) {
        return color;
    }

    // By id
    if (customActivityColors && customActivityColors[element.id]) {
        color = customActivityColors[element.id];
    }

    if (color !== null && color !== undefined) {
        return color;
    }

    return defaultColor;
}