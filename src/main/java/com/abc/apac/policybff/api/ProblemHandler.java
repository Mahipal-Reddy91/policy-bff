package com.abc.apac.policybff.api;

import com.abc.apac.policybff.application.service.PolicyExceptions.DuplicatePolicyIds;
import com.abc.apac.policybff.application.service.PolicyExceptions.InvalidPolicyQuery;
import com.abc.apac.policybff.application.service.PolicyExceptions.PolicyIdsNotFound;
import com.abc.apac.policybff.application.service.PolicyExceptions.PolicyNotFound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class ProblemHandler {
	private static final Logger logger = LoggerFactory.getLogger(ProblemHandler.class);

	@ExceptionHandler(PolicyNotFound.class)
	public ResponseEntity<ProblemDetail> policyNotFound(PolicyNotFound exception, HttpServletRequest request) {
		return problem(HttpStatus.NOT_FOUND, "Policy not found", exception.getMessage(),
				"POLICY_NOT_FOUND", request);
	}

	@ExceptionHandler(PolicyIdsNotFound.class)
	public ResponseEntity<ProblemDetail> policyIdsNotFound(
			PolicyIdsNotFound exception, HttpServletRequest request) {
		ProblemDetail detail = createProblem(
				HttpStatus.NOT_FOUND, "Policy not found", exception.getMessage(),
				"POLICY_IDS_NOT_FOUND", request);
		detail.setProperty("missingIds", exception.missingIds());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
	}

	@ExceptionHandler({DuplicatePolicyIds.class, InvalidPolicyQuery.class})
	public ResponseEntity<ProblemDetail> invalidPolicyRequest(
			RuntimeException exception, HttpServletRequest request) {
		String code = exception instanceof DuplicatePolicyIds
				? "DUPLICATE_POLICY_IDS"
				: "VALIDATION_ERROR";
		ProblemDetail detail = createProblem(
				HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage(), code, request);
		if (exception instanceof InvalidPolicyQuery invalidQuery) {
			detail.setProperty("fieldErrors", List.of(Map.of(
					"field", invalidQuery.field(),
					"message", invalidQuery.getMessage())));
		}
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> validationFailure(
			MethodArgumentNotValidException exception, HttpServletRequest request) {
		ProblemDetail detail = createProblem(
				HttpStatus.BAD_REQUEST, "Invalid request",
				"The request contains one or more invalid fields.", "VALIDATION_ERROR", request);
		List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(ProblemHandler::fieldError)
				.toList();
		detail.setProperty("fieldErrors", errors);
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler({
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class,
			HttpMessageNotReadableException.class,
			ConstraintViolationException.class
	})
	public ResponseEntity<ProblemDetail> malformedRequest(
			Exception exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request",
				"The request contains an invalid parameter or body.",
				"VALIDATION_ERROR", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> unexpected(
			Exception exception, HttpServletRequest request) {
		logger.error("Unexpected request failure for {}", request.getRequestURI(), exception);
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
				"An unexpected error occurred.", "INTERNAL_ERROR", request);
	}

	private ResponseEntity<ProblemDetail> problem(
			HttpStatus status,
			String title,
			String message,
			String code,
			HttpServletRequest request) {
		return ResponseEntity.status(status).body(createProblem(status, title, message, code, request));
	}

	private ProblemDetail createProblem(
			HttpStatus status,
			String title,
			String message,
			String code,
			HttpServletRequest request) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
		detail.setType(URI.create("about:blank"));
		detail.setTitle(title);
		detail.setInstance(URI.create(request.getRequestURI()));
		detail.setProperty("code", code);
		detail.setProperty("traceId", traceId());
		return detail;
	}

	private static Map<String, String> fieldError(FieldError error) {
		return Map.of("field", error.getField(), "message",
				error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage());
	}

	private static String traceId() {
		String traceId = MDC.get("traceId");
		return traceId == null ? UUID.randomUUID().toString() : traceId;
	}
}
