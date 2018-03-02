package com.yefanov.exceptionHandlers;

import com.yefanov.handlings.ScriptNotFoundHandling;
import com.yefanov.handlings.ScriptParsingHandling;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class ExceptionHandling implements ProblemHandling, ScriptNotFoundHandling, ScriptParsingHandling {

}
