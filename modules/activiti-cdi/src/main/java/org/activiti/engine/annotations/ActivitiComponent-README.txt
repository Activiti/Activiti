My thoughts:

I REALLY(!) like the idea. However, in CDI, the most natural thing would be to realize it using events:

<userTask id="confirm-receipt" />

in a bean: 
public void sendNotification(@Observes @StateEntered("confirm-receipt") DelegateExecution execution) {
	notify someone to confirm the receipt ...
}

and that's all. (Have a POC version of that working already)

then also interesting:
public void sendNotification(@Observes(during = AFTER_SUCCESS) @StateEntered("confirm-receipt") DelegateExecution execution) {
	notify someone to confirm the receipt ...
}
(transactional observer, could be VERY interesting with activiti. Possible alternatives:
    
IN_PROGESS observers are called immediately (default)
AFTER_SUCCESS observers are called during the after completion phase of the transaction, but only if the transaction completes successfully
AFTER_FAILURE observers are called during the after completion phase of the transaction, but only if the transaction fails to complete successfully
AFTER_COMPLETION observers are called during the after completion phase of the transaction
BEFORE_COMPLETION observers are called during the before completion phase of the transaction 
)

Transactional observers would only work as intended if Activiti is configured using Jta and sharing application transactions. 
 

////////////////////////////////////////////////////////////////////////////////////////////////
Josh writes:

/**
 * Indicates that the given bean is an Activiti handler. An activiti handler is a bean
 * that is so annotated to respond to events ("states") in an Activiti BPM process.
 * Generically, it is a class that has been adapted to be usable in an Activiti process
 *
 * <p/>
 * For example, suppose we have registered a BPMN process that has
 * the following declaration:
 * <p/>
 * <code>
 * &lt;service-task activiti:expression = "myBean" id = "confirm-receipt" /&gt;
 * </code>
 * <p/>
 * This is a state that will be entered from Activiti and execution will flow through to the bean
 * registered in the context as "myBean." To subscribe to that, a POJO need only implement
 * (optionally) {@link ActivitiComponent} and, on a method, add
 * {@link State} to indicate that the method in particular is
 * tasked with responding to a state. If applied to a bean and there are no {@link org.activiti.engine.annotations.ActivitiComponent}
 * annotations present, then one option might be to automatically enlist all public methods
 * as handlers for states whose IDs or names are inferred from the method name:
 * <p/>
 * <code>public void confirmReceipt(..)</code> would be treated the same as
 * <p/>
 * <code>@State( "confirm-receipt") public void confirmReceipt (..)</code>,
 *
 * @author Josh Long
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Component
public @interface ActivitiComponent {
	String processKey() default "";
}
