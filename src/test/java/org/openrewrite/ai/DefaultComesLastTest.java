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
package org.openrewrite.ai;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.HttpSenderExecutionContextView;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ai.model.GenerativeCodeExecutionContextView;
import org.openrewrite.ipc.http.HttpUrlConnectionSender;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static java.util.Objects.requireNonNull;
import static org.openrewrite.java.Assertions.java;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class DefaultComesLastTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        try (InputStream token = requireNonNull(getClass().getResourceAsStream("/token.txt"))) {
            ExecutionContext ctx = GenerativeCodeExecutionContextView.view(new InMemoryExecutionContext())
              .setOpenApiToken(new String(token.readAllBytes()).trim());
            ctx = HttpSenderExecutionContextView.view(ctx).setHttpSender(new HttpUrlConnectionSender(
              Duration.ofSeconds(10), Duration.ofSeconds(30)));

            spec.recipe(new DefaultComesLast())
              .cycles(1)
              .expectedCyclesThatMakeChanges(1)
              .executionContext(ctx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RepeatedTest(10)
    void edit() {
        rewriteRun(
          //language=java
          java(
            """
              class Test {
                  void doSomething() {}
                  void doSomethingElse() {}
                  void error() {}

                  void test(int param) {
                      switch (param) {
                          case 0:
                              doSomething();
                              break;
                          default:
                              error();
                              break;
                          case 1:
                              doSomethingElse();
                              break;
                      }
                  }
              }
              """,
            """
              class Test {
                  void doSomething() {}
                  void doSomethingElse() {}
                  void error() {}
                            
                  void test(int param) {
                      switch (param) {
                          case 0:
                              doSomething();
                              break;
                          case 1:
                              doSomethingElse();
                              break;
                          default:
                              error();
                              break;
                      }
                  }
              }
              """
          )
        );
    }
}
