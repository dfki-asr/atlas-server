/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest.exceptionmappers;

import javax.jcr.PathNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;

/**
 * Unknown entities passed to GET will throw a javax.jcr.PathNotFoundException.
 * This exception is thrown when an entity with a given ID could not be found in the JCR repository.
 */
@Provider
public class PathNotFound implements ExceptionMapper<PathNotFoundException> {

	@Override
	public Response toResponse(PathNotFoundException exception) {
		return Response.status(HttpStatus.SC_NOT_FOUND)
				.entity(exception.getMessage())
				.build();
	}
}
