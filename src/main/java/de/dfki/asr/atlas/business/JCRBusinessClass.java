/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jcrom.Jcrom;
import org.slf4j.Logger;


public class JCRBusinessClass implements Serializable {
	private static final long serialVersionUID = 6286264387398795749L;

	@Inject
	protected Session jcrSession;

	@Inject
	protected Jcrom jcromMapper;

	@Inject
	Logger log;

	@PostConstruct
	public void atPostConstruct() throws RepositoryException {
		ensureImportOperationPathStructure();
		ensureAssetsNodeExists();
	}

	public void ensureImportOperationPathStructure() throws RepositoryException {
		Node root = jcrSession.getRootNode();
		if (!root.hasNode("importoperation")) {
			root.addNode("importoperation");
		}
		jcrSession.save();
	}

	/**
	 * We have to ensure that the "assets"-folder exists. However this is the only point to this so far, due to the following reasons: 1)
	 * One can not do this in a startup singleton, since the jcrSession is requestScoped and there is no request at startup. 2)
	 * Communication between worker and JCR uses only the JCR build in REST interface. There is yet no other class, which deals with the
	 * assets folder.
	 *
	 * @throws RepositoryException
	 */
	public void ensureAssetsNodeExists() throws RepositoryException {
		final String relativePathToAssets = "assets";
		Node root = jcrSession.getRootNode();
		if (!root.hasNode(relativePathToAssets)) {
			root.addNode(relativePathToAssets);
		}
		jcrSession.save();
	}
}
