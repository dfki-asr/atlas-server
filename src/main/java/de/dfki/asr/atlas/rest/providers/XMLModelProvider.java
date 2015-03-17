/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest.providers;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBXmlRootElementProvider;

@Provider
@Produces("model/*+xml")
public class XMLModelProvider extends JAXBXmlRootElementProvider {
	// This class is deliberately empty.
	// It's adding support for the mimetype listed in @Produces above.
	// Unfortunately, it relies on implementation-specific providers.
}
