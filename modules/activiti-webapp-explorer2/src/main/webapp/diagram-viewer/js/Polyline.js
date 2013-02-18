/**
 * Class to generate polyline
 *
 * @author Dmitry Farafonov
 */
 
var ANCHOR_TYPE= {
	main: "main",
	middle: "middle",
	first: "first",
	last: "last"
};

function Anchor(uuid, type, x, y) {
	this.uuid = uuid; 
	this.x = x
	this.y = y
	this.type = (type == ANCHOR_TYPE.middle) ? ANCHOR_TYPE.middle : ANCHOR_TYPE.main;
};
Anchor.prototype = {
	uuid: null,
	x: 0,
	y: 0,
	type: ANCHOR_TYPE.main,
	isFirst: false,
	isLast: false,
	ndex: 0,
	typeIndex: 0
};

function Polyline(uuid, points, strokeWidth) {
	/* Array on coordinates:
	 * points: [{x: 410, y: 110}, 1
	 *			{x: 570, y: 110}, 1 2
	 *			{x: 620, y: 240},   2 3
	 *			{x: 750, y: 270},     3 4
	 *			{x: 650, y: 370}];      4
	 */
	this.points = points;
	
	/*
	 * path for graph
	 * [["M", x1, y1], ["L", x2, y2], ["C", ax, ay, bx, by, x3, y3], ["L", x3, y3]]
	 */
	this.path = [];
	
	this.anchors = [];
	
	if (strokeWidth) this.strokeWidth = strokeWidth;
	
	this.closePath = false;
	
	this.init();
};

Polyline.prototype = {
	id: null,
	points: [],
	path: [],
	anchors: [],
	strokeWidth: 1,
	radius: 15,
	showDetails: false,
	element: null,
	isDefaultConditionAvailable: false,
	closePath: false,
	
	init: function(points){
		var linesCount = this.getLinesCount();
		if (linesCount < 1)
			return;
			
		this.normalizeCoordinates();
		
		// create anchors
		
		this.pushAnchor(ANCHOR_TYPE.first, this.getLine(0).x1, this.getLine(0).y1);
		
		for(var i = 1; i < linesCount; i++){
			var line1 = this.getLine(i-1),
				line2 = this.getLine(i);
			
			//this.pushAnchor(ANCHOR_TYPE.middle, line1.x1 + line1.x2-line1.x1, line1.y1 + line1.y2-line1.y1);
			this.pushAnchor(ANCHOR_TYPE.main,  line1.x2, line1.y2);
			//this.pushAnchor(ANCHOR_TYPE.middle,  line2.x1 + line2.x2-line2.x1, line2.y1 + line2.y2-line2.y1);
		}
		
		this.pushAnchor(ANCHOR_TYPE.last, this.getLine(linesCount-1).x2, this.getLine(linesCount-1).y2);
		
		this.rebuildPath();
	},
	
	normalizeCoordinates: function(){
		for(var i=0; i < this.points.length; i++){
			this.points[i].x = parseFloat(this.points[i].x);
			this.points[i].y = parseFloat(this.points[i].y);
		}
	},
	
	getLinesCount: function(){
		return this.points.length-1;
	},
	_getLine: function(i){
		return {x1: this.points[i].x, y1: this.points[i].y, x2: this.points[i+1].x, y2: this.points[i+1].y};
	},
	getLine: function(i){
		var line = this._getLine(i);
		line.angle = this.getLineAngle(i) ;
		return line;
	},
	getLineAngle: function(i){
		var line = this._getLine(i);
		return Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
	},
	getLineLengthX: function(i){
		var line = this.getLine(i);
		return (line.x2 - line.x1);
	},
	getLineLengthY: function(i){
		var line = this.getLine(i);
		return (line.y2 - line.y1);
	},
	getLineLength: function(i){
		var line = this.getLine(i);
		return Math.sqrt(Math.pow(this.getLineLengthX(i), 2) + Math.pow(this.getLineLengthY(i), 2));
	},
	
	getAnchors: function(){
		// вернуть отсортированный массив
		// ????
		return this.anchors;
	},
	getAnchorsCount: function(type){
		if (!type)
			return this.anchors.length;
		else {
			var count = 0;
			for(var i=0; i < this.getAnchorsCount(); i++){
				var anchor = this.anchors[i];
				if (anchor.getType() == type) {
					count++;
				}
			}
			return count;
		}
	},
	
	pushAnchor: function(type, x, y, index){
		if (type == ANCHOR_TYPE.first) {
			index = 0;
			typeIndex = 0;
		} else if (type == ANCHOR_TYPE.last) {
			index = this.getAnchorsCount();
			typeIndex = 0;
		} else if (!index) {
			index = this.anchors.length;
		} else {
			// перебрать anchors, сдвинуть позицию для каждого, начиная с index
			//var anchor = this.getAnchor()
			for(var i=0; i < this.getAnchorsCount(); i++){
				var anchor = this.anchors[i];
				if (anchor.index > index) {
					anchor.index++;
					anchor.typeIndex++;
				}
			}
		}
		
		var anchor = new Anchor(this.id, ANCHOR_TYPE.main, x, y, index, typeIndex);
		
		this.anchors.push(anchor);
	},
	
	getAnchor: function(position){
		return this.anchors[position];
	},
	
	getAnchorByType: function(type, position){
		if (type == ANCHOR_TYPE.first)
			return this.anchors[0];
		if (type == ANCHOR_TYPE.last)
			return this.anchors[this.getAnchorsCount()-1];
		
		for(var i=0; i < this.getAnchorsCount(); i++){
			var anchor = this.anchors[i];
			if (anchor.type == type) {
				if( position == anchor.position)
					return anchor;
			}
		}
		return null;
	},
	
	addNewPoint: function(position, x, y){
		// 
		for(var i = 0; i < this.getLinesCount(); i++){
			var line = this.getLine(i);
			if (x > line.x1 && x < line.x2 && y > line.y1 && y < line.y2) {
				this.points.splice(i+1,0,{x: x, y: y});
				break;
			}
		}
		
		this.rebuildPath();
	},
	
	rebuildPath: function(){
		var path = [];
		
		for(var i = 0; i < this.getAnchorsCount(); i++){
			var anchor = this.getAnchor(i);
			
			var pathType = ""
			if (i==0)
				pathType = "M";
			else 
				pathType = "L";
			
// TODO: save previous points and calculate new path just if points are updated, and then save currents values as previous
			
			var targetX = anchor.x, targetY = anchor.y;
			if (i>0 && i < this.getAnchorsCount()-1) {
				// get new x,y
				var cx = anchor.x, cy = anchor.y;
				
				// pivot point of prev line
				var AO = this.getLineLength(i-1);
				if (AO < this.radius) {
					AO = this.radius;
				}
				
				this.isDefaultConditionAvailable = (this.isDefaultConditionAvailable || (i == 1 && AO > 10));
				//console.log("isDefaultConditionAvailable", this.isDefaultConditionAvailable);
				
				var ED = this.getLineLengthY(i-1) * this.radius / AO;
				var OD = this.getLineLengthX(i-1) * this.radius / AO;
					targetX = anchor.x - OD;
					targetY = anchor.y - ED;
				
				if (AO < 2*this.radius && i>1) {
					targetX = anchor.x - this.getLineLengthX(i-1)/2;
					targetY = anchor.y - this.getLineLengthY(i-1)/2;;
				}
					
				// pivot point of next line
				var AO = this.getLineLength(i);
				if (AO < this.radius) {
					AO = this.radius;
				}
				var ED = this.getLineLengthY(i) * this.radius / AO;
				var OD = this.getLineLengthX(i) * this.radius / AO;
					var nextSrcX = anchor.x + OD;
					var nextSrcY = anchor.y + ED;
					
				if (AO < 2*this.radius && i<this.getAnchorsCount()-2) {
					nextSrcX = anchor.x + this.getLineLengthX(i)/2;
					nextSrcY = anchor.y + this.getLineLengthY(i)/2;;
				}
					
				
				var dx0 = (cx - targetX) / 3,
					dy0 = (cy - targetY) / 3,
					ax = cx - dx0,
					ay = cy - dy0,
					
					dx1 = (cx - nextSrcX) / 3,
					dy1 = (cy - nextSrcY) / 3,
					bx = cx - dx1,
					by = cy - dy1,
					
					zx=nextSrcX, zy=nextSrcY;
					
				if (this.showDetails) {
					var c = ProcessDiagramCanvas.g.path("M"+targetX+","+targetY+"L"+ax+","+ay).attr({stroke: Color.get(255, 153, 51), "stroke-dasharray": "- "});
					var c = ProcessDiagramCanvas.g.path("M"+nextSrcX+","+nextSrcY+"L"+bx+","+by).attr({stroke: Color.get(255, 153, 51), "stroke-dasharray": "- "});
					var c = ProcessDiagramCanvas.g.ellipse(ax, ay, 2, 2).attr({stroke: Color.SlateGrey});
					var c = ProcessDiagramCanvas.g.ellipse(bx, by, 2, 2).attr({stroke: Color.SlateGrey});
					var c = ProcessDiagramCanvas.g.ellipse(cx, cy, this.radius, this.radius).attr({stroke: Color.Gainsboro});
					var c = ProcessDiagramCanvas.g.ellipse(targetX, targetY, 2, 2).attr({fill: Color.red});
					var c = ProcessDiagramCanvas.g.ellipse(nextSrcX, nextSrcY, 2, 2).attr({fill: Color.red});
				}
			} else if (i==1 && this.getAnchorsCount() == 2){
				var AO = this.getLineLength(i-1);
				if (AO < this.radius) {
					AO = this.radius;
				}
				this.isDefaultConditionAvailable = (this.isDefaultConditionAvailable || (i == 1 && AO > 10));
				//console.log("-- isDefaultConditionAvailable", this.isDefaultConditionAvailable);
			}

			// anti smoothing
			if (this.strokeWidth%2 == 1) {
				targetX += 0.5;
				targetY += 0.5;
			}
			
			path.push([pathType, targetX, targetY]);
			
			if (i>0 && i < this.getAnchorsCount()-1) {
				path.push(["C", ax, ay, bx, by, zx, zy]);
			}
		}
		
		if (this.closePath) {
			console.log("closePath:", this.closePath);
			path.push(["Z"]);
		}
		
		this.path = path;
	},
	
	transform: function(transformation){
		this.element.transform(transformation);
	},
	attr: function(attrs){
		//console.log("attrs: " +attrs, "", this.element);
		// TODO: foreach and set each
		this.element.attr(attrs);
	}
};

function Polygone(points, strokeWidth) {
	/* Array on coordinates:
	 * points: [{x: 410, y: 110}, 1
	 *			{x: 570, y: 110}, 1 2
	 *			{x: 620, y: 240},   2 3
	 *			{x: 750, y: 270},     3 4
	 *			{x: 650, y: 370}];      4
	 */
	this.points = points;
	
	/*
	 * path for graph
	 * [["M", x1, y1], ["L", x2, y2], ["C", ax, ay, bx, by, x3, y3], ["L", x3, y3]]
	 */
	this.path = [];
	
	this.anchors = [];
	
	if (strokeWidth) this.strokeWidth = strokeWidth;
	
	this.closePath = true;
	this.init();
};


/*
 * Poligone is inherited from Poliline: draws closedPath of polyline
 */

var Foo = function () { };
Foo.prototype = Polyline.prototype;

Polygone.prototype = new Foo();

Polygone.prototype.rebuildPath = function(){
	var path = [];
	//console.log("Polygone rebuildPath");
	for(var i = 0; i < this.getAnchorsCount(); i++){
		var anchor = this.getAnchor(i);
		
		var pathType = ""
		if (i==0)
			pathType = "M";
		else 
			pathType = "L";
		
		var targetX = anchor.x, targetY = anchor.y;
		
		// anti smoothing
		if (this.strokeWidth%2 == 1) {
			targetX += 0.5;
			targetY += 0.5;
		}
		
		path.push([pathType, targetX, targetY]);	
	}
	if (this.closePath)
		path.push(["Z"]);
	
	this.path = path;
};
/*
Polygone.prototype.transform = function(transformation){
	this.element.transform(transformation);
};
*/