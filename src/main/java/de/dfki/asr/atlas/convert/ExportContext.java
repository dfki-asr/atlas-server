/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import de.dfki.asr.atlas.business.AssetManager;
import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Folder;
import de.dfki.asr.atlas.rest.LocationBuilder;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExportContext {
	AssetManager manager;
	Asset asset;
	Folder startFolder;
	String extension;
	LocationBuilder startOfAssetPath;
	Set<Folder> externallyReferencedFolders;
	boolean includeChildren;

	public ExportContext(AssetManager manager,
						 Asset asset,
						 Folder startFolder,
						 String requestedExtension,
						 LocationBuilder startBuilder,
						 boolean includeChildren) {
		this.manager = manager;
		this.asset = asset;
		this.startFolder = startFolder;
		this.extension = requestedExtension;
		this.startOfAssetPath = startBuilder;
		this.externallyReferencedFolders = new HashSet<>();
		this.includeChildren = includeChildren;
	}

	/**
	 * Get the Asset Manager for this Export operation.
	 * The Asset manager is used to retrieve Blobs for the Asset and possibly
	 * other Storage operations.
	 * @return the AssetManager to use for the export operation.
	 */
	public AssetManager getAssetManager() { return manager; }

	/**
	 * Set the Source asset for this export operation.
	 * The contents of the exported document will come from this asset.
	 * @return The asset to export from.
	 */
	public Asset getSourceAsset() { return asset; }

	/**
	 * Set a starting folder for the export operation.
	 * The exported document will contain everything
	 * (that this ExportOperation can convert) from this folder below.
	 * Obviously, this Folder needs to be part of the Asset set with
	 * {@see setSourceAsset}. Defaults to the root folder of the Asset.
	 * @return The Folder from which to start exporting.
	 */
	public Folder getStartingFolder() { return startFolder; }

	public String getRequestedFileExtension() { return extension; }

	/**
	 * Get the Flag which tells the exporting operation, whether it should include
	 * the children of a node or not.
	 * @return the flag
	 */
	public boolean getIncludeChildren(){
		return includeChildren;
	}

	public URI uriFor(Folder folder) {
		LocationBuilder builderForFolder = startOfAssetPath.clone();
		builderForFolder.add(asset.getName());
		List<Integer> pathOfFolder = folder.getAddress();
		for (Integer integer : pathOfFolder) {
			builderForFolder.add(integer.toString());
		}
		return builderForFolder.uri();
	}

	public URI declareExternalReference(Folder referencedFolder) {
		externallyReferencedFolders.add(referencedFolder);
		return uriFor(referencedFolder);
	}

	public Collection<Folder> getExternalReferences() {
		return externallyReferencedFolders;
	}
}
