package com.yefanov.storage;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ScriptStorage {

    private List<Object> scripts = new CopyOnWriteArrayList<>();

    public long addScript(Object script) {
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i) == null) {
                scripts.set(i, script);
                return i;
            }
        }
        scripts.add(script);
        return scripts.size() - 1;
    }

    public boolean removeScript(Object script) {
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i).equals(script)) {
                scripts.set(i, null);
                return true;
            }
        }
        return false;
    }

    public Object getScript(Long id) {
        return scripts.get(id.intValue());
    }
}
