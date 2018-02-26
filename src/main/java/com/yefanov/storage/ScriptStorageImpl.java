package com.yefanov.storage;

import com.yefanov.entities.ScriptEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ScriptStorageImpl implements ScriptStorage  {

    private List<ScriptEntity> scripts = new CopyOnWriteArrayList<>();

    @Override
    public ScriptEntity addScript(String s) {
        ScriptEntity script = new ScriptEntity(s);
        scripts.add(script);
        script.setId(scripts.size() - 1);
        return script;
    }

    @Override
    public void removeScript(ScriptEntity script) {
        scripts.remove(script.getId());
    }

    @Override
    public ScriptEntity getScript(int id) {
        return scripts.get(id);
    }


}
