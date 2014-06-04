package org.activiti.crystalball.simulator;

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


import java.util.Random;

/**
 * Simulation utils used in the simulation run and scenario. 
 * e.g. random number generator should be centralized for all sim runs.
 *
 * @author martin.grofcik
 */

public class SimUtils {
	/** main random number generator */
	  private volatile static ThreadLocal<Random> randomGenerator = new ThreadLocal<Random>();
	  
	  public static void setSeed(long seed) {
		  randomGenerator.set( new Random(seed));
	  }
	  
	  public static int getRandomInt(int max) {
		  if (randomGenerator.get() == null ) {
			  randomGenerator.set( new Random());
		  }
		  return randomGenerator.get().nextInt(max);
	  }
}
