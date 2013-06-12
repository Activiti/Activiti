package org.activiti.engine.impl.email;

import org.apache.commons.mail.Email;

/**
 * @author Hariprasath Manivannan
 */
public abstract class AbstractEmailConfiguration {

  /**
   * Applies the email settings configuration onto the Email object
   */
  public abstract void apply(Email email);
}
