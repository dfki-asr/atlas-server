/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import de.dfki.asr.atlas.cdi.annotations.AtlasExporter;
import de.dfki.asr.atlas.model.ArrayMatrix4f;
import de.dfki.asr.atlas.model.Folder;
import de.dfki.asr.atlas.model.ListingFolder;

@AtlasExporter(contentType = "application/json", fileExtension = ".json", folderTypes = {"node"})
public class SubassetListingExporter implements ExportOperation<ListingFolder>{

	private ExportContext context;

	@Override
	public ListingFolder export(ExportContext context) {
		this.context = context;
		Folder root = context.getStartingFolder();
		ListingFolder assetListing = createListingFromFolder(root);
		for (Folder child : root.getChildFolders()) {
			convertFolder(child, assetListing);
		}
		return assetListing;
	}

	@Override
	public boolean canExportFolder(Folder folder) {
		return folder.getType().equals("node");
	}

	private void convertFolder(Folder currentFolder, ListingFolder parentAsset) {
		if (currentFolder.getType().equals("node")) {
			ListingFolder subasset = createListingFromFolder(currentFolder);
			parentAsset.getChildren().add(subasset);
			for (Folder child : currentFolder.getChildFolders()) {
				convertFolder(child, subasset);
				if (child.getType().equals("mesh")) {
					// any mesh triggers flag
					subasset.setHasGeometry(true);
				}
			}
		}
	}

	private String buildURLForFolder(Folder folder) {
		return context.uriFor(folder).toString();
	}

	private ListingFolder createListingFromFolder(Folder folder) {
		ListingFolder subasset = new ListingFolder();
		subasset.setName(folder.getName());
		subasset.setUrl(buildURLForFolder(folder));
		subasset.setTransform(readLocalTransform(folder));
		return subasset;
	}

	private ArrayMatrix4f readLocalTransform(Folder folder) {
		return ExporterUtils.readTransform(folder, "transform", context);
	}
}
