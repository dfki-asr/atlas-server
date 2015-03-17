/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.test;

import de.dfki.asr.atlas.model.Folder;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FolderAddressTest {
	private Folder rootFolder, firstChild, secondChild, grandChild;

	@BeforeClass
	public void setupFolderHierarchy() {
		rootFolder = new Folder();
		rootFolder.setChildren(new ArrayList<Folder>());
		firstChild = new Folder();
		rootFolder.getChildFolders().add(firstChild);
		firstChild.setParent(rootFolder);
		secondChild = new Folder();
		rootFolder.getChildFolders().add(secondChild);
		secondChild.setParent(rootFolder);
		grandChild = new Folder();
		// append grandchild to second to test list ordering
		secondChild.setChildren(new ArrayList<Folder>());
		secondChild.getChildFolders().add(grandChild);
		grandChild.setParent(secondChild);
	}

	@Test
	public void addressOfRootIsEmpty() {
		List<Integer> address = rootFolder.getAddress();
		assertThat(address.size(), is(0));
	}

	@Test
	public void addressOfGrandChildHasLengthTwo() {
		List<Integer> address = grandChild.getAddress();
		assertThat(address.size(), is(2));
	}

	@Test
	public void addressOfGrandChildMatchesPreOrder() {
		List<Integer> address = grandChild.getAddress();
		assertThat(address.get(0), is(1));
		assertThat(address.get(1), is(0));
	}
}
