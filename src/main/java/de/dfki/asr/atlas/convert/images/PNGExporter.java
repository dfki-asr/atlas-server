/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.images;

import de.dfki.asr.atlas.cdi.annotations.AtlasExporter;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.convert.ExportOperation;
import de.dfki.asr.atlas.model.Blob;
import de.dfki.asr.atlas.model.Folder;
import java.io.InputStream;

@AtlasExporter(contentType = "image/png", fileExtension = ".png", folderTypes = {"diffuse","specular","ambient","emissive",
	"displacement","height","lightmap","normals","opacity","reflection","shininess"})
public class PNGExporter implements ExportOperation<InputStream>{

	@Override
	public InputStream export(ExportContext context) {
		Folder folder = context.getStartingFolder();
		String textureType = folder.getType();
		String blobHash = folder.getHashOfBlobWithType(textureType);
		Blob textureBlob = context.getAssetManager().getBlobOfAsset(context.getSourceAsset().getName(), blobHash);
		return textureBlob.getData();
	}

	@Override
	public boolean canExportFolder(Folder folder) {
		String mimetype = folder.getAttribute("mimetype").toLowerCase();
		return mimetype != null && mimetype.equals("image/png");
	}
}
