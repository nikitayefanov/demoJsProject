package com.yefanov.service;

import com.yefanov.entities.ScriptEntity;

import javax.script.CompiledScript;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScriptService {

    ScriptEntity addScriptToStorage(String script);

    List<ScriptEntity> getAllScriptEntities();

    ScriptEntity getScriptEntityById(int id);

    String executeScript(ScriptEntity script);

    CompletableFuture<String> executeScriptAsync(ScriptEntity script);

    boolean cancelScript(int id);

    CompiledScript compileScript(String script);

}
