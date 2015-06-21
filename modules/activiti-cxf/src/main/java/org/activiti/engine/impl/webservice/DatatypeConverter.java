package org.activiti.engine.impl.webservice;

import java.util.Calendar;
import java.util.Date;

/**
 * XSD <-> Java type converter
 * 
 * @author Christohe DENEUX - Linagora
 *
 */
public class DatatypeConverter {

    /**
     * <p>
     * Converts the string argument into a date ({@link Date}) value.
     * 
     * @param lexicalXSDDate
     *            A string containing lexical representation of xsd:date.
     * @return A date ({@link Date}) value represented by the string argument.
     * @throws IllegalArgumentException
     *             if string parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes
     *             for xsd:date.
     */
    public static java.util.Date parseDate(final String lexicalXSDDate) {
        final Calendar calendar = javax.xml.bind.DatatypeConverter.parseDate(lexicalXSDDate);
        return calendar.getTime();
    }

    /**
     * <p>
     * Converts a date ({@link Date}) value into a string.
     * 
     * @param val
     *            A date ({@link Date}) value
     * @return A string containing a lexical representation of xsd:date
     * @throws IllegalArgumentException
     *             if <tt>val</tt> is null.
     */
    public static String printDate(final java.util.Date val) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(val);
        return javax.xml.bind.DatatypeConverter.printDate(calendar);
    }

    /**
     * <p>
     * Converts the string argument into a time ({@link Date}) value.
     * 
     * @param lexicalXSDTime
     *            A string containing lexical representation of xsd:time.
     * @return A time ({@link Date}) value represented by the string argument.
     * @throws IllegalArgumentException
     *             if string parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes
     *             for xsd:time.
     */
    public static java.util.Date parseTime(final String lexicalXSDTime) {
        final Calendar calendar = javax.xml.bind.DatatypeConverter.parseTime(lexicalXSDTime);
        return calendar.getTime();
    }

    /**
     * <p>
     * Converts a time ({@link Date}) value into a string.
     * 
     * @param val
     *            A time ({@link Date}) value
     * @return A string containing a lexical representation of xsd:time
     * @throws IllegalArgumentException
     *             if <tt>val</tt> is null.
     */
    public static String printTime(final java.util.Date val) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(val);
        return javax.xml.bind.DatatypeConverter.printTime(calendar);
    }

    /**
     * <p>
     * Converts the string argument into a date/time ({@link Date}) value.
     * 
     * @param lexicalXSDDateTime
     *            A string containing lexical representation of xsd:dateTime.
     * @return A date/time ({@link Date}) value represented by the string argument.
     * @throws IllegalArgumentException
     *             if string parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes
     *             for xsd:date/Time.
     */
    public static java.util.Date parseDateTime(final String lexicalXSDDateTime) {
        final Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(lexicalXSDDateTime);
        return calendar.getTime();
    }

    /**
     * <p>
     * Converts a date/time ({@link Date}) value into a string.
     * 
     * @param val
     *            A date/time ({@link Date}) value
     * @return A string containing a lexical representation of xsd:dateTime
     * @throws IllegalArgumentException
     *             if <tt>val</tt> is null.
     */
    public static String printDateTime(final java.util.Date val) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(val);
        return javax.xml.bind.DatatypeConverter.printDateTime(calendar);
    }

}
