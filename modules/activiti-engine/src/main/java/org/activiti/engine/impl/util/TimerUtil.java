package org.activiti.engine.impl.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class TimerUtil {

	private static final Logger logger = LoggerFactory.getLogger(TimerUtil.class);

	/**
	 * The event definition on which the timer is based.
	 * 
	 * Takes in an optional execution, if missing the
	 * {@link NoExecutionVariableScope} will be used (eg Timer start event)
	 */
	public static TimerEntity createTimerEntityForTimerEventDefinition(TimerEventDefinition timerEventDefinition, 
			boolean isInterruptingTimer, ExecutionEntity executionEntity, String jobHandlerType, String jobHandlerConfig) {

		String businessCalendarRef = null;
		Expression expression = null;
		ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
		if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDate())) {

			businessCalendarRef = DueDateBusinessCalendar.NAME;
			expression = expressionManager.createExpression(timerEventDefinition.getTimeDate());

		} else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeCycle())) {

			businessCalendarRef = CycleBusinessCalendar.NAME;
			expression = expressionManager.createExpression(timerEventDefinition.getTimeCycle());

		} else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDuration())) {

			businessCalendarRef = DueDateBusinessCalendar.NAME;
			expression = expressionManager.createExpression(timerEventDefinition.getTimeDuration());

		}

		if (expression == null) {
			throw new ActivitiException("Timer needs configuration (either timeDate, timeCycle or timeDuration is needed) (" + timerEventDefinition.getId() + ")");
		}

		BusinessCalendar businessCalendar = Context.getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(businessCalendarRef);

		String dueDateString = null;
		Date duedate = null;

		// ACT-1415: timer-declaration on start-event may contain expressionsNOT
		// evaluating variables but other context, evaluating should happen nevertheless
		VariableScope scopeForExpression = executionEntity;
		if (scopeForExpression == null) {
			scopeForExpression = NoExecutionVariableScope.getSharedInstance();
		}

		Object dueDateValue = expression.getValue(scopeForExpression);
		if (dueDateValue instanceof String) {
			dueDateString = (String) dueDateValue;
		} else if (dueDateValue instanceof Date) {
			duedate = (Date) dueDateValue;
		} else {
			throw new ActivitiException("Timer '" + executionEntity.getActivityId()
			        + "' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
		}

		if (duedate == null) {
			duedate = businessCalendar.resolveDuedate(dueDateString);
		}

		TimerEntity timer = new TimerEntity(jobHandlerType, jobHandlerConfig, true, TimerEntity.DEFAULT_RETRIES);
		timer.setDuedate(duedate);
		if (executionEntity != null) {
			timer.setExecution(executionEntity);
			timer.setProcessDefinitionId(executionEntity.getProcessDefinitionId());
			timer.setProcessInstanceId(executionEntity.getProcessInstanceId());

			// Inherit tenant identifier (if applicable)
			if (executionEntity != null && executionEntity.getTenantId() != null) {
				timer.setTenantId(executionEntity.getTenantId());
			}
		}

		if (StringUtils.isNotEmpty(timerEventDefinition.getTimeCycle())) {
			// See ACT-1427: A boundary timer with a cancelActivity='true', doesn't need to repeat itself
			boolean repeat = !isInterruptingTimer;

			// ACT-1951: intermediate catching timer events shouldn't repeat according to spec
			if (TimerCatchIntermediateEventJobHandler.TYPE.equals(jobHandlerType)) {
				repeat = false;
			}

			if (repeat) {
				String prepared = prepareRepeat(dueDateString);
				timer.setRepeat(prepared);
			}
		}

		if (executionEntity != null) {
			timer.setExecution(executionEntity);
			timer.setProcessDefinitionId(executionEntity.getProcessDefinitionId());

			// Inherit tenant identifier (if applicable)
			if (executionEntity != null && executionEntity.getTenantId() != null) {
				timer.setTenantId(executionEntity.getTenantId());
			}
		}

		return timer;
	}

	public static String prepareRepeat(String dueDate) {
		if (dueDate.startsWith("R") && dueDate.split("/").length == 2) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			return dueDate.replace("/", "/" + sdf.format(Context.getProcessEngineConfiguration().getClock().getCurrentTime()) + "/");
		}
		return dueDate;
	}

}
