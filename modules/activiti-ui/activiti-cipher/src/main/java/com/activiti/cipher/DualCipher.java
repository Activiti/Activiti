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
package com.activiti.cipher;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Class exposing a set of encryption and decryption cyphers, based on a set of unique keys both needed to construct.
 * 
 * @author Frederik Heremans
 */
public class DualCipher {
    
    private static final String AES_KEY = "AES";
    private static final String AES_CYPHER = "AES/CBC/PKCS5PADDING";
    private static final String UTF8_ENCODING = "UTF-8";
    
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    
    private String key;
    private String baseKey;
    
    /**
     * Creates a {@link DualCipher} based on existing keys, exposing Ciphers to encode/decode.
     */
    public DualCipher(String key, String baseKey) {
        this.baseKey = baseKey;
        this.key = key;
        
        byte[] baseKeyBytes = Base64.decodeBase64(baseKey.getBytes(Charset.forName(UTF8_ENCODING)));
        
        // First, create a cipher based on the base key
        Cipher baseCipher = createCipher(baseKeyBytes, false);
        try {
            byte[] decryptedBytes = baseCipher.doFinal(Base64.decodeBase64(key.getBytes(Charset.forName(UTF8_ENCODING))));
            encryptCipher = createCipher(decryptedBytes, true);
            decryptCipher = createCipher(decryptedBytes, false);
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException("Error while decrypting key", gse);
        }
    }
    
    /**
     * Creates a new {@link DualCipher} with a randomly generated baseKey and matching random key.
     */
    public DualCipher() {
        // Generate random key and baseKey
        byte[] keyBytes = new byte[32];
        new Random().nextBytes(keyBytes);
        
        byte[] baseBytes = new byte[32];
        new Random().nextBytes(baseBytes);
        
        encryptCipher = createCipher(keyBytes, true);
        decryptCipher = createCipher(keyBytes, false);
        Cipher baseCipher = createCipher(baseBytes, true);
        
        // Generate final key, based on the base cipher
        try {
            byte[] encryptedKey = baseCipher.doFinal(keyBytes);
            this.key = Base64.encodeBase64String(encryptedKey);
            this.baseKey = Base64.encodeBase64String(baseBytes);
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException("Error while generating base keys", gse);
        }
    }

    public Cipher getDecryptCipher() {
        return decryptCipher;
    }
    
    public Cipher getEncryptCipher() {
        return encryptCipher;
    }
    
    public String getBaseKey() {
        return baseKey;
    }
    
    public String getKey() {
        return key;
    }
    
    protected Cipher createCipher(byte[] keyBytes, boolean forEncryption) {
        
        if(keyBytes.length != 32) {
            throw new IllegalArgumentException("Key should be 32 bytes long (256bit)");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(keyBytes);
        byte[] baseKeyIv = new byte[16];
        byte[] baseKeySecret = new byte[16];
        
        buffer.get(baseKeyIv);
        buffer.get(baseKeySecret);
        
        IvParameterSpec initializationVectorSpec = new IvParameterSpec(baseKeyIv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(baseKeySecret, AES_KEY);
        
        Cipher result = null;
        try {
            result = Cipher.getInstance(AES_CYPHER);
            result.init(forEncryption ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, 
                    secretKeySpec, initializationVectorSpec);
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException("Error while creating cipher", gse);
        }
        return result;
    }
}
