/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dfki.asr.atlas.cdi.annotations.JcromMapped;
import java.io.IOException;
import org.jcrom.annotations.JcrProperty;

@JcromMapped
public class Asset extends AtlasEntity {
	private static final long serialVersionUID = 6218247235008402298L;

	@JsonIgnore
	private Folder rootFolder;

	@JcrProperty
	@JsonIgnore
	private String scene;

	private String name;

	public void parseScene() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		rootFolder = mapper.readValue(scene, Folder.class);
	}

	public Folder getRootFolder() {
		return rootFolder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
