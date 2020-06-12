package com.hw.config;

import com.hw.aggregate.address.exception.AddressAccessException;
import com.hw.aggregate.address.exception.AddressNotExistException;
import com.hw.aggregate.address.exception.DuplicateAddressException;
import com.hw.aggregate.address.exception.MaxAddressCountException;
import com.hw.aggregate.cart.exception.CartItemAccessException;
import com.hw.aggregate.cart.exception.CartItemNotExistException;
import com.hw.aggregate.cart.exception.MaxCartItemException;
import com.hw.aggregate.order.exception.*;
import com.hw.aggregate.profile.exception.ProfileAccessException;
import com.hw.aggregate.profile.exception.ProfileAlreadyExistException;
import com.hw.aggregate.profile.exception.ProfileNotExistException;
import com.hw.shared.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.hw.shared.AppConstant.HTTP_HEADER_ERROR_ID;

@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DomainExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
            OrderAlreadyPaidException.class,
            OrderNotExistException.class,
            OrderPaymentMismatchException.class,
            ProductInfoValidationException.class,
            ProfileAlreadyExistException.class,
            ProfileNotExistException.class,
            CartItemNotExistException.class,
            MaxCartItemException.class,
            AddressNotExistException.class,
            DuplicateAddressException.class,
            MaxAddressCountException.class,
            OrderPersistenceException.class
    })
    protected ResponseEntity<Object> handle400Exception(RuntimeException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HTTP_HEADER_ERROR_ID, errorMessage.getErrorId());
        return handleExceptionInternal(ex, errorMessage, httpHeaders, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {
            ActualStorageDecreaseException.class,
            OrderStorageDecreaseException.class,
            OrderCreationUnknownException.class,
            PaymentQRLinkGenerationException.class,
            StateMachineCreationException.class,
    })
    protected ResponseEntity<Object> handle500Exception(RuntimeException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HTTP_HEADER_ERROR_ID, errorMessage.getErrorId());
        return handleExceptionInternal(ex, errorMessage, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {
            OrderAccessException.class,
            ProfileAccessException.class,
            CartItemAccessException.class,
            AddressAccessException.class,
    })
    protected ResponseEntity<Object> handle403Exception(RuntimeException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HTTP_HEADER_ERROR_ID, errorMessage.getErrorId());
        return handleExceptionInternal(ex, errorMessage, httpHeaders, HttpStatus.FORBIDDEN, request);
    }
}
