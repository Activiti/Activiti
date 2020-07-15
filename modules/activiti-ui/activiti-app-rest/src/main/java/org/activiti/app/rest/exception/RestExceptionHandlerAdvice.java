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
package org.activiti.app.rest.exception;

import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.BaseModelerRestException;
import org.activiti.app.service.exception.ConflictingRequestException;
import org.activiti.app.service.exception.ErrorInfo;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.app.service.exception.NonJsonResourceNotFoundException;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.app.service.exception.UnauthorizedException;
import org.activiti.engine.identity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * From http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
 * 
 * @author Joram Barrez
 */
@ControllerAdvice
public class RestExceptionHandlerAdvice {

  private static final String UNAUTHORIZED_MESSAGE_KEY = "GENERAL.ERROR.UNAUTHORIZED";
  private static final String NOT_FOUND_MESSAGE_KEY = "GENERAL.ERROR.NOT-FOUND";
  private static final String BAD_REQUEST_MESSAGE_KEY = "GENERAL.ERROR.BAD-REQUEST";
  private static final String INTERNAL_SERVER_ERROR_MESSAGE_KEY = "GENERAL.ERROR.INTERNAL-SERVER_ERROR";
  private static final String FORBIDDEN_MESSAGE_KEY = "GENERAL.ERROR.FORBIDDEN";
  private static final String INACTIVE_USER_MESSAGE_KEY = "GENERAL.ERROR.INACTIVE_USER";
  private static final String UPLOAD_LIMIT_EXCEEDED = "GENERAL.ERROR.UPLOAD-LIMIT-EXCEEDED";
  private static final String UPLOAD_LIMIT_EXCEEDED_TRIAL_USER = "GENERAL.ERROR.UPLOAD-LIMIT-EXCEEDED-TRIAL-USER";
  private static final String QUOTA_EXCEEDED_PREFIX = "GENERAL.ERROR.QUOTA-EXCEEDED-";

  @ResponseStatus(HttpStatus.NOT_FOUND) // 404
  @ExceptionHandler(NotFoundException.class)
  @ResponseBody
  public ErrorInfo handleNotFound(NotFoundException e) {
    return createInfoFromException(e, NOT_FOUND_MESSAGE_KEY);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND) // 404
  @ExceptionHandler(NonJsonResourceNotFoundException.class)
  public void handleNonJsonResourceNotFound(NonJsonResourceNotFoundException e) {
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
  @ExceptionHandler(BadRequestException.class)
  @ResponseBody
  public ErrorInfo handleBadRequest(BadRequestException e) {
    return createInfoFromException(e, BAD_REQUEST_MESSAGE_KEY);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
  @ExceptionHandler(InternalServerErrorException.class)
  @ResponseBody
  public ErrorInfo handleInternalServerError(InternalServerErrorException e) {
    return createInfoFromException(e, INTERNAL_SERVER_ERROR_MESSAGE_KEY);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN) // 403
  @ExceptionHandler(NotPermittedException.class)
  @ResponseBody
  public ErrorInfo handleNoPermission(NotPermittedException e) {
    return createInfoFromException(e, FORBIDDEN_MESSAGE_KEY);
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
  @ExceptionHandler(LockedException.class)
  @ResponseBody
  public ErrorInfo handleLockedUser(LockedException e) {
    ErrorInfo result = new ErrorInfo(e.getMessage());
    result.setMessageKey(INACTIVE_USER_MESSAGE_KEY);
    return result;
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseBody
  public ErrorInfo handleUnauthorized(UnauthorizedException e) {
    return createInfoFromException(e, UNAUTHORIZED_MESSAGE_KEY);
  }

  @ResponseStatus(HttpStatus.CONFLICT) // 409
  @ExceptionHandler(ConflictingRequestException.class)
  @ResponseBody
  public ErrorInfo handleConflict(ConflictingRequestException e) {
    return createInfoFromException(e, BAD_REQUEST_MESSAGE_KEY);
  }

  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE) // 413
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseBody
  public ErrorInfo handleMaxFileSizeExceeded(MaxUploadSizeExceededException musee) {
    ErrorInfo errorInfo = new ErrorInfo("Maximum upload size exceeded");
    User currentUser = SecurityUtils.getCurrentUserObject();

    // TODO: SPECIFIC MESSAGE OPENS SOURCE?
    errorInfo.setMessageKey(UPLOAD_LIMIT_EXCEEDED_TRIAL_USER);
    errorInfo.addParameter("quota", musee.getMaxUploadSize());
    return errorInfo;
  }

  protected ErrorInfo createInfoFromException(BaseModelerRestException exception, String defaultMessageKey) {
    ErrorInfo result = null;
    result = new ErrorInfo(exception.getMessage());
    if (exception.getCustomData() != null) {
      result.setCustomData(exception.getCustomData());
    }
    if (exception.getMessageKey() != null) {
      result.setMessageKey(exception.getMessageKey());
    } else {
      result.setMessageKey(defaultMessageKey);
    }
    return result;
  }

  protected String getSafeMessageKey(String fragment) {
    if (fragment != null) {
      return fragment.toUpperCase();
    }
    return "";
  }

}
