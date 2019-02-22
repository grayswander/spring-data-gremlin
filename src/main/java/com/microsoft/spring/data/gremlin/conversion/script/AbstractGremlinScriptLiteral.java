/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion.script;

import com.microsoft.spring.data.gremlin.annotation.GeneratedValue;
import com.microsoft.spring.data.gremlin.common.GremlinEntityType;
import com.microsoft.spring.data.gremlin.common.GremlinUtils;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import lombok.NonNull;
import org.apache.tinkerpop.shaded.jackson.core.JsonProcessingException;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;

import static com.microsoft.spring.data.gremlin.common.Constants.*;

public abstract class AbstractGremlinScriptLiteral {

    protected static String generateEntityWithRequiredId(@NonNull Object id, GremlinEntityType type) {
        Assert.isTrue(type == GremlinEntityType.EDGE || type == GremlinEntityType.VERTEX, "should be edge/vertex type");

        final String prefix = (type == GremlinEntityType.VERTEX) ? "V" : "E";

        if (id instanceof String) {
            return prefix + String.format("('%s')", (String) id);
        } else if (id instanceof Integer) {
            return prefix + String.format("(%d)", (Integer) id);
        } else if (id instanceof Long) {
            return prefix + String.format("(%d)", (Long) id);
        }

        throw new GremlinInvalidEntityIdFieldException("Only String/Integer/Long of id is supported");
    }

    protected static String generatePropertyWithRequiredId(@NonNull Object id) {
        if (id instanceof String) {
            return String.format("property(id, '%s')", (String) id);
        } else if (id instanceof Integer) {
            return String.format("property(id, %d)", (Integer) id);
        } else if (id instanceof Long) {
            return String.format("property(id, %d)", (Long) id);
        }

        throw new GremlinInvalidEntityIdFieldException("Only String/Integer/Long of id is supported");
    }

    protected static String generateAsWithAlias(@NonNull String alias) {
        return String.format("as('%s')", alias);
    }

    protected static String generateAddEntityWithLabel(@NonNull String label, GremlinEntityType type) {
        Assert.isTrue(type == GremlinEntityType.EDGE || type == GremlinEntityType.VERTEX, "should be edge/vertex type");

        final String prefix = (type == GremlinEntityType.VERTEX) ? "addV" : "addE";

        return prefix + String.format("('%s')", label);
    }

    protected static List<String> completeScript(@NonNull List<String> scriptList) {
        return Collections.singletonList(String.join(GREMLIN_PRIMITIVE_INVOKE, scriptList));
    }

    public static String generateHasLabel(@NonNull String label) {
        // g.V().or(has('label', 'Animal'), and(has('labels'),has('labels', 'Animal')))
        // or(has('label', '%s'), and(has('labels'),has('labels', '%s')))
        return String.format("or(has('label', '%s'), and(has('labels'),has('labels', '%s')))", label, label);
    }

    public static String generateHasId(@NonNull Object id) {
        if (id instanceof String) {
            return String.format("hasId('%s')", id);
        } else if (id instanceof Integer) {
            return String.format("hasId(%d)", (Integer) id);
        } else if (id instanceof Long) {
            return String.format("hasId(%d)", (Long) id);
        } else {
            throw new GremlinInvalidEntityIdFieldException("the type of @Id/id field should be String/Integer/Long");
        }
    }

    public static String generateHasId(@NonNull Object id, @NonNull Field idFiled) {
        if (!idFiled.isAnnotationPresent(GeneratedValue.class)) {
            return generateHasId(id);
        } else if (id instanceof String) {
            return String.format("hasId('%s')", id);
        } else if (id instanceof Integer) {
            return String.format("hasId(%dL)", (Integer) id);
        } else if (id instanceof Long) {
            return String.format("hasId(%dL)", (Long) id);
        } else {
            throw new GremlinInvalidEntityIdFieldException("the type of @Id/id field should be String/Integer/Long");
        }
    }

    private static String generateProperty(@NonNull String name, @NonNull String value) {
        final String escapedValue = value.replace("'", "\\'");
        return String.format(GREMLIN_PRIMITIVE_PROPERTY_STRING, name, escapedValue);
    }

    private static String generateProperty(@NonNull String name, @NonNull Integer value) {
        return String.format(GREMLIN_PRIMITIVE_PROPERTY_NUMBER, name, value);
    }

    private static String generateProperty(@NonNull String name, @NonNull Boolean value) {
        return String.format(GREMLIN_PRIMITIVE_PROPERTY_BOOLEAN, name, value);
    }

    private static String generateProperty(@NonNull String name, @NonNull Long value) {
        return String.format(GREMLIN_PRIMITIVE_PROPERTY_NUMBER, name, value);
    }

    private static String generateProperty(@NonNull String name, @NonNull Object value) {
        if (value instanceof Integer) {
            return generateProperty(name, (Integer) value);
        } else if (value instanceof Boolean) {
            return generateProperty(name, (Boolean) value);
        } else if (value instanceof String) {
            return generateProperty(name, (String) value);
        } else if (value instanceof Date) {
            return generateProperty(name, GremlinUtils.timeToMilliSeconds(value));
        } else {
            final String propertyScript;

            try {
                propertyScript = generateProperty(name, GremlinUtils.getObjectMapper().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new GremlinUnexpectedEntityTypeException("Failed to write object to String", e);
            }

            return propertyScript;
        }
    }

    protected static List<String> generateProperties(@NonNull final Map<String, Object> properties) {
        final List<String> scripts = new ArrayList<>();

        properties.entrySet().stream().filter(e -> e.getValue() != null)
                .forEach(e -> scripts.add(generateProperty(e.getKey(), e.getValue())));

        return scripts;
    }

    private static String generateHas(@NonNull String name, @NonNull Integer value) {
        return String.format(GREMLIN_PRIMITIVE_HAS_NUMBER, name, value);
    }

    private static String generateHas(@NonNull String name, @NonNull Boolean value) {
        return String.format(GREMLIN_PRIMITIVE_HAS_BOOLEAN, name, value);
    }

    private static String generateHas(@NonNull String name, @NonNull String value) {
        return String.format(GREMLIN_PRIMITIVE_HAS_STRING, name, value);
    }

    private static String generateHas(@NonNull String name, @NonNull Long value) {
        return String.format(GREMLIN_PRIMITIVE_HAS_NUMBER, name, value);
    }

    // TODO: should move to query method part.
    public static String generateHas(@NonNull String name, @NonNull Object value) {

        if (value instanceof Integer) {
            return generateHas(name, (Integer) value);
        } else if (value instanceof Boolean) {
            return generateHas(name, (Boolean) value);
        } else if (value instanceof String) {
            return generateHas(name, (String) value);
        } else if (value instanceof Date) {
            return generateHas(name, GremlinUtils.timeToMilliSeconds(value));
        } else {
            final String hasScript;

            try {
                hasScript = generateHas(name, GremlinUtils.getObjectMapper().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new GremlinUnexpectedEntityTypeException("Failed to write object to String", e);
            }

            return hasScript;
        }
    }

    public List<String> generateLabelsList(@NonNull GremlinSource source) {
        final List<String> scriptList = new ArrayList<>();

        final Set<String> labels = source.getLabels();
        for (String label: labels) {
            scriptList.add(String.format("property(list, \'labels\', \'%s\')", label));
        }
        return scriptList;
    }
}
