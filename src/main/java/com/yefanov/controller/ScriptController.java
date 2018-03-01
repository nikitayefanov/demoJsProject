package com.yefanov.controller;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.service.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.yefanov.service.ScriptServiceImpl.ENGINE_NAME;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class ScriptController {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScriptController.class);

    @Autowired
    private ScriptService scriptService;

    /**
     * @param body
     * @param async
     * @return 202 - script is accepted and will be executed asynchronously
     * 201 - script was executed and result would be returned
     * 400 - script is empty or not valid
     * @throws URISyntaxException
     */
    @RequestMapping(
            value = "/scripts",
            method = RequestMethod.POST
            )
    public ResponseEntity<StreamingResponseBody> addScript(@RequestBody String body,
                                                           @RequestParam(value = "async", defaultValue = "false") boolean async

    ) throws URISyntaxException {
        LOGGER.debug("In /scripts by POST request");
        if (body.isEmpty()) {
            LOGGER.debug("Script is empty, return");
            return ResponseEntity.badRequest().build();
        }
        String error = compileAndGetErrorMessage(body);
        if (error != null) {
            LOGGER.error("Script isn't valid");
            return ResponseEntity.badRequest().body(outputStream -> outputStream.write(error.getBytes()));
        }
        LOGGER.debug("Script is valid");
        ScriptEntity entity = scriptService.addScriptToStorage(body);
        LOGGER.debug("Script added to storage with id {}", entity.getId());
        Link link = ControllerLinkBuilder.linkTo(methodOn(ScriptController.class).addScript(body, async)).slash(entity.getId()).withSelfRel();
        if (async) {
            LOGGER.debug("Script with id {} will be executed asyncronously", entity.getId());
//            StreamingResponseBody respBody = outputStream -> scriptService.executeScriptAsync(entity);
            scriptService.executeScriptAsync(entity);
            return ResponseEntity.accepted().location(new URI(link.getHref())).build();
        } else {
            LOGGER.debug("Script with id {} will be executed non-asyncronously", entity.getId());
            entity.setThread(Thread.currentThread());
            StreamingResponseBody respBody = outputStream -> {
                entity.setOutputStream(outputStream);
                scriptService.executeScript(entity);
            };
            return ResponseEntity.created(new URI(link.getHref())).contentType(MediaType.TEXT_PLAIN).body(respBody);
        }
    }

    @RequestMapping(value = "/scripts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ScriptEntity> getAllScripts() {
        return scriptService.getAllScriptEntities();
    }

    /**
     * @param id
     * @return 200 - script output
     * 410(GONE) - script has been cancelled
     * 406(NOT_ACCEPTABLE) - script has been completed exceptionally
     * 204 - script is still evaluating
     * 404 - script with this id doesn't exist
     * 500(INTERNAL_SERVER_ERROR) - server error
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.GET
    )
    public ResponseEntity getStatus(@PathVariable("id") int id) {
        LOGGER.debug("In /scripts/{} by GET request", id);
        ScriptEntity entity = scriptService.getScriptEntityById(id);
        switch (entity.getStatus()) {
            case RUNNING:
                LOGGER.debug("Script with id {} is running", id);
                return ResponseEntity.noContent().build();
            case CANCELLED:
                LOGGER.debug("Script with id {} has been cancelled", id);
                return ResponseEntity.status(HttpStatus.GONE).build();
            case COMPLETED_EXCEPTIONALLY:
                LOGGER.debug("Script with id {} has been completed exceptionally", id);
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(entity.getThrownException().getCause().toString());
            case DONE:
                LOGGER.debug("Script with id {} is done", id);
                return ResponseEntity.ok(entity.getResult());
        }
        LOGGER.error("Unexpected status, return INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * @param id
     * @return 406(NOT_ACCEPTABLE) - script has already been executed
     * 200 - script has been cancelled successfully
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity cancelScript(@PathVariable("id") int id) {
        LOGGER.debug("In /scripts/{} by DELETE request", id);
        boolean cancelled = scriptService.cancelScript(id);
        if (cancelled) {
            LOGGER.debug("Script with id {} has been cancelled", id);
            return ResponseEntity.ok().build();
        } else {
            LOGGER.debug("Script with id {} hasn't been cancelled", id);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    private String compileAndGetErrorMessage(String script) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        Compilable compilable = (Compilable) engine;
        try {
            compilable.compile(script);
        } catch (ScriptException e) {
            return e.getMessage();
        }
        return null;
    }

    //    @RequestMapping(
//            value = "/test",
//            method = RequestMethod.GET,
//            produces = MediaType.TEXT_PLAIN_VALUE
//    )
//    public ResponseEntity<StreamingResponseBody> test(OutputStream stream) {
//        StreamingResponseBody body = new StreamingResponseBody() {
//            @Override
//            public void writeTo(OutputStream outputStream) throws IOException {
//
//                for (int i = 0; i < 100; i++) {
//                    outputStream.write((Integer.toString(i) + "QWEWRWEIYRWEUYUREWYRUIWEYRUEYWIRUYWEUIRWEHJFHSDKFHSDJGFSDHGFHSDGFGSDHFGDSHFGSDHGFHSDGFHSDGJFHGSDHFGSDHFGJSDHGFJDHSGFHGSDH - ")
//                            .getBytes());
//                    outputStream.flush();
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        return ResponseEntity.ok().body(body);
//    }
//
//    @RequestMapping(
//            value = "/test2",
//            method = RequestMethod.GET,
//            produces = MediaType.TEXT_PLAIN_VALUE
//    )
//    public void test2(OutputStream stream) throws IOException, InterruptedException {
//        for (int i = 0; i < 50; i++) {
//            stream.write(("TESTD:JGSDLKJGLSDJGLSDLKGHSDLGHLSDHGLHDSLGHSDLJHGLJDSHGLHSDJLHGFSDFJLSDJFLSDJLFJKDS" + i).getBytes());
//            stream.flush();
//            Thread.sleep(200);
//        }
//    }
}