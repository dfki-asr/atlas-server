/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest.exceptionmappers;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;

/**
 * Unknown entities passed to GET may throw an EntityNotFoundException which should translate into a 404 response.
 */
@Provider
public class EntityNotFound implements ExceptionMapper<EntityNotFoundException> {

	@Override
	public Response toResponse(EntityNotFoundException exception) {
		return Response.status(HttpStatus.SC_NOT_FOUND)
				.entity(exception.getMessage())
				.build();
	}
}
