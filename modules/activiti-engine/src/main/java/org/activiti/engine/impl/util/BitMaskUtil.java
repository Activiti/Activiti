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

package org.activiti.engine.impl.util;


/**
 * Util class for manipulating bit-flag in ints.
 * 
 * Currently, only 8-bits are supporten, but can be extended to use all
 * 31 bits in the integer (1st of 32 bits is used for sign).
 * 
 * @author Frederik Heremans
 */
public class BitMaskUtil {
  
  // First 8 masks as constant to prevent having to math.pow() every time a bit needs flippin'.
  
  private static final int FLAG_BIT_1 = 1;    // 000...00000001
  private static final int FLAG_BIT_2 = 2;    // 000...00000010
  private static final int FLAG_BIT_3 = 4;    // 000...00000100
  private static final int FLAG_BIT_4 = 8;    // 000...00001000
  private static final int FLAG_BIT_5 = 16;   // 000...00010000
  private static final int FLAG_BIT_6 = 32;   // 000...00100000
  private static final int FLAG_BIT_7 = 64;   // 000...01000000
  private static final int FLAG_BIT_8 = 128;  // 000...10000000
  
  private static int[] MASKS = {FLAG_BIT_1, FLAG_BIT_2, FLAG_BIT_3, FLAG_BIT_4, FLAG_BIT_5, FLAG_BIT_6, FLAG_BIT_7, FLAG_BIT_8};
  
  /**
   * Set bit to '1' in the given int.
   * @param current integer value
   * @param bitNumber number of the bit to set to '1' (right first bit starting at 1).
   */
  public static int setBitOn(int value, int bitNumber) {
    if(bitNumber <= 0 && bitNumber > 8) {
      throw new IllegalArgumentException("Only bits 1 htrough 8 are supported");
    }
    
    // To turn on, OR with the correct mask
    return value | MASKS[bitNumber - 1];
  }
  
  /**
   * Set bit to '0' in the given int.
   * @param current integer value
   * @param bitNumber number of the bit to set to '0' (right first bit starting at 1).
   */
  public static int setBitOff(int value, int bitNumber) {
    if(bitNumber <= 0 && bitNumber > 8) {
      throw new IllegalArgumentException("Only bits 1 htrough 8 are supported");
    }
    
    // To turn on, OR with the correct mask
    return value &~MASKS[bitNumber - 1];
  }
  
  /**
   * Check if the bit is set to '1'
   * @param value integer to check bit
   * @param number of bit to check (right first bit starting at 1)
   */
  public static boolean isBitOn(int value, int bitNumber) {
    if(bitNumber <= 0 && bitNumber > 8) {
      throw new IllegalArgumentException("Only bits 1 htrough 8 are supported");
    }
    
    return ((value & MASKS[bitNumber - 1]) == MASKS[bitNumber - 1]);
  }
  
  /**
   * Set bit to '0' or '1' in the given int.
   * @param current integer value
   * @param bitNumber number of the bit to set to '0' or '1' (right first bit starting at 1).
   * @param bitValue if true, bit set to '1'. If false, '0'.
   */
  public static int setBit(int value, int bitNumber, boolean bitValue)
  {
    if(bitValue) {
      return setBitOn(value, bitNumber);
    } else {
      return setBitOff(value, bitNumber);
    }
  }
}
