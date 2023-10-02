//package com.esq.rbac.service.exception;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.support.DefaultMessageSourceResolvable;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.ObjectError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.stream.Stream;
//
//@RestControllerAdvice
//@Slf4j
//public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
//
//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
//        HashMap<Object, Object> errorMap = new HashMap<>();
//        List<String> errors = ex.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
//        errorMap.put("errors",errors);
//        log.error("error : : {}",errorMap);
////        return new ResponseEntity<Object>(errorMap, HttpStatus.BAD_REQUEST);
//        return super.handleMethodArgumentNotValid(ex, headers, status, request);
//    }
//
//    @ExceptionHandler({ErrorInfoException.class})
//    public ResponseEntity<String> customResponseThrowException(RuntimeException e){
//        String message = e.getMessage();
//        return new ResponseEntity<String>(message,HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}
