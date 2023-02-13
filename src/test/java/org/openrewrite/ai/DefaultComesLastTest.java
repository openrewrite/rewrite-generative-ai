package org.openrewrite.ai;

import org.junit.jupiter.api.RepeatedTest;
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
