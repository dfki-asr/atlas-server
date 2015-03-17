/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert.collada;

import de.dfki.asr.atlas.business.AssetManager;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Blob;
import de.dfki.asr.atlas.model.Folder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.collada._2008._03.colladaschema.COLLADA;

@Slf4j
public class ColladaZipCompressor implements StreamingOutput {

	protected Asset asset;
	protected AssetManager manager;
	protected SimpleCOLLADADocument collada;
	private final List<String> alreadyZippedTextures = new ArrayList<>();
	private final ExportContext context;

	public ColladaZipCompressor(ExportContext ctx, SimpleCOLLADADocument collada){
		this.asset = ctx.getSourceAsset();
		this.manager = ctx.getAssetManager();
		this.collada = collada;
		this.context = ctx;
	}

	private void addColladaToZip(ZipOutputStream zip) throws IOException, JAXBException{
		ZipEntry colladaEntry = new ZipEntry(asset.getName()+".dae");
		zip.putNextEntry(colladaEntry);
		JAXBContext jaxbContext = JAXBContext.newInstance(COLLADA.class);
		Marshaller m = jaxbContext.createMarshaller();
		m.marshal(collada.getDocument(), zip);
	}

	private void addTexturesToZip(ZipOutputStream zip) throws IOException {
		for (Folder folder : context.getExternalReferences()) {
			switch(folder.getType()) {
				case "diffuse":
				case "specular":
				case "ambient":
				case "emissive":
					String filename = folder.getAttribute("filename");
					String blobHash = folder.getHashOfBlobWithType(folder.getType());
					Blob blob = manager.getBlobOfAsset(asset.getName(), blobHash);
					alreadyZippedTextures.add(filename);
					addTextureToZip(blob, filename, zip);
				default:
					log.warn("Not adding Folder of type {} to zip for asset {}, despite request.", folder.getType(), asset.getName());
			}
		}
	}

	private void addTextureToZip(Blob texBlob, String filename, ZipOutputStream zip) throws IOException {
		ZipEntry texEntry = new ZipEntry(filename);
		zip.putNextEntry(texEntry);
		byte[] bytes = IOUtils.toByteArray(texBlob.getData());
		zip.write(bytes);
	}

	@Override
	public void write(OutputStream output) throws IOException, WebApplicationException {
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			addColladaToZip(zip);
			addTexturesToZip(zip);
		} catch (JAXBException ex) {
			throw new WebApplicationException(ex);
		}
	}
}
