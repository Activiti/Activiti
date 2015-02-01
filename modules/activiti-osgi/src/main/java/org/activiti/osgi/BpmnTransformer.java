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
package org.activiti.osgi;

import static org.activiti.osgi.Constants.BUNDLE_ACTIVITI_HEADER;
import static org.osgi.framework.Constants.BUNDLE_MANIFESTVERSION;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Helper class to actually transform the BPMN xml file into a bundle.
 * 
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class BpmnTransformer {

  static DocumentBuilderFactory dbf;
  static TransformerFactory tf;

  @SuppressWarnings({ "rawtypes" })
  public static void transform(URL url, OutputStream os) throws Exception {
    // Build dom document
    Document doc = parse(url);
    // Heuristicly retrieve name and version
    String name = url.getPath();
    int idx = name.lastIndexOf('/');
    if (idx >= 0) {
      name = name.substring(idx + 1);
    }
    String[] str = extractNameVersionType(name);
    // Create manifest
    Manifest m = new Manifest();
    m.getMainAttributes().putValue("Manifest-Version", "2");
    m.getMainAttributes().putValue(BUNDLE_MANIFESTVERSION, "2");
    m.getMainAttributes().putValue(BUNDLE_SYMBOLICNAME, str[0]);
    m.getMainAttributes().putValue(BUNDLE_VERSION, str[1]);
    m.getMainAttributes()
        .putValue(BUNDLE_ACTIVITI_HEADER, "OSGI-INF/activiti/");
    // Extract manifest entries from the DOM
    NodeList l = doc.getElementsByTagName("manifest");
    if (l != null) {
      for (int i = 0; i < l.getLength(); i++) {
        Element e = (Element) l.item(i);
        String text = e.getTextContent();
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(text.trim().getBytes()));
        Enumeration en = props.propertyNames();
        while (en.hasMoreElements()) {
          String k = (String) en.nextElement();
          String v = props.getProperty(k);
          m.getMainAttributes().putValue(k, v);
        }
        e.getParentNode().removeChild(e);
      }
    }

    JarOutputStream out = new JarOutputStream(os);
    ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
    out.putNextEntry(e);
    m.write(out);
    out.closeEntry();
    e = new ZipEntry("OSGI-INF/");
    out.putNextEntry(e);
    e = new ZipEntry("OSGI-INF/activiti/");
    out.putNextEntry(e);
    out.closeEntry();
    e = new ZipEntry("OSGI-INF/activiti/" + name);
    out.putNextEntry(e);
    // Copy the new DOM
    if (tf == null) {
      tf = TransformerFactory.newInstance();
    }
    tf.newTransformer().transform(new DOMSource(doc), new StreamResult(out));
    out.closeEntry();
    out.close();
  }

  private static final String DEFAULT_VERSION = "0.0.0";

  private static final Pattern ARTIFACT_MATCHER = Pattern
      .compile(
          "(.+)(?:-(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:[^a-zA-Z0-9](.*))?)(?:\\.([^\\.]+))",
          Pattern.DOTALL);
  private static final Pattern FUZZY_MODIFIDER = Pattern.compile(
      "(?:\\d+[.-])*(.*)", Pattern.DOTALL);

  public static String[] extractNameVersionType(String url) {
    Matcher m = ARTIFACT_MATCHER.matcher(url);
    if (!m.matches()) {
      return new String[] { url, DEFAULT_VERSION };
    } else {
      // System.err.println(m.groupCount());
      // for (int i = 1; i <= m.groupCount(); i++) {
      // System.err.println("Group " + i + ": " + m.group(i));
      // }

      StringBuffer v = new StringBuffer();
      String d1 = m.group(1);
      String d2 = m.group(2);
      String d3 = m.group(3);
      String d4 = m.group(4);
      String d5 = m.group(5);
      String d6 = m.group(6);
      if (d2 != null) {
        v.append(d2);
        if (d3 != null) {
          v.append('.');
          v.append(d3);
          if (d4 != null) {
            v.append('.');
            v.append(d4);
            if (d5 != null) {
              v.append(".");
              cleanupModifier(v, d5);
            }
          } else if (d5 != null) {
            v.append(".0.");
            cleanupModifier(v, d5);
          }
        } else if (d5 != null) {
          v.append(".0.0.");
          cleanupModifier(v, d5);
        }
      }
      return new String[] { d1, v.toString(), d6 };
    }
  }

  private static void cleanupModifier(StringBuffer result, String modifier) {
    Matcher m = FUZZY_MODIFIDER.matcher(modifier);
    if (m.matches()) {
      modifier = m.group(1);
    }
    for (int i = 0; i < modifier.length(); i++) {
      char c = modifier.charAt(i);
      if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
          || (c >= 'A' && c <= 'Z') || c == '_' || c == '-') {
        result.append(c);
      }
    }
  }

  protected static Document parse(URL url) throws Exception {
    if (dbf == null) {
      dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
    }
    DocumentBuilder db = dbf.newDocumentBuilder();
    return db.parse(url.toString());
  }

  protected static void copyInputStream(InputStream in, OutputStream out)
      throws Exception {
    byte[] buffer = new byte[4096];
    int len = in.read(buffer);
    while (len >= 0) {
      out.write(buffer, 0, len);
      len = in.read(buffer);
    }
  }

}
