/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Matrix4f;

@JsonIgnoreProperties({"m00","m01","m02","m03","m10","m11","m12","m13","m20","m21","m22","m23","m30","m31","m32","m33"})
public class ArrayMatrix4f extends Matrix4f {
	private static final long serialVersionUID = 5264274544357406927L;

	public ArrayMatrix4f(Iterator<Float> it) {
		super(it.next(), it.next(), it.next(), it.next(),
				it.next(), it.next(), it.next(), it.next(),
				it.next(), it.next(), it.next(), it.next(),
				it.next(), it.next(), it.next(), it.next());
	}

	public ArrayMatrix4f(List<Float> list) {
		this(list.iterator());
	}

	public ArrayMatrix4f(ArrayMatrix4f other) {
		super(other);
	}

	public ArrayMatrix4f() {

	}

	public List<Float> asListColumnMajor() {
		List<Float> list = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			list.add(getElement(i % 4, i / 4));
		}
		return list;
	}

	@JsonValue
	public List<Float> asListRowMajor() {
		List<Float> list = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			list.add(getElement(i / 4, i % 4));
		}
		return list;
	}

}
