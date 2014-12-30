/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.interceptor;

import org.activiti.engine.impl.context.Context;

/**
 * @author Joram Barrez
 */
public class CommandInvoker extends AbstractCommandInterceptor {

    @Override
    @SuppressWarnings("unchecked")
	public <T> T execute(final CommandConfig config, final Command<T> command) {
		final CommandContext commandContext = Context.getCommandContext();
		
		// Execute the command. 
		// This will produce operations that will be put on the agenda.
		commandContext.getAgenda().planOperation(new Runnable() {

			@Override
			public void run() {
				commandContext.setResult(command.execute(commandContext));
			}
		});

		// Run loop for agenda
		executeOperations(commandContext);

//		// At the end, call the execution tree change listeners.
//		// TODO: optimization: only do this when the tree has actually changed
//		// (ie check dbSqlSession).
//		if (commandContext.hasInvolvedExecutions()) {
//			commandContext.getAgenda().add(new ExecuteInactivatedBehavior(commandContext.getCoreEngine(), commandContext));
//			executeOperations(commandContext);
//		}

		return (T) commandContext.getResult();
	}
	
	protected void executeOperations(final CommandContext commandContext) {
		while (!commandContext.getAgenda().isEmpty()) {
			Runnable runnable = commandContext.getAgenda().getNextOperation();
			runnable.run();
		}
	}

	@Override
	public CommandInterceptor getNext() {
		return null;
	}

	@Override
	public void setNext(CommandInterceptor next) {
		throw new UnsupportedOperationException("CommandInvoker must be the last interceptor in the chain");
	}

}
