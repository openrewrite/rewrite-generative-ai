package org.openrewrite.ai.model;

import lombok.Value;

@Value
public class CodeEditRequest {
    String model = "code-davinci-edit-001";
    double temperature = 0.3;
    String instruction;
    String input;
}
