package com.behl;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

class UnacceptableAnswerException extends ErrorResponseException {

    public UnacceptableAnswerException() {
        super(HttpStatus.UNPROCESSABLE_ENTITY);
    }

}