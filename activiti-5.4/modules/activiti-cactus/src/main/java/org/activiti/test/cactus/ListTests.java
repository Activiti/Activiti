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

package org.activiti.test.cactus;

import java.io.File;
import java.io.PrintWriter;


/**
 * @author Tom Baeyens
 */
public class ListTests {

  public static void main(String[] args) {
    try {
      File rootPath = new File("../activiti-engine/src/test/java");
      System.out.println("Listing tests in dir "+rootPath.getCanonicalPath()+" in target/classes/activiti.cactus.tests.txt");
      PrintWriter writer = new PrintWriter("target/classes/activiti.cactus.tests.txt");
      try {

        scan(rootPath, null, writer);
        
      } finally {
        writer.flush();
        writer.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public static void scan(File directory, String packageName, PrintWriter writer) {
    for (File file: directory.listFiles()) {
      if (file.isFile()) {
        String fileName = file.getName();
        if (fileName.endsWith("Test.java")) {
          String className = packageName+"."+fileName.substring(0, fileName.length()-5);
          writer.println(className);
        }
      } else if (file.isDirectory()) {
        String fileName = file.getName();
        String newPackageName = (packageName==null ? fileName : packageName+"."+fileName);
        if (!newPackageName.startsWith("org.activiti.standalone")) {
          scan(file, newPackageName, writer);
        }
      }
    }
  }
}
