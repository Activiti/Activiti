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

package org.activiti.engine.repository;

import java.util.List;


/**
 * Stores waypoints of a diagram edge.
 *
 * @author Falko Menge
 */
public class DiagramEdge extends DiagramElement {

  private static final long serialVersionUID = 1L;
  
  private List<DiagramEdgeWaypoint> waypoints;

  public DiagramEdge() {
  }

  public DiagramEdge(String id, List<DiagramEdgeWaypoint> waypoints) {
    super(id);
    this.waypoints = waypoints;
  }

  @Override
  public boolean isNode() {
    return false;
  }

  @Override
  public boolean isEdge() {
    return true;
  }

  public List<DiagramEdgeWaypoint> getWaypoints() {
    return waypoints;
  }

  public void setWaypoints(List<DiagramEdgeWaypoint> waypoints) {
    this.waypoints = waypoints;
  }

}
