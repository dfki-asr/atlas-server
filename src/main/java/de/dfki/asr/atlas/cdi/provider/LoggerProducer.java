/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.cdi.provider;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerProducer {

	@Produces
	public Logger getLogger(InjectionPoint injectionPoint) {
		return getLogger(injectionPoint.getMember().getDeclaringClass());
	}

	static public Logger getLogger(Class<?> targetClass) {
		return LoggerFactory.getLogger(targetClass);
	}
}
