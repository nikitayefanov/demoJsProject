package com.yefanov.service;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.entities.ScriptStatus;
import com.yefanov.exceptions.ScriptParsingException;
import com.yefanov.storage.ScriptStorage;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of ScriptService interface
 * @see ScriptService
 */
@Service
public class ScriptServiceImpl implements ScriptService {

    private static final String CHARSET = "UTF-8";
    private static final String ENGINE_NAME = "nashorn";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptServiceImpl.class);

    @Autowired
    private ScriptStorage storage;

    /**
     * @param script script text
     * @return ScriptEntity
     * @see ScriptEntity
     */
    @Override
    public ScriptEntity create(String script) {
        LOGGER.debug("Start compiling script");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        Compilable compilable = (Compilable) engine;
        CompiledScript compiledScript;
        try {
            compiledScript = compilable.compile(script);
        } catch (ScriptException e) {
            LOGGER.error("Script isn't valid, exception thrown");
            throw new ScriptParsingException(e);
        }
        LOGGER.debug("Script has been compiled");
        ScriptEntity entity = storage.addScript(script);
        entity.setCompiledScript(compiledScript);
        LOGGER.debug("Script with id {} has been created", entity.getId());
        return entity;
    }

    /**
     * @return all scripts
     */
    @Override
    public List<ScriptEntity> getAllScriptEntities() {
        List<ScriptEntity> allScriptEntities = storage.getAllScriptEntities();
        for (ScriptEntity s : allScriptEntities) {
            s.setResult(s.getResultWriter().toString());
        }
        return allScriptEntities;
    }

    /**
     * @param id script id
     * @return ScriptEntity from storage
     */
    @Override
    public ScriptEntity getScriptEntityById(int id) {
        return storage.getScript(id);
    }

    /**
     * @param entity script entity to execute
     * @return output of transmitted script
     */
    @Override
    public String executeScript(ScriptEntity entity) {
        LOGGER.debug("Start executing script with id {} non-asynchronously", entity.getId());
        entity.setThread(Thread.currentThread());
        try {
            OutputStream output = entity.getOutputStream() == null ? System.out : entity.getOutputStream();
            LOGGER.debug("OutputStream in ScriptEntity with id {} has been set", entity.getId());
            OutputStream outputStream = new TeeOutputStream(new WriterOutputStream(entity.getResultWriter(), Charset.forName(CHARSET)), output);
            entity.getCompiledScript().getEngine().getContext().setWriter(new OutputStreamWriter(outputStream, Charset.forName(CHARSET)));
            LOGGER.debug("OutputStream in ScriptEngine with id {} has been set", entity.getId());
            entity.setStartTime(new Timestamp(System.currentTimeMillis()));
            entity.getCompiledScript().eval();
            entity.setEndTime(new Timestamp(System.currentTimeMillis()));
            LOGGER.debug("Script with id {} has been evaluated", entity.getId());
            String consoleOutput = entity.getResultWriter().toString();
            entity.setResult(consoleOutput);
            LOGGER.debug("Result in script with id {} has been set", entity.getId());
            entity.setStatus(ScriptStatus.DONE);
            LOGGER.debug("Status in script with id {} has been changed", entity.getId());
            LOGGER.debug("Return output of script with id {}", entity.getId());
            return entity.getResultWriter().toString();
        } catch (Exception e) {
            entity.setEndTime(new Timestamp(System.currentTimeMillis()));
            LOGGER.error("Error during execution script with id {}, message of exception: {}", entity.getId(), e.getMessage());
            entity.setStatus(ScriptStatus.COMPLETED_EXCEPTIONALLY);
            entity.setThrownException(e);
            entity.setResult(e.getMessage());
            LOGGER.debug("Script with id {} execution has been finished with exception, return error message", entity.getId());
            return e.getMessage();
        }
    }

    /**
     * @param script script entity to execute
     * @return CompletableFuture of execution
     */
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

    /**
     * @param id script id
     * @return true if cancellation is successful, false if not
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean cancelScript(int id) {
        LOGGER.debug("Trying to cancel script with id {}", id);
        ScriptEntity script = storage.getScript(id);
        if (script.getStatus() == ScriptStatus.RUNNING) {
            LOGGER.debug("Script with id {} hasn't finished yet", id);
            script.getThread().stop();
            script.setEndTime(new Timestamp(System.currentTimeMillis()));
            LOGGER.debug("Script with id {} has been cancelled", id);
            script.setStatus(ScriptStatus.CANCELLED);
            return true;
        } else {
            LOGGER.debug("Script with id {} is completed, can't be cancelled", id);
            return false;
        }
    }
}
