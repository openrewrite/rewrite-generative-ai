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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.HttpSenderExecutionContextView;
import org.openrewrite.ipc.http.HttpSender;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.marker.Markup;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class GenerativeCodeEditor {
    private static final ObjectMapper mapper = JsonMapper.builder()
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .build()
            .registerModule(new ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final Supplier<Cursor> cursor;
    private final HttpSender http;
    private final GenerativeCodeExecutionContextView ctx;

    public GenerativeCodeEditor(Supplier<Cursor> cursor, ExecutionContext context) {
        this.cursor = cursor;
        this.ctx = GenerativeCodeExecutionContextView.view(context);
        this.http = HttpSenderExecutionContextView.view(context).getHttpSender();
    }

    public <J2 extends Statement> J2 edit(J2 j, String instruction) {
        String input = j.printTrimmed(cursor.get());
        try (HttpSender.Response raw = http
                .post("https://api.openai.com/v1/chat/completions")
                .withHeader("Authorization", "Bearer " + ctx.getOpenapiToken().trim())
                .withContent("application/json", mapper.writeValueAsBytes(new CodeEditRequest(Arrays.asList(
                        new Message("user", instruction),
                        new Message("user", "```java\n" + input + "\n```")
                ))))
                .send()) {

            CodeEditResponse response = mapper.readValue(raw.getBodyAsBytes(), CodeEditResponse.class);
            if (response.getError() != null) {
                return Markup.warn(j, new IllegalStateException("Code edit failed: " + response.getError()));
            }

            String messageContent = response.getChoices().get(0).getMessage().getContent();
            String codeSnippet = messageContent.substring(
                    messageContent.indexOf("```java\n") + 8,
                    messageContent.lastIndexOf("\n```"));
            return JavaTemplate.builder(codeSnippet)
                    .contextSensitive()
                    .build()
                    .apply(cursor.get(), j.getCoordinates().replace());
        } catch (IOException e) {
            return Markup.warn(j, e);
        }
    }
}

@Value
class CodeEditRequest {
    String model = "gpt-4o";
    double temperature = 0.3;
    List<Message> messages;
}

@Value
class CodeEditResponse {
    List<Choice> choices;

    @Nullable
    Error error;

    @Value
    static class Choice {
        Message message;
    }

    @Value
    static class Error {
        String message;
    }
}

@Value
class Message {
    String role;
    String content;
}
