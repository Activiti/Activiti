package org.activiti.impl.interceptor;

import static org.junit.Assert.assertEquals;

import org.activiti.impl.ProcessEngineImpl;
import org.activiti.test.ProcessEngineBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultCommandExecutorTest {

  // TODO: This wouldn't be necessary if CommandContext and factory were
  // interfaces
  @Rule
  public ProcessEngineBuilder processEngineBuilder = new ProcessEngineBuilder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private StringBuilder builder = new StringBuilder();

  @Test
  public void testContextAwareInterceptors() throws Exception {
    DefaultCommandExecutor chain = new DefaultCommandExecutor(((ProcessEngineImpl) processEngineBuilder.getProcessEngine()).getProcessEngineConfiguration()
            .getCommandContextFactory());
    chain.addContextAwareCommandInterceptor(new ContextAwareCommandInterceptor() {

      public <T> T invoke(CommandExecutor next, Command<T> command, CommandContext context) {
        builder.append("b:1:");
        T result = next.execute(command);
        builder.append(":a:1");
        return result;
      }
    });
    chain.addContextAwareCommandInterceptor(new ContextAwareCommandInterceptor() {

      public <T> T invoke(CommandExecutor next, Command<T> command, CommandContext context) {
        builder.append("b:2:");
        T result = next.execute(command);
        builder.append(":a:2");
        return result;
      }
    });
    String result = chain.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        return builder.append("c").toString();
      }
    });
    assertEquals("b:1:b:2:c", result);
    assertEquals("b:1:b:2:c:a:2:a:1", builder.toString());
  }

  @Test
  public void testVanillaInterceptors() throws Exception {
    DefaultCommandExecutor chain = new DefaultCommandExecutor(((ProcessEngineImpl) processEngineBuilder.getProcessEngine()).getProcessEngineConfiguration()
            .getCommandContextFactory());
    chain.addCommandInterceptor(new CommandInterceptor() {

      public <T> T invoke(CommandExecutor next, Command<T> command) {
        builder.append("b:1:");
        T result = next.execute(command);
        builder.append(":a:1");
        return result;
      }
    });
    chain.addCommandInterceptor(new CommandInterceptor() {

      public <T> T invoke(CommandExecutor next, Command<T> command) {
        builder.append("b:2:");
        T result = next.execute(command);
        builder.append(":a:2");
        return result;
      }
    });
    String result = chain.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        return builder.append("c").toString();
      }
    });
    assertEquals("b:1:b:2:c", result);
    assertEquals("b:1:b:2:c:a:2:a:1", builder.toString());
  }

  @Test
  public void testMixedInterceptors() throws Exception {
    DefaultCommandExecutor chain = new DefaultCommandExecutor(((ProcessEngineImpl) processEngineBuilder.getProcessEngine()).getProcessEngineConfiguration()
            .getCommandContextFactory());
    chain.addCommandInterceptor(new CommandInterceptor() {

      public <T> T invoke(CommandExecutor next, Command<T> command) {
        builder.append("b:1:");
        T result = next.execute(command);
        builder.append(":a:1");
        return result;
      }
    });
    chain.addContextAwareCommandInterceptor(new ContextAwareCommandInterceptor() {

      public <T> T invoke(CommandExecutor next, Command<T> command, CommandContext context) {
        builder.append("b:2:");
        T result = next.execute(command);
        builder.append(":a:2");
        return result;
      }
    });
    String result = chain.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        return builder.append("c").toString();
      }
    });
    assertEquals("b:1:b:2:c", result);
    assertEquals("b:1:b:2:c:a:2:a:1", builder.toString());
  }

  @Test
  public void testVanillaInterceptorWithException() throws Exception {
    DefaultCommandExecutor chain = new DefaultCommandExecutor(((ProcessEngineImpl) processEngineBuilder.getProcessEngine()).getProcessEngineConfiguration()
            .getCommandContextFactory());
    chain.addCommandInterceptor(new CommandInterceptor() {

      public <T> T invoke(CommandExecutor next, Command<T> command) {
        builder.append("a");
        throw new RuntimeException("Planned failure");
      }
    });
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Planned failure");
    String result = null;
    try {
      result = chain.execute(new Command<String>() {

        public String execute(CommandContext commandContext) {
          return builder.append("c").toString();
        }
      });
    } finally {
      assertEquals(null, result);
      assertEquals("a", builder.toString());
    }
  }
}
