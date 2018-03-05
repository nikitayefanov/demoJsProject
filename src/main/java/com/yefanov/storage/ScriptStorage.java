package com.yefanov.storage;

import com.yefanov.entities.ScriptEntity;

import java.util.List;

/**
 * Service layer for storing scripts
 */
public interface ScriptStorage {

    /**
     * Adds script to storage
     * @param script script to save to storage
     * @return ScriptEntity
     * @see ScriptEntity
     */
    ScriptEntity addScript(String script);

    /**
     * Removes script from storage
     * @param script script to remove
     */
    void removeScript(ScriptEntity script);

    /**
     * Returns all ScriptEntity
     * @return all ScriptEntity
     */
    List<ScriptEntity> getAllScriptEntities();

    /**
     * Gets script with this id
     * @param id id of script
     * @return script with such id
     */
    ScriptEntity getScript(int id);
}
