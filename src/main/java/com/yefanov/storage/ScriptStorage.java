package com.yefanov.storage;

import com.yefanov.entities.ScriptEntity;

public interface ScriptStorage {

    public ScriptEntity addScript(String script);

    public void removeScript(ScriptEntity script);

    public ScriptEntity getScript(int id);
}
