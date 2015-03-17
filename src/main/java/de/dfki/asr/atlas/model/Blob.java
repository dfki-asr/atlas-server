/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import java.io.InputStream;

public class Blob {

	private String hash;

	private InputStream data;

	public Blob() {
	}

	public Blob(String hash, InputStream data) {
		this.hash = hash;
		this.data = data;
	}

	public String getHash() {
		return hash;
	}

	public InputStream getData() {
		return data;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setData(InputStream data) {
		this.data = data;
	}


}
