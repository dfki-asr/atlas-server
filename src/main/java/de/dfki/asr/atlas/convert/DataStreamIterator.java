/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

// Reads C-Types from an InputStream using DataInputStream
// This generic type is used to keep the actual readers (descendants of this class) small.
// Since DataInputStream's API is a bit silly (different functions for different types),
// we're wrapping this using a DataReadOperation in the child classes.
//
// We need to cache one value to implement hasNext, since DataInputStream doesn't have a
// way of checking for EOF except for exceptions.
public class DataStreamIterator<T> implements Iterator<T> {

	public interface DataReadOperation<T> {

		T read(DataInputStream s) throws IOException;
	}

	DataInputStream stream;
	DataReadOperation<T> reader;
	T nextValue;
	boolean endReached;

	public DataStreamIterator(InputStream is, DataReadOperation<T> readOp) {
		stream = new DataInputStream(is);
		reader = readOp;
		try {
			nextValue = reader.read(stream);
		} catch (IOException e) {
			endReached = true;
			nextValue = null;
		}
	}

	@Override
	public boolean hasNext() {
		return !endReached;
	}

	@Override
	public T next() {
		T currentValue = nextValue;
		try {
			nextValue = reader.read(stream);
		} catch (IOException e) {
			endReached = true;
			nextValue = null;
		}
		return currentValue;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
