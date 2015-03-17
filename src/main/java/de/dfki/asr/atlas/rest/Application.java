/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application Entry point. JAX-RS Resources will be available under /[webapp_contex]/[@ApplicationPath]/[@Path].
 */
@ApplicationPath("/rest")
public class Application extends javax.ws.rs.core.Application {

}
