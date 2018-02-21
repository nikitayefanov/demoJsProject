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
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ScriptServiceImpl implements ScriptService {

    @Autowired
    private ScriptStorage storage;

    @Override
    public ScriptEntity getScriptEntityById(int id) {
        return storage.getScript(id);
    }

    @Override
    public String executeScript(ScriptEntity entity) {
        storage.addScript(entity);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try (Writer stringWriter = new StringWriter()){
            OutputStream outputStream = new TeeOutputStream(new WriterOutputStream(stringWriter, Charset.defaultCharset()), System.out);
            engine.getContext().setWriter(new OutputStreamWriter(outputStream));
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

//    @Override
//    public boolean addOutputStream(OutputStream outputStream) {
//        return outputStreams.add(outputStream);
//    }

//    @Override
//    public OutputStream createOutputStream() {
//        if (outputStreams.size() == 1) {
//            return outputStreams.get(0);
//        } else if (outputStreams.size() == 2) {
//            return new TeeOutputStream(outputStreams.get(0), outputStreams.get(1));
//        } else {
//            OutputStream result = new TeeOutputStream(outputStreams.get(0), outputStreams.get(1));
//            for (int i = 2; i < outputStreams.size(); i++) {
//                result = new TeeOutputStream(result, outputStreams.get(i));
//            }
//            return result;
//        }
//        return new TeeOutputStream(outputStreams.get(0), outputStreams.get(1));
//    }

}
