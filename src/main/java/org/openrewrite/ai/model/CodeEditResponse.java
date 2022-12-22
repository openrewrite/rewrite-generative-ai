package org.openrewrite.ai.model;

import lombok.Value;
import org.openrewrite.internal.lang.Nullable;

import java.util.List;

@Value
public class CodeEditResponse {
    List<Choice> choices;

    @Nullable
    Error error;

    @Value
    static class Choice {
        String text;
    }

    @Value
    static class Error {
        String message;
    }
}
