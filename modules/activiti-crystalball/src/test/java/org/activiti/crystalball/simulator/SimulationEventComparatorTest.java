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


import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;

/**
 * @author martin.grofcik
 */
public class SimulationEventComparatorTest {

  Comparator<SimulationEvent> comparator = new SimulationEventComparator();

  @Test
  public void testCompare() throws Exception {
    SimulationEvent event1_0 = new SimulationEvent.Builder("type").simulationTime(0).build();
    SimulationEvent eq1_0 = new SimulationEvent.Builder("type").simulationTime(0).build();
    SimulationEvent event1_1 = new SimulationEvent.Builder("type").simulationTime(0).priority(1).build();
    SimulationEvent event2 = new SimulationEvent.Builder("type").simulationTime(1).build();
    assertEquals(0, comparator.compare(event1_0, eq1_0));
    assertEquals(-1, comparator.compare(event1_0, event2));
    assertEquals(1, comparator.compare(event2, event1_0));
    assertEquals(-1, comparator.compare(event1_0, event1_1));
    assertEquals(1, comparator.compare(event1_1, event1_0));
    assertEquals(-1, comparator.compare(event1_1, event2));
    assertEquals(1, comparator.compare(event2, event1_1));
  }
}
