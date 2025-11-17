package com.selimhorri.app.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.selimhorri.app.exception.payload.ExceptionMsg;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler {
	
	@ExceptionHandler(value = {
		MethodArgumentNotValidException.class,
		HttpMessageNotReadableException.class,
	})
	public <T extends BindException> ResponseEntity<ExceptionMsg> handleValidationException(final T e) {
		
		log.info("**ApiExceptionHandler controller, handle validation exception*\n");
		final var badRequest = HttpStatus.BAD_REQUEST;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("*" + e.getBindingResult().getFieldError().getDefaultMessage() + "!**")
					.httpStatus(badRequest)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), badRequest);
	}
	
	@ExceptionHandler(value = {
		FavouriteNotFoundException.class,
	})
	public <T extends RuntimeException> ResponseEntity<ExceptionMsg> handleApiRequestException(final T e) {
		
		log.info("**ApiExceptionHandler controller, handle API request*\n");
		final var badRequest = HttpStatus.BAD_REQUEST;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### " + e.getMessage() + "! ####")
					.httpStatus(badRequest)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), badRequest);
	}
	
	@ExceptionHandler(value = {
		DataIntegrityViolationException.class,
	})
	public ResponseEntity<ExceptionMsg> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
		
		log.info("**ApiExceptionHandler controller, handle data integrity violation*\n");
		final var conflict = HttpStatus.CONFLICT;
		
		// Verificar si es un error de clave duplicada
		String errorMessage = e.getMessage();
		if (errorMessage != null && (errorMessage.contains("Duplicate") || 
		                             errorMessage.contains("PRIMARY KEY") ||
		                             errorMessage.contains("unique constraint"))) {
			errorMessage = "Favourite already exists with the same userId, productId, and likeDate";
		} else {
			errorMessage = "Data integrity violation: " + (errorMessage != null ? errorMessage : "Unknown error");
		}
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### " + errorMessage + "! ####")
					.httpStatus(conflict)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), conflict);
	}
	
	
	
}
