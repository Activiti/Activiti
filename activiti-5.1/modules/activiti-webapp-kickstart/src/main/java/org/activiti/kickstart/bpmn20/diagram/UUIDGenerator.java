package  org.activiti.kickstart.bpmn20.diagram;

public class UUIDGenerator {
	public static String generate(){
		return "sid-" + java.util.UUID.randomUUID().toString();
	}
}