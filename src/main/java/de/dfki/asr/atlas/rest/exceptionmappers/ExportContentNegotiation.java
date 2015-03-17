/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest.exceptionmappers;

import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.rest.ExportContentNegotiationException;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Unknown entities passed to GET may throw an EntityNotFoundException which should translate into a 404 response.
 */
@Provider
public class ExportContentNegotiation implements ExceptionMapper<ExportContentNegotiationException> {

	@Override
	public Response toResponse(ExportContentNegotiationException exception) {
		return Response.notAcceptable(exception.getAcceptableVariants())
					.header("Content-Type", "application/json")
					.entity(buildAcceptableVariantsEntity(exception.getContext(), exception.getAcceptableVariants()))
					.build();
	}

	private String buildAcceptableVariantsEntity(ExportContext context, List<Variant> variants) {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		builder.append("\t\"url\": \"");
		builder.append(context.uriFor(context.getStartingFolder()).toString());
		builder.append(context.getRequestedFileExtension());
		builder.append("\",\n");
		builder.append("\t\"availableMimeTypes\": [");
		boolean first = true;
		for (Variant variant : variants) {
			if (first) { first = false; }
			else { builder.append(", "); }
			builder.append("\"");
			builder.append(variant.getMediaType().toString());
			builder.append("\"");
		}
		builder.append("]\n");
		builder.append("}\n");
		return builder.toString();
	}
}
