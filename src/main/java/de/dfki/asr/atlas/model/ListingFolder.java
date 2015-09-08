/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListingFolder implements Serializable {
	private static final long serialVersionUID = -2023298770715831080L;

	@JsonProperty
	protected String name;

	@JsonProperty
	protected String url;

	@JsonProperty
	protected List<ListingFolder> children;

	public ListingFolder() {
		children = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<ListingFolder> getChildren() {
		return children;
	}

	public void setChildren(List<ListingFolder> children) {
		this.children = children;
	}


}
