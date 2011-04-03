
The idea put forward by Tom is for the activiti-spring and the activiti-cdi projects 
to share a common set of annotations (maybe adding custom annotations).

I tried to implement the annotations as proposed by Josh in the "spring with modules" project. 

These are my notes:
1)  General technical problem: in CDI annotations are annotated with meta-annotations, example:
	Suppose I want to be able to inject process variables into a bean:
		I can achieve this using 
			@Inject @ProcessVariable username;
	In this case the @ProcessVariable annotation is a @Qualifier-Annotation:
	
		@Qualifier << requires the CDI-provided Meta-Annotation 		
		public @interface ProcessVariable {  
			/**
			   * The name of the process variable to look up. Defaults to the name of the
			   * annotated field or parameter
			   */
			  @Nonbinding << requires the CDI-provided Meta-Annotation 
			  public String value() default "";
		}
		
	The same is true for @InterceptorBindings (see e.g. StartProcess). 
	
	So if the spring and cdi modules were to share a common set of annotations, these annotations 
	would need to carry such Meta-annotations. Another implication of this would be that the project providing
	these annotations would have a compile-time (at least) dependency on the javaee6-api. 
	
2)  This is why I think that sharing a physical set of annotations would be difficult to achieve, technically.
	However, we could try to provide the 'same' annotations in terms of their name and their semantics. 
	I tried to do that here. 
	
3)  I have some annotations, which don't make sense in Spring, like @BusinessProcessScoped.	
	
Notes on individual annotations are placed in separate files.