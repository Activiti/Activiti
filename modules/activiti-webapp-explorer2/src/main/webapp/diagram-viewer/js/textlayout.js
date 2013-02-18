	window.onload = function () {
			var paper = Raphael("holder");

			//var curve = paper.ellipse(100, 100, 1, 1).attr({"stroke-width": 0, fill: Color.red});
			
			var text = "Betty Botter bought some butter but, she said, the butter's bitter. If I put it in my batter, it will make my batter bitter. But a bit of better butter will make my batter better. So, she bought a bit of butter, better than her bitter butter, and she put it in her batter, and the batter was not bitter. It was better Betty Botter bought a bit better butter.";
			var font = {font: "11px Arial", "font-style":"italic", opacity: 1, "fill": LABEL_COLOR, stroke: LABEL_COLOR, "stroke-width":.3};
			var font = {font: "11px Arial", opacity: 1, "fill": LABEL_COLOR};
			var boxWidth = 100
			
			var AttributedStringIterator = function(text){
				//this.text = this.rtrim(this.ltrim(text));
				text = text.replace(/(\s)+/, " ");
				this.text = this.rtrim(text);
				/*
				if (beginIndex < 0 || beginIndex > endIndex || endIndex > length()) {
					throw new IllegalArgumentException("Invalid substring range");
				}
				*/
				this.beginIndex = 0;
				this.endIndex = this.text.length;
				this.currentIndex = this.beginIndex;
				
				//console.group("[AttributedStringIterator]");
				var i = 0;
				var string = this.text;
				var fullPos = 0;
				
				//console.log("string: \"" + string + "\", length: " + string.length);
				this.startWordOffsets = [];
				this.startWordOffsets.push(fullPos);
				
				// TODO: remove i 1000
				while (i<1000) {
					var pos = string.search(/[ \t\n\f-\.\,]/);
					if (pos == -1)
						break;
					
					// whitespace start
					fullPos += pos;
					string = string.substr(pos);
					////console.log("fullPos: " + fullPos + ", pos: " + pos +  ", string: ", string);
					
					// remove whitespaces
					var pos = string.search(/[^ \t\n\f-\.\,]/);
					if (pos == -1)
						break;
						
					// whitespace end
					fullPos += pos;
					string = string.substr(pos);
					
					////console.log("fullPos: " + fullPos);
					this.startWordOffsets.push(fullPos);
					
					i++;
				}
				//console.log("startWordOffsets: ", this.startWordOffsets);
				//console.groupEnd();
			};
			AttributedStringIterator.prototype = {
				getEndIndex: function(pos){
					if (typeof(pos) == "undefined")
						return this.endIndex;
						
					var string = this.text.substr(pos, this.endIndex - pos);
					
					var posEndOfLine = string.search(/[\n]/);
					if (posEndOfLine == -1)
						return this.endIndex;
					else
						return pos + posEndOfLine;
				},
				getBeginIndex: function(){
					return this.beginIndex;
				},
				isWhitespace: function(pos){
					var str = this.text[pos];
					var whitespaceChars = " \t\n\f";
					
					return (whitespaceChars.indexOf(str) != -1);
				},
				isNewLine: function(pos){
					var str = this.text[pos];
					var whitespaceChars = "\n";
					
					return (whitespaceChars.indexOf(str) != -1);
				},
				preceding: function(pos){
					//console.group("[AttributedStringIterator.preceding]");
					for(var i in this.startWordOffsets) {
						var startWordOffset = this.startWordOffsets[i];
						if (pos < startWordOffset && i>0) {
							//console.log("startWordOffset: " + this.startWordOffsets[i-1]);
							//console.groupEnd();
							return this.startWordOffsets[i-1];
						}
					}
					//console.log("pos: " + pos);
					//console.groupEnd();
					return this.startWordOffsets[i];
				},
				following: function(pos){
					//console.group("[AttributedStringIterator.following]");
					for(var i in this.startWordOffsets) {
						var startWordOffset = this.startWordOffsets[i];
						if (pos < startWordOffset && i>0) {
							//console.log("startWordOffset: " + this.startWordOffsets[i]);
							//console.groupEnd();
							return this.startWordOffsets[i];
						}
					}
					//console.log("pos: " + pos);
					//console.groupEnd();
					return this.startWordOffsets[i];
				},
				ltrim: function(str){
					var patt2=/^\s+/g;
					return str.replace(patt2, "");
				}, 
				rtrim: function(str){
					var patt2=/\s+$/g;
					return str.replace(patt2, "");
				},
				getLayout: function(start, limit){
					return this.text.substr(start, limit - start);
				},
				getCharAtPos: function(pos) {
					return this.text[pos];
				}
			};
			
			/*
			var TextMeasurer = function(paper, text, fontAttrs){
				this.text = text;
				this.paper = paper;
				this.fontAttrs = fontAttrs;
				
				this.fStart = this.text.getBeginIndex();

			};
			TextMeasurer.prototype = {
				getLineBreakIndex: function(start, maxAdvance){
					var localStart = start - this.fStart;
				},
				getLayout: function(){
				}
			}
			*/
			
			
			var LineBreakMeasurer = function(paper, text, fontAttrs){
				this.paper = paper;
				this.text = new AttributedStringIterator(text);
				this.fontAttrs = fontAttrs;
				
				if (this.text.getEndIndex() - this.text.getBeginIndex() < 1) {
					throw {message: "Text must contain at least one character.", code: "IllegalArgumentException"};
				}
				
				//this.measurer = new TextMeasurer(paper, this.text, this.fontAttrs);
				this.limit = this.text.getEndIndex();
				this.pos = this.start = this.text.getBeginIndex();
				
				this.rafaelTextObject = this.paper.text(100, 100, this.text.text).attr(fontAttrs).attr("text-anchor", "start");
				this.svgTextObject = this.rafaelTextObject[0];
			};
			LineBreakMeasurer.prototype = {
				nextOffset: function(wrappingWidth, offsetLimit, requireNextWord) {
					//console.group("[nextOffset]");
					var nextOffset = this.pos;
					if (this.pos < this.limit) {
						if (offsetLimit <= this.pos) {
							throw {message: "offsetLimit must be after current position", code: "IllegalArgumentException"};
						}
						
						var charAtMaxAdvance = this.getLineBreakIndex(this.pos, wrappingWidth);
						//charAtMaxAdvance --;
						//console.log("charAtMaxAdvance:", charAtMaxAdvance, ", [" + this.text.getCharAtPos(charAtMaxAdvance) + "]");
						
						if (charAtMaxAdvance == this.limit) {
							nextOffset = this.limit;
							//console.log("charAtMaxAdvance == this.limit");
						} else if (this.text.isNewLine(charAtMaxAdvance)) {
							console.log("isNewLine");
							nextOffset = charAtMaxAdvance+1;
						} else if (this.text.isWhitespace(charAtMaxAdvance)) {
							// TODO: find next noSpaceChar
							//return nextOffset;
							nextOffset = this.text.following(charAtMaxAdvance);
						} else {
							// Break is in a word;  back up to previous break.
							/*
							var testPos = charAtMaxAdvance + 1;
							if (testPos == this.limit) {
								console.error("hbz...");
							} else {
								nextOffset = this.text.preceding(charAtMaxAdvance);
							}
							*/
							nextOffset = this.text.preceding(charAtMaxAdvance);
							
							if (nextOffset <= this.pos) {
								nextOffset = Math.max(this.pos+1, charAtMaxAdvance);
							}
						}
					}
					if (nextOffset > offsetLimit) {
						nextOffset = offsetLimit;
					}
					//console.log("nextOffset: " + nextOffset);
					//console.groupEnd();
					return nextOffset;
				},
				nextLayout: function(wrappingWidth) {
					//console.groupCollapsed("[nextLayout]");
					if (this.pos < this.limit) {
						var requireNextWord = false;
						var layoutLimit = this.nextOffset(wrappingWidth, this.limit, requireNextWord);
						//console.log("layoutLimit:", layoutLimit);
						if (layoutLimit == this.pos) {
							//console.groupEnd();
							return null;
						}
						var result = this.text.getLayout(this.pos, layoutLimit);
						//console.log("layout: \"" + result + "\"");
						
						// remove end of line
						
						//var posEndOfLine = this.text.getEndIndex(this.pos);
						//if (posEndOfLine < result.length)
						//	result = result.substr(0, posEndOfLine);
						
						this.pos = layoutLimit;
						
						//console.groupEnd();
						return result;
					} else {
						//console.groupEnd();
						return null;
					}
				},
				getLineBreakIndex: function(pos, wrappingWidth) {
					//console.group("[getLineBreakIndex]");
					//console.log("pos:"+pos + ", text: \""+ this.text.text.replace(/\n/g, "_").substr(pos, 1) + "\"");
					
					var bb = this.rafaelTextObject.getBBox();
					
					var charNum = -1;
					try {
						var svgPoint = this.svgTextObject.getStartPositionOfChar(pos);
						//var dot = this.paper.ellipse(svgPoint.x, svgPoint.y, 1, 1).attr({"stroke-width": 0, fill: Color.blue});
						svgPoint.x = svgPoint.x + wrappingWidth;
						//svgPoint.y = bb.y;
						//console.log("svgPoint:", svgPoint);
					
						//var dot = this.paper.ellipse(svgPoint.x, svgPoint.y, 1, 1).attr({"stroke-width": 0, fill: Color.red});
					
						charNum = this.svgTextObject.getCharNumAtPosition(svgPoint);
					} catch (e){
						console.warn("getStartPositionOfChar error, pos:" + pos);
						/*
						var testPos = pos + 1;
						if (testPos < this.limit) {
							return testPos
						}
						*/
					}
					//console.log("charNum:", charNum);
					if (charNum == -1) {
						//console.groupEnd();
						return this.text.getEndIndex(pos);
					} else {
						// When case there is new line between pos and charnum then use this new line
						var newLineIndex = this.text.getEndIndex(pos);
						if (newLineIndex < charNum ) {
							console.log("newLineIndex <= charNum, newLineIndex:"+newLineIndex+", charNum:"+charNum, "\"" + this.text.text.substr(newLineIndex+1).replace(/\n/g, "↵") + "\"");
							//console.groupEnd();
							
							return newLineIndex;
						}
							
						//var charAtMaxAdvance  = this.text.text.substring(charNum, charNum + 1);
						var charAtMaxAdvance  = this.text.getCharAtPos(charNum);
						//console.log("!!charAtMaxAdvance: " + charAtMaxAdvance);
						//console.groupEnd();
						return charNum;
					}
				}, 
				getPosition: function() {
					return this.pos;
				}
			};
			
			
			
			// ******
			function drawMultilineText(text, x, y, boxWidth, boxHeight, options) {
				var TEXT_PADDING = 3;
				var width = boxWidth - (2 * TEXT_PADDING);
				if (boxHeight)
					var height = boxHeight - (2 * TEXT_PADDING);
			
				var layouts = [];
				
				var measurer = new LineBreakMeasurer(paper, text, font);
				var lineHeight = measurer.rafaelTextObject.getBBox().height;
				console.log("text: ", text.replace(/\n/g, "↵"));
				
				if (height) {
					var availableLinesCount = parseInt(height/lineHeight);
					console.log("availableLinesCount: " + availableLinesCount);
				}
				
				var i = 1;
				while (measurer.getPosition() < measurer.text.getEndIndex()) {
					var layout = measurer.nextLayout(width);
					//console.log("LAYOUT: " + layout + ", getPosition: " + measurer.getPosition());
					
					if (layout != null) {
						if (!availableLinesCount || i < availableLinesCount) {
							layouts.push(layout);
						} else {
							layouts.push(fitTextToWidth(layout + "...", boxWidth));
							break;
						}
					}
					i++;
				};
				console.log(layouts);
				
				measurer.rafaelTextObject.attr({"text": layouts.join("\n")});
				//measurer.rafaelTextObject.attr({"text-anchor": "end"});
				//measurer.rafaelTextObject.attr({"text-anchor": "middle"});
				if (options)
					measurer.rafaelTextObject.attr({"text-anchor": options["text-anchor"]});
					
				var bb = measurer.rafaelTextObject.getBBox();
				//measurer.rafaelTextObject.attr({"x": x + boxWidth/2});
				if (options["vertical-align"] == "top")
					measurer.rafaelTextObject.attr({"y": y + bb.height/2 + TEXT_PADDING});
				else
					measurer.rafaelTextObject.attr({"y": y + height/2});
				//var bb = measurer.rafaelTextObject.getBBox();
				
				if (measurer.rafaelTextObject.attr("text-anchor") == "middle" )
					measurer.rafaelTextObject.attr("x",  x + boxWidth/2 + TEXT_PADDING/2);
				else if (measurer.rafaelTextObject.attr("text-anchor") == "end" )
					measurer.rafaelTextObject.attr("x",  x + boxWidth + TEXT_PADDING/2);
				else 
					measurer.rafaelTextObject.attr("x", x + boxWidth/2 - bb.width/2 + TEXT_PADDING/2);
				
				var boxStyle = {stroke: Color.LightSteelBlue2, "stroke-width": 1.0, "stroke-dasharray": "- "};
				/*
				var box = paper.rect(x+.0 + boxWidth/2 - bb.width/2+ TEXT_PADDING/2, y + .5 + boxHeight/2 - bb.height/2, width, height).attr(boxStyle);
				box.attr("height", bb.height);
				*/
				//var box = paper.rect(bb.x - .5 + bb.width/2 + TEXT_PADDING, bb.y + bb.height/2, bb.width, bb.height).attr(boxStyle);
				
				var textAreaCX = x + boxWidth/2;
				var textAreaCY = y + height/2;
				var dotLeftTop = paper.ellipse(x, y, 3, 3).attr({"stroke-width": 0, fill: Color.LightSteelBlue, stroke: "none"});
				var dotCenter = paper.ellipse(textAreaCX, textAreaCY, 3, 3).attr({fill: Color.LightSteelBlue2, stroke: "none"});

				/*
				// real bbox
				var bb = measurer.rafaelTextObject.getBBox();
				var rect = paper.rect(bb.x+.5, bb.y + .5, bb.width, bb.height).attr({"stroke-width": 1});
				*/
				var boxStyle = {stroke: Color.LightSteelBlue2, "stroke-width": 1.0, "stroke-dasharray": "- "};
				var rect = paper.rect(x+.5, y + .5, boxWidth, boxHeight).attr(boxStyle);
			}
			
			
			
			
			/*
			for (var i=0; i<1; i++) {
				var t = text;
				//var t = "Высококвалифицирова";
				
				var text = paper.text(300, 100, t).attr(font).attr("text-anchor", "start");
				var bbText = text.getBBox();
				paper.rect(300+.5, 100 + .5, bbText.width, bbText.height).attr({"stroke-width": 1});
				console.log("t: ", t.replace(/\n/g, "↵"));
				
				while (measurer.getPosition() < measurer.text.getEndIndex()) {
					var layout = measurer.nextLayout(width);
					//console.log("LAYOUT: " + layout + ", getPosition: " + measurer.getPosition());
					if (layout != null)
						layouts.push(layout);
				};
				
				measurer.rafaelTextObject.attr("text", layouts.join("\n"));
				var bb = measurer.rafaelTextObject.getBBox();
				var rect = paper.rect(bb.x+.5, bb.y + .5, bb.width, bb.height).attr({"stroke-width": 1});
				
				lay.push(layouts);
				console.log(layouts);
			}
			*/
			
			
			var fitTextToWidth = function(original, width) {
				var text = original;

				// TODO: move attr on parameters
				var attr = {font: "11px Arial", opacity: 0};
				
				// remove length for "..."
				var dots = paper.text(0, 0, "...").attr(attr).hide();
				var dotsBB = dots.getBBox();
				
				var maxWidth = width - dotsBB.width;
				
				var textElement = paper.text(0, 0, text).attr(attr).hide();
				var bb = textElement.getBBox();
				
				// it's a little bit incorrect with "..."
				while (bb.width > maxWidth && text.length > 0) {
					text = text.substring(0, text.length - 1);
					textElement.attr({"text": text});
					bb = textElement.getBBox();
				}

				// remove element from paper
				textElement.remove();
				
				if (text != original) {
					text = text + "...";
				}

				return text;
			}
			
			
			var x=100, y=90, height=20;
			var options = {"text-anchor": "middle", "boxHeight": 150, "vertical-align": "top"};
			var options = {"boxHeight": 150, "vertical-align": "top"};
			drawMultilineText(text, x, y, 150, 100, options);
	};