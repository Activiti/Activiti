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

import java.io.OutputStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

/**
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class BarTransformer {

    public static void transform(URL url, OutputStream os) throws Exception {
        // Heuristicly retrieve name and version
        String name = url.getPath();
        int idx = name.lastIndexOf('/');
        if (idx >= 0) {
            name = name.substring(idx + 1);
        }
        String[] str = extractNameVersionType(name);
        // Build the list of folders containing resources
        String pathHeader;
        JarInputStream jis = new JarInputStream(url.openStream());
        try {
            Set<String> paths = new TreeSet<String>();
            ZipEntry e;
            while ((e = jis.getNextEntry()) != null) {
                String n = e.getName();
                int i = n.lastIndexOf('/');
                if (-1 == i) {// Add root path if the .bpmn20.xml is in the root of the bar file and the value is example.bpmn20.xml
                    paths.add("/"); //Extender#checkBundle calls the HeaderParser#parseHeader and it does not parse an empty string
                } else if (i < n.length() - 1) {
                    paths.add(n.substring(0, i + 1));
                }
            }
            StringBuilder sb = new StringBuilder();
            for (String s : paths) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(s);
            }
            pathHeader = sb.toString();
        } finally {
            jis.close();
        }
        // Build the stream
        jis = new JarInputStream(url.openStream());
        try {
            JarOutputStream jos = new JarOutputStream(os);
            jos.setLevel(Deflater.NO_COMPRESSION);
            // Transform manifest
            Manifest m = jis.getManifest();
            if (m == null) {
                m = new Manifest();
                m.getMainAttributes().putValue("Manifest-Version", "2");
            }
            if (m.getMainAttributes().getValue(BUNDLE_MANIFESTVERSION) == null) {
                m.getMainAttributes().putValue(BUNDLE_MANIFESTVERSION, "2");
            }
            if (m.getMainAttributes().getValue(BUNDLE_SYMBOLICNAME) == null) {
                m.getMainAttributes().putValue(BUNDLE_SYMBOLICNAME, str[0]);
            }
            if (m.getMainAttributes().getValue(BUNDLE_VERSION) == null) {
                m.getMainAttributes().putValue(BUNDLE_VERSION, str[1]);
            }
            m.getMainAttributes().putValue(BUNDLE_ACTIVITI_HEADER, pathHeader);
            // Write manifest
            ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
            jos.putNextEntry(e);
            m.write(jos);
            jos.closeEntry();
            // Write all entries
            byte[] readBuffer = new byte[8192];
            while ((e = jis.getNextEntry()) != null) {
                ZipEntry e2 = new ZipEntry(e.getName());
//                e2.setMethod(ZipEntry.STORED);
//                e2.setSize(e.getSize());
//                e2.setCrc(e.getCrc());
                jos.putNextEntry(e2);
                int bytesIn = jis.read(readBuffer);
                while (bytesIn != -1)
                {
                    jos.write(readBuffer, 0, bytesIn);
                    bytesIn = jis.read(readBuffer);
                }
                jos.closeEntry();
            }
            jos.finish();
            jos.flush();
        } finally {
            jis.close();
        }
    }

    private static final String DEFAULT_VERSION = "0.0.0";

    private static final Pattern ARTIFACT_MATCHER = Pattern.compile("(.+)(?:-(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:[^a-zA-Z0-9](.*))?)(?:\\.([^\\.]+))", Pattern.DOTALL);
    private static final Pattern FUZZY_MODIFIDER = Pattern.compile("(?:\\d+[.-])*(.*)", Pattern.DOTALL);

    public static String[] extractNameVersionType(String url) {
        Matcher m = ARTIFACT_MATCHER.matcher(url);
        if (!m.matches()) {
            return new String[] { url, DEFAULT_VERSION };
        }
        else {
            //System.err.println(m.groupCount());
            //for (int i = 1; i <= m.groupCount(); i++) {
            //    System.err.println("Group " + i + ": " + m.group(i));
            //}

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
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-') {
                result.append(c);
            }
        }
    }

}
