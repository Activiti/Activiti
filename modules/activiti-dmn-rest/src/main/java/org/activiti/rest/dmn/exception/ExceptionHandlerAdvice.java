package org.activiti.rest.dmn.exception;

import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * From http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
 * 
 * @author Yvo Swillens
 */
@ControllerAdvice
public class ExceptionHandlerAdvice {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE) // 415
  @ExceptionHandler(ActivitiDmnContentNotSupportedException.class)
  @ResponseBody
  public ErrorInfo handleNotSupported(ActivitiDmnContentNotSupportedException e) {
    return new ErrorInfo("Content is not supported", e);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  // 409
  @ExceptionHandler(ActivitiDmnConflictException.class)
  @ResponseBody
  public ErrorInfo handleConflict(ActivitiDmnConflictException e) {
    return new ErrorInfo("Conflict", e);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  // 404
  @ExceptionHandler(ActivitiDmnObjectNotFoundException.class)
  @ResponseBody
  public ErrorInfo handleNotFound(ActivitiDmnObjectNotFoundException e) {
    return new ErrorInfo("Not found", e);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  // 403
  @ExceptionHandler(ActivitiDmnForbiddenException.class)
  @ResponseBody
  public ErrorInfo handleForbidden(ActivitiDmnForbiddenException e) {
    return new ErrorInfo("Forbidden", e);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  // 400
  @ExceptionHandler(ActivitiDmnIllegalArgumentException.class)
  @ResponseBody
  public ErrorInfo handleIllegal(ActivitiDmnIllegalArgumentException e) {
    return new ErrorInfo("Bad request", e);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
  @ExceptionHandler(HttpMessageConversionException.class)
  @ResponseBody
  public ErrorInfo handleBadMessageConversion(HttpMessageConversionException e) {
    return new ErrorInfo("Bad request", e);
  }

  // Fall back

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ErrorInfo handleOtherException(Exception e) {
    LOGGER.error("Unhandled exception", e);
    return new ErrorInfo("Internal server error", e);
  }

}
