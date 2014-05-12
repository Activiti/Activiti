package org.activiti.crystalball.simulator;

import org.activiti.engine.ActivitiException;

/**
 * @author martin.grofcik
 */
public class CrystalballException extends ActivitiException {

	public CrystalballException(String msg) {
		super(msg);
	}

  @SuppressWarnings("UnusedDeclaration")
  public CrystalballException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
