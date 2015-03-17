/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.model.ImportOperation;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

@Named
@RequestScoped
public class ImportOperationManager extends JCRBusinessClass {
	private static final long serialVersionUID = 3664684767618391453L;

	public ImportOperation findImportOperation(String id) throws PathNotFoundException, RepositoryException {
		Node importNode = jcrSession.getNode("/importoperation/"+id);
		ImportOperation op = jcromMapper.fromNode(ImportOperation.class, importNode);
		return op;
	}

	public ImportOperation createNewImportOperation(String assetName) throws RepositoryException {
		ImportOperation op = new ImportOperation(assetName);
		op.setId(UUID.randomUUID().toString());
		addImportOperationToSession(op);
		return op;
	}

	public void save(ImportOperation op) throws RepositoryException {
		Node operationNode = jcrSession.getNode("/importoperation/" + op.getId());
		jcromMapper.updateNode(operationNode, op);
		jcrSession.save();
	}

	public void removeImportOperation(ImportOperation op) throws RepositoryException {
		jcrSession.removeItem("/importoperation/"+op.getId());
		jcrSession.save();
	}

	private void addImportOperationToSession(ImportOperation op) throws RepositoryException {
		Node importOpNode = jcrSession.getNode("/importoperation");
		jcromMapper.addNode(importOpNode, op);
		jcrSession.save();
	}
}
