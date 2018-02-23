package com.yefanov.service;

import com.yefanov.entities.ScriptEntity;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public interface ScriptService {

    ScriptEntity addScriptToStorage(String script);

    ScriptEntity getScriptEntityById(int id);

    String executeScript(ScriptEntity script);

    CompletableFuture<String> executeScriptAsync(ScriptEntity script);

    boolean cancelScript(int id);

}
