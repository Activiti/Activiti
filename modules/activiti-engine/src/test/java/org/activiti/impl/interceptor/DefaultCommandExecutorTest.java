package org.activiti.impl.interceptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultCommandExecutorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private StringBuilder builder = new StringBuilder();

  @Test
  public void testContextAwareInterceptors() throws Exception {

    CommandContextFactory commandContextFactory = mock(CommandContextFactory.class);
    CommandContext commandContext = mock(CommandContext.class);

    DefaultCommandExecutor chain = new DefaultCommandExecutor();
    chain.setCommandContextFactory(commandContextFactory);

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

    Command<String> command = new Command<String>() {

      public String execute(CommandContext commandContext) {
        return builder.append("c").toString();
      }
    };

    when(commandContextFactory.createCommandContext(command)).thenReturn(commandContext);

    String result = chain.execute(command);
    assertEquals("b:1:b:2:c", result);
    assertEquals("b:1:b:2:c:a:2:a:1", builder.toString());
  }

  @Test
  public void testVanillaInterceptors() throws Exception {
    CommandContextFactory commandContextFactory = mock(CommandContextFactory.class);
    CommandContext commandContext = mock(CommandContext.class);

    DefaultCommandExecutor chain = new DefaultCommandExecutor();
    chain.setCommandContextFactory(commandContextFactory);

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
    Command<String> command = new Command<String>() {

      public String execute(CommandContext commandContext) {
        return builder.append("c").toString();
      }
    };

    when(commandContextFactory.createCommandContext(command)).thenReturn(commandContext);

    String result = chain.execute(command);
    assertEquals("b:1:b:2:c", result);
    assertEquals("b:1:b:2:c:a:2:a:1", builder.toString());
  }

  @Test
  public void testMixedInterceptors() throws Exception {
    CommandContextFactory commandContextFactory = mock(CommandContextFactory.class);
    CommandContext commandContext = mock(CommandContext.class);

    DefaultCommandExecutor chain = new DefaultCommandExecutor();
    chain.setCommandContextFactory(commandContextFactory);

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
    Command<String> command = new Command<String>() {

      public String execute(CommandContext commandContext) {
        return builder.append("c").toString();
      }
    };

    when(commandContextFactory.createCommandContext(command)).thenReturn(commandContext);

    String result = chain.execute(command);

    assertEquals("b:1:b:2:c", result);
    assertEquals("b:1:b:2:c:a:2:a:1", builder.toString());
  }

  @Test
  public void testVanillaInterceptorWithException() throws Exception {
    CommandContextFactory commandContextFactory = mock(CommandContextFactory.class);
    CommandContext commandContext = mock(CommandContext.class);


    DefaultCommandExecutor chain = new DefaultCommandExecutor();
    chain.setCommandContextFactory(commandContextFactory);

    Command<String> command = new Command<String>() {

        public String execute(CommandContext commandContext) {
          return builder.append("c").toString();
        }
      };

    when(commandContextFactory.createCommandContext(command)).thenReturn(commandContext);

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

      result = chain.execute(command);
    } finally {
      assertEquals(null, result);
      assertEquals("a", builder.toString());
    }
  }
}
