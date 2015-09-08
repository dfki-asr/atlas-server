/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.xml3d;

import de.dfki.asr.atlas.business.AssetManager;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Blob;
import de.dfki.asr.atlas.model.Color3D;
import de.dfki.asr.atlas.model.Folder;
import de.dfki.asr.atlas.model.Material;
import de.dfki.asr.xml3d.jaxb.FloatList;
import de.dfki.asr.xml3d.jaxb.FloatValue;
import de.dfki.asr.xml3d.jaxb.Image;
import de.dfki.asr.xml3d.jaxb.Shader;
import de.dfki.asr.xml3d.jaxb.Texture;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XML3DShaderExporter {

	private final Logger log = LoggerFactory.getLogger(XML3DShaderExporter.class);
	protected Asset asset;
	protected AssetManager manager;
	protected Shader shader;
	protected ExportContext context;

	public XML3DShaderExporter(ExportContext context) {
		this.asset = context.getSourceAsset();
		this.manager = context.getAssetManager();
		this.context = context;
	}

	public Shader exportShader(Folder materialFolder){
		shader = new Shader();
		processMaterialBlobsForFolder(materialFolder);
		for (Folder child : materialFolder.getChildFolders()) {
			processTextureBlobsForFolder(child);
		}
		setShaderType(materialFolder);
		return shader;
	}

	private void processMaterialBlobsForFolder(Folder folder) {
		String hash = folder.getHashOfBlobWithType("material");
		fillPhongProperties(manager.getBlobOfAsset(asset.getName(), hash));
	}

	private void processTextureBlobsForFolder(Folder folder) {
		Map<String, String> blobs = folder.getBlobs();
		Set<Map.Entry<String, String>> entries = blobs.entrySet();
		for(Map.Entry<String, String> entry : entries){
			switch (entry.getKey()) {
				case "diffuse":
				case "specular":
				case "ambient":
				case "emissive":
					addTextureToFolder(entry.getKey(), folder);
					break;
				case "displacement":
				case "height":
				case "lightmap":
				case "normals":
				case "opacity":
				case "reflection":
				case "shininess":
					log.warn("Texture type '" + entry.getKey() + "' known, but unsupported in XML3D. Ignoring.");
					break;
				default:
					log.error("Encountered unknown blob in texture folder:" + entry.getKey() + " You changed the import, didn't you?");
			}
		}
	}

	private void fillPhongProperties(Blob blob) {
		Material mat = Material.fromInputStream(blob.getData());
		shader.setAmbientIntensity(new FloatValue("ambientIntensity", mat.ambient.getLuminosity()));
		shader.setShininess(new FloatValue("shininess", mat.shininess));
		shader.setEmissiveColor(convertToXML3DColor("emissiveColor", mat.emissive));
		shader.setDiffuseColor(convertToXML3DColor("diffuseColor", mat.diffuse));
		shader.setSpecularColor(convertToXML3DColor("specularColor", mat.specular));
		shader.setTransparency(new FloatValue("transparency", 1.0f - mat.opacity));
	}

	private void setShaderType(Folder materialFolder){
		String shadingModel = materialFolder.getAttribute("shadingModel");
		switch(shadingModel){
			case "phong":
				shader.setScript(Shader.PHONG);
				break;
			case "flat":
				shader.setScript(Shader.FLAT);
				break;
			case "blinn":
			case "cookTorrance":
			case "fresnel":
			case "gouraud":
			case "minnaert":
			case "noShading":
			case "orenNayar":
			case "toon":
				log.warn("Shader model '"+shadingModel+"' known, but not supported in XML3D. Falling back to Phong.");
				shader.setScript(Shader.PHONG);
				break;
			default:
				log.error("Encountered unknown shader type:" + shadingModel + ". You changed the import pipeline, didn't you?");
		}
	}

	private FloatList convertToXML3DColor(String name, Color3D color){
		FloatList list = new FloatList(name);
		list.add(color.r);
		list.add(color.g);
		list.add(color.b);
		return list;
	}

	private void addTextureToFolder(String textureName, Folder folder) {
		Image img = createImageFromFolder(folder);
		Texture texture = new Texture();
		texture.setName(textureName + "Texture");
		texture.setImage(img);
		texture.setWrapS("repeat");
		texture.setWrapT("repeat");
		shader.getTextures().add(texture);
	}

	private Image createImageFromFolder(Folder folder) {
		Image img = new Image();
		URI imageUri = context.declareExternalReference(folder);
		img.setSrc(imageUri.toString());
		return img;
	}
}
