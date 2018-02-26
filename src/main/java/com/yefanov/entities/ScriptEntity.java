package com.yefanov.entities;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public class ScriptEntity {

    private int id;

    private String script;

    private CompletableFuture<String> future;

    private String result;

    private ScriptStatus status;

    private Exception thrownException;

    private OutputStream outputStream;

    public ScriptEntity() {
        this.status = ScriptStatus.RUNNING;
    }

    public ScriptEntity(String script) {
        this.script = script;
        this.status = ScriptStatus.RUNNING;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public CompletableFuture<String> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<String> future) {
        this.future = future;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ScriptStatus getStatus() {
        return status;
    }

    public void setStatus(ScriptStatus status) {
        this.status = status;
    }

    public Exception getThrownException() {
        return thrownException;
    }

    public void setThrownException(Exception thrownException) {
        this.thrownException = thrownException;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
