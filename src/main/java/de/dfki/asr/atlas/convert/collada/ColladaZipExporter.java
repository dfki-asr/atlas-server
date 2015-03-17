/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.collada;

import de.dfki.asr.atlas.cdi.annotations.AtlasExporter;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.convert.ExportOperation;
import de.dfki.asr.atlas.model.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AtlasExporter(contentType = "application/zip", fileExtension = ".zip", folderTypes = {"node"})
public class ColladaZipExporter implements ExportOperation<ColladaZipCompressor> {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private ColladaFolderExporter folderExporter = new ColladaFolderExporter();

	@Override
	public ColladaZipCompressor export(ExportContext ctx) {
		SimpleCOLLADADocument doc = folderExporter.export(ctx);
		return new ColladaZipCompressor(ctx, doc);
	}

	@Override
	public boolean canExportFolder(Folder folder) {
		return folderExporter.canExportFolder(folder);
	}
}
