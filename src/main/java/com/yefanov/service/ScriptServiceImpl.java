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
import java.time.LocalTime;
import java.util.List;
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
    public List<ScriptEntity> getAllScriptEntities() {
        return storage.getAllScriptEntities();
    }

    @Override
    public ScriptEntity getScriptEntityById(int id) {
        return storage.getScript(id);
    }

    @Override
    public String executeScript(ScriptEntity entity) {
        LOGGER.debug("Start executing script with id {} non-asynchronously", entity.getId());
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        try (Writer stringWriter = new StringWriter()){
            OutputStream output = entity.getOutputStream() == null ? System.out : entity.getOutputStream();
            LOGGER.debug("OutputStream in ScriptEntity with id {} has been set", entity.getId());
            OutputStream outputStream = new TeeOutputStream(new WriterOutputStream(stringWriter, Charset.forName(CHARSET)), output);
            engine.getContext().setWriter(new OutputStreamWriter(outputStream, Charset.forName(CHARSET)));
            LOGGER.debug("OutputStream in ScriptEngine with id {} has been set", entity.getId());
            entity.setStartTime(LocalTime.now());
            engine.eval(entity.getScript());
            entity.setEndTime(LocalTime.now());
            LOGGER.debug("Script with id {} has been evaluated", entity.getId());
            String consoleOutput = stringWriter.toString();
            entity.setResult(consoleOutput);
            LOGGER.debug("Result in script with id {} has been set", entity.getId());
            entity.setStatus(ScriptStatus.DONE);
            LOGGER.debug("Status in script with id {} has been changed", entity.getId());
            LOGGER.debug("Return output of script with id {}", entity.getId());
            return stringWriter.toString();
        } catch (Exception e) {
            entity.setEndTime(LocalTime.now());
            LOGGER.error("Error during execution script with id {}, message of exception: {}", entity.getId(), e.getMessage());
            entity.setStatus(ScriptStatus.COMPLETED_EXCEPTIONALLY);
            entity.setThrownException(e);
            entity.setResult(e.getMessage());
            LOGGER.debug("Script with id {} execution has been finished with exception, return error message", entity.getId());
            return e.getMessage();
        }
    }

    @Async
    @Override
    public CompletableFuture<String> executeScriptAsync(ScriptEntity script) {
        LOGGER.debug("Start executing script with id {} asynchronously", script.getId());
        CompletableFuture<String> result = new CompletableFuture<>();
        script.setFuture(result);
        result.complete(executeScript(script));
        LOGGER.debug("Return result of executing script with id {}", script.getId());
        return result;
    }

    @Override
    public boolean cancelScript(int id) {
        LOGGER.debug("Trying to cancel script with id {}", id);
        ScriptEntity script = storage.getScript(id);
        if (script.getResult() != null) {
            LOGGER.debug("Script with id {} is completed, can't be cancelled", id);
            return false;
        } else if (script.getThread() != null) {
            LOGGER.debug("Thread, which is executing script with id {} has been stopped", id);
            script.getThread().stop();
            script.setEndTime(LocalTime.now());
            return true;
        } else {
            LOGGER.debug("Script with id {} has no result yet", id);
            CompletableFuture<String> future = script.getFuture();
            future.cancel(true);
            script.setEndTime(LocalTime.now());
            LOGGER.debug("Script with id {} has been cancelled", id);
            script.setStatus(ScriptStatus.CANCELLED);
            return true;
        }
    }
}
