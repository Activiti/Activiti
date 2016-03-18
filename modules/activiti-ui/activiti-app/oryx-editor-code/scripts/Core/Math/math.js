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

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Math) {ORYX.Core.Math = {};}
	
/**
 * Calculate the middle point between two given points
 * @param {x:double, y:double} point1
 * @param {x:double, y:double} point2
 * @return the middle point
 */
ORYX.Core.Math.midPoint = function(point1, point2) {
	return 	{
				x: (point1.x + point2.x) / 2.0,
				y: (point1.y + point2.y) / 2.0
			}
}
			
/**
 * Returns a TRUE if the point is over a line (defined by
 * point1 and point 2). In Addition a threshold can be set,
 * which defines the weight of those line.
 * 
 * @param {int} pointX - Point X
 * @param {int} pointY - Point Y
 * @param {int} lPoint1X - Line first Point X
 * @param {int} lPoint1Y - Line first Point Y
 * @param {int} lPoint2X - Line second Point X
 * @param {int} lPoint2Y - Line second Point y
 * @param {int} offset {optional} - maximal distance to line
 * @class ORYX.Core.Math.prototype
 */
ORYX.Core.Math.isPointInLine = function (pointX, pointY, lPoint1X, lPoint1Y, lPoint2X, lPoint2Y, offset) {

	offset = offset ? Math.abs(offset) : 1;
	
	// Check if the edge is vertical
	if(Math.abs(lPoint1X-lPoint2X)<=offset && Math.abs(pointX-lPoint1X)<=offset && pointY-Math.max(lPoint1Y, lPoint2Y)<=offset && Math.min(lPoint1Y, lPoint2Y)-pointY<=offset) {
		return true
	}

	// Check if the edge is horizontal
	if(Math.abs(lPoint1Y-lPoint2Y)<=offset && Math.abs(pointY-lPoint1Y)<=offset && pointX-Math.max(lPoint1X, lPoint2X)<=offset && Math.min(lPoint1X, lPoint2X)-pointX<=offset) {
		return true
	}

	if(pointX > Math.max(lPoint1X, lPoint2X) || pointX < Math.min(lPoint1X, lPoint2X)) {
		return false
	}

	if(pointY > Math.max(lPoint1Y, lPoint2Y) || pointY < Math.min(lPoint1Y, lPoint2Y)) {
		return false
	}
			
	var s = (lPoint1Y - lPoint2Y) / (lPoint1X - lPoint2X);
	
	return 	Math.abs(pointY - ((s * pointX) + lPoint1Y - s * lPoint1X)) < offset
}

/**
 * Get a boolean if the point is in the polygone
 * 
 */
ORYX.Core.Math.isPointInEllipse = function (pointX, pointY, cx, cy, rx, ry) {

	if(cx === undefined || cy === undefined || rx === undefined || ry === undefined) {
		throw "ORYX.Core.Math.isPointInEllipse needs a ellipse with these properties: x, y, radiusX, radiusY"
	} 
	
    var tx = (pointX - cx) / rx;
    var ty = (pointY - cy) / ry;
	
    return tx * tx + ty * ty < 1.0;
}
	
/**
 * Get a boolean if the point is in the polygone
 * @param {int} pointX
 * @param {int} pointY
 * @param {[int]} Cornerpoints of the Polygone (x,y,x,y,...)
 */
ORYX.Core.Math.isPointInPolygone = function(pointX, pointY, polygone){

	if (arguments.length < 3) {
		throw "ORYX.Core.Math.isPointInPolygone needs two arguments"
	}
	
	var lastIndex = polygone.length-1;
	
	if (polygone[0] !== polygone[lastIndex - 1] || polygone[1] !== polygone[lastIndex]) {
		polygone.push(polygone[0]);
		polygone.push(polygone[1]);
	}
	
	var crossings = 0;

	var x1, y1, x2, y2, d;
	
    for (var i = 0; i < polygone.length - 3; ) {
        x1=polygone[i];
        y1=polygone[++i];
        x2=polygone[++i];
        y2=polygone[i+1];
        d=(pointY - y1) * (x2 - x1) - (pointX - x1) * (y2 - y1);

        if ((y1 >= pointY) != (y2 >= pointY)) {
            crossings += y2 - y1 >= 0 ? d >= 0 : d <= 0;
        }
        if (!d && Math.min(x1,x2) <= pointX && pointX <= Math.max(x1,x2)
            && Math.min(y1,y2) <= pointY && pointY <= Math.max(y1,y2)) {
            return true;
        }
    }
	return (crossings%2)?true:false;
}

/**
 *	Calculates the distance between a point and a line. It is also testable, if 
 *  the distance orthogonal to the line, matches the segment of the line.
 *  
 *  @param {float} lineP1
 *  	The starting point of the line segment
 *  @param {float} lineP2
 *  	The end point of the line segment
 *  @param {Point} point
 *  	The point to calculate the distance to.
 *  @param {boolean} toSegmentOnly
 *  	Flag to signal if only the segment of the line shell be evaluated.
 */
ORYX.Core.Math.distancePointLinie = function(
									lineP1, 
									lineP2, 
									point, 
									toSegmentOnly) {
	
	var intersectionPoint = 
				ORYX.Core.Math.getPointOfIntersectionPointLine(lineP1, 
																lineP2, 
																point, 
																toSegmentOnly);
	
	if(!intersectionPoint) {
		return null;
	}
	
	return ORYX.Core.Math.getDistancePointToPoint(point, intersectionPoint);
};

/**
 * Calculates the distance between two points.
 * 
 * @param {point} point1
 * @param {point} point2
 */
ORYX.Core.Math.getDistancePointToPoint = function(point1, point2) {
	return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
};

/**
 * Calculates the relative distance of a point which is between two other points.
 * 
 * @param {point} between1
 * @param {point} between2
 * @param {point} point
 */
ORYX.Core.Math.getDistanceBetweenTwoPoints = function(between1, between2, point) {
	return 	ORYX.Core.Math.getDistancePointToPoint(point, between1) /
			ORYX.Core.Math.getDistancePointToPoint(between1, between2);
};


/**
 * Returns true, if the point is of the left hand
 * side of the regarding the line.
 * 
 * @param {point} lineP1 Line first point
 * @param {point} lineP2 Line second point
 * @param {point} point
 */
ORYX.Core.Math.pointIsLeftOfLine = function(lineP1, lineP2, point){
	
	var vec1 = ORYX.Core.Math.getVector(lineP1, lineP2);
	var vec2 = ORYX.Core.Math.getVector(lineP1, point);
	// if the cross produkt is more than 0
	return ((vec1.x*vec2.y) - (vec2.x*vec1.y)) > 0
};

/**
 * Calculates the a point which is relatively between two other points.
 * 
 * @param {point} point1
 * @param {point} point2
 * @param {number} relative Relative which is between 0 and 1
 */
ORYX.Core.Math.getPointBetweenTwoPoints = function(point1, point2, relative) {
	relative = Math.max(Math.min(relative || 0, 1), 0);
	
	if (relative === 0){
		return point1;
	} else if (relative === 1){
		return point2;
	}
	
	return {
		x: point1.x + ((point2.x - point1.x) * relative),
		y: point1.y + ((point2.y - point1.y) * relative)
	}
};


/**
 * Returns the vector of the both points
 *
 * @param {point} point1
 * @param {point} point2
 */
ORYX.Core.Math.getVector = function(point1, point2){
	return {
		x: point2.x - point1.x,
		y: point2.y - point1.y
	}
}

/**
 * Returns the an identity vector of the given vector, 
 * which has the length ot one.
 *
 * @param {point} vector
 * or 
 * @param {point} point1
 * @param {point} point2
 */
ORYX.Core.Math.getIdentityVector = function(vector){
	
	if (arguments.length == 2){
		vector = ORYX.Core.Math.getVector(arguments[0], arguments[1]);
	}
	
	var length = Math.sqrt((vector.x*vector.x)+(vector.y*vector.y))
	return {
		x: vector.x / (length || 1),
		y: vector.y / (length || 1)
	}
}


ORYX.Core.Math.getOrthogonalIdentityVector = function(point1, point2){
	var vec = arguments.length == 1 ? point1 : ORYX.Core.Math.getIdentityVector(point1, point2);
	return {
		x: vec.y,
		y: -vec.x
	}
}


/**
 * Returns the intersection point of a line and a point that defines a line
 * orthogonal to the given line.
 * 
 *  @param {float} lineP1
 *  	The starting point of the line segment
 *  @param {float} lineP2
 *  	The end point of the line segment
 *  @param {Point} point
 *  	The point to calculate the distance to.
 *  @param {boolean} onSegmentOnly
 *  	Flag to signal if only the segment of the line shell be evaluated.
 */
ORYX.Core.Math.getPointOfIntersectionPointLine = function(
													lineP1, 
													lineP2, 
													point, 
													onSegmentOnly) {

	/* 
	 * [P3 - P1 - u(P2 - P1)] dot (P2 - P1) = 0
	 * u =((x3-x1)(x2-x1)+(y3-y1)(y2-y1))/(p2-p1)²
	 */
	var denominator = Math.pow(lineP2.x - lineP1.x, 2) 
						+ Math.pow(lineP2.y - lineP1.y, 2);
	if(denominator == 0) {
		return undefined;
	}
	
	var u = ((point.x - lineP1.x) * (lineP2.x - lineP1.x)  
			+ (point.y - lineP1.y) * (lineP2.y - lineP1.y))
			/ denominator;
			
	if(onSegmentOnly) {
		if (!(0 <= u && u <= 1)) {
			return undefined;
		}
	}
	
	pointOfIntersection = new Object();
	pointOfIntersection.x = lineP1.x + u * (lineP2.x - lineP1.x);
	pointOfIntersection.y = lineP1.y + u * (lineP2.y - lineP1.y);	
	
	return pointOfIntersection;												
};

/**
 * Translated the point with the given matrix.
 * @param {Point} point
 * @param {Matrix} matrix 
 * @return {Object} Includes x, y
 */
ORYX.Core.Math.getTranslatedPoint = function(point, matrix){
	var x = matrix.a*point.x+matrix.c*point.y+matrix.e*1;
	var y = matrix.b*point.x+matrix.d*point.y+matrix.f*1;
    return {x:x, y:y}
}


/**
 * Returns the inverse matrix of the given SVG transformation matrix
 * @param {SVGTransformationMatrix} matrix
 * @return {Matrix}
 */
ORYX.Core.Math.getInverseMatrix = function(matrix){

	var det = ORYX.Core.Math.getDeterminant(matrix), m = matrix;
	// +-     -+
	// | a c e |
	// | b d f |
	// | 0 0 1 |
	// +-     -+
	return {
		a: det * ((m.d*1)-(m.f*0)),
		b: det * ((m.f*0)-(m.b*1)),
		c: det * ((m.e*0)-(m.c*1)),
		d: det * ((m.a*1)-(m.e*0)),
		e: det * ((m.c*m.f)-(m.e*m.d)),
		f: det * ((m.e*m.b)-(m.a*m.f))
	}
}

/**
 * Returns the determinant of the svg transformation matrix
 * @param {SVGTranformationMatrix} matrix
 * @return {Number}
 *
 */
ORYX.Core.Math.getDeterminant = function(m){
	// a11a22a33+a12a23a31+a13a21a32-a13a22a31-a12a21a33-a11a23a32
	return (m.a*m.d*1)+(m.c*m.f*0)+(m.e*m.b*0)-(m.e*m.d*0)-(m.c*m.b*1)-(m.a*m.f*0);
}

/**
 * Returns the bounding box of the given node. Translates the 
 * origin bounding box with the tranlation matrix.
 * @param {SVGElement} node
 * @return {Object} Includes x, y, width, height
 */
ORYX.Core.Math.getTranslatedBoundingBox = function(node){
	var matrix = node.getCTM();
	var bb = node.getBBox();
	var ul = ORYX.Core.Math.getTranslatedPoint({x:bb.x, y:bb.y}, matrix);
	var ll = ORYX.Core.Math.getTranslatedPoint({x:bb.x, y:bb.y+bb.height}, matrix);
	var ur = ORYX.Core.Math.getTranslatedPoint({x:bb.x+bb.width, y:bb.y}, matrix);
	var lr = ORYX.Core.Math.getTranslatedPoint({x:bb.x+bb.width, y:bb.y+bb.height}, matrix);
	
	var minPoint = {
	    x: Math.min(ul.x, ll.x, ur.x, lr.x),
	    y: Math.min(ul.y, ll.y, ur.y, lr.y)
	}
	var maxPoint = {
	    x: Math.max(ul.x, ll.x, ur.x, lr.x),
	    y: Math.max(ul.y, ll.y, ur.y, lr.y)
	}
	return {
	    x: minPoint.x,
	    y: minPoint.y,
	    width: maxPoint.x - minPoint.x,
	    height: maxPoint.y - minPoint.y
	}
};


/**
 * Returns the angle of the given line, which is representated by the two points
 * @param {Point} p1
 * @param {Point} p2
 * @return {Number} 0 <= x <= 359.99999
 */
ORYX.Core.Math.getAngle = function(p1, p2){
	if(p1.x == p2.x && p1.y == p2.y)
		return 0;

	var angle = Math.asin(Math.sqrt(Math.pow(p1.y-p2.y, 2))
				/(Math.sqrt(Math.pow(p2.x-p1.x, 2)+Math.pow(p1.y-p2.y, 2))))
				*180/Math.PI;
	
	if(p2.x >= p1.x && p2.y <= p1.y)
		return angle;
	else if(p2.x < p1.x && p2.y <= p1.y)
		return 180 - angle;
	else if(p2.x < p1.x && p2.y > p1.y)
		return 180 + angle;
	else
		return 360 - angle;
};


/**
 * Implementation of the cohen-sutherland algorithm
 */
new function(){
	
	var RIGHT = 2, TOP = 8,  BOTTOM = 4, LEFT = 1;
	
 	function computeOutCode (x, y, xmin, ymin, xmax, ymax) {
		var code = 0;
		if (y > ymax)
		    code |= TOP;
		else if (y < ymin)
		    code |= BOTTOM;
		if (x > xmax)
		    code |= RIGHT;
		else if (x < xmin)
		    code |= LEFT;
		return code;
	}
	
	/**
	 * Returns TRUE if the rectangle is over the edge and has intersection points or includes it
	 * @param {Object} x1 Point A of the line
	 * @param {Object} y1
	 * @param {Object} x2 Point B of the line
	 * @param {Object} y2
	 * @param {Object} xmin Point A of the rectangle
	 * @param {Object} ymin
	 * @param {Object} xmax Point B of the rectangle
	 * @param {Object} ymax
	 */
	ORYX.Core.Math.isRectOverLine = function(x1, y1, x2, y2, xmin, ymin, xmax, ymax){
		return !!ORYX.Core.Math.clipLineOnRect.apply(ORYX.Core.Math, arguments);
	}
	
	/**
	 * Returns the clipped line on the given rectangle. If there is 
	 * no intersection, it will return NULL.
	 *  
	 * @param {Object} x1 Point A of the line
	 * @param {Object} y1
	 * @param {Object} x2 Point B of the line
	 * @param {Object} y2
	 * @param {Object} xmin Point A of the rectangle
	 * @param {Object} ymin
	 * @param {Object} xmax Point B of the rectangle
	 * @param {Object} ymax
	 */
	ORYX.Core.Math.clipLineOnRect = function(x1, y1, x2, y2, xmin, ymin, xmax, ymax){
        //Outcodes for P0, P1, and whatever point lies outside the clip rectangle
        var outcode0, outcode1, outcodeOut, hhh = 0;
        var accept = false, done = false;
 
        //compute outcodes
        outcode0 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
        outcode1 = computeOutCode(x2, y2, xmin, ymin, xmax, ymax);
 
        do {
            if ((outcode0 | outcode1) == 0 ){
                accept = true;
                done = true;
            } else if ( (outcode0 & outcode1) > 0 ) {
                done = true;
            } else {
                //failed both tests, so calculate the line segment to clip
                //from an outside point to an intersection with clip edge
                var x = 0, y = 0;
                //At least one endpoint is outside the clip rectangle; pick it.
                outcodeOut = outcode0 != 0 ? outcode0: outcode1;
                //Now find the intersection point;
                //use formulas y = y0 + slope * (x - x0), x = x0 + (1/slope)* (y - y0)
                if ( (outcodeOut & TOP) > 0 ) {
                    x = x1 + (x2 - x1) * (ymax - y1)/(y2 - y1);
                    y = ymax;
                } else if ((outcodeOut & BOTTOM) > 0 ) {
                    x = x1 + (x2 - x1) * (ymin - y1)/(y2 - y1);
                    y = ymin;
                } else if ((outcodeOut & RIGHT)> 0) {
                    y = y1 + (y2 - y1) * (xmax - x1)/(x2 - x1);
                    x = xmax;
                } else if ((outcodeOut & LEFT) > 0) {
                    y = y1 + (y2 - y1) * (xmin - x1)/(x2 - x1);
                    x = xmin;
                }
				
                //Now we move outside point to intersection point to clip
                //and get ready for next pass.
                if (outcodeOut == outcode0) {
                    x1 = x;
                    y1 = y;
                    outcode0 = computeOutCode (x1, y1, xmin, ymin, xmax, ymax);
                } else {
                    x2 = x;
                    y2 = y;
                    outcode1 = computeOutCode (x2, y2, xmin, ymin, xmax, ymax);
                }
            }
            hhh ++;
        } while (done  != true && hhh < 5000);
 
        if(accept) {
            return {a:{x:x1, y:y1}, b:{x:x2, y:y2}};
        }
		return null;
    }
}();


