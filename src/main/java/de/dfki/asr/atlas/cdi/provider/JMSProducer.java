/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.cdi.provider;

import de.dfki.asr.atlas.business.MessageBuilder;
import de.dfki.asr.atlas.cdi.qualifier.Queue;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.slf4j.Logger;

/**
 * Manages the necessary entities for setting up a JMS session. It will produce the message
 * producers for certain types that can be injected using the {@link CompassJMSProducer} qualifier.
 */
@javax.inject.Singleton
public class JMSProducer {

	@Inject
	protected Logger log;

	@Resource(lookup = "java:/ConnectionFactory")
	protected static ConnectionFactory connectionFactory;

	private Connection jmsConnection;
	private Session jmsSession;
	private List<MessageProducer> producers;

	@PostConstruct
	public void onPostConstruct() {
		createJMSConnection();
		producers = new ArrayList<>();
	}

	private void createJMSConnection() {
		try {
			jmsConnection = connectionFactory.createConnection();
			// session: 1st argument: no transaction
			jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			jmsConnection.start();
		} catch (JMSException e) {
			log.error("Cannot setup JMS connection: " + e.getMessage());
		}
	}

	@PreDestroy
	public void onPreDestroy() {
		closeJMSConnection();
	}

	@Produces
	public Session getJMSSession() {
		return jmsSession;
	}

	@Produces
	@Queue("") // also works for nonempty values, beacuse value is @Nonbinding
	public MessageProducer getQueueProducer(InjectionPoint injection) {
		String requestedQueue = injection.getAnnotated().getAnnotation(Queue.class).value();
		try {
			javax.jms.Queue theQueue = jmsSession.createQueue(requestedQueue);
			MessageProducer producer = jmsSession.createProducer(theQueue);
			producers.add(producer);
			log.info("Created MessageProducer for Topic: "+requestedQueue);
			return producer;
		} catch (javax.jms.IllegalStateException state) {
			closeJMSConnection();
			createJMSConnection();
			return getQueueProducer(injection);
		} catch (JMSException jMSException) {
			log.error("Cannot create Topic '"+requestedQueue+"'", jMSException);
			return null;
		}
	}

	@Produces
	@Queue("") // also works for nonempty values, beacuse value is @Nonbinding
	public MessageBuilder getMessageBuilderForQueue(InjectionPoint injection) {
		MessageProducer producer = getQueueProducer(injection);
		return new MessageBuilder(jmsSession, producer);
	}

	public void closeTopicProducer(@Disposes @Queue("") MessageProducer producer) {
		try {
			producer.close();
		} catch (JMSException ex) {
			log.error("JMS Exception trying to dispose of topic",ex);
		}
	}

	private void closeJMSConnection() {
		try {
			for (MessageProducer producer : producers) {
				closeTopicProducer(producer);
			}
			jmsSession.close();
			jmsConnection.close();
		} catch (JMSException ex) {
			log.error("Error closing JMS", ex);
		}
	}
}
