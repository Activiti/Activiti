/**
 * Web color table
 * 
 * @author Dmitry Farafonov
 */

var Color = {
   /**
   * The color white.  In the default sRGB space.
   */
  white     : Raphael.getRGB("rgb(255,255,255)"),
  
  /**
   * The color white.  In the default sRGB space.
   */
  WHITE : this.white,
  
  /**
   * The color light gray.  In the default sRGB space.
   */
  lightGray : Raphael.getRGB("rgb(192, 192, 192)"),
  
  /**
   * The color light gray.  In the default sRGB space.
   */
  LIGHT_GRAY : this.lightGray,
  
  /**
   * The color gray.  In the default sRGB space.
   */
  gray : Raphael.getRGB("rgb(128, 128, 128)"),
  
  /**
   * The color gray.  In the default sRGB space.
   */
  GRAY : this.gray,
  
  /**
   * The color dark gray.  In the default sRGB space.
   */
  darkGray : Raphael.getRGB("rgb(64, 64, 64)"),
  
  /**
   * The color dark gray.  In the default sRGB space.
   */
  DARK_GRAY : this.darkGray,
  
  /**
   * The color black.  In the default sRGB space.
   */
  black : Raphael.getRGB("rgb(0, 0, 0)"),
  
  /**
   * The color black.  In the default sRGB space.
   */
  BLACK : this.black,
  
  /**
   * The color red.  In the default sRGB space.
   */
  red : Raphael.getRGB("rgb(255, 0, 0)"),
  
  /**
   * The color red.  In the default sRGB space.
   */
  RED : this.red,
  
  /**
   * The color pink.  In the default sRGB space.
   */
  pink : Raphael.getRGB("rgb(255, 175, 175)"),
  
  /**
   * The color pink.  In the default sRGB space.
   */
  PINK : this.pink,
  
  /**
   * The color orange.  In the default sRGB space.
   */
  orange : Raphael.getRGB("rgb(255, 200, 0)"),
  
  /**
   * The color orange.  In the default sRGB space.
   */
  ORANGE : this.orange,
  
  /**
   * The color yellow.  In the default sRGB space.
   */
  yellow : Raphael.getRGB("rgb(255, 255, 0)"),
  
  /**
   * The color yellow.  In the default sRGB space.
   */
  YELLOW : this.yellow,
  
  /**
   * The color green.  In the default sRGB space.
   */
  green : Raphael.getRGB("rgb(0, 255, 0)"),
  
  /**
   * The color green.  In the default sRGB space.
   */
  GREEN : this.green,
  
  /**
   * The color magenta.  In the default sRGB space.
   */
  magenta : Raphael.getRGB("rgb(255, 0, 255)"),
  
  /**
   * The color magenta.  In the default sRGB space.
   */
  MAGENTA : this.magenta,
  
  /**
   * The color cyan.  In the default sRGB space.
   */
  cyan : Raphael.getRGB("rgb(0, 255, 255)"),
  
  /**
   * The color cyan.  In the default sRGB space.
   */
  CYAN : this.cyan,
  
  /**
   * The color blue.  In the default sRGB space.
   */
  blue : Raphael.getRGB("rgb(0, 0, 255)"),
  
  /**
   * The color blue.  In the default sRGB space.
   */
  BLUE : this.blue,
  
  /************************************************************************/

  // http://www.stm.dp.ua/web-design/color-html.php
  
	Snow			:   Raphael.getRGB("#FFFAFA	"),  // 	255 250 250
	GhostWhite		:   Raphael.getRGB("#F8F8FF	"),  // 	248 248 255
	WhiteSmoke		:   Raphael.getRGB("#F5F5F5	"),  // 	245 245 245
	Gainsboro		:   Raphael.getRGB("#DCDCDC	"),  // 	220 220 220
	FloralWhite		:   Raphael.getRGB("#FFFAF0	"),  // 	255 250 240
	OldLace			:   Raphael.getRGB("#FDF5E6	"),  // 	253 245 230
	Linen			:   Raphael.getRGB("#FAF0E6	"),  // 	250 240 230
	AntiqueWhite	:   Raphael.getRGB("#FAEBD7	"),  // 	250 235 215
	PapayaWhip		:   Raphael.getRGB("#FFEFD5	"),  // 	255 239 213
	BlanchedAlmond	:   Raphael.getRGB("#FFEBCD	"),  // 	255 235 205
	Bisque			:   Raphael.getRGB("#FFE4C4	"),  // 	255 228 196
	PeachPuff		:   Raphael.getRGB("#FFDAB9	"),  // 	255 218 185
	NavajoWhite		:   Raphael.getRGB("#FFDEAD	"),  // 	255 222 173
	Moccasin		:   Raphael.getRGB("#FFE4B5	"),  // 	255 228 181
	Cornsilk		:   Raphael.getRGB("#FFF8DC	"),  // 	255 248 220
	Ivory			:   Raphael.getRGB("#FFFFF0	"),  // 	255 255 240
	LemonChiffon	:   Raphael.getRGB("#FFFACD	"),  // 	255 250 205
	Seashell		:   Raphael.getRGB("#FFF5EE	"),  // 	255 245 238
	Honeydew		:   Raphael.getRGB("#F0FFF0	"),  // 	240 255 240
	MintCream		:   Raphael.getRGB("#F5FFFA	"),  // 	245 255 250
	Azure			:   Raphael.getRGB("#F0FFFF	"),  // 	240 255 255
	AliceBlue		:   Raphael.getRGB("#F0F8FF	"),  // 	240 248 255
	lavender		:   Raphael.getRGB("#E6E6FA	"),  // 	230 230 250
	LavenderBlush	:   Raphael.getRGB("#FFF0F5	"),  // 	255 240 245
	MistyRose		:   Raphael.getRGB("#FFE4E1	"),  // 	255 228 225
	White			:   Raphael.getRGB("#FFFFFF	"),  // 	255 255 255
	Black			:   Raphael.getRGB("#000000	"),  // 	0 0 0
	DarkSlateGray	:   Raphael.getRGB("#2F4F4F	"),  // 	47 79 79
	DimGrey			:   Raphael.getRGB("#696969	"),  // 	105 105 105
	SlateGrey		:   Raphael.getRGB("#708090	"),  // 	112 128 144
	LightSlateGray	:   Raphael.getRGB("#778899	"),  // 	119 136 153
	Grey			:   Raphael.getRGB("#BEBEBE	"),  // 	190 190 190
	LightGray		:   Raphael.getRGB("#D3D3D3	"),  // 	211 211 211
	MidnightBlue	:   Raphael.getRGB("#191970	"),  // 	25 25 112
	NavyBlue		:   Raphael.getRGB("#000080	"),  // 	0 0 128
	CornflowerBlue	:   Raphael.getRGB("#6495ED	"),  // 	100 149 237
	DarkSlateBlue	:   Raphael.getRGB("#483D8B	"),  // 	72 61 139
	SlateBlue		:   Raphael.getRGB("#6A5ACD	"),  // 	106 90 205
	MediumSlateBlue	:   Raphael.getRGB("#7B68EE	"),  // 	123 104 238
	LightSlateBlue	:   Raphael.getRGB("#8470FF	"),  // 	132 112 255
	MediumBlue		:   Raphael.getRGB("#0000CD	"),  // 	0 0 205
	RoyalBlue		:   Raphael.getRGB("#4169E1	"),  // 	65 105 225
	Blue			:   Raphael.getRGB("#0000FF	"),  // 	0 0 255
	DodgerBlue		:   Raphael.getRGB("#1E90FF	"),  // 	30 144 255
	DeepSkyBlue		:   Raphael.getRGB("#00BFFF	"),  // 	0 191 255
	SkyBlue			:   Raphael.getRGB("#87CEEB	"),  // 	135 206 235
	LightSkyBlue	:   Raphael.getRGB("#87CEFA	"),  // 	135 206 250
	SteelBlue		:   Raphael.getRGB("#4682B4	"),  // 	70 130 180
	LightSteelBlue	:   Raphael.getRGB("#B0C4DE	"),  // 	176 196 222
	LightBlue		:   Raphael.getRGB("#ADD8E6	"),  // 	173 216 230
	PowderBlue		:   Raphael.getRGB("#B0E0E6	"),  // 	176 224 230
	PaleTurquoise	:   Raphael.getRGB("#AFEEEE	"),  // 	175 238 238
	DarkTurquoise	:   Raphael.getRGB("#00CED1	"),  // 	0 206 209
	MediumTurquoise	:   Raphael.getRGB("#48D1CC	"),  // 	72 209 204
	Turquoise		:   Raphael.getRGB("#40E0D0	"),  // 	64 224 208
	Cyan			:   Raphael.getRGB("#00FFFF	"),  // 	0 255 255
	LightCyan		:   Raphael.getRGB("#E0FFFF	"),  // 	224 255 255
	CadetBlue		:   Raphael.getRGB("#5F9EA0	"),  // 	95 158 160
	MediumAquamarine:   Raphael.getRGB("#66CDAA	"),  // 	102 205 170
	Aquamarine		:   Raphael.getRGB("#7FFFD4	"),  // 	127 255 212
	DarkGreen		:   Raphael.getRGB("#006400	"),  // 	0 100 0
	DarkOliveGreen	:   Raphael.getRGB("#556B2F	"),  // 	85 107 47
	DarkSeaGreen	:   Raphael.getRGB("#8FBC8F	"),  // 	143 188 143
	SeaGreen		:   Raphael.getRGB("#2E8B57	"),  // 	46 139 87
	MediumSeaGreen	:   Raphael.getRGB("#3CB371	"),  // 	60 179 113
	LightSeaGreen	:   Raphael.getRGB("#20B2AA	"),  // 	32 178 170
	PaleGreen		:   Raphael.getRGB("#98FB98	"),  // 	152 251 152
	SpringGreen		:   Raphael.getRGB("#00FF7F	"),  // 	0 255 127
	LawnGreen		:   Raphael.getRGB("#7CFC00	"),  // 	124 252 0
	Green			:   Raphael.getRGB("#00FF00	"),  // 	0 255 0
	Chartreuse		:   Raphael.getRGB("#7FFF00	"),  // 	127 255 0
	MedSpringGreen	:   Raphael.getRGB("#00FA9A	"),  // 	0 250 154
	GreenYellow		:   Raphael.getRGB("#ADFF2F	"),  // 	173 255 47
	LimeGreen		:   Raphael.getRGB("#32CD32	"),  // 	50 205 50
	YellowGreen		:   Raphael.getRGB("#9ACD32	"),  // 	154 205 50
	ForestGreen		:   Raphael.getRGB("#228B22	"),  // 	34 139 34
	OliveDrab		:   Raphael.getRGB("#6B8E23	"),  // 	107 142 35
	DarkKhaki		:   Raphael.getRGB("#BDB76B	"),  // 	189 183 107
	PaleGoldenrod	:   Raphael.getRGB("#EEE8AA	"),  // 	238 232 170
	LtGoldenrodYello:   Raphael.getRGB("#FAFAD2	"),  // 	250 250 210
	LightYellow		:   Raphael.getRGB("#FFFFE0	"),  // 	255 255 224
	Yellow			:   Raphael.getRGB("#FFFF00	"),  // 	255 255 0
	Gold			:   Raphael.getRGB("#FFD700	"),  // 	255 215 0
	LightGoldenrod	:   Raphael.getRGB("#EEDD82	"),  // 	238 221 130
	goldenrod		:   Raphael.getRGB("#DAA520	"),  // 	218 165 32
	DarkGoldenrod	:   Raphael.getRGB("#B8860B	"),  // 	184 134 11
	RosyBrown		:   Raphael.getRGB("#BC8F8F	"),  // 	188 143 143
	IndianRed		:   Raphael.getRGB("#CD5C5C	"),  // 	205 92 92
	SaddleBrown		:   Raphael.getRGB("#8B4513	"),  // 	139 69 19
	Sienna			:   Raphael.getRGB("#A0522D	"),  // 	160 82 45
	Peru			:   Raphael.getRGB("#CD853F	"),  // 	205 133 63
	Burlywood		:   Raphael.getRGB("#DEB887	"),  // 	222 184 135
	Beige			:   Raphael.getRGB("#F5F5DC	"),  // 	245 245 220
	Wheat			:   Raphael.getRGB("#F5DEB3	"),  // 	245 222 179
	SandyBrown		:   Raphael.getRGB("#F4A460	"),  // 	244 164 96
	Tan				:   Raphael.getRGB("#D2B48C	"),  // 	210 180 140
	Chocolate		:   Raphael.getRGB("#D2691E	"),  // 	210 105 30
	Firebrick		:   Raphael.getRGB("#B22222	"),  // 	178 34 34
	Brown			:   Raphael.getRGB("#A52A2A	"),  // 	165 42 42
	DarkSalmon		:   Raphael.getRGB("#E9967A	"),  // 	233 150 122
	Salmon			:   Raphael.getRGB("#FA8072	"),  // 	250 128 114
	LightSalmon		:   Raphael.getRGB("#FFA07A	"),  // 	255 160 122
	Orange			:   Raphael.getRGB("#FFA500	"),  // 	255 165 0
	DarkOrange		:   Raphael.getRGB("#FF8C00	"),  // 	255 140 0
	Coral			:   Raphael.getRGB("#FF7F50	"),  // 	255 127 80
	LightCoral		:   Raphael.getRGB("#F08080	"),  // 	240 128 128
	Tomato			:   Raphael.getRGB("#FF6347	"),  // 	255 99 71
	OrangeRed		:   Raphael.getRGB("#FF4500	"),  // 	255 69 0
	Red				:   Raphael.getRGB("#FF0000	"),  // 	255 0 0
	HotPink			:   Raphael.getRGB("#FF69B4	"),  // 	255 105 180
	DeepPink		:   Raphael.getRGB("#FF1493	"),  // 	255 20 147
	Pink			:   Raphael.getRGB("#FFC0CB	"),  // 	255 192 203
	LightPink		:   Raphael.getRGB("#FFB6C1	"),  // 	255 182 193
	PaleVioletRed	:   Raphael.getRGB("#DB7093	"),  // 	219 112 147
	Maroon			:   Raphael.getRGB("#B03060	"),  // 	176 48 96
	MediumVioletRed	:   Raphael.getRGB("#C71585	"),  // 	199 21 133
	VioletRed		:   Raphael.getRGB("#D02090	"),  // 	208 32 144
	Magenta			:   Raphael.getRGB("#FF00FF	"),  // 	255 0 255
	Violet			:   Raphael.getRGB("#EE82EE	"),  // 	238 130 238
	Plum			:   Raphael.getRGB("#DDA0DD	"),  // 	221 160 221
	Orchid			:   Raphael.getRGB("#DA70D6	"),  // 	218 112 214
	MediumOrchid	:   Raphael.getRGB("#BA55D3	"),  // 	186 85 211
	DarkOrchid		:   Raphael.getRGB("#9932CC	"),  // 	153 50 204
	DarkViolet		:   Raphael.getRGB("#9400D3	"),  // 	148 0 211
	BlueViolet		:   Raphael.getRGB("#8A2BE2	"),  // 	138 43 226
	Purple			:   Raphael.getRGB("#A020F0	"),  // 	160 32 240
	MediumPurple	:   Raphael.getRGB("#9370DB	"),  // 	147 112 219
	Thistle			:   Raphael.getRGB("#D8BFD8	"),  // 	216 191 216
	Snow1			:   Raphael.getRGB("#FFFAFA	"),  // 	255 250 250
	Snow2			:   Raphael.getRGB("#EEE9E9	"),  // 	238 233 233
	Snow3			:   Raphael.getRGB("#CDC9C9	"),  // 	205 201 201
	Snow4			:   Raphael.getRGB("#8B8989	"),  // 	139 137 137
	Seashell1		:   Raphael.getRGB("#FFF5EE	"),  // 	255 245 238
	Seashell2		:   Raphael.getRGB("#EEE5DE	"),  // 	238 229 222
	Seashell3		:   Raphael.getRGB("#CDC5BF	"),  // 	205 197 191
	Seashell4		:   Raphael.getRGB("#8B8682	"),  // 	139 134 130
	AntiqueWhite1	:   Raphael.getRGB("#FFEFDB	"),  // 	255 239 219
	AntiqueWhite2	:   Raphael.getRGB("#EEDFCC	"),  // 	238 223 204
	AntiqueWhite3	:   Raphael.getRGB("#CDC0B0	"),  // 	205 192 176
	AntiqueWhite4	:   Raphael.getRGB("#8B8378	"),  // 	139 131 120
	Bisque1			:   Raphael.getRGB("#FFE4C4	"),  // 	255 228 196
	Bisque2			:   Raphael.getRGB("#EED5B7	"),  // 	238 213 183
	Bisque3			:   Raphael.getRGB("#CDB79E	"),  // 	205 183 158
	Bisque4			:   Raphael.getRGB("#8B7D6B	"),  // 	139 125 107
	PeachPuff1		:   Raphael.getRGB("#FFDAB9	"),  // 	255 218 185
	PeachPuff2		:   Raphael.getRGB("#EECBAD	"),  // 	238 203 173
	PeachPuff3		:   Raphael.getRGB("#CDAF95	"),  // 	205 175 149
	PeachPuff4		:   Raphael.getRGB("#8B7765	"),  // 	139 119 101
	NavajoWhite1	:   Raphael.getRGB("#FFDEAD	"),  // 	255 222 173
	NavajoWhite2	:   Raphael.getRGB("#EECFA1	"),  // 	238 207 161
	NavajoWhite3	:   Raphael.getRGB("#CDB38B	"),  // 	205 179 139
	NavajoWhite4	:   Raphael.getRGB("#8B795E	"),  // 	139 121 94
	LemonChiffon1	:   Raphael.getRGB("#FFFACD	"),  // 	255 250 205
	LemonChiffon2	:   Raphael.getRGB("#EEE9BF	"),  // 	238 233 191
	LemonChiffon3	:   Raphael.getRGB("#CDC9A5	"),  // 	205 201 165
	LemonChiffon4	:   Raphael.getRGB("#8B8970	"),  // 	139 137 112
	Cornsilk1		:   Raphael.getRGB("#FFF8DC	"),  // 	255 248 220
	Cornsilk2		:   Raphael.getRGB("#EEE8CD	"),  // 	238 232 205
	Cornsilk3		:   Raphael.getRGB("#CDC8B1	"),  // 	205 200 177
	Cornsilk4		:   Raphael.getRGB("#8B8878	"),  // 	139 136 120
	Ivory1			:   Raphael.getRGB("#FFFFF0	"),  // 	255 255 240
	Ivory2			:   Raphael.getRGB("#EEEEE0	"),  // 	238 238 224
	Ivory3			:   Raphael.getRGB("#CDCDC1	"),  // 	205 205 193
	Ivory4			:   Raphael.getRGB("#8B8B83	"),  // 	139 139 131
	Honeydew1		:   Raphael.getRGB("#F0FFF0	"),  // 	240 255 240
	Honeydew2		:   Raphael.getRGB("#E0EEE0	"),  // 	224 238 224
	Honeydew3		:   Raphael.getRGB("#C1CDC1	"),  // 	193 205 193
	Honeydew4		:   Raphael.getRGB("#838B83	"),  // 	131 139 131
	LavenderBlush1	:   Raphael.getRGB("#FFF0F5	"),  // 	255 240 245
	LavenderBlush2	:   Raphael.getRGB("#EEE0E5	"),  // 	238 224 229
	LavenderBlush3	:   Raphael.getRGB("#CDC1C5	"),  // 	205 193 197
	LavenderBlush4	:   Raphael.getRGB("#8B8386	"),  // 	139 131 134
	MistyRose1		:   Raphael.getRGB("#FFE4E1	"),  // 	255 228 225
	MistyRose2		:   Raphael.getRGB("#EED5D2	"),  // 	238 213 210
	MistyRose3		:   Raphael.getRGB("#CDB7B5	"),  // 	205 183 181
	MistyRose4		:   Raphael.getRGB("#8B7D7B	"),  // 	139 125 123
	Azure1			:   Raphael.getRGB("#F0FFFF	"),  // 	240 255 255
	Azure2			:   Raphael.getRGB("#E0EEEE	"),  // 	224 238 238
	Azure3			:   Raphael.getRGB("#C1CDCD	"),  // 	193 205 205
	Azure4			:   Raphael.getRGB("#838B8B	"),  // 	131 139 139
	SlateBlue1		:   Raphael.getRGB("#836FFF	"),  // 	131 111 255
	SlateBlue2		:   Raphael.getRGB("#7A67EE	"),  // 	122 103 238
	SlateBlue3		:   Raphael.getRGB("#6959CD	"),  // 	105 89 205
	SlateBlue4		:   Raphael.getRGB("#473C8B	"),  // 	71 60 139
	RoyalBlue1		:   Raphael.getRGB("#4876FF	"),  // 	72 118 255
	RoyalBlue2		:   Raphael.getRGB("#436EEE	"),  // 	67 110 238
	RoyalBlue3		:   Raphael.getRGB("#3A5FCD	"),  // 	58 95 205
	RoyalBlue4		:   Raphael.getRGB("#27408B	"),  // 	39 64 139
	Blue1			:   Raphael.getRGB("#0000FF	"),  // 	0 0 255
	Blue2			:   Raphael.getRGB("#0000EE	"),  // 	0 0 238
	Blue3			:   Raphael.getRGB("#0000CD	"),  // 	0 0 205
	Blue4			:   Raphael.getRGB("#00008B	"),  // 	0 0 139
	DodgerBlue1		:   Raphael.getRGB("#1E90FF	"),  // 	30 144 255
	DodgerBlue2		:   Raphael.getRGB("#1C86EE	"),  // 	28 134 238
	DodgerBlue3		:   Raphael.getRGB("#1874CD	"),  // 	24 116 205
	DodgerBlue4		:   Raphael.getRGB("#104E8B	"),  // 	16 78 139
	SteelBlue1		:   Raphael.getRGB("#63B8FF	"),  // 	99 184 255
	SteelBlue2		:   Raphael.getRGB("#5CACEE	"),  // 	92 172 238
	SteelBlue3		:   Raphael.getRGB("#4F94CD	"),  // 	79 148 205
	SteelBlue4		:   Raphael.getRGB("#36648B	"),  // 	54 100 139
	DeepSkyBlue1	:   Raphael.getRGB("#00BFFF	"),  // 	0 191 255
	DeepSkyBlue2	:   Raphael.getRGB("#00B2EE	"),  // 	0 178 238
	DeepSkyBlue3	:   Raphael.getRGB("#009ACD	"),  // 	0 154 205
	DeepSkyBlue4	:   Raphael.getRGB("#00688B	"),  // 	0 104 139
	SkyBlue1		:   Raphael.getRGB("#87CEFF	"),  // 	135 206 255
	SkyBlue2		:   Raphael.getRGB("#7EC0EE	"),  // 	126 192 238
	SkyBlue3		:   Raphael.getRGB("#6CA6CD	"),  // 	108 166 205
	SkyBlue4		:   Raphael.getRGB("#4A708B	"),  // 	74 112 139
	LightSkyBlue1	:   Raphael.getRGB("#B0E2FF	"),  // 	176 226 255
	LightSkyBlue2	:   Raphael.getRGB("#A4D3EE	"),  // 	164 211 238
	LightSkyBlue3	:   Raphael.getRGB("#8DB6CD	"),  // 	141 182 205
	LightSkyBlue4	:   Raphael.getRGB("#607B8B	"),  // 	96 123 139
	SlateGray1		:   Raphael.getRGB("#C6E2FF	"),  // 	198 226 255
	SlateGray2		:   Raphael.getRGB("#B9D3EE	"),  // 	185 211 238
	SlateGray3		:   Raphael.getRGB("#9FB6CD	"),  // 	159 182 205
	SlateGray4		:   Raphael.getRGB("#6C7B8B	"),  // 	108 123 139
	LightSteelBlue1	:   Raphael.getRGB("#CAE1FF	"),  // 	202 225 255
	LightSteelBlue2	:   Raphael.getRGB("#BCD2EE	"),  // 	188 210 238
	LightSteelBlue3	:   Raphael.getRGB("#A2B5CD	"),  // 	162 181 205
	LightSteelBlue4	:   Raphael.getRGB("#6E7B8B	"),  // 	110 123 139
	LightBlue1		:   Raphael.getRGB("#BFEFFF	"),  // 	191 239 255
	LightBlue2		:   Raphael.getRGB("#B2DFEE	"),  // 	178 223 238
	LightBlue3		:   Raphael.getRGB("#9AC0CD	"),  // 	154 192 205
	LightBlue4		:   Raphael.getRGB("#68838B	"),  // 	104 131 139
	LightCyan1		:   Raphael.getRGB("#E0FFFF	"),  // 	224 255 255
	LightCyan2		:   Raphael.getRGB("#D1EEEE	"),  // 	209 238 238
	LightCyan3		:   Raphael.getRGB("#B4CDCD	"),  // 	180 205 205
	LightCyan4		:   Raphael.getRGB("#7A8B8B	"),  // 	122 139 139
	PaleTurquoise1	:   Raphael.getRGB("#BBFFFF	"),  // 	187 255 255
	PaleTurquoise2	:   Raphael.getRGB("#AEEEEE	"),  // 	174 238 238
	PaleTurquoise3	:   Raphael.getRGB("#96CDCD	"),  // 	150 205 205
	PaleTurquoise4	:   Raphael.getRGB("#668B8B	"),  // 	102 139 139
	CadetBlue1		:   Raphael.getRGB("#98F5FF	"),  // 	152 245 255
	CadetBlue2		:   Raphael.getRGB("#8EE5EE	"),  // 	142 229 238
	CadetBlue3		:   Raphael.getRGB("#7AC5CD	"),  // 	122 197 205
	CadetBlue4		:   Raphael.getRGB("#53868B	"),  // 	83 134 139
	Turquoise1		:   Raphael.getRGB("#00F5FF	"),  // 	0 245 255
	Turquoise2		:   Raphael.getRGB("#00E5EE	"),  // 	0 229 238
	Turquoise3		:   Raphael.getRGB("#00C5CD	"),  // 	0 197 205
	Turquoise4		:   Raphael.getRGB("#00868B	"),  // 	0 134 139
	Cyan1			:   Raphael.getRGB("#00FFFF	"),  // 	0 255 255
	Cyan2			:   Raphael.getRGB("#00EEEE	"),  // 	0 238 238
	Cyan3			:   Raphael.getRGB("#00CDCD	"),  // 	0 205 205
	Cyan4			:   Raphael.getRGB("#008B8B	"),  // 	0 139 139
	DarkSlateGray1	:   Raphael.getRGB("#97FFFF	"),  // 	151 255 255
	DarkSlateGray2	:   Raphael.getRGB("#8DEEEE	"),  // 	141 238 238
	DarkSlateGray3	:   Raphael.getRGB("#79CDCD	"),  // 	121 205 205
	DarkSlateGray4	:   Raphael.getRGB("#528B8B	"),  // 	82 139 139
	Aquamarine1		:   Raphael.getRGB("#7FFFD4	"),  // 	127 255 212
	Aquamarine2		:   Raphael.getRGB("#76EEC6	"),  // 	118 238 198
	Aquamarine3		:   Raphael.getRGB("#66CDAA	"),  // 	102 205 170
	Aquamarine4		:   Raphael.getRGB("#458B74	"),  // 	69 139 116
	DarkSeaGreen1	:   Raphael.getRGB("#C1FFC1	"),  // 	193 255 193
	DarkSeaGreen2	:   Raphael.getRGB("#B4EEB4	"),  // 	180 238 180
	DarkSeaGreen3	:   Raphael.getRGB("#9BCD9B	"),  // 	155 205 155
	DarkSeaGreen4	:   Raphael.getRGB("#698B69	"),  // 	105 139 105
	SeaGreen1		:   Raphael.getRGB("#54FF9F	"),  // 	84 255 159
	SeaGreen2		:   Raphael.getRGB("#4EEE94	"),  // 	78 238 148
	SeaGreen3		:   Raphael.getRGB("#43CD80	"),  // 	67 205 128
	SeaGreen4		:   Raphael.getRGB("#2E8B57	"),  // 	46 139 87
	PaleGreen1		:   Raphael.getRGB("#9AFF9A	"),  // 	154 255 154
	PaleGreen2		:   Raphael.getRGB("#90EE90	"),  // 	144 238 144
	PaleGreen3		:   Raphael.getRGB("#7CCD7C	"),  // 	124 205 124
	PaleGreen4		:   Raphael.getRGB("#548B54	"),  // 	84 139 84
	SpringGreen1	:   Raphael.getRGB("#00FF7F	"),  // 	0 255 127
	SpringGreen2	:   Raphael.getRGB("#00EE76	"),  // 	0 238 118
	SpringGreen3	:   Raphael.getRGB("#00CD66	"),  // 	0 205 102
	SpringGreen4	:   Raphael.getRGB("#008B45	"),  // 	0 139 69
	Green1			:   Raphael.getRGB("#00FF00	"),  // 	0 255 0
	Green2			:   Raphael.getRGB("#00EE00	"),  // 	0 238 0
	Green3			:   Raphael.getRGB("#00CD00	"),  // 	0 205 0
	Green4			:   Raphael.getRGB("#008B00	"),  // 	0 139 0
	Chartreuse1		:   Raphael.getRGB("#7FFF00	"),  // 	127 255 0
	Chartreuse2		:   Raphael.getRGB("#76EE00	"),  // 	118 238 0
	Chartreuse3		:   Raphael.getRGB("#66CD00	"),  // 	102 205 0
	Chartreuse4		:   Raphael.getRGB("#458B00	"),  // 	69 139 0
	OliveDrab1		:   Raphael.getRGB("#C0FF3E	"),  // 	192 255 62
	OliveDrab2		:   Raphael.getRGB("#B3EE3A	"),  // 	179 238 58
	OliveDrab3		:   Raphael.getRGB("#9ACD32	"),  // 	154 205 50
	OliveDrab4		:   Raphael.getRGB("#698B22	"),  // 	105 139 34
	DarkOliveGreen1	:   Raphael.getRGB("#CAFF70	"),  // 	202 255 112
	DarkOliveGreen2	:   Raphael.getRGB("#BCEE68	"),  // 	188 238 104
	DarkOliveGreen3	:   Raphael.getRGB("#A2CD5A	"),  // 	162 205 90
	DarkOliveGreen4	:   Raphael.getRGB("#6E8B3D	"),  // 	110 139 61
	Khaki1			:   Raphael.getRGB("#FFF68F	"),  // 	255 246 143
	Khaki2			:   Raphael.getRGB("#EEE685	"),  // 	238 230 133
	Khaki3			:   Raphael.getRGB("#CDC673	"),  // 	205 198 115
	Khaki4			:   Raphael.getRGB("#8B864E	"),  // 	139 134 78
	LightGoldenrod1	:   Raphael.getRGB("#FFEC8B	"),  // 	255 236 139
	LightGoldenrod2	:   Raphael.getRGB("#EEDC82	"),  // 	238 220 130
	LightGoldenrod3	:   Raphael.getRGB("#CDBE70	"),  // 	205 190 112
	LightGoldenrod4	:   Raphael.getRGB("#8B814C	"),  // 	139 129 76
	LightYellow1	:   Raphael.getRGB("#FFFFE0	"),  // 	255 255 224
	LightYellow2	:   Raphael.getRGB("#EEEED1	"),  // 	238 238 209
	LightYellow3	:   Raphael.getRGB("#CDCDB4	"),  // 	205 205 180
	LightYellow4	:   Raphael.getRGB("#8B8B7A	"),  // 	139 139 122
	Yellow1			:   Raphael.getRGB("#FFFF00	"),  // 	255 255 0
	Yellow2			:   Raphael.getRGB("#EEEE00	"),  // 	238 238 0
	Yellow3			:   Raphael.getRGB("#CDCD00	"),  // 	205 205 0
	Yellow4			:   Raphael.getRGB("#8B8B00	"),  // 	139 139 0
	Gold1			:   Raphael.getRGB("#FFD700	"),  // 	255 215 0
	Gold2			:   Raphael.getRGB("#EEC900	"),  // 	238 201 0
	Gold3			:   Raphael.getRGB("#CDAD00	"),  // 	205 173 0
	Gold4			:   Raphael.getRGB("#8B7500	"),  // 	139 117 0
	Goldenrod1		:   Raphael.getRGB("#FFC125	"),  // 	255 193 37
	Goldenrod2		:   Raphael.getRGB("#EEB422	"),  // 	238 180 34
	Goldenrod3		:   Raphael.getRGB("#CD9B1D	"),  // 	205 155 29
	Goldenrod4		:   Raphael.getRGB("#8B6914	"),  // 	139 105 20
	DarkGoldenrod1	:   Raphael.getRGB("#FFB90F	"),  // 	255 185 15
	DarkGoldenrod2	:   Raphael.getRGB("#EEAD0E	"),  // 	238 173 14
	DarkGoldenrod3	:   Raphael.getRGB("#CD950C	"),  // 	205 149 12
	DarkGoldenrod4	:   Raphael.getRGB("#8B658B	"),  // 	139 101 8
	RosyBrown1		:   Raphael.getRGB("#FFC1C1	"),  // 	255 193 193
	RosyBrown2		:   Raphael.getRGB("#EEB4B4	"),  // 	238 180 180
	RosyBrown3		:   Raphael.getRGB("#CD9B9B	"),  // 	205 155 155
	RosyBrown4		:   Raphael.getRGB("#8B6969	"),  // 	139 105 105
	IndianRed1		:   Raphael.getRGB("#FF6A6A	"),  // 	255 106 106
	IndianRed2		:   Raphael.getRGB("#EE6363	"),  // 	238 99 99
	IndianRed3		:   Raphael.getRGB("#CD5555	"),  // 	205 85 85
	IndianRed4		:   Raphael.getRGB("#8B3A3A	"),  // 	139 58 58
	Sienna1			:   Raphael.getRGB("#FF8247	"),  // 	255 130 71
	Sienna2			:   Raphael.getRGB("#EE7942	"),  // 	238 121 66
	Sienna3			:   Raphael.getRGB("#CD6839	"),  // 	205 104 57
	Sienna4			:   Raphael.getRGB("#8B4726	"),  // 	139 71 38
	Burlywood1		:   Raphael.getRGB("#FFD39B	"),  // 	255 211 155
	Burlywood2		:   Raphael.getRGB("#EEC591	"),  // 	238 197 145
	Burlywood3		:   Raphael.getRGB("#CDAA7D	"),  // 	205 170 125
	Burlywood4		:   Raphael.getRGB("#8B7355	"),  // 	139 115 85
	Wheat1			:   Raphael.getRGB("#FFE7BA	"),  // 	255 231 186
	Wheat2			:   Raphael.getRGB("#EED8AE	"),  // 	238 216 174
	Wheat3			:   Raphael.getRGB("#CDBA96	"),  // 	205 186 150
	Wheat4			:   Raphael.getRGB("#8B7E66	"),  // 	139 126 102
	Tan1			:   Raphael.getRGB("#FFA54F	"),  // 	255 165 79
	Tan2			:   Raphael.getRGB("#EE9A49	"),  // 	238 154 73
	Tan3			:   Raphael.getRGB("#CD853F	"),  // 	205 133 63
	Tan4			:   Raphael.getRGB("#8B5A2B	"),  // 	139 90 43
	Chocolate1		:   Raphael.getRGB("#FF7F24	"),  // 	255 127 36
	Chocolate2		:   Raphael.getRGB("#EE7621	"),  // 	238 118 33
	Chocolate3		:   Raphael.getRGB("#CD661D	"),  // 	205 102 29
	Chocolate4		:   Raphael.getRGB("#8B4513	"),  // 	139 69 19
	Firebrick1		:   Raphael.getRGB("#FF3030	"),  // 	255 48 48
	Firebrick2		:   Raphael.getRGB("#EE2C2C	"),  // 	238 44 44
	Firebrick3		:   Raphael.getRGB("#CD2626	"),  // 	205 38 38
	Firebrick4		:   Raphael.getRGB("#8B1A1A	"),  // 	139 26 26
	Brown1			:   Raphael.getRGB("#FF4040	"),  // 	255 64 64
	Brown2			:   Raphael.getRGB("#EE3B3B	"),  // 	238 59 59
	Brown3			:   Raphael.getRGB("#CD3333	"),  // 	205 51 51
	Brown4			:   Raphael.getRGB("#8B2323	"),  // 	139 35 35
	Salmon1			:   Raphael.getRGB("#FF8C69	"),  // 	255 140 105
	Salmon2			:   Raphael.getRGB("#EE8262	"),  // 	238 130 98
	Salmon3			:   Raphael.getRGB("#CD7054	"),  // 	205 112 84
	Salmon4			:   Raphael.getRGB("#8B4C39	"),  // 	139 76 57
	LightSalmon1	:   Raphael.getRGB("#FFA07A	"),  // 	255 160 122
	LightSalmon2	:   Raphael.getRGB("#EE9572	"),  // 	238 149 114
	LightSalmon3	:   Raphael.getRGB("#CD8162	"),  // 	205 129 98
	LightSalmon4	:   Raphael.getRGB("#8B5742	"),  // 	139 87 66
	Orange1			:   Raphael.getRGB("#FFA500	"),  // 	255 165 0
	Orange2			:   Raphael.getRGB("#EE9A00	"),  // 	238 154 0
	Orange3			:   Raphael.getRGB("#CD8500	"),  // 	205 133 0
	Orange4			:   Raphael.getRGB("#8B5A00	"),  // 	139 90 0
	DarkOrange1		:   Raphael.getRGB("#FF7F00	"),  // 	255 127 0
	DarkOrange2		:   Raphael.getRGB("#EE7600	"),  // 	238 118 0
	DarkOrange3		:   Raphael.getRGB("#CD6600	"),  // 	205 102 0
	DarkOrange4		:   Raphael.getRGB("#8B4500	"),  // 	139 69 0
	Coral1			:   Raphael.getRGB("#FF7256	"),  // 	255 114 86
	Coral2			:   Raphael.getRGB("#EE6A50	"),  // 	238 106 80
	Coral3			:   Raphael.getRGB("#CD5B45	"),  // 	205 91 69
	Coral4			:   Raphael.getRGB("#8B3E2F	"),  // 	139 62 47
	Tomato1			:   Raphael.getRGB("#FF6347	"),  // 	255 99 71
	Tomato2			:   Raphael.getRGB("#EE5C42	"),  // 	238 92 66
	Tomato3			:   Raphael.getRGB("#CD4F39	"),  // 	205 79 57
	Tomato4			:   Raphael.getRGB("#8B3626	"),  // 	139 54 38
	OrangeRed1		:   Raphael.getRGB("#FF4500	"),  // 	255 69 0
	OrangeRed2		:   Raphael.getRGB("#EE4000	"),  // 	238 64 0
	OrangeRed3		:   Raphael.getRGB("#CD3700	"),  // 	205 55 0
	OrangeRed4		:   Raphael.getRGB("#8B2500	"),  // 	139 37 0
	Red1			:   Raphael.getRGB("#FF0000	"),  // 	255 0 0
	Red2			:   Raphael.getRGB("#EE0000	"),  // 	238 0 0
	Red3			:   Raphael.getRGB("#CD0000	"),  // 	205 0 0
	Red4			:   Raphael.getRGB("#8B0000	"),  // 	139 0 0
	DeepPink1		:   Raphael.getRGB("#FF1493	"),  // 	255 20 147
	DeepPink2		:   Raphael.getRGB("#EE1289	"),  // 	238 18 137
	DeepPink3		:   Raphael.getRGB("#CD1076	"),  // 	205 16 118
	DeepPink4		:   Raphael.getRGB("#8B0A50	"),  // 	139 10 80
	HotPink1		:   Raphael.getRGB("#FF6EB4	"),  // 	255 110 180
	HotPink2		:   Raphael.getRGB("#EE6AA7	"),  // 	238 106 167
	HotPink3		:   Raphael.getRGB("#CD6090	"),  // 	205 96 144
	HotPink4		:   Raphael.getRGB("#8B3A62	"),  // 	139 58 98
	Pink1			:   Raphael.getRGB("#FFB5C5	"),  // 	255 181 197
	Pink2			:   Raphael.getRGB("#EEA9B8	"),  // 	238 169 184
	Pink3			:   Raphael.getRGB("#CD919E	"),  // 	205 145 158
	Pink4			:   Raphael.getRGB("#8B636C	"),  // 	139 99 108
	LightPink1		:   Raphael.getRGB("#FFAEB9	"),  // 	255 174 185
	LightPink2		:   Raphael.getRGB("#EEA2AD	"),  // 	238 162 173
	LightPink3		:   Raphael.getRGB("#CD8C95	"),  // 	205 140 149
	LightPink4		:   Raphael.getRGB("#8B5F65	"),  // 	139 95 101
	PaleVioletRed1	:   Raphael.getRGB("#FF82AB	"),  // 	255 130 171
	PaleVioletRed2	:   Raphael.getRGB("#EE799F	"),  // 	238 121 159
	PaleVioletRed3	:   Raphael.getRGB("#CD6889	"),  // 	205 104 137
	PaleVioletRed4	:   Raphael.getRGB("#8B475D	"),  // 	139 71 93
	Maroon1			:   Raphael.getRGB("#FF34B3	"),  // 	255 52 179
	Maroon2			:   Raphael.getRGB("#EE30A7	"),  // 	238 48 167
	Maroon3			:   Raphael.getRGB("#CD2990	"),  // 	205 41 144
	Maroon4			:   Raphael.getRGB("#8B1C62	"),  // 	139 28 98
	VioletRed1		:   Raphael.getRGB("#FF3E96	"),  // 	255 62 150
	VioletRed2		:   Raphael.getRGB("#EE3A8C	"),  // 	238 58 140
	VioletRed3		:   Raphael.getRGB("#CD3278	"),  // 	205 50 120
	VioletRed4		:   Raphael.getRGB("#8B2252	"),  // 	139 34 82
	Magenta1		:   Raphael.getRGB("#FF00FF	"),  // 	255 0 255
	Magenta2		:   Raphael.getRGB("#EE00EE	"),  // 	238 0 238
	Magenta3		:   Raphael.getRGB("#CD00CD	"),  // 	205 0 205
	Magenta4		:   Raphael.getRGB("#8B008B	"),  // 	139 0 139
	Orchid1			:   Raphael.getRGB("#FF83FA	"),  // 	255 131 250
	Orchid2			:   Raphael.getRGB("#EE7AE9	"),  // 	238 122 233
	Orchid3			:   Raphael.getRGB("#CD69C9	"),  // 	205 105 201
	Orchid4			:   Raphael.getRGB("#8B4789	"),  // 	139 71 137
	Plum1			:   Raphael.getRGB("#FFBBFF	"),  // 	255 187 255
	Plum2			:   Raphael.getRGB("#EEAEEE	"),  // 	238 174 238
	Plum3			:   Raphael.getRGB("#CD96CD	"),  // 	205 150 205
	Plum4			:   Raphael.getRGB("#8B668B	"),  // 	139 102 139
	MediumOrchid1	:   Raphael.getRGB("#E066FF	"),  // 	224 102 255
	MediumOrchid2	:   Raphael.getRGB("#D15FEE	"),  // 	209 95 238
	MediumOrchid3	:   Raphael.getRGB("#B452CD	"),  // 	180 82 205
	MediumOrchid4	:   Raphael.getRGB("#7A378B	"),  // 	122 55 139
	DarkOrchid1		:   Raphael.getRGB("#BF3EFF	"),  // 	191 62 255
	DarkOrchid2		:   Raphael.getRGB("#B23AEE	"),  // 	178 58 238
	DarkOrchid3		:   Raphael.getRGB("#9A32CD	"),  // 	154 50 205
	DarkOrchid4		:   Raphael.getRGB("#68228B	"),  // 	104 34 139
	Purple1			:   Raphael.getRGB("#9B30FF	"),  // 	155 48 255
	Purple2			:   Raphael.getRGB("#912CEE	"),  // 	145 44 238
	Purple3			:   Raphael.getRGB("#7D26CD	"),  // 	125 38 205
	Purple4			:   Raphael.getRGB("#551A8B	"),  // 	85 26 139
	MediumPurple1	:   Raphael.getRGB("#AB82FF	"),  // 	171 130 255
	MediumPurple2	:   Raphael.getRGB("#9F79EE	"),  // 	159 121 238
	MediumPurple3	:   Raphael.getRGB("#8968CD	"),  // 	137 104 205
	MediumPurple4	:   Raphael.getRGB("#5D478B	"),  // 	93 71 139
	Thistle1		:   Raphael.getRGB("#FFE1FF	"),  // 	255 225 255
	Thistle2		:   Raphael.getRGB("#EED2EE	"),  // 	238 210 238
	Thistle3		:   Raphael.getRGB("#CDB5CD	"),  // 	205 181 205
	Thistle4		:   Raphael.getRGB("#8B7B8B	"),  // 	139 123 139
	grey11			:   Raphael.getRGB("#1C1C1C	"),  // 	28 28 28
	grey21			:   Raphael.getRGB("#363636	"),  // 	54 54 54
	grey31			:   Raphael.getRGB("#4F4F4F	"),  // 	79 79 79
	grey41			:   Raphael.getRGB("#696969	"),  // 	105 105 105
	grey51			:   Raphael.getRGB("#828282	"),  // 	130 130 130
	grey61			:   Raphael.getRGB("#9C9C9C	"),  // 	156 156 156
	grey71			:   Raphael.getRGB("#B5B5B5	"),  // 	181 181 181
	gray81			:   Raphael.getRGB("#CFCFCF	"),  // 	207 207 207
	gray91			:   Raphael.getRGB("#E8E8E8	"),  // 	232 232 232
	DarkGrey		:   Raphael.getRGB("#A9A9A9	"),  // 	169 169 169
	DarkBlue		:   Raphael.getRGB("#00008B	"),  // 	0 0 139
	DarkCyan		:   Raphael.getRGB("#008B8B	"),  // 	0 139 139
	DarkMagenta		:   Raphael.getRGB("#8B008B	"),  // 	139 0 139
	DarkRed			:   Raphael.getRGB("#8B0000	"),  // 	139 0 0
	LightGreen		:   Raphael.getRGB("#90EE90	"),  // 	144 238 144

  
  
  get: function(R, G, B){
	return Raphael.getRGB("rgb(" + R + ", " + G + ", " + B + ")");
  }
};