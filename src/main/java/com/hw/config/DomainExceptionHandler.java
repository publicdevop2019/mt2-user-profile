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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
@Slf4j
@ControllerAdvice
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
            MaxAddressCountException.class
    })
    protected ResponseEntity<?> handle400Exception(RuntimeException ex, WebRequest request) {
        log.debug("will return 400");
        return handleExceptionInternal(ex, new ErrorMessage(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {
            ActualStorageDecreaseException.class,
            OrderStorageDecreaseException.class,
            PaymentQRLinkGenerationException.class,
    })
    protected ResponseEntity<?> handle500Exception(RuntimeException ex, WebRequest request) {
        log.debug("will return 500");
        return handleExceptionInternal(ex, new ErrorMessage(ex), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {
            OrderAccessException.class,
            ProfileAccessException.class,
            CartItemAccessException.class,
            AddressAccessException.class,
    })
    protected ResponseEntity<?> handle403Exception(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorMessage(ex), new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }
}
