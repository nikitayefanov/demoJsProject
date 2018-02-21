package com.yefanov.storage;

import com.yefanov.entities.ScriptEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ScriptStorageImpl implements ScriptStorage  {

    List<ScriptEntity> scripts = new CopyOnWriteArrayList<>();

    @Override
    public boolean addScript(ScriptEntity script) {
        boolean res = scripts.add(script);
        script.setId(scripts.size() - 1);
        return res;
    }

    @Override
    public void removeScript(ScriptEntity script) {
        scripts.remove(script.getId());
    }

    @Override
    public ScriptEntity getScript(int id) {
        return scripts.get(id);
    }


//    private List<Object> scripts = new CopyOnWriteArrayList<>();

//    public int addScript(Object script) {
//        for (int i = 0; i < scripts.size(); i++) {
//            if (scripts.get(i) == null) {
//                scripts.set(i, script);
//                return i;
//            }
//        }
//        scripts.add(script);
//        return scripts.size() - 1;
//    }
//
//    public boolean removeScript(Object script) {
//        for (int i = 0; i < scripts.size(); i++) {
//            if (scripts.get(i).equals(script)) {
//                scripts.set(i, null);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public Object getScript(Long id) {
//        return scripts.get(id.intValue());
//    }
}
