package org.openrewrite.ai.model;

import org.openrewrite.DelegatingExecutionContext;
import org.openrewrite.ExecutionContext;
import org.openrewrite.HttpSenderExecutionContextView;

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
