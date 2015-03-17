/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.test;

import de.dfki.asr.atlas.convert.FloatStreamIterator;
import java.io.ByteArrayInputStream;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class FloatStreamIteratorTest {

	public FloatStreamIteratorTest() {
	}

	@org.testng.annotations.BeforeClass
	public static void setUpClass() throws Exception {
	}

	@org.testng.annotations.AfterClass
	public static void tearDownClass() throws Exception {
	}

	@org.testng.annotations.BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@org.testng.annotations.AfterMethod
	public void tearDownMethod() throws Exception {
	}

	@Test
	public void testParseZero() {
		byte[] zero = {0x00, 0x00, 0x00, 0x00};
		ByteArrayInputStream is = new ByteArrayInputStream(zero);
		FloatStreamIterator it = new FloatStreamIterator(is);
		assertTrue(it.hasNext());
		assertEquals(it.next(), 0.0f);
		assertFalse(it.hasNext());
	}

	@Test
	public void testParseOne() {
		byte[] one = {0x00, 0x00, (byte)0x80, (byte)0x3f};
		ByteArrayInputStream is = new ByteArrayInputStream(one);
		FloatStreamIterator it = new FloatStreamIterator(is);
		assertTrue(it.hasNext());
		assertEquals(it.next(), 1.0f);
		assertFalse(it.hasNext());
	}

	void testEmptyInputStream() {
		byte[] none = {};
		ByteArrayInputStream is = new ByteArrayInputStream(none);
		FloatStreamIterator it = new FloatStreamIterator(is);
		assertFalse(it.hasNext());
	}
}
