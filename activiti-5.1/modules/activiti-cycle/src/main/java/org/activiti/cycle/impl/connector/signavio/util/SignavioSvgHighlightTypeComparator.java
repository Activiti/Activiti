package org.activiti.cycle.impl.connector.signavio.util;

import java.util.Comparator;

public class SignavioSvgHighlightTypeComparator implements Comparator<SignavioSvgHighlightType> {

  public int compare(SignavioSvgHighlightType o1, SignavioSvgHighlightType o2) {
    if (o1.equals(o2))
      return 0;
    switch (o1) {
    case INFO:
      return -1;
    case WARNING:
      return 0;
    case ERROR:
      return 1;
    default:
      return 0;
    }

  }

}