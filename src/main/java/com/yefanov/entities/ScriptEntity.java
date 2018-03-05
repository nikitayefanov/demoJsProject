package com.yefanov.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.script.CompiledScript;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

/**
 * Class, encapsulating information about script
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScriptEntity {

    private int id;

    private String script;

    @JsonIgnore
    private CompiledScript compiledScript;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp endTime;

    @JsonIgnore
    private CompletableFuture<String> future;

    private String result;

    private ScriptStatus status;

    @JsonIgnore
    private Thread thread;

    @JsonIgnore
    private Exception thrownException;

    @JsonIgnore
    private OutputStream outputStream;

    @JsonIgnore
    private Writer resultWriter = new StringWriter();

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
        if (this.status == ScriptStatus.RUNNING) {
            return this.resultWriter.toString();
        }
        return this.result;
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

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public CompiledScript getCompiledScript() {
        return compiledScript;
    }

    public void setCompiledScript(CompiledScript compiledScript) {
        this.compiledScript = compiledScript;
    }

    public Writer getResultWriter() {
        return resultWriter;
    }

    public void setResultWriter(Writer resultWriter) {
        this.resultWriter = resultWriter;
    }
}
