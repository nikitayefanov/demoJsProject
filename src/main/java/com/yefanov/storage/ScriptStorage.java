package com.yefanov.storage;

import com.yefanov.entities.ScriptEntity;

import java.util.List;

public interface ScriptStorage {

    ScriptEntity addScript(String script);

    void removeScript(ScriptEntity script);

    List<ScriptEntity> getAllScriptEntities();

    ScriptEntity getScript(int id);
}
