/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.test;

import de.dfki.asr.atlas.business.FolderHasher;
import de.dfki.asr.atlas.model.Folder;
import org.testng.annotations.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.testng.annotations.BeforeClass;

public class FolderHashTest {
	Folder empty;

	@BeforeClass
	public void setUp() {
		empty = new Folder();
		empty.setType("node");
		empty.setName("Empty Node Folder");
	}

	@Test
	public void emptyFolderShouldHash() {
		FolderHasher hash = new FolderHasher(empty);
		// computed using: `echo "# node" |shasum`
		assertThat(hash.toString(), is("168b7940ff34daeeacfe15e80b915d5970708486"));
	}

	@Test
	public void differentNameDoesntMatter() {
		Folder named = new Folder();
		named.setType("node");
		named.setName("Very Differently Named Folder");
		FolderHasher hash_a = new FolderHasher(named);
		FolderHasher hash_b = new FolderHasher(empty);
		assertThat(hash_a.toString(), is(hash_b.toString()));
	}

	@Test
	public void withBlobs() {
		Folder blobbed = new Folder();
		blobbed.setType("node");
		// fake idendity transform
		blobbed.getBlobs().put("transform", "40126e70d1873c3c3306b219aafe18f9daf7580b");
		FolderHasher hash = new FolderHasher(blobbed);
		// shasum
		// # node
		// transform 40126e70d1873c3c3306b219aafe18f9daf7580b
		// ^D
		assertThat(hash.toString(), is("5a1ded219760b97fd5e5092d4381eb34f42d93ba"));
	}

	@Test
	public void withChild() {
		Folder parent = new Folder();
		parent.setType("node");
		parent.getChildFolders().add(empty);
		FolderHasher hash = new FolderHasher(parent);
		// shasum
		// # node
		// folder 168b7940ff34daeeacfe15e80b915d5970708486
		// ^D
		assertThat(hash.toString(), is("d30f76d0a27bd2f2cb679c0b17454902c129432f"));
	}

	@Test
	public void withAttribute() {
		Folder attributed = new Folder();
		attributed.setType("node");
		attributed.getAttributes().put("herp", "derp");
		FolderHasher hash = new FolderHasher(attributed);
		// shasum
		// # node
		// # herp: derp
		// ^D
		assertThat(hash.toString(), is("c500ad6f305d5c7578fa3e5b9a403cc78d98981c"));
	}

	@Test
	public void allTogetherNow() {
		Folder chockFull = new Folder();
		chockFull.setType("node");
		chockFull.getAttributes().put("herp", "derp");
		chockFull.getChildFolders().add(empty);
		chockFull.getBlobs().put("transform", "40126e70d1873c3c3306b219aafe18f9daf7580b");
		chockFull.getBlobs().put("identity", "40126e70d1873c3c3306b219aafe18f9daf7580b");
		FolderHasher hash = new FolderHasher(chockFull);
		// shasum
		// # node
		// folder 168b7940ff34daeeacfe15e80b915d5970708486
		// identity 40126e70d1873c3c3306b219aafe18f9daf7580b
		// transform 40126e70d1873c3c3306b219aafe18f9daf7580b
		// # herp: derp
		// ^D
		assertThat(hash.toString(), is("12caf0ad39a2e53b0f3f5caa0efd43daf63fe791"));
	}
}
