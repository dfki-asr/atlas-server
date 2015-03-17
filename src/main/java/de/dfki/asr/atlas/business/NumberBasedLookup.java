/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.model.Folder;
import java.util.LinkedList;
import java.util.List;

public class NumberBasedLookup implements HierarchyLookup<Integer> {

	@Override
	public Folder lookup(List<Integer> pathEntries, Folder startFolder) throws IllegalArgumentException {
		if (pathEntries == null || pathEntries.isEmpty()) return startFolder;
		LinkedList<Integer> remainingEntries = new LinkedList<>(pathEntries);
		Integer nextTraversalStep = remainingEntries.poll();
		try {
			Folder newStartFolder = startFolder.getChildFolders().get(nextTraversalStep);
			return lookup(remainingEntries, newStartFolder);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Child "+nextTraversalStep+" not found in "+startFolder, e);
		}
	}

	@Override
	public boolean canLookup(Class<?> pathType) {
		return Integer.class.isAssignableFrom(pathType);
	}
}
