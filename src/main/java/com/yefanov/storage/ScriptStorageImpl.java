package com.yefanov.storage;

import com.yefanov.entities.ScriptEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ScriptStorageImpl implements ScriptStorage  {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScriptStorageImpl.class);

    private List<ScriptEntity> scripts = new CopyOnWriteArrayList<>();

    @Override
    public ScriptEntity addScript(String s) {
        LOGGER.debug("Adding script");
        ScriptEntity script = new ScriptEntity(s);
        scripts.add(script);
        script.setId(scripts.size() - 1);
        LOGGER.debug("Script has been added");
        return script;
    }

    @Override
    public void removeScript(ScriptEntity script) {
        scripts.remove(script.getId());
        LOGGER.debug("Script has been removed");
    }

    @Override
    public List<ScriptEntity> getAllScriptEntities() {
        return scripts;
    }

    @Override
    public ScriptEntity getScript(int id) {
        LOGGER.debug("Script has been returned");
        return scripts.get(id);
    }
}
