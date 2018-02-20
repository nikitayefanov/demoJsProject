package com.yefanov.service;

import org.springframework.http.HttpStatus;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface ScriptService {

    String executeScript(String script) throws ScriptException, IOException, Exception;

    CompletableFuture<String> executeScriptAsync(String script);

    HttpStatus cancelScript(long id);

}
