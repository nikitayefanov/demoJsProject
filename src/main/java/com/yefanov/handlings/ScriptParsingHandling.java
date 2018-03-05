package com.yefanov.handlings;

import com.yefanov.exceptions.ScriptParsingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.AdviceTrait;

/**
 * Handling for ScriptParsingException
 * @see ScriptParsingException
 */
public interface ScriptParsingHandling extends AdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem> handleScriptNotFound(final ScriptParsingException exception, final NativeWebRequest request) {
        return create(Status.BAD_REQUEST, exception, request);
    }
}
