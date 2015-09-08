/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ValueOfBasedListSplitter<ElementType> {
	String separator;
	Method valueOfMethod;

	public ValueOfBasedListSplitter(String separator, Class<ElementType> elementClass) {
		getValueOfMethodReference(elementClass);
		this.separator = separator;
	}

	private void getValueOfMethodReference(Class<ElementType> elementClass) {
		Method candidate = maybeFindMethod(elementClass, String.class);
		if (candidate == null) {
			candidate = maybeFindMethod(elementClass, Object.class);
		}
		if (candidate == null) {
			throw new IllegalArgumentException("unable to find valueOf method for element class "+elementClass);
		}
		valueOfMethod = candidate;
	}

	private Method maybeFindMethod(Class<?> declaringClass, Class<?>... params) {
		try {
			Method candidate = declaringClass.getDeclaredMethod("valueOf", params);
			if (isValidValueOfForClass(candidate, declaringClass)) {
				return candidate;
			} else {
				return null;
			}
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private boolean isValidValueOfForClass(Method candidate, Class<?> declaringClass) {
		boolean isAssignable = declaringClass.isAssignableFrom(candidate.getReturnType());
		boolean isStatic = Modifier.isStatic(candidate.getModifiers());
		return isAssignable && isStatic;
	}

	public List<ElementType> split(String input) {
		List<ElementType> list = new LinkedList<>();
		if (input.equals("")) return list; // empty string gives empty list.
		String[] splitString = input.split(separator);
		for (String string : splitString) {
			if (string.equals("")) continue;
			ElementType value = invokeValueOf(string);
			list.add(value);
		}
		return list;
	}

	private ElementType invokeValueOf(String input) {
		try {
			return (ElementType) valueOfMethod.invoke(null, input);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// should hopefully not land here, since prereqs are checked in
			// isValidValueOfForClass()
			throw new IllegalArgumentException(e);
		}
	}
}
