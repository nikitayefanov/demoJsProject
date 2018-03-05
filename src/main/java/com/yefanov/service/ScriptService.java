package com.yefanov.service;

import com.yefanov.entities.ScriptEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service layer for script processing
 */
public interface ScriptService {

    /**
     * Creates ScriptEntity from String, containing script
     * @param script text of script
     * @return ScriptEntity
     */
    ScriptEntity create(String script);

    /**
     * Returns all ScriptEntity
     * @return all ScriptEntity
     */
    List<ScriptEntity> getAllScriptEntities();

    /**
     * Returns ScriptEntity with such id
     * @param id id of the script
     * @return ScriptEntity
     */
    ScriptEntity getScriptEntityById(int id);

    /**
     * Executes script
     * @param script script to execute
     * @return output of script
     */
    String executeScript(ScriptEntity script);

    /**
     * Executes script asynchronously
     * @param script script to execute
     * @return output of the script
     */
    CompletableFuture<String> executeScriptAsync(ScriptEntity script);

    /**
     * Cancels script execution if it is still running
     * @param id script id
     * @return true if script has been cancelled, false if not
     */
    boolean cancelScript(int id);
}
