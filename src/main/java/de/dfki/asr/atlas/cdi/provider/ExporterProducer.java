/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.cdi.provider;

import de.dfki.asr.atlas.cdi.annotations.AtlasExporter;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.convert.ExportOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@javax.inject.Singleton
public class ExporterProducer {

	private Logger log = LoggerFactory.getLogger(ExporterProducer.class);

	private Map<Variant, Class<? extends ExportOperation>> exportersByVariant = new HashMap<>();
	private Map<String, Set<Variant>> variantsByFileExtension = new HashMap<>();
	private Map<String, Set<Variant>> variantsByNodeType = new HashMap<>();

	@PostConstruct
	private void registerExporterClasses() {
		Reflections reflections = new Reflections("de.dfki.asr.atlas");
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(AtlasExporter.class);
		for (Class<?> clazz : annotated) {
			if (ExportOperation.class.isAssignableFrom(clazz)) {
				registerExporterClass(clazz);
			}
		}
	}

	private void registerExporterClass(Class<?> clazz) {
		log.info("Registering Exporter class: " + clazz.getCanonicalName());
		AtlasExporter exporterInfo = clazz.getAnnotation(AtlasExporter.class);
		Variant variant = makeVariant(exporterInfo.contentType());
		addVariantForFolderTypes(variant, exporterInfo.folderTypes());
		addVariantForExtension(variant, exporterInfo.fileExtension().toLowerCase());
		exportersByVariant.put(variant, clazz.asSubclass(ExportOperation.class));
	}

	public ExportOperation<? extends Object> getExporterForVariant(Variant selectedVariant) {
		if(selectedVariant == null){
			return null;
		}
		Class<? extends ExportOperation> expoClass = exportersByVariant.get(selectedVariant);
		if (expoClass == null) {
			return null;
		}
		try {
			return expoClass.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			log.error("Cannot instantiate exporter", ex);
			return null;
		}
	}

	public Set<String> getAvailableExporterTypes() {
		return variantsByFileExtension.keySet();
	}

	public List<Variant> getVariants(ExportContext context) {
		Set<Variant> availableVariants = new HashSet<>();
		addVariantsByNodeType(context, availableVariants);
		pruneVariantsByFileName(context, availableVariants);
		pruneVariantsByAskingExporters(context, availableVariants);
		return new ArrayList<>(availableVariants);
	}

	private void pruneVariantsByFileName(ExportContext context, Set<Variant> availableVariants) {
		String ext = context.getRequestedFileExtension().toLowerCase();
		if (ext != null && variantsByFileExtension.containsKey(ext)) {
			Set<Variant> fileNameVariants = variantsByFileExtension.get(ext);
			availableVariants.retainAll(fileNameVariants);
		}
	}

	private void addVariantsByNodeType(ExportContext context, Set<Variant> availableVariants) {
		Set<Variant> forNode = variantsByNodeType.get(context.getStartingFolder().getType());
		if (forNode != null) {
			availableVariants.addAll(forNode);
		}
	}

	private Variant makeVariant(String contentType) {
		MediaType type = MediaType.valueOf(contentType);
		return new Variant(type, null, null);
	}

	private void addVariantForFolderTypes(Variant variant, String[] folderTypes) {
		for (String type : folderTypes) {
			ensureSet(variantsByNodeType, type);
			variantsByNodeType.get(type).add(variant);
		}
	}

	private void addVariantForExtension(Variant variant, String fileExtension) {
		ensureSet(variantsByFileExtension, fileExtension);
		variantsByFileExtension.get(fileExtension).add(variant);
	}

	private void ensureSet(Map<String, Set<Variant>> map, String type) {
		if (!map.containsKey(type)) {
			map.put(type, new HashSet<Variant>());
		}
	}

	private void pruneVariantsByAskingExporters(ExportContext context, Set<Variant> availableVariants) {
		Set<Variant> confirmedOK = new HashSet<>();
		for (Variant variant : availableVariants) {
			ExportOperation<?> exporter = getExporterForVariant(variant);
			if (exporter.canExportFolder(context.getStartingFolder())) {
				confirmedOK.add(variant);
			}
		}
		availableVariants.retainAll(confirmedOK);
	}

}
