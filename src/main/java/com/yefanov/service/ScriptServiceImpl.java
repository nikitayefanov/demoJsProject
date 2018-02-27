package com.yefanov.service;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.entities.ScriptStatus;
import com.yefanov.storage.ScriptStorage;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final Logger LOGGER = LoggerFactory.getLogger(ScriptServiceImpl.class);

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
        LOGGER.debug("Start executing script non-asynchronously");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        try (Writer stringWriter = new StringWriter()){
            OutputStream output = entity.getOutputStream() == null ? System.out : entity.getOutputStream();
            LOGGER.debug("OutputStream in ScriptEntity has been set");
            OutputStream outputStream = new TeeOutputStream(new WriterOutputStream(stringWriter, Charset.forName(CHARSET)), output);
            engine.getContext().setWriter(new OutputStreamWriter(outputStream, Charset.forName(CHARSET)));
            LOGGER.debug("OutputStream in ScriptEngine has been set");
            engine.eval(entity.getScript());
            LOGGER.debug("Script has been evaluated");
            String consoleOutput = stringWriter.toString();
            entity.setResult(consoleOutput);
            LOGGER.debug("Result has been set");
            entity.setStatus(ScriptStatus.DONE);
            LOGGER.debug("Status has been changed");
            LOGGER.debug("Return output");
            return stringWriter.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            entity.setStatus(ScriptStatus.COMPLETED_EXCEPTIONALLY);
            entity.setThrownException(e);
            LOGGER.debug("Return error message");
            return e.getMessage();
        }
    }

    @Async
    @Override
    public CompletableFuture<String> executeScriptAsync(ScriptEntity script) {
        LOGGER.debug("Start executing script asynchronously");
        CompletableFuture<String> result = new CompletableFuture<>();
        script.setFuture(result);
        result.complete(executeScript(script));
        LOGGER.debug("Return result");
        return result;
    }

    @Override
    public boolean cancelScript(int id) {
        LOGGER.debug("Trying to cancel script");
        ScriptEntity script = storage.getScript(id);
        if (script.getResult() != null) {
            LOGGER.debug("Script has result, can't be cancelled");
            LOGGER.debug("Return false");
            return false;
        } else {
            LOGGER.debug("Script has no result");
            CompletableFuture<String> future = script.getFuture();
            future.cancel(true);
            LOGGER.debug("Script has been cancelled");
            script.setStatus(ScriptStatus.CANCELLED);
            LOGGER.debug("Return true");
            return true;
        }
    }
}
