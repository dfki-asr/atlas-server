/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.cdi.provider;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.slf4j.Logger;

@javax.inject.Singleton
public class JCRSession {
	@Inject
	private Logger log;

	@Resource(lookup = "jcr/atlas")
	private Repository repository;

	@Produces
	@RequestScoped
	public Session getSession() {
		try {
			return repository.login();
		} catch (RepositoryException ex) {
			log.error("Failed to log in to JCR", ex);
			return null;
		}
	}
}
