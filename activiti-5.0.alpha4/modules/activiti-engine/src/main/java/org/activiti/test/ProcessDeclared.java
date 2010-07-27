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

package org.activiti.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * test method annotation which asserts that there is a BPMN 2.0 XML process
 * definition for this method. If no arguments are given, the definition should
 * be in a single file accessible through the classpath, in the same package as
 * the test case and with a file name
 * <code>&lt;methodName&gt;.bpmn20.xml.</code> Resources to load can be
 * explicitly specified using either resources or value attributes.
 * 
 * 
 * @author Dave Syer
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ProcessDeclared {

  /**
   * Synonym for {@link #resources()}.
   */
  public String[] value() default {};

  /**
   * Specify resources that make up the process definition. A resource can be
   * "absolute" (in the root of the classpath) if it starts with "/", or it can
   * be relative to the package of the declaring test case.  E.g.
   * 
   * <pre>
   * &#64;Test
   * &#64;ProcessDeclared(resources = { "VacationRequest.bpmn20.xml", "approve.form", "request.form" })
   * public void testFormsWithVacationRequestProcess() {
   *  ...
   * }
   * </pre>
   */
  public String[] resources() default {};

}
