/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.collada;

import de.dfki.asr.atlas.business.AssetManager;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.convert.ExportOperation;
import de.dfki.asr.atlas.convert.FloatStreamIterator;
import de.dfki.asr.atlas.convert.FloatToDoubleIterator;
import de.dfki.asr.atlas.convert.IntStreamIterator;
import de.dfki.asr.atlas.convert.IntToBigIntIterator;
import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Blob;
import de.dfki.asr.atlas.model.Folder;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.collada._2008._03.colladaschema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColladaFolderExporter implements ExportOperation<SimpleCOLLADADocument> {

	private final Logger log = LoggerFactory.getLogger(ColladaFolderExporter.class);

	private static final Double[] identity = {1.0, 0.0, 0.0, 0.0,
											  0.0, 1.0, 0.0, 0.0,
											  0.0, 0.0, 1.0, 0.0,
											  0.0, 0.0, 0.0, 1.0};

	private AssetManager assetManager;
	private Asset asset;
	private Folder startFolder;
	private String assetName;
	private SimpleCOLLADADocument colladaDocument;
	private Set<String> usedGlobalIDs;
	private ExportContext context;

	public ColladaFolderExporter() {
		colladaDocument = new SimpleCOLLADADocument();
		usedGlobalIDs = new HashSet<>();
	}

	public void exportFolderTreeOfAsset() {
		assetName = asset.getName();
		VisualSceneType scene = colladaDocument.getVisualScene();
		NodeType rootNode = recursiveFolderExport(startFolder);
		scene.getNodes().add(rootNode);
	}

	private NodeType recursiveFolderExport(Folder currentFolder) {
		NodeType node = createNodeFromFolder(currentFolder);
		for (Folder child : currentFolder.getChildFolders()) {
			switch(child.getType()) {
				case "node":
					node.getNodes().add(recursiveFolderExport(child));
					break;
				case "mesh":
					//Since we only support one material per mesh, we can use this symbol for all the meshes.
					String symbol = "material";
					InstanceGeometryType mesh = meshExport(child, symbol);
					node.getInstanceGeometries().add(mesh);
					processMaterialForMeshFolder(child, mesh, symbol);
					break;
				default:
					log.warn("Unknown folder type encountered " + child.getType());
			}
		}
		return node;
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

	private void processMaterialForMeshFolder(Folder meshFolder, InstanceGeometryType mesh, String materialSymbol) {
		Folder materialFolder = findMaterialFolderForMeshFolder(meshFolder);
		if (materialFolder != null) {
			ColladaMaterialExporter materialExporter = new ColladaMaterialExporter(context);
			materialExporter.exportMaterial(assetName, materialFolder, mesh, colladaDocument, materialSymbol);
		} else {
			log.warn("Did not find material for mesh node " + meshFolder.getName());
		}
	}

	private NodeType createNodeFromFolder(Folder folder) {
		NodeType node = new NodeType();
		setNodeProperties(node, folder);
		processBlobs(node, folder);
		return node;
	}

	private void processBlobs(NodeType node, Folder folder) {
		String hash = folder.getHashOfBlobWithType("transform");
		if(hash == null){
			return;
		}
		Blob blob = assetManager.getBlobOfAsset(assetName, hash);
		if(blob != null) {
			setNodeTransform(node, blob);
		}else{
			setNodeTransformIdentity(node);
		}
	}

	private void setNodeProperties(NodeType node, Folder folder) {
		node.setName(folder.getName());
		node.setType(NodeEnum.NODE);
	}

	private void setNodeTransform(NodeType node, Blob transformBlob) {
		MatrixType mat = new MatrixType();
		Iterator<Float> floatValues = new FloatStreamIterator(transformBlob.getData());
		Iterator<Double> doubleValues = new FloatToDoubleIterator(floatValues);
		CollectionUtils.addAll(mat.getValues(), doubleValues);
		node.getLookatsAndMatrixesAndRotates().add(mat);
	}

	private void setNodeTransformIdentity(NodeType node){
		MatrixType mat = new MatrixType();
		mat.getValues().addAll(Arrays.asList(identity));
		node.getLookatsAndMatrixesAndRotates().add(mat);
	}

	private GeometryType makeGeometry(Folder meshFolder, String materialSymbol) {
		if (!meshFolder.getType().equals("mesh")) {
			throw new IllegalArgumentException("Expected folder of type 'mesh', got '"+meshFolder.getType()+"'!");
		}
		GeometryType geometry = new GeometryType();
		geometry.setId(makeGlobalID(meshFolder.getName()+"-mesh"));
		geometry.setName(meshFolder.getName());
		SourceType positions = null;
		SourceType normals = null;
		SourceType texcoords = null;
		TrianglesType triangles = null;
		for (String blobHash : meshFolder.getAllBlobHashes()) {
			Blob blob = assetManager.getBlobOfAsset(assetName, blobHash);
			String blobType = meshFolder.getTypeOfBlob(blobHash);
			switch (blobType) {
				case "positions":
					positions = convertFloatArrayBlob(blobType, blob);
					insertXYZAccessorInto(positions);
					break;
				case "normals":
					normals = convertFloatArrayBlob(blobType, blob);
					insertXYZAccessorInto(normals);
					break;
				case "texcoords":
					texcoords = convertFloatArrayBlob(blobType, blob);
					insertSTAccessorInto(texcoords);
					break;
				case "index":
					triangles = convertToTriangles(blob);
					triangles.setMaterial(materialSymbol);
					break;
				default:
					log.info("Unsupported Blob type: "+blobType);
			}
		}

		if (triangles != null) {
			// Only need to do anything if there's triangles in the Mesh.
			MeshType mesh = new MeshType();
			geometry.setMesh(mesh);
			mesh.getLinesAndLinestripsAndPolygons().add(triangles);

			if (positions != null) {
				VerticesType verts = new VerticesType();
				InputLocalType input = new InputLocalType();
				input.setSemantic("POSITION");
				input.setSource(idRef(positions.getId()));
				verts.getInputs().add(input);
				verts.setId(makeGlobalID(meshFolder.getName() + "-vertices"));
				mesh.setVertices(verts);
				mesh.getSources().add(positions);
				InputLocalOffsetType vertexInput = createInput("VERTEX",idRef(verts.getId()),0);
				triangles.getInputs().add(vertexInput);
			}
			if (normals != null) {
				mesh.getSources().add(normals);
				InputLocalOffsetType normalsInput = createInput("NORMAL", idRef(normals.getId()), 0);
				triangles.getInputs().add(normalsInput);
			}
			if (texcoords != null) {
				mesh.getSources().add(texcoords);
				InputLocalOffsetType texcoordsInput = createInput("TEXCOORD", idRef(texcoords.getId()), 0);
				triangles.getInputs().add(texcoordsInput);
			}
		}
		return geometry;
	}

	private InputLocalOffsetType createInput(String semantic, String reference, int ordinal) {
		InputLocalOffsetType vertexInput = new InputLocalOffsetType();
		vertexInput.setSemantic(semantic);
		vertexInput.setSource(reference);
		vertexInput.setOffset(BigInteger.valueOf(ordinal));
		return vertexInput;
	}

	public SourceType convertFloatArrayBlob(String blobType, Blob b) {
		FloatStreamIterator fsi = new FloatStreamIterator(b.getData());
		SourceType src = new SourceType();
		src.setFloatArray(new FloatArrayType());
		src.setId("hash-"+b.getHash()+"-"+blobType+"-source");
		src.getFloatArray().setId("hash-"+b.getHash()+"-"+blobType+"-source-data");
		CollectionUtils.addAll(src.getFloatArray().getValues(), new FloatToDoubleIterator(fsi));
		int posCount = src.getFloatArray().getValues().size();
		src.getFloatArray().setCount(BigInteger.valueOf(posCount));
		return src;
	}

	private void insertXYZAccessorInto(SourceType source) {
		int posCount = source.getFloatArray().getValues().size();
		source.setTechniqueCommon(new SourceType.TechniqueCommon());
		AccessorType access = new AccessorType();
		access.setSource(idRef(source.getFloatArray().getId()));
		access.setCount(BigInteger.valueOf(posCount / 3));
		access.setStride(BigInteger.valueOf(3));
		access.getParams().add(createParamType("X", "float"));
		access.getParams().add(createParamType("Y", "float"));
		access.getParams().add(createParamType("Z", "float"));
		source.getTechniqueCommon().setAccessor(access);
	}

	private void insertSTAccessorInto(SourceType source) {
		int posCount = source.getFloatArray().getValues().size();
		source.setTechniqueCommon(new SourceType.TechniqueCommon());
		AccessorType access = new AccessorType();
		access.setSource(idRef(source.getFloatArray().getId()));
		access.setCount(BigInteger.valueOf(posCount / 3));
		access.setStride(BigInteger.valueOf(3));
		access.getParams().add(createParamType("S", "float"));
		access.getParams().add(createParamType("T", "float"));
		source.getTechniqueCommon().setAccessor(access);
	}

	public ParamType createParamType(String name, String type) {
		ParamType pt = new ParamType();
		pt.setName(name);
		pt.setType(type);
		return pt;
	}

	public TrianglesType convertToTriangles(Blob blob) {
		TrianglesType triangles = new TrianglesType();
		triangles.setP(new PType());
		IntStreamIterator isi = new IntStreamIterator(blob.getData());
		IntToBigIntIterator it = new IntToBigIntIterator(isi);
		CollectionUtils.addAll(triangles.getP().getValues(), it);
		triangles.setCount(BigInteger.valueOf(triangles.getP().getValues().size()/3));
		triangles.setName(blob.getHash());
		return triangles;
	}

	public String idRef(String id) {
		return "#"+id;
	}

	private InstanceGeometryType meshExport(Folder child, String materialSymbol) {
		InstanceGeometryType instance = new InstanceGeometryType();
		GeometryType geometry = makeGeometry(child, materialSymbol);
		LibraryGeometriesType geometries = colladaDocument.getLibrary(LibraryGeometriesType.class);
		geometries.getGeometries().add(geometry);
		instance.setUrl(idRef(geometry.getId()));
		return instance;
	}

	@Override
	public SimpleCOLLADADocument export(ExportContext ctx) {
		assetManager = ctx.getAssetManager();
		asset = ctx.getSourceAsset();
		context = ctx;
		setStartingFolder(ctx.getStartingFolder());
		exportFolderTreeOfAsset();
		return colladaDocument;
	}

	public void setStartingFolder(Folder folder) {
		if (!canExportFolder(folder)) {
			throw new IllegalArgumentException("This Exporter cannot handle that folder: "+folder);
		}
		startFolder = folder;
	}

	@Override
	public boolean canExportFolder(Folder folder) {
		return folder.getType().equals("node");
	}

	public String makeGlobalID(String template) {
		String proposal = template;
		int i = 0;
		while (usedGlobalIDs.contains(proposal)) {
			proposal = template + "-" + i;
			i++;
		}
		usedGlobalIDs.add(proposal);
		return proposal;
	}
}
