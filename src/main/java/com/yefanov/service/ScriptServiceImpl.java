package com.yefanov.service;

import com.yefanov.storage.ScriptStorage;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Service
public class ScriptServiceImpl implements ScriptService {

    @Autowired
    private ScriptStorage storage;

    @Override
    public String executeScript(String script) throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        PrintStream console = System.out;
        try (Writer stringWriter = new StringWriter()){
            OutputStream output = new TeeOutputStream(console, new WriterOutputStream(stringWriter, Charset.defaultCharset()));
            engine.getContext().setWriter(new OutputStreamWriter(output));
            engine.eval(script);
            return stringWriter.toString();
        }
    }

    @Async
    @Override
    public CompletableFuture<String> executeScriptAsync(String script) {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            result.complete(executeScript(script));
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    @Override
    public HttpStatus cancelScript(long id) {
        Object script;
        try {
            script = storage.getScript(id);
        } catch (IndexOutOfBoundsException e) {
            return HttpStatus.NOT_FOUND;
        }
        if (script instanceof String) {
            return HttpStatus.NOT_ACCEPTABLE;
        } else if (script instanceof CompletableFuture) {
            CompletableFuture<String> future = (CompletableFuture<String>) script;
            future.cancel(true);
            return HttpStatus.OK;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
