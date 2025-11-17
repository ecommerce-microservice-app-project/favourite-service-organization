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

	
				ExceptionHandler(value = {
				MethodArgumentNotValidException.class,
		HttpMessageNotReadableException.class,
	})

		
		log.info("**ApiExceptionHandler controller, ha

		
		return new ResponseEntit
							xceptionMsg.builder()
							.msg("*" + e.getBindin
							.httpStatus(badRequest)
									imestamp(ZonedDateTime
									.now(Z
				
		

	
			
				ExceptionHandler(value = {
			FavouriteNotFoundException.class,
	})

		
		log.info("**ApiExceptionHandler controller, ha

		
		return new ResponseEntit
						xceptionMsg.builder()
							.msg("#### " + e.getMe
							.httpStatus(badRequest)
								timestamp(ZonedDateTime
									.now(Z
				neId.systemDefault()))
		

	
							.now(ZoneId.systemDefault()))
					.build(), conflict);
	}
	
	
	
}
