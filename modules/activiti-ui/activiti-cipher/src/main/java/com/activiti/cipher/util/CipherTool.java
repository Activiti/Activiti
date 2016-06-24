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
package com.activiti.cipher.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.crypto.CipherOutputStream;

import org.apache.commons.io.IOUtils;

import com.activiti.cipher.DualCipher;

/**
 * Simple tool to encrypt a file using a fresh random {@link DualCipher}.
 * Encrypted file will be written to the file-system, along with a set of keys
 * needed for creating a {@link DualCipher} to decrypt.
 * 
 * <br>
 * 
 * Usage: pass in absolute path to the file that needs encryption as
 * command-line argument. Encrypted file will be created in the same folder as
 * the original file and will remain unaffected. 2 additional files are created
 * containing both keys needed to construct a {@link DualCipher} later on.
 * 
 * @author Frederik Heremans
 */
public class CipherTool {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No file path given as argument");
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + args[0]);
        }

        // Create new random DualCipher
        DualCipher cipher = new DualCipher();
        
        // Store key and base key in 2 separate files
        FileOutputStream keyFileOut = null;
        FileOutputStream baseKeyFileOut = null;
        
        File parent = file.getParentFile();
        try {
            File keyFile = new File(parent, "key");
            keyFile.createNewFile();
            keyFileOut = new FileOutputStream(keyFile);
            PrintWriter writer = new PrintWriter(keyFile);
            writer.write(cipher.getKey());
            writer.flush();
            writer.close();

            File baseKeyFile = new File(parent, "javakey");
            baseKeyFile.createNewFile();
            writer = new PrintWriter(baseKeyFile);
            writer.write(cipher.getBaseKey());
            writer.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Error while writing key files", ioe);
        } finally {
            IOUtils.closeQuietly(keyFileOut);
            IOUtils.closeQuietly(baseKeyFileOut);
        }
        
        // Encrypt the input file
        FileOutputStream finalStream = null;
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);
            File finalFile = new File(parent, file.getName() + ".encrypted");
            finalFile.createNewFile();
            finalStream = new FileOutputStream(finalFile);

            // Actually write to the cipher-aware stream
            CipherOutputStream finalOut = new CipherOutputStream(finalStream, cipher.getEncryptCipher());
            IOUtils.copy(fileStream, finalOut);
            finalOut.flush();
            finalOut.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Error while writing encrypted files", ioe);
        } finally {
            IOUtils.closeQuietly(finalStream);
        }
    }

}
