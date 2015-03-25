/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FloatStreamIterator extends DataStreamIterator<Float> {
	public FloatStreamIterator(InputStream in) {
		super(in, new DataReadOperation<Float>() {
			@Override
			public Float read(DataInputStream s) throws IOException {
				int fourBytes = s.readInt();
				fourBytes = Integer.reverseBytes(fourBytes);
				return Float.intBitsToFloat(fourBytes);
			}
		});
	}
}
