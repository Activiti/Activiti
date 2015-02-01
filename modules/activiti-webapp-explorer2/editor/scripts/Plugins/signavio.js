/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!Signavio) {
	var Signavio = new Object();
}

if (!Signavio.Plugins) {
	Signavio.Plugins = new Object();
}

if (!Signavio.Plugins.Utils) {
	Signavio.Plugins.Utils = new Object();
}

if (!Signavio.Helper) {
	Signavio.Helper = new Object();
}


new function() {

	/**
	 * Provides an uniq id
	 * @overwrite
	 * @return {String}
	 *
	 */
	ORYX.Editor.provideId = function() {
		var res = [], hex = '0123456789ABCDEF';
	
		for (var i = 0; i < 36; i++) res[i] = Math.floor(Math.random()*0x10);
	
		res[14] = 4;
		res[19] = (res[19] & 0x3) | 0x8;
	
		for (var i = 0; i < 36; i++) res[i] = hex[res[i]];
	
		res[8] = res[13] = res[18] = res[23] = '-';
	
		return "sid-" + res.join('');
	};


}();

