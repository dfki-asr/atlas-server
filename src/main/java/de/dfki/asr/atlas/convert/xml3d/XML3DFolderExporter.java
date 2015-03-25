/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.xml3d;

import de.dfki.asr.atlas.business.AssetManager;
import de.dfki.asr.atlas.cdi.annotations.AtlasExporter;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.convert.ExportOperation;
import de.dfki.asr.atlas.model.ArrayMatrix4f;
import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Folder;
import de.dfki.asr.xml3d.jaxb.Assetmesh;
import de.dfki.asr.xml3d.jaxb.Defs;
import de.dfki.asr.xml3d.jaxb.XML3D;
import de.dfki.asr.xml3d.jaxb.XML3DAsset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AtlasExporter(contentType = "model/vnd.xml3d.model+xml", fileExtension = ".xml", folderTypes = {"node"})
public class XML3DFolderExporter implements ExportOperation<XML3D> {

	private final Logger log = LoggerFactory.getLogger(XML3DFolderExporter.class);
	protected Asset asset;
	protected AssetManager manager;
	protected Folder startingFolder;
	protected ExportContext context;

	private final XML3D xml3d;
	private Defs defs;
	private XML3DAsset assetTag;
	private final ArrayMatrix4f identityMatrix;
	private XML3DMeshExporter meshExporter;

	public XML3DFolderExporter() {
		identityMatrix = new ArrayMatrix4f();
		identityMatrix.setIdentity();
		this.xml3d = new XML3D();
	}

	public void exportFolderTreeOfAsset(){
		setupDocument();
		meshExporter = new XML3DMeshExporter(context, defs);
		exportFoldersRecursivly(startingFolder);
	}

	private void setupDocument() {
		defs = new Defs();
		xml3d.setDefs(defs);
		assetTag = new XML3DAsset();
		defs.setAsset(assetTag);
		assetTag.setId(asset.getName());
	}

	private void exportFoldersRecursivly(Folder folder){
		switch(folder.getType()) {
			case "node":
				if(folder.hasChildren()){
					exportChildFolder(folder);
				}
				break;
			case "mesh":
				Assetmesh mesh = meshExporter.export(folder);
				assetTag.addAssetmesh(mesh);
				break;
			default:
				log.warn("Unknown folder type encountered " + folder.getType());
		}
	}

	private void exportChildFolder(Folder folder){
		for (Folder child : folder.getChildFolders()) {
			if(shouldExportFolder(child)){
				exportFoldersRecursivly(child);
			}
		}
	}

	private boolean shouldExportFolder(Folder folder){
		if( folder.getType().equals("node") ){
			return context.getIncludeChildren();
		}
		return true;
	}

	@Override
	public XML3D export(ExportContext ctx) {
		manager = ctx.getAssetManager();
		asset = ctx.getSourceAsset();
		context = ctx;
		setStartingFolder(ctx.getStartingFolder());
		exportFolderTreeOfAsset();
		return xml3d;
	}

	public void setStartingFolder(Folder folder) {
		if (!canExportFolder(folder)) {
			throw new IllegalArgumentException("This Exporter cannot export the Folder "+folder);
		}
		startingFolder = folder;
	}

	@Override
	public boolean canExportFolder(Folder folder) {
		return folder.getType().equals("node");
	}
}
