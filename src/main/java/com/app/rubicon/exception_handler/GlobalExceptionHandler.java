package com.app.rubicon.exception_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.app.rubicon.dto.ErrorResponse;

//***********Centralised Exception Handling****************

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	// handle any exception
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleAnyException(Exception exc, WebRequest request) {
		// System.out.println("in exc handle");
		ErrorResponse errResp = new ErrorResponse(exc.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errResp, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
