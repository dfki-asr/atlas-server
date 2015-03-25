/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel
public class AssetTransaction {
	private static final long serialVersionUID = -3959032447227164539L;

	private final ImportOperation importOperation;

	public AssetTransaction(ImportOperation importOperation) {
		this.importOperation = importOperation;
	}

	@JsonProperty
	@ApiModelProperty(value = "Id of the transaction")
	public String getId() {
		return importOperation.getId();
	}

	@JsonProperty
	@ApiModelProperty(value = "Status of the transaction")
	public ImportOperation.Status getStatus() {
		return importOperation.getStatus();
	}

	@JsonProperty
	@ApiModelProperty(value = "Description of the status")
	public String getStatusDescription() {
		switch(importOperation.getStatus()) {
			case RECEIVED: return "The asset has been received by the server";
			case ACCEPTED: return "The asset has been accepted for processing";
			case REJECTED: return "The asset has been rejected. See the 'detail' field for more information.";
			case PROCESSING: return "The asset is being processed";
			case FINISHED: return "The asset has been persisted and is now accessible";
			default: return "";
		}
	}

	@JsonProperty
	@ApiModelProperty(value = "Details from the import, e.g. errors or warning that came up during the import")
	public String getDetail() {
		return importOperation.getDetail();
	}

	@JsonProperty
	@ApiModelProperty(value = "Location of the asset")
	public String getLocation() {
		return importOperation.getFinishedAssetLocation();
	}

	@JsonProperty
	@ApiModelProperty(value = "Asset name")
	public String getAssetName() {
		return importOperation.getAssetName();
	}

	@JsonProperty
	@ApiModelProperty(value = "Imported data type")
	public String getImportedAs() {
		return importOperation.getImportedAs();
	}

}
