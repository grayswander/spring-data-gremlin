/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion.script;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GremlinScriptEdgeLiteralFactory implements GremlinScriptFactory {

    @Override
    public GremlinScriptEdgeLiteral createGremlinScript() {
        return new GremlinScriptEdgeLiteral();
    }
}