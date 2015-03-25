/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest.exceptionmappers;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;

/**
 * A RepositoryException is a generic exception thrown by JCR when something went wrong. In this case we should return a
 * simple server error (500) and log the exception.
 */
@Provider
public class Repository implements ExceptionMapper<RepositoryException> {

	@Inject
	Logger logger;

	@Override
	public Response toResponse(RepositoryException exception) {
		logger.error("REST interface encountered an exception", exception);
		return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.build();
	}
}
