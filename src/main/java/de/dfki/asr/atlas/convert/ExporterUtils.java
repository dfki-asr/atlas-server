/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import de.dfki.asr.atlas.model.ArrayMatrix4f;
import de.dfki.asr.atlas.model.Blob;
import de.dfki.asr.atlas.model.Folder;

public class ExporterUtils {

	private ExporterUtils() {
		// static helper class, doesn't need constructor.
	}

	public static Blob fetchBlobWithTypeFromFolder(String type, Folder folder, ExportContext ctx) {
		String hash = folder.getHashOfBlobWithType(type);
		if (hash == null) {
			return null;
		}
		return ctx.getAssetManager().getBlobOfAsset(ctx.getSourceAsset().getName(), hash);
	}

	public static ArrayMatrix4f readTransform(Folder folder, String transformType, ExportContext context) {
		Blob transformBlob = fetchBlobWithTypeFromFolder(transformType, folder, context);
		ArrayMatrix4f transform;
		if (transformBlob != null) {
			transform = new ArrayMatrix4f(new FloatStreamIterator(transformBlob.getData()));
		} else {
			transform = new ArrayMatrix4f();
			transform.setIdentity();
		}
		return transform;
	}
}
