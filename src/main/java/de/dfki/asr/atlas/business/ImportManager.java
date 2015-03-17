/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.business.scratchSpace.ScratchSpace;
import de.dfki.asr.atlas.model.ImportOperation;
import java.io.InputStream;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jms.JMSException;

@Named
@RequestScoped
public class ImportManager implements Serializable {
	private static final long serialVersionUID = -4132580689692641577L;

	@Inject
	ScratchSpace scratchSpace;

	@Inject
	WorkQueue queue;

	@Inject
	ImportOperationManager importOperationManager;

	public ImportOperation createImportOperation(String assetName) throws RepositoryException {
		return importOperationManager.createNewImportOperation(assetName);
	}

	/**
	 * Saves a source file for an import operation to the JCR repository
	 *
	 * @param op The ImportOperation that this source file is associated with
	 * @param inputStream File as a InputStream
	 * @throws RepositoryException
	 * @throws javax.jms.JMSException
	 */
	public void saveSourceFileToScratchSpace(ImportOperation op, InputStream inputStream) throws RepositoryException, JMSException {
		String jcrNodeName = scratchSpace.addAssetFileToScratch(inputStream, op.getAssetName());
		op.setScratchSpaceFile(jcrNodeName);
	}

	public void enqueueWork(ImportOperation op) throws RepositoryException, JMSException {
		importOperationManager.save(op);
		queue.enqueueWork(op);
	}

}
