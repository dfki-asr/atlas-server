/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class MessageBuilder {
	Session session;
	MessageProducer channel;
	Message message;

	public MessageBuilder(Session jmsSession, MessageProducer producer) {
		session = jmsSession;
		message = null;
		channel = producer;
	}

	public MessageBuilder text(String content) throws JMSException {
		TextMessage msg = session.createTextMessage(content);
		message = msg;
		return this;
	}

	public MessageBuilder binary(byte[] data) throws JMSException {
		BytesMessage msg = session.createBytesMessage();
		msg.writeBytes(data);
		message = msg;
		return this;
	}

	public MessageBuilder property(String key, String value) throws JMSException {
		message.setStringProperty(key, value);
		return this;
	}

	public MessageBuilder property(String key, boolean value) throws JMSException {
		message.setBooleanProperty(key, value);
		return this;
	}

	public MessageBuilder send() throws JMSException {
		if (message == null) return this;
		channel.send(message);
		message = null;
		return this;
	}
}
