/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business.scratchSpace;

import java.io.InputStream;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Named
@RequestScoped
public class ScratchSpace {

	@Inject
	Session jcrSession;

	private final String relativePathToScratch = "scratch";

	public String addAssetFileToScratch(InputStream in, String assetName) throws RepositoryException {
		Node scratchSpace = getScratchRootNode();
		String nodeName = UUID.randomUUID().toString();
		Node newFileNode = scratchSpace.addNode(nodeName, "nt:file");
		Node contentNode = newFileNode.addNode("jcr:content", "nt:resource");
		Binary binary = jcrSession.getValueFactory().createBinary(in);
		contentNode.setProperty("jcr:data", binary);
		jcrSession.save();
		return nodeName;
	}

	public void removeFile(String fileNode) throws RepositoryException {
		jcrSession.removeItem("/" + relativePathToScratch + "/" + fileNode);
	}

	private Node getScratchRootNode() throws RepositoryException {
		Node root = jcrSession.getRootNode();
		ensureNodeHasNodeChildren(root, relativePathToScratch);
		return root.getNode(relativePathToScratch);
	}

	private void ensureNodeHasNodeChildren(Node root, String path) throws RepositoryException {
		if (!root.hasNode(path)) {
			root.addNode(path);
		}
	}
}
