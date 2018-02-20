package com.yefanov.example;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Example {

    public static void main(String[] args) throws ScriptException {
        ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
        nashorn.eval("print('GGGG');");

    }
}
