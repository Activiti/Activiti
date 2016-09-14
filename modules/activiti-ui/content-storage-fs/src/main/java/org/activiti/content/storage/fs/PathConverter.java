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
package org.activiti.content.storage.fs;

import java.io.File;
import java.math.BigInteger;

import org.activiti.content.storage.exception.ContentStorageException;

/**
 * Converts between a unique content index and a relative {@link File} path. Uses a nested tree of folders 
 * (depth based on iterationDepth) with a maximum number of children (based on blockSize). The leaves of the
 * trees will be the actual content files. This way, no folder will have more children than the 'blockSize' set,
 * keeping the folders balanced. 
 * 
 * @author Frederik Heremans
 */
public class PathConverter {

    public BigInteger blockSize = BigInteger.valueOf(1024L);
    public int iterationDepth = 4;
    public int blockSizeInt = 1024;
    
    /**
     * @return a path representing the content with the given unique content index.
     */
    public File getPathForId(BigInteger id) {
        BigInteger remainder = null;
        
        if(id.compareTo(BigInteger.ZERO) < 0) {
            throw new ContentStorageException("ID cannot be negative");
        }
        
        Long[] blocks = new Long[iterationDepth];
        for(int i=0; i< iterationDepth; i++) {
            remainder = id.remainder(blockSize);
            blocks[i] = remainder.longValue();
            id = id.subtract(remainder).divide(blockSize);
        }
        
        if(!id.equals(BigInteger.ZERO)) {
            throw new ContentStorageException("ID out of range of content storage limit: " + 
               blockSize.pow(iterationDepth).subtract(BigInteger.ONE));
        }
        
        StringBuffer buffer = new StringBuffer();
        for(int i=iterationDepth - 1; i>=0; i--) {
            buffer.append(blocks[i].toString()).append(File.separatorChar);
        }
        return new File(buffer.toString());
    }
    
    public BigInteger getIdForPath(File path) {
        BigInteger result = BigInteger.ZERO;
        BigInteger currentFactor = BigInteger.ONE;
        int depth = 0;
        File parent = path;
        
        while(parent != null && depth < iterationDepth) {
            try {
                result = result.add(new BigInteger(parent.getName()).multiply(currentFactor));
                
                // Move on to next iteration
                parent = parent.getParentFile();
                depth++;
                currentFactor = currentFactor.multiply(blockSize);
            } catch(NumberFormatException nfe) {
                throw new ContentStorageException("Illegal format of path segment: " + parent.getName(), nfe);
            }
        }
        
        return result;
    }
    
    public void setBlockSize(int blockSize) {
        this.blockSizeInt = blockSize;
        this.blockSize = BigInteger.valueOf(blockSize);
    }
    
    public int getBlockSize() {
        return blockSizeInt;
    }
    
    public void setIterationDepth(int iterationDepth) {
        this.iterationDepth = iterationDepth;
    }
    
    public int getIterationDepth() {
        return iterationDepth;
    }
}
