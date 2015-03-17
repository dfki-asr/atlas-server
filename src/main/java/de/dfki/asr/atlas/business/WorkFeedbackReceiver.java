/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.business.scratchSpace.ScratchSpace;
import de.dfki.asr.atlas.model.ImportOperation;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;

@MessageDriven(activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	@ActivationConfigProperty(propertyName = "destination",propertyValue = "atlas.work.feedback")
})
public class WorkFeedbackReceiver implements MessageListener{
	@Inject
	Logger log;

	@Inject
	ImportOperationManager operationManager;

	@Inject
	ScratchSpace scratchSpace;

	@Override
	public void onMessage(Message message){
		try {
			if (message instanceof TextMessage) {
				TextMessage msg = (TextMessage) message;
				logMessageReceived(msg);
				parseTextMessage(msg);
			}
		} catch (JMSException | RepositoryException e) {
			log.error("", e);
		}
	}

	private void logMessageReceived(TextMessage msg) throws JMSException {
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("TextMessage received: ");
		logMessage.append(msg.getText());
		logMessage.append(" ");
		logMessage.append(msg.getStringProperty("status"));
		logMessage.append(" for transaction ");
		logMessage.append(msg.getStringProperty("importOperationId"));
		logMessage.append(" with result path ");
		logMessage.append(msg.getStringProperty("resultPath"));
		log.info(logMessage.toString());
	}

	private void parseTextMessage(TextMessage msg) throws JMSException, RepositoryException {
		String subject = msg.getText();
		if (subject.equals("import")) {
			updateImportOperation(msg);
		}
	}

	private void updateImportOperation(TextMessage message) throws JMSException, RepositoryException {
		String status = message.getStringProperty("status");
		if (status.equals("processing")) {
			setOperationProcessing(message);
		} else if (status.equals("complete")) {
			completeImportOperation(message);
		} else if (status.equals("failed")) {
			setTransactionFailed(message);
		}
	}

	private void completeImportOperation(TextMessage msg) throws JMSException, RepositoryException {
		ImportOperation op = operationManager.findImportOperation(msg.getStringProperty("importOperationId"));
		scratchSpace.removeFile(op.getScratchSpaceFile());
		convertDetailField(msg, op);
		//cannot use the locationbuilder here :(
		String resultPath = "../asset/" + msg.getStringProperty("resultPath");
		resultPath = resultPath.substring(0, resultPath.lastIndexOf('/'));
		op.setFinishedAssetLocation(resultPath);
		op.setStatus(ImportOperation.Status.FINISHED);
		op.setImportedAs(msg.getStringProperty("importedAs"));
		operationManager.save(op);
	}

	private void convertDetailField(TextMessage msg, ImportOperation op) throws JMSException {
		String detail = msg.getStringProperty("detail");
		detail = detail.replace('^', '\n');
		op.setDetail(detail);
	}

	private void setOperationProcessing(TextMessage msg) throws JMSException, RepositoryException {
		ImportOperation op = operationManager.findImportOperation(msg.getStringProperty("importOperationId"));
		op.setStatus(ImportOperation.Status.PROCESSING);
		operationManager.save(op);
	}

	private void setTransactionFailed(TextMessage msg) throws JMSException, RepositoryException {
		ImportOperation op = operationManager.findImportOperation(msg.getStringProperty("importOperationId"));
		scratchSpace.removeFile(op.getScratchSpaceFile());
		convertDetailField(msg, op);
		op.setStatus(ImportOperation.Status.REJECTED);
		operationManager.save(op);
	}
}
