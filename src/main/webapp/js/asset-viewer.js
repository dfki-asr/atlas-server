/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
XML3D.tools.namespace("Atlas");

(function() {
	"use strict";
	Atlas.AssetViewer = new XML3D.tools.Singleton({
		_availableAssetsList: [],
		_sceneInitialized: false,
		_dollySpeed: 8,
		cameraController: null,

		initialize: function() {
			window.addEventListener("load", this._onWindowLoad.bind(this));
			window.addEventListener("resize", this._onWindowResize.bind(this));
		},

		_onWindowLoad: function() {
			$("#xml3dmain").on("framedrawn", this._onFrameDrawn.bind(this));
			$("#xml3dmain").on("load", this._initScene.bind(this));
			this._createCameraController();
			this._onWindowResize();
			this._getAsset();
		},

		_onWindowResize: function() {
			$("#xml3dmain").width($(window).width());
			$("#xml3dmain").height($(window).height());
		},

		_onFrameDrawn: function(evt) {
			var data = evt.originalEvent.detail;
			$("#asset-stats-objects").text(data.count.objects);
			$("#asset-stats-faces").text(data.count.primitives);
		},

		_initScene: function() {
			var scenebbox = $("#xml3dmain")[0].getBoundingBox();
			var sceneDimensions = this._getSceneDimensions(scenebbox);
			this._dollySpeed = Math.max(Math.max(sceneDimensions.x, sceneDimensions.y), sceneDimensions.z);
			this.cameraController.behavior._dollySpeed = this._dollySpeed;
			this._centerAssetOnOrigin(sceneDimensions, scenebbox);
			scenebbox = $("#xml3dmain")[0].getBoundingBox();
			sceneDimensions = this._getSceneDimensions(scenebbox);
			this._setCameraDistanceToEncloseScene(sceneDimensions);
			this._setLightDirections(sceneDimensions);
		},

		_setLightDirections: function(sceneDimensions) {
			var maxDim = Math.max( Math.max(sceneDimensions.x, sceneDimensions.y), sceneDimensions.z);
			var maxDimHalf = maxDim / 2;
			var pos1 = new XML3DVec3(maxDimHalf, maxDimHalf, maxDimHalf);
			$("#t_Lamp1")[0].translation.set(pos1);
			var pos2 = new XML3DVec3(-maxDimHalf, maxDimHalf, 0);
			$("#t_Lamp2")[0].translation.set(pos2);
			var pos3 = new XML3DVec3(0, -maxDimHalf, -maxDimHalf);
			$("#t_Lamp3")[0].translation.set(pos3);

			var dirRot = new XML3DRotation();
			dirRot.setRotation(new XML3DVec3(0, 0, -1), pos1.negate());
			$("#t_Lamp1")[0].rotation.set(dirRot);
			dirRot.setRotation(new XML3DVec3(0, 0, -1), pos2.negate());
			$("#t_Lamp2")[0].rotation.set(dirRot);
			dirRot.setRotation(new XML3DVec3(0, 0, -1), pos3.negate());
			$("#t_Lamp3")[0].rotation.set(dirRot);
		},

		_getSceneDimensions: function(scenebbox) {
			var dims = {};
			dims.x = Math.abs(scenebbox.max.x - scenebbox.min.x);
			dims.y = Math.abs(scenebbox.max.y - scenebbox.min.y);
			dims.z = Math.abs(scenebbox.max.z - scenebbox.min.z);
			return dims;
		},

		_centerAssetOnOrigin: function(sceneDimensions, scenebbox) {
			var xOffset = scenebbox.max.x - sceneDimensions.x / 2;
			var yOffset = scenebbox.max.y - sceneDimensions.y / 2;
			var zOffset = scenebbox.max.z - sceneDimensions.z / 2;
			var trans = $("#t_asset")[0].translation;
			trans.x -= xOffset;
			trans.y -= yOffset;
			trans.z -= zOffset;
			$("#t_asset")[0].translation.set(trans);
		},

		_setCameraDistanceToEncloseScene: function(sceneDimensions) {
			var vFoV = $("#Camera")[0].fieldOfView;
			var hFoV = this._calculateHorizontalFoV(vFoV);
			var xDist = Math.abs(sceneDimensions.x) / Math.tan(hFoV / 2);
			var yDist = Math.abs(sceneDimensions.y) / Math.tan(vFoV / 2);
			var trans = $("#t_Camera")[0].translation;
			var delta = -(trans.z - Math.max(xDist, yDist));
			this.cameraController.dolly(delta / this._dollySpeed);
		},

		_calculateHorizontalFoV: function(vFoV) {
			var screenWidth = $(window).width();
			var screenHeight = $(window).height();
			return 2 * Math.atan(Math.tan(vFoV / 2) * screenWidth/screenHeight);
		},

		_getAsset: function() {
			var assetName = this._getParameterFromUrl("asset-name");
			$("#asset-name").text(assetName);
			$("#asset-model").attr("src", "rest/asset/" + assetName + ".xml" + " #" + assetName);
		},

		_createCameraController: function() {
			this.cameraController = new XML3D.tools.MouseExamineController($("#camera_group")[0], {
				rotateSpeed: 5,
				dollySpeed: this._dollySpeed
			});
			this.cameraController.attach();
		},

		_getParameterFromUrl: function(paramName) {
			var sPageURL = window.location.search.substring(1);
			var sURLVariables = sPageURL.split('&');
			for (var i = 0; i < sURLVariables.length; i++)
			{
				var sParameterName = sURLVariables[i].split('=');
				if (sParameterName[0] === paramName)
				{
					return sParameterName[1];
				}
			}
		}
	});
})();
