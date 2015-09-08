/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.cdi.provider;

import de.dfki.asr.atlas.cdi.annotations.JcromMapped;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import org.jcrom.Jcrom;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JcromMapperProducer {

	private Logger log = LoggerFactory.getLogger(JcromMapperProducer.class);
	private Jcrom jcromMapper;

	@PostConstruct
	public void createJcomMapper() {
		jcromMapper = new Jcrom();
		registerMappedClasses(jcromMapper);
	}

	private void registerMappedClasses(Jcrom mapper) {
		Reflections reflections = new Reflections("de.dfki.asr.atlas");
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(JcromMapped.class);
		for (Class<?> clazz : annotated) {
			log.info("Registered JCROM mapped class: "+clazz.getCanonicalName());
			mapper.map(clazz);
		}
	}

	@Produces
	public Jcrom getJcromMapper() {
		return jcromMapper;
	}
}
