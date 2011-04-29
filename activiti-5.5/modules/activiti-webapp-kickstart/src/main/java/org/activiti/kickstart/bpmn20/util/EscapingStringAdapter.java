package org.activiti.kickstart.bpmn20.util;

import java.util.HashSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Sven Wagner-Boysen
 * 
 *         Removes XML-invalid string sequences.
 * 
 */
public class EscapingStringAdapter extends XmlAdapter<String, String> {

//	public static final char substitute = '\uFFFD';
	private static final HashSet<Character> illegalChars;

	static {
		final String escapeString = "\u0000\u0001\u0002\u0003\u0004\u0005"
				+ "\u0006\u0007\u0008\u000B\u000C\u000E\u000F\u0010\u0011\u0012"
				+ "\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C"
				+ "\u001D\u001E\u001F\uFFFE\uFFFF";

		illegalChars = new HashSet<Character>();
		for (int i = 0; i < escapeString.length(); i++) {
			illegalChars.add(escapeString.charAt(i));
		}
	}

	private boolean isIllegal(char c) {
		return illegalChars.contains(c);
	}

	/**
	 * Deletes all illegal characters in the given string. If no illegal characters were
	 * found, no copy is made and the given string is returned.
	 * 
	 * @param string
	 * @return
	 */
	private String escapeCharacters(String string) {
		if(string == null) {
			return string;
		}
		
		StringBuffer copyBuffer = null;
		boolean copied = false;
		for (int i = 0; i < string.length(); i++) {
			if (isIllegal(string.charAt(i))) {
				if (!copied) {
					copyBuffer = new StringBuffer(string);
					copied = true;
				}
				copyBuffer.deleteCharAt(i);
			}
		}
		return copied ? copyBuffer.toString() : string;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	// @Override
	public String marshal(String text) throws Exception {
		return escapeCharacters(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	// @Override
	public String unmarshal(String text) throws Exception {
		return text;
	}

}
