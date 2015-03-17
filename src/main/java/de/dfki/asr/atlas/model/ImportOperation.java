/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import de.dfki.asr.atlas.cdi.annotations.JcromMapped;
import org.jcrom.annotations.JcrProperty;

@JcromMapped
public class ImportOperation extends AtlasEntity {
	private static final long serialVersionUID = 5107340713101912736L;

	public static enum Status { RECEIVED, ACCEPTED, REJECTED, PROCESSING, FINISHED }

	@JcrProperty
	private String assetName = "";

	@JcrProperty
	private Status status = Status.RECEIVED;

	@JcrProperty
	private String importedAs = "";

	@JcrProperty
	private String detail = "";

	@JcrProperty
	private String scratchSpaceFile = "";

	@JcrProperty
	private String fileType = "";

	@JcrProperty
	private String finishedAssetLocation = "";

	public ImportOperation() {
		// The JCROM mapper needs a no-argument constructor to be present
	}

	public ImportOperation(String assetName) {
		this.assetName = assetName;
		status = Status.RECEIVED;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getImportedAs() {
		return importedAs;
	}

	public void setImportedAs(String importedAs) {
		this.importedAs = importedAs;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getScratchSpaceFile() {
		return scratchSpaceFile;
	}

	public void setScratchSpaceFile(String scratchSpaceFile) {
		this.scratchSpaceFile = scratchSpaceFile;
	}

	public String getFinishedAssetLocation() {
		return finishedAssetLocation;
	}

	public void setFinishedAssetLocation(String finishedAssetLocation) {
		this.finishedAssetLocation = finishedAssetLocation;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}


}
