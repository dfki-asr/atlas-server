/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

public class Color3D {
	public float b;
	public float g;
	public float r;

	public Color3D(float r, float g, float b) {
		this.b = b;
		this.g = g;
		this.r = r;
	}

	public float getLuminosity() {
		return (float) (0.2126 * r + 0.7152 * g + 0.0722 * b);
	}

}
