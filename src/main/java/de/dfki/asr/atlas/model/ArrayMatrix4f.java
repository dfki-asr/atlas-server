/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Matrix4f;

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

	public List<Float> asList() {
		List<Float> list = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			list.add(getElement(i % 4, i / 4));
		}
		return list;
	}

}
