/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import de.dfki.asr.atlas.model.Folder;

public interface ExportOperation<DocumentType> {
	/**
	 * Export a Document for an asset.
	 * Needs to have the source Asset set with {@see setSourceAsset},
	 * and can optionally have a Folder from which to export downwards.
	 * @param context
	 * @return the converted source asset.
	 */
	DocumentType export(ExportContext context);

	/**
	 * Check whether that folder can be exported by this ExportOperation.
	 * @param folder the Folder to check.
	 * @return true, if the folder can be exported.
	 */
	boolean canExportFolder(Folder folder);

}
