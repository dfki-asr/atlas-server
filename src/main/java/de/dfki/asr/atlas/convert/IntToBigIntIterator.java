/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import java.math.BigInteger;
import java.util.Iterator;

public class IntToBigIntIterator implements Iterator<BigInteger> {

	Iterator<Integer> it;

	public IntToBigIntIterator(Iterator<Integer> it) {
		this.it = it;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public BigInteger next() {
		return BigInteger.valueOf(it.next());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
