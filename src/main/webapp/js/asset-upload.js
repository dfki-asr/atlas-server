/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
var Atlas = Atlas || {};

(function() {
	"use strict";
	Atlas.Upload = new function(){
		this.file= null;

		this.initialize = function() {
			$('#file_upload_form').on('change', this._prepareFileUpload.bind(this));
			$("#asset_name_input").keyup(this._onAssetNameChanged.bind(this));
			$("#upload_button").on("click", this._onUploadButtonClicked.bind(this));
		};

		this._onAssetNameChanged = function(evt) {
			var assetNameValid = $("#asset_name_input").val().match(/^[A-Za-z][A-Za-z0-9_:\\.-]*$/);
			if (assetNameValid) {
				$("#asset_name_input").removeClass("input-fail").addClass("input-success");
			} else {
				$("#asset_name_input").removeClass("input-success").addClass("input-fail");
			}
			this._validateUploadRequirements();
		};

		this._prepareFileUpload = function(evt) {
			$("#file_upload_label").text("No file").removeClass().addClass("label label-warning");
			this._resetUploadStatus();
			this.file = null;
			if (this._validateFileType(evt.target.files[0])) {
				this.file = evt.target.files[0];
				$("#file_upload_label").text(this.file.name).removeClass().addClass("label label-success");
			} else {
				$("#file_upload_label").text(evt.target.files[0].name).removeClass().addClass("label label-danger");
			}
			this._validateUploadRequirements();
		};

		this._resetUploadStatus = function(){
			$("#status-container").removeClass().addClass("hidden color-processing");
			$("#status-text").text("Uploading");
			$("#status-icon").removeClass().addClass("glyphicon glyphicon-retweet");
		};

		this._validateFileType = function(file) {
			if (!file) {
				return false;
			}
			return !!this._guessMimetypeFromFileName(file);
		};

		this._validateUploadRequirements = function() {
			var assetNameValid = $("#asset_name_input").val().match(/^[A-Za-z][A-Za-z0-9_:\\.-]*$/);
			var selectedFileValid = this.file;
			if (assetNameValid && selectedFileValid) {
				$("#upload_button").prop("disabled", false).addClass("btn-success");
			} else {
				$("#upload_button").prop("disabled", true).removeClass("btn-success");
			}
		};

		this._onUploadButtonClicked = function(evt) {
			$("#status-container").removeClass("hidden");
			$.ajax({
				url: 'rest/asset/' + $("#asset_name_input").val(),
				type: 'PUT',
				data: this.file,
				cache: false,
				dataType: "text",
				contentType: this._guessMimetypeFromFileName(this.file),
				processData: false,
				success: this._onFileUploadSuccess.bind(this),
				error: this._onFileUploadError.bind(this)
			});
		};

		this._guessMimetypeFromFileName = function(file) {
			var extension = file.name.substring(file.name.lastIndexOf('.'), file.name.length).toLowerCase();
			switch(extension) {
				case ".dae": return "model/vnd.collada+x";
				case ".zip": return "application/zip";
				default: return "";
			}
		};

		this._onFileUploadSuccess = function(evt) {
			this._setUploadStatusSuccess();
		};

		this._setUploadStatusSuccess = function(){
			$("#status-container").removeClass("color-processing").addClass("color-success");
			$("#status-text").text("Upload Success");
			$("#status-icon").removeClass("glyphicon-retweet").addClass("glyphicon-ok");
		};

		this._onFileUploadError = function(evt) {
			this._setUploadStatusFailure();
		};

		this._setUploadStatusFailure = function(){
			$("#status-container").removeClass("color-processing").addClass("color-failure");
			$("#status-text").text("Upload failed");
			$("#status-icon").removeClass("glyphicon-retweet").addClass("glyphicon-remove");
		};
	};
	window.addEventListener("load", Atlas.Upload.initialize.bind(Atlas.Upload));

})();