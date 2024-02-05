/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.ai.model;

import org.openrewrite.DelegatingExecutionContext;
import org.openrewrite.ExecutionContext;

import static java.util.Objects.requireNonNull;

public class GenerativeCodeExecutionContextView extends DelegatingExecutionContext {
    private static final String OPENAPI_TOKEN = "org.openrewrite.ai.openapi.token";

    public GenerativeCodeExecutionContextView(ExecutionContext delegate) {
        super(delegate);
    }

    public static GenerativeCodeExecutionContextView view(ExecutionContext ctx) {
        if (ctx instanceof GenerativeCodeExecutionContextView) {
            return (GenerativeCodeExecutionContextView) ctx;
        }
        return new GenerativeCodeExecutionContextView(ctx);
    }

    public GenerativeCodeExecutionContextView setOpenApiToken(String token) {
        putMessage(OPENAPI_TOKEN, token);
        return this;
    }

    public String getOpenapiToken() {
        return requireNonNull(getMessage(OPENAPI_TOKEN));
    }
}
