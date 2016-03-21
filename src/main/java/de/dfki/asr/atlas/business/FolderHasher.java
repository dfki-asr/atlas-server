/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.model.Folder;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Hash a Folder.
 *
 * An ATLAS folder hash is defined as follows:
 *
 * collect all child folder hashes.
 * create a line "# [foldertype]\n"
 * numerically sort the union of child folder and blob hashes.
 * if two items have the same hash, sort by type alphabetically ascending.
 * create lines from the sorted hashes: "[type] [hash]\n"
 * for folders, the type is "folder", otherwise use the blob type
 * sort the folder's attributes by their name
 * create lines from the sorted attributes: "# [name]: [value]"
 * concatenate all lines and compute a sha1 hash over the text
 *
 * Note that, BY DESIGN, this does not include the Folder's name.
 * Names should be purely descriptive and not correlate with content.
 * A folder hash is a content-only hash.
 */
public class FolderHasher {
	@lombok.Data
	@lombok.AllArgsConstructor
	private static class HashAndType {
		public String hash;
		public String type;
		public static class Compare implements Comparator<HashAndType> {
			@Override
			public int compare(HashAndType a, HashAndType b) {
				BigInteger int_a = new BigInteger(a.hash, 16);
				BigInteger int_b = new BigInteger(b.hash, 16);
				int int_result = int_a.compareTo(int_b);
				if (int_result == 0) {
					return a.type.compareTo(b.type);
				}
				return int_result;
			}
		}
	}

	Folder root;
	MessageDigest shaDigest;
	byte[] digest;
	List<HashAndType> items;

	public FolderHasher(Folder root) {
		this.items = new LinkedList<>();
		this.root = root;
		try {
			shaDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException ex) {
			// SHA-256 is always available
			// https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest
			throw new RuntimeException(ex);
		}
	}

	private void update() {
		shaDigest.reset();
		collectContents();
		shaDigest.update(typeLine().getBytes());
		shaDigest.update(contentLines().getBytes());
		shaDigest.update(attributeLines().getBytes());
		digest = shaDigest.digest();
	}

	@Override
	public String toString() {
		if (digest == null) {
			update();
		}
		return String.format("%040x", new java.math.BigInteger(1, digest));
	}

	private String typeLine() {
		return "# " + root.getType() + "\n";
	}

	private String contentLines() {
		StringBuilder contentLines = new StringBuilder();
		Collections.sort(items, new HashAndType.Compare());
		for (HashAndType item: items) {
			contentLines.append(item.type);
			contentLines.append(" ");
			contentLines.append(item.hash);
			contentLines.append("\n");
		}
		return contentLines.toString();
	}

	private void collectContents() {
		for (Map.Entry<String, String> typeAndHash : root.getBlobs().entrySet()) {
			items.add(new HashAndType(typeAndHash.getValue(), typeAndHash.getKey()));
		}
		for (Folder child : root.getChildFolders()) {
			String hash = new FolderHasher(child).toString();
			items.add(new HashAndType(hash, "folder"));
		}
	}

	private String attributeLines() {
		SortedMap<String, String> sortedAttrs = new TreeMap<>();
		sortedAttrs.putAll(root.getAttributes());
		StringBuilder attributeLines = new StringBuilder();
		for (Map.Entry<String, String> attribute : sortedAttrs.entrySet()) {
			attributeLines.append("# ");
			attributeLines.append(attribute.getKey());
			attributeLines.append(": ");
			attributeLines.append(attribute.getValue());
			attributeLines.append("\n");
		}
		return attributeLines.toString();
	}
}
