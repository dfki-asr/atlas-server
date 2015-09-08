/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
XML3D.tools.namespace("Atlas");
XML3D.tools.namespace("Atlas.Spinner");

(function() {
	"use strict";
	Atlas.SpinnerManager = new XML3D.tools.Singleton({

		_spinnerOpts: {
			lines: 12,            // The number of lines to draw
			length: 7,            // The length of each line
			width: 5,             // The line thickness
			radius: 10,           // The radius of the inner circle
			rotate: 0,            // Rotation offset
			corners: 1,           // Roundness (0..1)
			color: '#428bca',     // #rgb or #rrggbb
			direction: 1,         // 1: clockwise, -1: counterclockwise
			speed: 1,             // Rounds per second
			trail: 100,           // Afterglow percentage
			opacity: 1/4,         // Opacity of the lines
			fps: 30,              // Frames per second when using setTimeout()
			zIndex: 2e9,          // Use a high z-index by default
			className: 'spinner', // CSS class to assign to the element
			top: '57%',           // center vertically
			left: '50%',          // center horizontally
			position: 'absolute'  // element position
		},

		_xml3dSpinner: null,

		initialize: function() {
			window.addEventListener("DOMContentLoaded", this.showXML3DSpinner.bind(this));
		},

		showXML3DSpinner: function(){
			if (XML3D && XML3D.webgl && XML3D.webgl.supported()) {
				$("#xml3d_spinnerContainer").show();
				this._xml3dSpinner = new Spinner(this._spinnerOpts);
				this._xml3dSpinner.spin($("#xml3d_spinner")[0]);
			} else {
				// let XML3D's error code handle this case.
				this._switchToXML3D();
			}
			//TODO: Proper spinner handling when a different asset is selected
			this.hideXml3DSpinner();
		},

		hideXml3DSpinner: function(){
			this._switchToXML3D();
			this._xml3dSpinner.stop();
		},

		_switchToXML3D: function() {
			$("#xml3d_spinnerContainer").hide();
			$(".xml3d_hidden").removeClass("xml3d_hidden");
		}
	});
}());
