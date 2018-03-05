package com.yefanov.handlings;

import com.yefanov.exceptions.ScriptNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.AdviceTrait;

/**
 * Handling for ScriptNotFoundException
 * @see ScriptNotFoundException
 */
public interface ScriptNotFoundHandling extends AdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem> handleScriptNotFound(final ScriptNotFoundException exception, final NativeWebRequest request) {
        return create(Status.NOT_FOUND, exception, request);
    }
}
