package com.yefanov.model;

import java.util.concurrent.Future;

public class ScriptShell {

    private Future future;

    private String result;

    public ScriptShell() {
    }

    public ScriptShell(Future script, String result) {
        this.future = script;
        this.result = result;
    }


}
