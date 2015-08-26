/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
import java.io.File;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.activiti.content.storage.exception.ContentStorageException;
import com.activiti.content.storage.fs.PathConverter;


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
