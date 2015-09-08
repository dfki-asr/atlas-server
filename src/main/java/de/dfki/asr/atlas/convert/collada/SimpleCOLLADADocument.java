/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.collada;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.extern.slf4j.Slf4j;
import org.collada._2008._03.colladaschema.*;
import org.collada._2008._03.colladaschema.AssetType.Unit;

@Slf4j
public class SimpleCOLLADADocument {
	COLLADA document;
	Map<Class<?>, Object> libraries;
	VisualSceneType visualScene = null;

	public SimpleCOLLADADocument() {
		document = new COLLADA();
		document.setVersion("1.5.0");
		addAssetMetadataToDocument(document);
		libraries = new HashMap<>();
	}

	// Ensures only one of each library types exists.
	// makes this a "Typesafe Heterogeneous Container".
	public<LibType> LibType getLibrary(Class<LibType> clazz) {
		if (libraries.containsKey(clazz)) {
			return clazz.cast(libraries.get(clazz));
		}
		try {
			LibType lib = clazz.newInstance();
			libraries.put(clazz, lib);
			document.getLibraryAnimationsAndLibraryAnimationClipsAndLibraryCameras().add(lib);
			return lib;
		} catch (InstantiationException | IllegalAccessException ex) {
			log.error("Cannot instantiate COLLADA Library type", ex);
			return null;
		}
	}

	// in the special case of the visual scene, there should only be one.
	public VisualSceneType getVisualScene() {
		if (visualScene == null) {
			visualScene = new VisualSceneType();
			visualScene.setId("Scene");
			visualScene.setName("Scene");
			LibraryVisualScenesType lvs = getLibrary(LibraryVisualScenesType.class);
			lvs.getVisualScenes().add(visualScene);
			getInstanceVisualScene().setUrl('#'+visualScene.getId());
		}
		return visualScene;
	}

	public InstanceWithExtraType getInstanceVisualScene(){
		InstanceWithExtraType visScene = getScene().getInstanceVisualScene();
		if(visScene == null){
			visScene = new InstanceWithExtraType();
			getScene().setInstanceVisualScene(visScene);
		}
		return visScene;
	}

	public COLLADA.Scene getScene(){
		COLLADA.Scene scene = document.getScene();
		if(scene == null){
			scene = new COLLADA.Scene();
			document.setScene(scene);
		}
		return scene;
	}

	public COLLADA getDocument() {
		return document;
	}

	private void addAssetMetadataToDocument(COLLADA document) {
		AssetType assetType = new AssetType();
		Unit units = new Unit();
		units.setMeter(1.0);
		units.setName("meter");
		assetType.setUnit(units);
		assetType.setUpAxis(UpAxisEnum.Y_UP);
		AssetType.Contributor atlas = new AssetType.Contributor();
		atlas.setAuthoringTool("ATLAS");
		atlas.setComments("Advanced Three-dimensional Large-scale Asset Server");
		assetType.getContributors().add(atlas);
		try {
			XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
			assetType.setCreated(now);
			assetType.setModified(now);
		} catch (DatatypeConfigurationException ex) {
			log.warn("Not adding creation date to COLLADA",ex);
		}
		assetType.setKeywords("");
		document.setAsset(assetType);
	}
}
