/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.model.Folder;
import java.util.List;

public interface HierarchyLookup<T> {
	/**
	 * Find a Folder in a Hierarchy
	 * @param pathEntries the components of the path leading to the target folder
	 * @param startFolder the folder from which to start traversing
	 * @throws IllegalArgumentException if the path cannot be resolved from the startFolder
	 * @return the folder requested
	 */
	Folder lookup(List<T> pathEntries, Folder startFolder) throws IllegalArgumentException;
	/**
	 * Checks if this Lookup can handle the requested type.
	 * @param pathType the class of the path entries
	 * @return pathType.equals(T)
	 */
	boolean canLookup(Class<?> pathType);
}
