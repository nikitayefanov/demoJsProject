package com.yefanov.example;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ExampleSecond {

    public static void main(String[] args) throws ScriptException {
        ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
        nashorn.eval("// Adopted from here: https://gist.github.com/bripkens/8597903\n" +
                "// Makes ES7 Promises polyfill work on Nashorn https://github.com/jakearchibald/es6-promise\n" +
                "// (Haven't verified how correct it is, use with care)\n" +
                "(function(context) {\n" +
                "  'use strict';\n" +
                " \n" +
                "  var Timer = Java.type('java.util.Timer');\n" +
                "  var Phaser = Java.type('java.util.concurrent.Phaser');\n" +
                "\n" +
                "  var timer = new Timer('jsEventLoop', false);\n" +
                "  var phaser = new Phaser();\n" +
                " \n" +
                "  var onTaskFinished = function() {\n" +
                "    phaser.arriveAndDeregister();\n" +
                "  };\n" +
                " \n" +
                "  context.setTimeout = function(fn, millis /* [, args...] */) {\n" +
                "    var args = [].slice.call(arguments, 2, arguments.length);\n" +
                " \n" +
                "    var phase = phaser.register();\n" +
                "    var canceled = false;\n" +
                "    timer.schedule(function() {\n" +
                "      if (canceled) {\n" +
                "        return;\n" +
                "      }\n" +
                " \n" +
                "      try {\n" +
                "        fn.apply(context, args);\n" +
                "      } catch (e) {\n" +
                "        print(e);\n" +
                "      } finally {\n" +
                "        onTaskFinished();\n" +
                "      }\n" +
                "    }, millis);\n" +
                " \n" +
                "    return function() {\n" +
                "      onTaskFinished();\n" +
                "      canceled = true;\n" +
                "    };\n" +
                "  };\n" +
                " \n" +
                "  context.clearTimeout = function(cancel) {\n" +
                "    cancel();\n" +
                "  };\n" +
                " \n" +
                "  context.setInterval = function(fn, delay /* [, args...] */) {\n" +
                "    var args = [].slice.call(arguments, 2, arguments.length);\n" +
                " \n" +
                "    var cancel = null;\n" +
                " \n" +
                "    var loop = function() {\n" +
                "      cancel = context.setTimeout(loop, delay);\n" +
                "      fn.apply(context, args);\n" +
                "    };\n" +
                " \n" +
                "    cancel = context.setTimeout(loop, delay);\n" +
                "    return function() {\n" +
                "      cancel();\n" +
                "    };\n" +
                "  };\n" +
                " \n" +
                "  context.clearInterval = function(cancel) {\n" +
                "    cancel();\n" +
                "  };\n" +
                " \n" +
                "})(this);\n" +
                "\n" +
                "function printNumbersInterval() {\n" +
                "  var i = 1;\n" +
                "  var timerId = setInterval(function() {\n" +
                "    print(i);\n" +
                "    if (i == 20) clearInterval(timerId);\n" +
                "    i++;\n" +
                "  }, 100);\n" +
                "}\n" +
                "\n" +
                "// вызов\n" +
                "printNumbersInterval();");

    }
}
