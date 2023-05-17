package org.openrewrite.ai.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.HttpSenderExecutionContextView;
import org.openrewrite.ipc.http.HttpSender;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.marker.Markup;

import java.io.IOException;
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
            .post("https://api.openai.com/v1/edits")
            .withHeader("Authorization", "Bearer " + ctx.getOpenapiToken().trim())
            .withContent("application/json", mapper.writeValueAsBytes(new CodeEditRequest(instruction, input)))
            .send()) {

            CodeEditResponse response = mapper.readValue(raw.getBodyAsBytes(), CodeEditResponse.class);
            if (response.getError() != null) {
                return Markup.warn(j, new IllegalStateException("Code edit failed: " + response.getError()));
            }

            return j.withTemplate(JavaTemplate.builder(response.getChoices().get(0).getText()).context(cursor).build(),
                cursor.get(),
                j.getCoordinates().replace());
        } catch (IOException e) {
            return Markup.warn(j, e);
        }
    }
}
