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


import org.activiti.engine.delegate.VariableScope;

/**
 * Allows to run simulation in debug mode
 *
 * @author martin.grofcik
 */
public interface SimulationDebugger {
  /**
   * initialize simulation run
   * @param execution - variable scope to transfer variables from and to simulation run
   */
  void init(VariableScope execution);

  /**
   * step one simulation event forward
   */
  void step();

  /**
   * continue in the simulation run
   */
  void runContinue();

  /**
   * execute simulation run till simulationTime
   */
  void runTo(long simulationTime);

  /**
   * execute simulation run till simulation event of the specific type
   */
  void runTo(String simulationEventType);

  /**
   * close simulation run
   */
  void close();
}
