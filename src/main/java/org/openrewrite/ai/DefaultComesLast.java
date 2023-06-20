/*
 * Copyright 2021 the original author or authors.
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
package org.openrewrite.ai;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.ai.model.GenerativeCodeEditor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

@Value
@EqualsAndHashCode(callSuper = true)
public class DefaultComesLast extends Recipe {
    @Override
    public String getDisplayName() {
        return "Switch `default` clauses should be last";
    }

    @Override
    public String getDescription() {
        return "`switch` can contain a `default` clause for various reasons: to handle unexpected values, " +
               "to show that all the cases were properly considered. " +
               "For readability purposes, to help a developer to quickly find the default behavior of a switch statement, " +
               "it is recommended to put the default clause at the end of the switch statement. This rule raises an issue " +
               "if the default clause is not the last one of the switch's cases.";
    }

    @Override
    public JavaVisitor<ExecutionContext> getVisitor() {
        return new JavaVisitor<>() {
            @Override
            public J visitSwitch(J.Switch aSwitch, ExecutionContext ctx) {
                GenerativeCodeEditor editor = new GenerativeCodeEditor(this::getCursor, ctx);

                for (Statement statement : aSwitch.getCases().getStatements()) {
                    if (statement instanceof J.Case && ((J.Case) statement).getExpressions().get(0) instanceof J.Identifier identifier) {
                        if ("default".equals(identifier.getSimpleName())) {
                            return editor.edit(
                                aSwitch,
                                "Write this switch statement with the default case last."
                            );
                        }
                    }
                }

                return super.visitSwitch(aSwitch, ctx);
            }
        };
    }
}
