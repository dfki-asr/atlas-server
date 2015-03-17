/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest;

import de.dfki.asr.atlas.convert.ExportContext;
import java.util.List;
import javax.ws.rs.core.Variant;

public class ExportContentNegotiationException extends Throwable {
	protected ExportContext context;
	protected List<Variant> acceptableVariants;

	public ExportContentNegotiationException(ExportContext context, List<Variant> acceptableVariants) {
		this.context = context;
		this.acceptableVariants = acceptableVariants;
	}

	public ExportContext getContext() {
		return context;
	}

	public List<Variant> getAcceptableVariants() {
		return acceptableVariants;
	}
}
