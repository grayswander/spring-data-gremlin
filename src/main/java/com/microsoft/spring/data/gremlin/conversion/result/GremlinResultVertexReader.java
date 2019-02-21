/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion.result;

import com.microsoft.spring.data.gremlin.common.GremlinUtils;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import lombok.NoArgsConstructor;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.microsoft.spring.data.gremlin.common.Constants.*;

@NoArgsConstructor
public class GremlinResultVertexReader extends AbstractGremlinResultReader implements GremlinResultsReader {

    private void validate(List<Result> results, GremlinSource source) {
        Assert.notNull(results, "Results should not be null.");
        Assert.notNull(source, "GremlinSource should not be null.");
        Assert.isTrue(results.size() == 1, "Vertex should contain only one result.");

        final Result result = results.get(0);

        System.out.println(result);

        Assert.isInstanceOf(Map.class, result.getObject(), "should be one instance of Map");

        @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) result.getObject();

        if(! map.containsKey(PROPERTY_PROPERTIES)) {
            map.put(PROPERTY_PROPERTIES, new LinkedHashMap<>());
        }

        Assert.isTrue(map.containsKey(PROPERTY_ID), "should contain id property");
        Assert.isTrue(map.containsKey(PROPERTY_LABEL), "should contain label property");
        Assert.isTrue(map.containsKey(PROPERTY_TYPE), "should contain type property");
        Assert.isTrue(map.containsKey(PROPERTY_PROPERTIES), "should contain properties property");
        Assert.isTrue(map.get(PROPERTY_TYPE).equals(RESULT_TYPE_VERTEX), "must be vertex type");

        Assert.isInstanceOf(Map.class, map.get(PROPERTY_PROPERTIES), "should be one instance of Map");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(@NonNull List<Result> results, @NonNull GremlinSource source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("Should be instance of GremlinSourceVertex");
        }

        validate(results, source);

        final String initialClassName = source.getDomainClass().getName();

        final Map<String, Object> map = (Map<String, Object>) results.get(0).getObject();
        final Map<String, Object> properties = (Map<String, Object>) map.get(PROPERTY_PROPERTIES);

        super.readResultProperties(properties, source);

        final String className = Optional.ofNullable(source.getProperties().get(GREMLIN_PROPERTY_CLASSNAME))
                .orElse(initialClassName).toString();

        source.setIdField(GremlinUtils.getIdField(GremlinUtils.toEntityClass(className)));
        source.setId(map.get(PROPERTY_ID));
        source.setLabel(map.get(PROPERTY_LABEL).toString());
    }
}
