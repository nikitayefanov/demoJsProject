package com.yefanov.service;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.entities.ScriptStatus;
import com.yefanov.storage.ScriptStorage;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Service
public class ScriptServiceImpl implements ScriptService {

    public static final String CHARSET = "UTF-8";
    public static final String ENGINE_NAME = "nashorn";

    @Autowired
    private ScriptStorage storage;

    @Override
    public ScriptEntity addScriptToStorage(String script) {
        return storage.addScript(script);
    }

    @Override
    public ScriptEntity getScriptEntityById(int id) {
        return storage.getScript(id);
    }

    @Override
    public String executeScript(ScriptEntity entity) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        try (Writer stringWriter = new StringWriter()){
            OutputStream output = entity.getOutputStream() == null ? System.out : entity.getOutputStream();
//            OutputStream output = entity.getOutputStream();
            OutputStream outputStream = new TeeOutputStream(new WriterOutputStream(stringWriter, Charset.forName(CHARSET)), output);
            engine.getContext().setWriter(new OutputStreamWriter(outputStream, Charset.forName(CHARSET)));
            engine.eval(entity.getScript());
            String consoleOutput = stringWriter.toString();
            entity.setResult(consoleOutput);
            entity.setStatus(ScriptStatus.DONE);
            return stringWriter.toString();
        } catch (Exception e) {
            entity.setStatus(ScriptStatus.COMPLETED_EXCEPTIONALLY);
            entity.setThrownException(e);
            return e.getMessage();
        }
    }

    @Async
    @Override
    public CompletableFuture<String> executeScriptAsync(ScriptEntity script) {
        CompletableFuture<String> result = new CompletableFuture<>();
        script.setFuture(result);
        result.complete(executeScript(script));
        return result;
    }

    @Override
    public boolean cancelScript(int id) {
        ScriptEntity script = storage.getScript(id);
        if (script.getResult() != null) {
            return false;
        } else {
            CompletableFuture<String> future = script.getFuture();
            future.cancel(true);
            script.setStatus(ScriptStatus.CANCELLED);
            return true;
        }
    }
}
