/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Blob;
import java.util.List;

public interface AssetManager {

	public Asset findAsset(String assetName, String revisionId);

	public Asset findLatestRevisionForAsset(String assetName);

	public Blob getBlobOfAsset(String assetName, String hash);

	public List<Asset> getAssetListing();
}
