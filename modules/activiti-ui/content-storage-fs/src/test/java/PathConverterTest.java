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
import java.io.File;
import java.math.BigInteger;

import org.activiti.content.storage.exception.ContentStorageException;
import org.activiti.content.storage.fs.PathConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test for conversion between paths and id/indexes.
 * @author Frederik Heremans
 */
public class PathConverterTest {

    private  PathConverter converter;
    
    @Before
    public  void setupConverter() {
        converter = new PathConverter();
        converter.setBlockSize(1024);
        converter.setIterationDepth(4);
    }
   
    @Test
    public void testIdToPath() throws Exception {
        BigInteger id = BigInteger.valueOf(12345678910L);
        File result = converter.getPathForId(id);
        checkPath(result, "11", "509", "775", "62");
        
        // Lower limit
        id = BigInteger.valueOf(0L);
        result = converter.getPathForId(id);
        checkPath(result, "0", "0", "0", "0");
        
        // Upper limit
        id = BigInteger.valueOf(1099511627775L);
        result = converter.getPathForId(id);
        checkPath(result, "1023", "1023", "1023", "1023");
        
        
        System.out.println(converter.getPathForId(BigInteger.valueOf(50)));
        System.out.println(converter.getPathForId(BigInteger.valueOf(20000)));
    }
    
    @Test
    public void testIdToPathBadRange() throws Exception {
        // Above upper limit
        BigInteger id = BigInteger.valueOf(1099511627776L);
        try {
            converter.getPathForId(id);
            Assert.fail("Exception expected");
        } catch(ContentStorageException expected) {
            // Expected
        }
        
        // below zero
        id = BigInteger.valueOf(-1L);
        try {
            converter.getPathForId(id);
            Assert.fail("Exception expected");
        } catch(ContentStorageException expected) {
            // Expected
        }
    }
    
    @Test
    public void testPathToId() throws Exception {
        File path =  createPath("1023", "1023", "1023", "1023");
        BigInteger result = converter.getIdForPath(path);
        Assert.assertEquals(BigInteger.valueOf(1099511627775L), result);
        
        path =  createPath("0", "0", "0", "0");
        result = converter.getIdForPath(path);
        Assert.assertEquals(BigInteger.ZERO, result);
        
        path =  createPath("11", "509", "775", "62");
        result = converter.getIdForPath(path);
        Assert.assertEquals(BigInteger.valueOf(12345678910L), result);
    }
    
    @Test
    public void testPathToIdSyntax() throws Exception {
        // Too deep
        File path =  createPath("abc", "1023", "1023", "0");
        try {
           converter.getIdForPath(path);
            Assert.fail("Exception expected");
        } catch(ContentStorageException expected) {
            // Expected
        }
    }
    
    protected void checkPath(File path, String... segments) {
        Assert.assertEquals((createPath(segments).getPath()), path.getPath());
    }
    
    protected File createPath(String... segments) {
        StringBuffer buffer = new StringBuffer();
        for(String s : segments) {
            buffer.append(s)
            .append(File.separator);
        }
        return new File(buffer.toString());
    }
}
