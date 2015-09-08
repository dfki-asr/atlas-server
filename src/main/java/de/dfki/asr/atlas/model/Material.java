/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import de.dfki.asr.atlas.convert.FloatStreamIterator;
import java.io.InputStream;

public class Material {


	public Color3D ambient;
	public Color3D diffuse;
	public Color3D emissive;
	public Color3D specular;
	public float opacity;
	public float shininess;

	public static Material fromInputStream(InputStream data) {
		Material mat = new Material();
		FloatStreamIterator r = new FloatStreamIterator(data);
		mat.ambient = new Color3D(r.next(), r.next(), r.next());
		mat.diffuse = new Color3D(r.next(), r.next(), r.next());
		mat.emissive = new Color3D(r.next(), r.next(), r.next());
		mat.specular = new Color3D(r.next(), r.next(), r.next());
		mat.opacity = r.next();
		mat.shininess = r.next();
		return mat;
	}

}
