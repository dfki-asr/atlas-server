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

public class IntStreamIterator extends DataStreamIterator<Integer> {
	public IntStreamIterator(InputStream in) {
		super(in, new DataReadOperation<Integer>() {
			@Override
			public Integer read(DataInputStream s) throws IOException {
				return Integer.reverseBytes(s.readInt());
			}
		});
	}
}
