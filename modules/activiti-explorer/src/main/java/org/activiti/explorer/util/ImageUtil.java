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

package org.activiti.explorer.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.activiti.explorer.Constants;

import com.thebuzzmedia.imgscalr.Scalr;
import com.thebuzzmedia.imgscalr.Scalr.Mode;


/**
 * @author Joram Barrez
 */
public class ImageUtil {
  
  protected static final Logger LOGGER = Logger.getLogger(ImageUtil.class.getName());

  /**
   * Resizes the given image (passed as {@link InputStream}.
   * If the image is smaller then the given maximum width or height, the image
   * will be proportionally resized.
   */
  public static InputStream smallify(InputStream imageInputStream, String mimeType, int maxWidth, int maxHeight) {
    try {
      BufferedImage image = ImageIO.read(imageInputStream);
      
      int width = Math.min(image.getWidth(), maxWidth);
      int height = Math.min(image.getHeight(), maxHeight);
      
      Mode mode = Mode.AUTOMATIC;
      if (image.getHeight() > maxHeight) {
        mode = Mode.FIT_TO_HEIGHT;
      }
      
      if (width != image.getWidth() || height != image.getHeight()) {
        image = Scalr.resize(image, mode, width, height);
      }
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ImageIO.write(image, Constants.MIMETYPE_EXTENSION_MAPPING.get(mimeType), bos);
      return new ByteArrayInputStream(bos.toByteArray());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Exception while resizing image", e);
      return null;
    }
  }
  
}
