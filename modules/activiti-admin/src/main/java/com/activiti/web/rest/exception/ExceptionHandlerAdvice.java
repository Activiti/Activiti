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
package com.activiti.web.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * From http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
 * 
 * @author Joram Barrez
 */
@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)  // 404
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ErrorInfo handleNotFound(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ErrorInfo handleBadRequest(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseBody
    public ErrorInfo handleInternalServerError(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.UNAUTHORIZED)  // 401
    @ExceptionHandler(NotPermittedException.class)
    @ResponseBody
    public ErrorInfo handleNotPermittedException(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(ConflictException.class)
    @ResponseBody
    public ErrorInfo handleConflict(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
}
