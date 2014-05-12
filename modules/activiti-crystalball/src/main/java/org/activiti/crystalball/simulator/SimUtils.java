package org.activiti.crystalball.simulator;

import java.util.Random;

/**
 * Simulation utils used in the simulation run and scenario. 
 * e.g. random number generator should be centralized for all sim runs.
 *
 * @author martin.grofcik
 */
@SuppressWarnings("UnusedDeclaration")
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
