/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.collada;

import de.dfki.asr.atlas.business.AssetManager;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.model.Blob;
import de.dfki.asr.atlas.model.Color3D;
import de.dfki.asr.atlas.model.Folder;
import de.dfki.asr.atlas.model.Material;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.collada._2008._03.colladaschema.*;
import org.collada._2008._03.colladaschema.FxCommonColorOrTextureType.Color;
import org.collada._2008._03.colladaschema.FxCommonColorOrTextureType.Texture;
import org.collada._2008._03.colladaschema.FxCommonFloatOrParamType.Float;
import org.collada._2008._03.colladaschema.ImageType.InitFrom;
import org.collada._2008._03.colladaschema.ProfileCommonType.Technique;
import org.collada._2008._03.colladaschema.ProfileCommonType.Technique.Phong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColladaMaterialExporter {

	private final Logger log = LoggerFactory.getLogger(ColladaMaterialExporter.class);
	private final AssetManager assetManager;
	private SimpleCOLLADADocument document;
	private String assetName;
	private ProfileCommonType profile;
	private EffectType effect;
	private Technique technique;
	private InstanceGeometryType geometryNode;
	private ExportContext context;

	public ColladaMaterialExporter(ExportContext context) {
		assetManager = context.getAssetManager();
		this.context = context;
	}

	public void exportMaterial(String assetName, Folder materialFolder, InstanceGeometryType geometryNode, SimpleCOLLADADocument document, String materialSymbol) {
		String materialName = materialFolder.getName();
		this.geometryNode = geometryNode;
		this.assetName = assetName;
		this.document = document;
		if (!materialAlreadyInLibrary(materialName)) {
			createEnclosingTags(materialName);
			addPhongType(materialFolder);
			addMaterialToLibrary(materialFolder);
			addEffectToLibrary();
		}
		addMaterialReferenceToGeometry(materialFolder, materialSymbol);
	}


	private void createEnclosingTags(String materialName) {
		effect = new EffectType();
		profile = new ProfileCommonType();
		technique = new Technique();
		profile.setTechnique(technique);

		effect.setId(materialName + "-effect");
		effect.getProfileCOMMONsAndProfileBRIDGEsAndProfileGLES2s().add(profile);
	}

	private void addMaterialReferenceToGeometry(Folder materialFolder, String materialSymbol) {
		BindMaterialType bind = new BindMaterialType();
		BindMaterialType.TechniqueCommon matTechnique = new BindMaterialType.TechniqueCommon();
		InstanceMaterialType matRef = new InstanceMaterialType();
		matRef.setSymbol(materialSymbol);
		matRef.setTarget("#" + materialFolder.getName());
		matTechnique.getInstanceMaterials().add(matRef);
		bind.setTechniqueCommon(matTechnique);
		geometryNode.setBindMaterial(bind);
	}

	private void addEffectToLibrary() {
		document.getLibrary(LibraryEffectsType.class).getEffects().add(effect);
	}

	private void addMaterialToLibrary(Folder materialFolder) {
		String matName = materialFolder.getName();
		MaterialType mat = new MaterialType();
		mat.setId(matName);
		mat.setName(matName);
		InstanceEffectType instanceEffect = new InstanceEffectType();
		instanceEffect.setUrl("#" + matName + "-effect");
		mat.setInstanceEffect(instanceEffect);
		document.getLibrary(LibraryMaterialsType.class).getMaterials().add(mat);
	}

	private void addPhongType(Folder materialFolder) {
		Phong phong = new Phong();
		processMaterialBlobsForFolder(materialFolder, phong);
		for (Folder child : materialFolder.getChildFolders()) {
			processMaterialBlobsForFolder(child, phong);
		}
		technique.setPhong(phong);
		technique.setSid("common");
	}

	private void processMaterialBlobsForFolder(Folder folder, Phong phong) {
		Map<String, String> blobs = folder.getBlobs();
		Set<Map.Entry<String, String>> entries = blobs.entrySet();
		for(Map.Entry<String, String> entry : entries){
			switch (entry.getKey()) {
				case "material":
					fillPhongProperties(assetManager.getBlobOfAsset(assetName, entry.getValue()), phong);
					break;
				case "diffuse":
				case "specular":
				case "ambient":
				case "emissive":
					addTextureFromBlob(folder, assetManager.getBlobOfAsset(assetName, entry.getValue()), phong);
					break;
				default:
					log.warn("Encountered unknown blob in material " + entry.getKey());
			}
		}
	}

	private void addTextureFromBlob(Folder blobFolder, Blob blob, Phong phong) {
		addImageToLibrary(blobFolder, blob);
		addSamplerToProfile(blob);
		FxCommonColorOrTextureType texType = new FxCommonColorOrTextureType();
		Texture tex = new Texture();
		tex.setTexcoord("TEX0"); //We currently only support one set of texcoords
		tex.setTexture("hash-"+blob.getHash() + "-sampler");
		texType.setTexture(tex);
		switch(blobFolder.getTypeOfBlob(blob.getHash())) {
			case "diffuse":
				phong.setDiffuse(texType);
				break;
			case "ambient":
				phong.setAmbient(texType);
				break;
			case "emissive":
				phong.setEmission(texType);
				break;
			case "specular":
				phong.setSpecular(texType);
				break;
		}
	}

	private void addSamplerToProfile(Blob blob) {
		FxCommonNewparamType samplerParam = new FxCommonNewparamType();
		samplerParam.setSid("hash-"+blob.getHash() + "-sampler");
		FxSampler2DType samplerType = new FxSampler2DType();
		InstanceImageType img = new InstanceImageType();
		img.setUrl("#"+ "hash-" + blob.getHash());
		samplerType.setInstanceImage(img);
		samplerParam.setSampler2D(samplerType);
		profile.getNewparams().add(samplerParam);
	}

	private void addImageToLibrary(Folder blobFolder, Blob blob) {
		ImageType img = new ImageType();
		img.setId("hash-"+blob.getHash());
		img.setName(blobFolder.getAttribute("filename"));
		InitFrom init = new InitFrom();
		init.setRef(blobFolder.getAttribute("filename")); //TODO: Is the subtype always the same as the file ending?
		img.setInitFrom(init);
		document.getLibrary(LibraryImagesType.class).getImages().add(img);
		context.declareExternalReference(blobFolder);
	}

	private void fillPhongProperties(Blob blob, Phong phong) {
		Material mat = Material.fromInputStream(blob.getData());
		phong.setAmbient(convertToColladaColorType(mat.ambient, mat.opacity));
		phong.setEmission(convertToColladaColorType(mat.emissive, mat.opacity));
		phong.setDiffuse(convertToColladaColorType(mat.diffuse, mat.opacity));
		phong.setSpecular(convertToColladaColorType(mat.specular, mat.opacity));
		phong.setShininess(convertToColladaFloatType(mat.shininess));
	}

	private FxCommonFloatOrParamType convertToColladaFloatType(float floatIn) {
		FxCommonFloatOrParamType floatType = new FxCommonFloatOrParamType();
		Float colladaFloat = new Float();
		colladaFloat.setValue((double) floatIn);
		floatType.setFloat(colladaFloat);
		return floatType;
	}

	private FxCommonColorOrTextureType convertToColladaColorType(Color3D colorIn, float opacity) {
		FxCommonColorOrTextureType colorType = new FxCommonColorOrTextureType();
		Color color = new Color();
		List<Double> components = color.getValues();
		components.add(new Double(colorIn.r));
		components.add(new Double(colorIn.g));
		components.add(new Double(colorIn.b));
		components.add(new Double(opacity));
		colorType.setColor(color);
		return colorType;
	}

	private boolean materialAlreadyInLibrary(final String materialName) {
		List<MaterialType> mats = document.getLibrary(LibraryMaterialsType.class).getMaterials();
		MaterialType mat = CollectionUtils.find(mats, new Predicate<MaterialType>() {
			@Override
			public boolean evaluate(MaterialType t) {
				return t.getId().equals(materialName);
			}
		});
		return (mat != null);
	}
}
