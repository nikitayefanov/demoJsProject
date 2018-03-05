package com.yefanov.storage;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.exceptions.ScriptNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of ScriptStorage interface
 * @see ScriptStorage
 */
@Component
public class ScriptStorageImpl implements ScriptStorage  {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScriptStorageImpl.class);

    private List<ScriptEntity> scripts = new CopyOnWriteArrayList<>();

    /**
     * @param s script to add
     * @return ScriptEntity, containing this script
     */
    @Override
    public ScriptEntity addScript(String s) {
        LOGGER.debug("Adding script");
        ScriptEntity script = new ScriptEntity(s);
        scripts.add(script);
        script.setId(scripts.size() - 1);
        LOGGER.debug("Script has been added");
        return script;
    }

    /**
     * @param script script entity to remove
     */
    @Override
    public void removeScript(ScriptEntity script) {
        scripts.remove(script.getId());
        LOGGER.debug("Script has been removed");
    }

    /**
     * @return all script entities
     */
    @Override
    public List<ScriptEntity> getAllScriptEntities() {
        return scripts;
    }

    /**
     * @param id script id
     * @return script entity with such id
     */
    @Override
    public ScriptEntity getScript(int id) {
        try {
            return scripts.get(id);
        } catch (IndexOutOfBoundsException e) {
            throw new ScriptNotFoundException(e);
        } finally {
            LOGGER.debug("Script has been returned");
        }
    }
}
