/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
function _drawUserTaskIcon(paper, startX, startY, element)
{
  
  var color = _bpmnGetColor(element, "#d1b575");
	var path1 = paper.path("m 1,17 16,0 0,-1.7778 -5.333332,-3.5555 0,-1.7778 c 1.244444,0 1.244444,-2.3111 1.244444,-2.3111 l 0,-3.0222 C 12.555557,0.8221 9.0000001,1.0001 9.0000001,1.0001 c 0,0 -3.5555556,-0.178 -3.9111111,3.5555 l 0,3.0222 c 0,0 0,2.3111 1.2444443,2.3111 l 0,1.7778 L 1,15.2222 1,17 17,17");
	path1.attr({
		"opacity": 1,
		"stroke": "none",
		"fill": color
 	});
	
	var userTaskIcon = paper.set();
	userTaskIcon.push(path1);

	userTaskIcon.transform("T" + startX + "," + startY);
}

function _drawServiceTaskIcon(paper, startX, startY, element)
{
  var color = _bpmnGetColor(element, "#72a7d0");
	var path1 = paper.path("M 8,1 7.5,2.875 c 0,0 -0.02438,0.250763 -0.40625,0.4375 C 7.05724,3.330353 7.04387,3.358818 7,3.375 6.6676654,3.4929791 6.3336971,3.6092802 6.03125,3.78125 6.02349,3.78566 6.007733,3.77681 6,3.78125 5.8811373,3.761018 5.8125,3.71875 5.8125,3.71875 l -1.6875,-1 -1.40625,1.4375 0.96875,1.65625 c 0,0 0.065705,0.068637 0.09375,0.1875 0.002,0.00849 -0.00169,0.022138 0,0.03125 C 3.6092802,6.3336971 3.4929791,6.6676654 3.375,7 3.3629836,7.0338489 3.3239228,7.0596246 3.3125,7.09375 3.125763,7.4756184 2.875,7.5 2.875,7.5 L 1,8 l 0,2 1.875,0.5 c 0,0 0.250763,0.02438 0.4375,0.40625 0.017853,0.03651 0.046318,0.04988 0.0625,0.09375 0.1129372,0.318132 0.2124732,0.646641 0.375,0.9375 -0.00302,0.215512 -0.09375,0.34375 -0.09375,0.34375 L 2.6875,13.9375 4.09375,15.34375 5.78125,14.375 c 0,0 0.1229911,-0.09744 0.34375,-0.09375 0.2720511,0.147787 0.5795915,0.23888 0.875,0.34375 0.033849,0.01202 0.059625,0.05108 0.09375,0.0625 C 7.4756199,14.874237 7.5,15.125 7.5,15.125 L 8,17 l 2,0 0.5,-1.875 c 0,0 0.02438,-0.250763 0.40625,-0.4375 0.03651,-0.01785 0.04988,-0.04632 0.09375,-0.0625 0.332335,-0.117979 0.666303,-0.23428 0.96875,-0.40625 0.177303,0.0173 0.28125,0.09375 0.28125,0.09375 l 1.65625,0.96875 1.40625,-1.40625 -0.96875,-1.65625 c 0,0 -0.07645,-0.103947 -0.09375,-0.28125 0.162527,-0.290859 0.262063,-0.619368 0.375,-0.9375 0.01618,-0.04387 0.04465,-0.05724 0.0625,-0.09375 C 14.874237,10.52438 15.125,10.5 15.125,10.5 L 17,10 17,8 15.125,7.5 c 0,0 -0.250763,-0.024382 -0.4375,-0.40625 C 14.669647,7.0572406 14.641181,7.0438697 14.625,7 14.55912,6.8144282 14.520616,6.6141566 14.4375,6.4375 c -0.224363,-0.4866 0,-0.71875 0,-0.71875 L 15.40625,4.0625 14,2.625 l -1.65625,1 c 0,0 -0.253337,0.1695664 -0.71875,-0.03125 l -0.03125,0 C 11.405359,3.5035185 11.198648,3.4455201 11,3.375 10.95613,3.3588185 10.942759,3.3303534 10.90625,3.3125 10.524382,3.125763 10.5,2.875 10.5,2.875 L 10,1 8,1 z m 1,5 c 1.656854,0 3,1.3431458 3,3 0,1.656854 -1.343146,3 -3,3 C 7.3431458,12 6,10.656854 6,9 6,7.3431458 7.3431458,6 9,6 z");
	path1.attr({
		"opacity": 1,
		"stroke": "none",
		"fill": color
 	});
	
	var serviceTaskIcon = paper.set();
	serviceTaskIcon.push(path1);

	serviceTaskIcon.transform("T" + startX + "," + startY);
}

function _drawScriptTaskIcon(paper, startX, startY, element)
{
  var color = _bpmnGetColor(element, "#72a7d0");
	var path1 = paper.path("m 5,2 0,0.094 c 0.23706,0.064 0.53189,0.1645 0.8125,0.375 0.5582,0.4186 1.05109,1.228 1.15625,2.5312 l 8.03125,0 1,0 1,0 c 0,-3 -2,-3 -2,-3 l -10,0 z M 4,3 4,13 2,13 c 0,3 2,3 2,3 l 9,0 c 0,0 2,0 2,-3 L 15,6 6,6 6,5.5 C 6,4.1111 5.5595,3.529 5.1875,3.25 4.8155,2.971 4.5,3 4.5,3 L 4,3 z");
	path1.attr({
		"opacity": 1,
		"stroke": "none",
		"fill": color
 	});
	
	var scriptTaskIcon = paper.set();
	scriptTaskIcon.push(path1);

	scriptTaskIcon.transform("T" + startX + "," + startY);
}

function _drawBusinessRuleTaskIcon(paper, startX, startY, element)
{
  var color = _bpmnGetColor(element, "#B3B1B3");
	var rect1 = paper.rect(0, 0, 22, 4);
	rect1.attr({
		"stroke": "#000000",
		"fill": color
 	});
	
	var rect2 = paper.rect(0, 4, 22, 12);
	rect2.attr({
		"stroke": "#000000",
		"fill": "none"
 	});
	
	var path1 = paper.path("M 0 10 L 22 10");
	path1.attr({
		"stroke": "#000000",
		"fill": "none"
 	});
	
	var path2 = paper.path("M 7 4 L 7 16");
	path2.attr({
		"stroke": "#000000",
		"fill": "none"
 	});
	
	var businessRuleTaskIcon = paper.set();
	businessRuleTaskIcon.push(rect1, rect2, path1, path2);

	businessRuleTaskIcon.transform("S0.7,0.7,0,0 T" + startX + "," + startY);
}

function _drawSendTaskIcon(paper, startX, startY, element)
{
  var color = _bpmnGetColor(element, "#16964d");
	var path1 = paper.path("M8,11 L8,21 L24,21 L24,11 L16,17z");
	path1.attr({
		"stroke": "white",
		"fill": color
 	});
	
	var path2 = paper.path("M7,10 L16,17 L25 10z6");
	path2.attr({
		"stroke": "white",
		"fill": color
 	});
	
	var sendTaskIcon = paper.set();
	sendTaskIcon.push(path1, path2);

	sendTaskIcon.transform("T" + startX + "," + startY);
}

function _drawManualTaskIcon(paper, startX, startY, element)
{
  var color = _bpmnGetColor(element, "#d1b575");
	var path1 = paper.path("m 17,9.3290326 c -0.0069,0.5512461 -0.455166,1.0455894 -0.940778,1.0376604 l -5.792746,0 c 0.0053,0.119381 0.0026,0.237107 0.0061,0.355965 l 5.154918,0 c 0.482032,-0.0096 0.925529,0.49051 0.919525,1.037574 -0.0078,0.537128 -0.446283,1.017531 -0.919521,1.007683 l -5.245273,0 c -0.01507,0.104484 -0.03389,0.204081 -0.05316,0.301591 l 2.630175,0 c 0.454137,-0.0096 0.872112,0.461754 0.866386,0.977186 C 13.619526,14.554106 13.206293,15.009498 12.75924,15 L 3.7753054,15 C 3.6045812,15 3.433552,14.94423 3.2916363,14.837136 c -0.00174,0 -0.00436,0 -0.00609,0 C 1.7212035,14.367801 0.99998255,11.458641 1,11.458641 L 1,7.4588393 c 0,0 0.6623144,-1.316333 1.8390583,-2.0872584 1.1767614,-0.7711868 6.8053358,-2.40497 7.2587847,-2.8052901 0.453484,-0.40032 1.660213,1.4859942 0.04775,2.4010487 C 8.5332315,5.882394 8.507351,5.7996113 8.4370292,5.7936859 l 6.3569748,-0.00871 c 0.497046,-0.00958 0.952273,0.5097676 0.94612,1.0738232 -0.0053,0.556126 -0.456176,1.0566566 -0.94612,1.0496854 l -4.72435,0 c 0.01307,0.1149374 0.0244,0.2281319 0.03721,0.3498661 l 5.952195,0 c 0.494517,-0.00871 0.947906,0.5066305 0.940795,1.0679848 z");
	path1.attr({
		"opacity": 1,
		"stroke": "none",
		"fill": color
 	});
	
	var manualTaskIcon = paper.set();
	manualTaskIcon.push(path1);

	manualTaskIcon.transform("T" + startX + "," + startY);
}

function _drawReceiveTaskIcon(paper, startX, startY, element)
{
  var color = _bpmnGetColor(element, "#16964d");
	var path = paper.path("m 0.5,2.5 0,13 17,0 0,-13 z M 2,4 6.5,8.5 2,13 z M 4,4 14,4 9,9 z m 12,0 0,9 -4.5,-4.5 z M 7.5,9.5 9,11 10.5,9.5 15,14 3,14 z");
	path.attr({
		"opacity": 1,
		"stroke": "none",
		"fill": color
 	});
	
	startX += 4;
	startY += 2;
	
	path.transform("T" + startX + "," + startY);
	
}

function _drawEventIcon(paper, element)
{
	if (element.eventDefinition && element.eventDefinition.type)
	{
		if ("timer" === element.eventDefinition.type)
		{
			_drawTimerIcon(paper, element);
		}
		else if ("error" === element.eventDefinition.type)
		{
			_drawErrorIcon(paper, element);
		}
		else if ("signal" === element.eventDefinition.type)
		{
			_drawSignalIcon(paper, element);
		}
		else if ("message" === element.eventDefinition.type)
		{
			_drawMessageIcon(paper, element);
		}
	}
}

function _drawTimerIcon(paper, element)
{
	var x = element.x + (element.width / 2);
	var y = element.y + (element.height / 2);
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
  
	var circle = paper.circle(x, y, 10);

	circle.attr({"stroke-width": 1,
		"stroke": strokeColor,
		"fill": "none"
 	});
	
	var path = paper.path("M 16 6 L 16 9 M 21 7 L 19.5 10 M 25 11 L 22 12.5 M 26 16 L 23 16 " +
		"M 25 21 L 22 19.5 M 21 25 L 19.5 22 M 16 26 L 16 23 M 11 25 L 12.5 22 M 7 21 L 10 19.5 " +
		"M 6 16 L 9 16 M 7 11 L 10 12.5 M 11 7 L 12.5 10 M 18 9 L 16 16 L 20 16");
	path.attr({
		"stroke": strokeColor,
		"stroke-width": 1,
		"fill": "none"
 	});
	path.transform("T" + (element.x - 1) + "," + (element.y - 1));
	return path;
}

function _drawErrorIcon(paper, element)
{
	var path = paper.path("M 22.820839,11.171502 L 19.36734,24.58992 L 13.54138,14.281819 L 9.3386512,20.071607 L 13.048949,6.8323057 L 18.996148,16.132659 L 22.820839,11.171502 z");
	
	var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
	
	var fill = "none";
	var x = element.x - 1;
	var y = element.y - 1;
	if (element.type === "EndEvent")
	{
		fill = strokeColor;
		x -= 1;
		y -= 1;
	}
	
	
	path.attr({
		"stroke": strokeColor,
		"stroke-width": 1,
		"fill": fill
 	});
	
	path.transform("T" + x + "," + y);
	return path;
}

function _drawSignalIcon(paper, element)
{
  var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
  
	var fill = "none";
	if (element.type === "ThrowEvent")
	{
		fill = strokeColor;
	}
	
	var path = paper.path("M 8.7124971,21.247342 L 23.333334,21.247342 L 16.022915,8.5759512 L 8.7124971,21.247342 z");
	path.attr({
		"stroke": strokeColor,
		"stroke-width": 1,
		"fill": fill
 	});
	path.transform("T" + (element.x - 1) + "," + (element.y - 1));
	return path;
}

function _drawMessageIcon(paper, element)
{
  var strokeColor = _bpmnGetColor(element, MAIN_STROKE_COLOR);
  
	var path = paper.path("M8,11 L8,21 L24,21 L24,11z M8,11 L16,17 L24,11");
	path.attr({
		"stroke": strokeColor,
		"stroke-width": 1,
		"fill": "none"
 	});
	path.transform("T" + (element.x - 1) + "," + (element.y - 1));
	return path;
}