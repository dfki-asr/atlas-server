/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
var Atlas = Atlas || {};

(function() {
	"use strict";
	Atlas.App = new function(){
		this._template = undefined;

		this._onWindowLoad = function(){
			this._getAvailableAssets();
			var handleBarSource = $("#asset-list-entry-template").html();
			this._template = Handlebars.compile(handleBarSource);
		};

		this._getAvailableAssets = function(){
			$.ajax("rest/asset/", {
				dataType: "json",
				success: this._fillAvailableAssetsFromResponse.bind(this),
				error: this._onError.bind(this)
			});
		};

		this._fillAvailableAssetsFromResponse = function(data){
			data.sort();
			for (var index in data) {
				var assetUrl = data[index];
				var assetName = assetUrl.substring(assetUrl.lastIndexOf('/')+1, assetUrl.length);
				var listEntryHTML = this._template({assetname: assetName});
				$("#asset-list").append($(listEntryHTML));
			}
		};

		this._onError = function(error){
			console.error(error);
		};
	};
	window.addEventListener("load", Atlas.App._onWindowLoad.bind(Atlas.App));
})();
