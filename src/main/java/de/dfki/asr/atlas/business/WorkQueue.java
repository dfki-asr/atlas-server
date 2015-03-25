/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.cdi.qualifier.Queue;
import de.dfki.asr.atlas.model.ImportOperation;
import javax.inject.Inject;
import javax.jms.JMSException;

public class WorkQueue {
	@Inject
	@Queue("atlas.work.import")
	MessageBuilder jms;

	public void enqueueWork(ImportOperation op) throws JMSException {
		jms.text("import")
		   .property("importOperationId", op.getId())
		   .property("filePath", op.getScratchSpaceFile())
		   .property("fileType", op.getFileType())
		   .property("assetName", op.getAssetName())
		   .send();
	}
}
