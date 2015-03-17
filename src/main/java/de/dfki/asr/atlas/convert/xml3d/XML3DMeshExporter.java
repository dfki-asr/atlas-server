/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.xml3d;

import de.dfki.asr.xml3d.jaxb.Assetmesh;
import de.dfki.asr.xml3d.jaxb.FloatList;
import de.dfki.asr.xml3d.jaxb.Defs;
import de.dfki.asr.xml3d.jaxb.Shader;
import de.dfki.asr.xml3d.jaxb.IntList;
import de.dfki.asr.atlas.business.AssetManager;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.convert.ExporterUtils;
import de.dfki.asr.atlas.convert.FloatStreamIterator;
import de.dfki.asr.atlas.convert.IntStreamIterator;
import de.dfki.asr.atlas.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XML3DMeshExporter {

	private final Logger log = LoggerFactory.getLogger(XML3DMeshExporter.class);
	protected Asset asset;
	protected AssetManager manager;
	protected Defs defs;
	protected ExportContext context;
	private final Map<String, String> addedShaders = new HashMap<>();
	private final List<String> usedAssetmeshNames = new ArrayList<>();
	private long id = 1;

	public XML3DMeshExporter(ExportContext context, Defs defs){
		this.manager = context.getAssetManager();
		this.asset = context.getSourceAsset();
		this.context = context;
		this.defs = defs;
	}

	Assetmesh export(Folder folder) {
		Assetmesh mesh = new Assetmesh();
		mesh.setType("triangles");
		mesh.setName(generateAssetmeshName(folder.getName()));
		addTransformMatrixToMesh(mesh, readGlobalTransform(folder.getParent()));
		processPositions(folder, mesh);
		processColors(folder, mesh);
		processNormals(folder, mesh);
		processTexcoords(folder, mesh);
		processIndex(folder, mesh);

		Folder materialFolder = findMaterialFolderForMeshFolder(folder);
		if(materialFolder != null){
			String shaderId = addShaderToDefs(materialFolder);
			mesh.setShader("#" + shaderId);
		} else {
			log.warn("Did not find material for mesh node " + folder.getName());
		}
		return mesh;
	}

	private String generateAssetmeshName(String assetName){
		if(!usedAssetmeshNames.contains(assetName)){
			usedAssetmeshNames.add(assetName);
			return assetName;
		}else{
			//We do not have to add this to the list. The suffix will be unique, until we run out of longs ...
			return assetName + generateNewIDSuffix();
		}
	}

	private String generateNewIDSuffix() {
		String idAsString = Long.toString(id);
		id++;
		return idAsString;
	}

	private void addTransformMatrixToMesh(Assetmesh mesh, ArrayMatrix4f matrix) {
		FloatList matrixFloatList = new FloatList("meshTransform");
		matrixFloatList.setValues(matrix.asList());
		mesh.setTransform(matrixFloatList);
	}

	private void processPositions(Folder meshFolder, Assetmesh mesh){
		Blob blob = fetchBlobWithTypeFromFolder("positions", meshFolder);
		if(blob == null){
			return;
		}
		FloatList positions = new FloatList("position");
		positions.setValues(IteratorUtils.toList(new FloatStreamIterator(blob.getData())));
		mesh.setPosition(positions);
	}

	private Blob fetchBlobWithTypeFromFolder(String type, Folder folder){
		String hash = folder.getHashOfBlobWithType(type);
		if(hash == null){
			return null;
		}
		return manager.getBlobOfAsset(asset.getName(), hash);
	}

	private void processColors(Folder meshFolder, Assetmesh mesh) {
		Blob blob = fetchBlobWithTypeFromFolder("colors", meshFolder);
		if(blob == null){
			return;
		}
		FloatList colors = new FloatList("color");
		colors.setValues(IteratorUtils.toList(new FloatStreamIterator(blob.getData())));
		mesh.setColor(colors);
	}

	private void processNormals(Folder meshFolder, Assetmesh mesh) {
		Blob blob = fetchBlobWithTypeFromFolder("normals", meshFolder);
		if(blob == null){
			return;
		}
		FloatList normals = new FloatList("normal");
		normals.setValues(IteratorUtils.toList(new FloatStreamIterator(blob.getData())));
		mesh.setNormal(normals);
	}

	private void processTexcoords(Folder meshFolder, Assetmesh mesh) {
		Blob blob = fetchBlobWithTypeFromFolder("texcoords", meshFolder);
		if(blob == null){
			return;
		}
		setTexcoord(blob, mesh);
	}

	private void setTexcoord(Blob blob, Assetmesh mesh){
		FloatStreamIterator iter = new FloatStreamIterator(blob.getData());
		ArrayList<Float> values = new ArrayList<>();
		//Texcoords are stored as float3's to accomodate 3D textures, but XML3D only handles 2D textures by default so we
		//remove the third component for each entry
		int i = 1;
		while(iter.hasNext()){
			Float value = iter.next();
			if(i%3 != 0){
				values.add(value);
			} else {
				if (!value.equals(0.0f)) {
					log.error("3D Texture coordinates erroneously downconverted to 2D. Bailing out.");
					throw new IllegalArgumentException("Tried to export a 3D textured Model to XML3D.");
				}
			}
			i++;
		}
		FloatList texcoords = new FloatList("texcoord");
		texcoords.setValues(values);
		mesh.setTexcoord(texcoords);
	}

	private void processIndex(Folder folder, Assetmesh mesh) {
		Blob blob = fetchBlobWithTypeFromFolder("index", folder);
		if(blob == null){
			return;
		}
		IntList indexList = new IntList("index");
		indexList.setValues(IteratorUtils.toList(new IntStreamIterator(blob.getData())));
		mesh.setIndex(indexList);
	}

	private Folder findMaterialFolderForMeshFolder(Folder meshFolder) {
		List<Folder> children = meshFolder.getChildFolders();
		for (Folder child : children) {
			if (child.getType().equals("material")) {
				return child;
			}
		}
		return null;
	}

	private String addShaderToDefs(Folder materialFolder){
		String materialName = materialFolder.getName();
		if( addedShaders.containsKey(materialName) ){
			return addedShaders.get(materialName);
		}
		String materialId = generateShaderId(materialName);
		XML3DShaderExporter shaderExporter = new XML3DShaderExporter(context);
		Shader shader = shaderExporter.exportShader(materialFolder);
		shader.setId(materialId);
		defs.getShaders().add(shader);
		addedShaders.put(materialName, materialId);
		return materialId;
	}

	private String generateShaderId(String materialName){
		if( stringIsValidHTMLId(materialName) ){
			return materialName;
		}else{
			return "shader_" + generateNewIDSuffix();
		}
	}

	private boolean stringIsValidHTMLId(String str){
		//ID must start with a letter A-Z or a-z.
		//Furthermore use only the following: letters A-Z and a-z, numbers 0-9, undercore _ , colon : and hyphen -
		//It must not use any other special characters
		//We do not allow the dot character, since it breaks all jquery selectors used within xml3d
		String regex = "[A-Za-z][A-Za-z0-9_:-]*";
		return str.matches(regex);
	}

	private ArrayMatrix4f readGlobalTransform(Folder folder) {
		ArrayMatrix4f globalTransform = new ArrayMatrix4f();
		globalTransform.setIdentity();
		if (context.getIncludeChildren()) {
			globalTransform = combineParentTransforms(folder);
		}
		return globalTransform;
	}

	private ArrayMatrix4f combineParentTransforms(Folder folder) {
		ArrayMatrix4f parentGlobalTransform;
		if (folder.equals(context.getSourceAsset().getRootFolder())) {
			parentGlobalTransform = new ArrayMatrix4f();
			parentGlobalTransform.setIdentity();
		} else {
			parentGlobalTransform = combineParentTransforms(folder.getParent());
		}
		ArrayMatrix4f localTransform = ExporterUtils.readTransform(folder, "transform", context);
		parentGlobalTransform.mul(localTransform);
		return parentGlobalTransform;
	}
}
