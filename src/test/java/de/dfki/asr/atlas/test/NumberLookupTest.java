/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.test;

import de.dfki.asr.atlas.business.NumberBasedLookup;
import de.dfki.asr.atlas.model.Folder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import org.testng.annotations.Test;

public class NumberLookupTest {
	private NumberBasedLookup lookup = new NumberBasedLookup();
	private Folder rootFolder, firstChild, secondChild, grandChild;

	@org.testng.annotations.BeforeClass
	public void setUpMethod() throws Exception {
		rootFolder = new Folder();
		rootFolder.setChildren(new ArrayList<Folder>());
		firstChild = new Folder();
		rootFolder.getChildFolders().add(firstChild);
		secondChild = new Folder();
		rootFolder.getChildFolders().add(secondChild);
		grandChild = new Folder();
		// append grandchild to second to test list ordering
		secondChild.setChildren(new ArrayList<Folder>());
		secondChild.getChildFolders().add(grandChild);
	}

	private List<Integer> makeList(Integer... elements) {
		return Arrays.asList(elements);
	}

	@Test
	public void testEmptyPath() {
		Folder folder = lookup.lookup(makeList(), rootFolder);
		assertThat(folder, is(rootFolder));
	}

	@Test
	public void testGetFirstChild() {
		Folder folder = lookup.lookup(makeList(0), rootFolder);
		assertThat(folder, is(firstChild));
	}

	@Test
	public void testGetSecondChild() {
		Folder folder = lookup.lookup(makeList(1), rootFolder);
		assertThat(folder, is(secondChild));
	}

	@Test
	public void testGetGrandChild() {
		// root->2nd element->1st element (but 0-based counting)
		Folder folder = lookup.lookup(makeList(1,0), rootFolder);
		assertThat(folder, is(grandChild));
	}

	@Test(expectedExceptions = {IllegalArgumentException.class})
	public void testNegativePathElement() {
		lookup.lookup(makeList(-1), rootFolder);
	}

	@Test(expectedExceptions = {IllegalArgumentException.class})
	public void testTooBigPathElement() {
		lookup.lookup(makeList(9999), rootFolder);
	}

	@Test
	public void testNullPath() {
		assertThat("null path gives root",lookup.lookup(null, rootFolder), is(rootFolder));
	}

	@Test
	public void testNullFolder() {
		assertThat("null root gives root",lookup.lookup(makeList(), null), is(nullValue()));
	}
}
