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
import org.collada._2008._03.colladaschema.COLLADA;

@AtlasExporter(contentType = "model/collada+xml", fileExtension = ".dae", folderTypes = {"node"})
public class ColladaXMLExporter implements ExportOperation<COLLADA> {
	ColladaFolderExporter folderExporter = new ColladaFolderExporter();

	@Override
	public COLLADA export(ExportContext context) {
		SimpleCOLLADADocument doc = folderExporter.export(context);
		return doc.getDocument();
	}

	@Override
	public boolean canExportFolder(Folder folder) {
		return folderExporter.canExportFolder(folder);
	}
}
