/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.jsonld.spi;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;

/**
 * Provides methods for processing JSON objects.
 */
public class TypeUtil {

    /**
     * Returns the {@code @type} of a JSON object. If more than one type is specified, this method
     * will return the first.
     *
     * @param object some JSON object.
     * @return type of JSON object as string.
     */
    @Nullable
    public static String nodeType(JsonObject object) {
        var typeNode = object.get(TYPE);
        if (typeNode == null) {
            return null;
        }

        if (typeNode instanceof JsonString) {
            return ((JsonString) typeNode).getString();
        } else if (typeNode instanceof JsonArray) {
            var array = typeValueArray(typeNode);
            if (array == null) {
                return null;
            }

            var typeValue = array.get(0); // a note can have more than one type, take the first
            if (!(typeValue instanceof JsonString)) {
                return null;
            }

            return ((JsonString) typeValue).getString();
        }

        return null;
    }

    /**
     * Returns a json value as {@link JsonArray}.
     *
     * @param typeNode some JSON value.
     * @return JSON array.
     */
    @Nullable
    public static JsonArray typeValueArray(JsonValue typeNode) {
        if (!(typeNode instanceof JsonArray)) {
            return null;
        }

        var array = (JsonArray) typeNode;
        if (array.isEmpty()) {
            return null;
        }

        return array;
    }

    /**
     * Checks if a JSON object is of expected type.
     *
     * @param object a JSON object.
     * @param expected type that is expected.
     * @return true if types match, false if not.
     */
    public static boolean isOfExpectedType(JsonObject object, String expected) {
        var actual = nodeType(object);

        return actual != null && actual.equals(expected);
    }
}
