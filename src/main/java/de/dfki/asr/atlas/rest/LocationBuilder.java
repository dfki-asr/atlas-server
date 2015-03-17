/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest;

import de.dfki.asr.atlas.model.AtlasEntity;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class LocationBuilder {
	private UriBuilder builder;

	public static LocationBuilder locationOf(UriInfo info) {
		return new LocationBuilder(info.getAbsolutePathBuilder());
	}

	public static LocationBuilder locationOf(UriBuilder builder) {
		return new LocationBuilder(builder);
	}

	public static LocationBuilder locationOf(Class<?> resourceClass) {
		return new LocationBuilder(UriBuilder.fromResource(resourceClass));
	}

	public static LocationBuilder locationOf(Object resourceObject) {
		return new LocationBuilder(UriBuilder.fromResource(resourceObject.getClass()));
	}

	public static LocationBuilder locationOf(ServletContext context) {
		return new LocationBuilder(UriBuilder.fromPath(context.getContextPath()));
	}

	public LocationBuilder(UriBuilder builder) {
		this.builder = builder;
	}

	public LocationBuilder add(Class<?> clazz) {
		ApplicationPath annotation = (ApplicationPath) clazz.getAnnotation(ApplicationPath.class);
		if (annotation != null) {
			// Someone wants to add the path of the root application.
			// Unfortunately UriBuilder doesn't handle this case directly.
			builder = builder.path(annotation.value());
		} else {
			builder = builder.path(clazz);
		}
		return this;
	}
	public LocationBuilder add(String relativePath) {
		builder = builder.path(relativePath);
		return this;
	}

	public LocationBuilder add(AtlasEntity entity) {
		builder = builder.path(""+entity.getId());
		return this;
	}

	public LocationBuilder and() {
		return this;
	}

	public URI uri() {
		return builder.build().normalize();
	}

	public LocationBuilder clone() {
		return new LocationBuilder(builder.clone());
	}
}
