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

import javax.script.CompiledScript;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class ScriptController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptController.class);

    @Autowired
    private ScriptService scriptService;

    /**
     * @param body  script to execute
     * @param async should script be executed async or not, default - not
     * @return 202 - script is accepted and will be executed asynchronously
     * 201 - script was executed and result would be returned
     * 400 - script is empty or not valid
     * @throws URISyntaxException string could not be parsed as a URI reference
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
        CompiledScript compiledScript = scriptService.compileScript(body);
        LOGGER.debug("Script is valid");
        ScriptEntity entity = scriptService.addScriptToStorage(body);
        LOGGER.debug("Script added to storage with id {}", entity.getId());
        entity.setCompiledScript(compiledScript);
        Link link = ControllerLinkBuilder.linkTo(methodOn(ScriptController.class).addScript(body, async)).slash(entity.getId()).withSelfRel();
        if (async) {
            LOGGER.debug("Script with id {} will be executed asynchronously", entity.getId());
            scriptService.executeScriptAsync(entity);
            return ResponseEntity.accepted().location(new URI(link.getHref())).build();
        } else {
            LOGGER.debug("Script with id {} will be executed non-asynchronously", entity.getId());
//            entity.setThread(Thread.currentThread());
            StreamingResponseBody respBody = outputStream -> {
                entity.setOutputStream(outputStream);
                scriptService.executeScript(entity);
            };
            return ResponseEntity.created(new URI(link.getHref())).contentType(MediaType.TEXT_PLAIN).body(respBody);
        }
    }

    /**
     *
     * @return all scripts
     */
    @RequestMapping(value = "/scripts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ScriptEntity> getAllScripts() {
        return scriptService.getAllScriptEntities();
    }

    /**
     * @param id script id
     * @return 200 - script is done
     * 202 - script is still evaluating
     * 410 - script has been cancelled
     * 406 - script has been completed exceptionally
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.GET
    )
    public ResponseEntity getStatus(@PathVariable("id") int id) {
        LOGGER.debug("In /scripts/{} by GET request", id);
        ScriptEntity entity = scriptService.getScriptEntityById(id);
        LOGGER.debug("Current output for script with id {} has been set", id);
        entity.setResult(entity.getResultWriter().toString());
        switch (entity.getStatus()) {
            case RUNNING:
                LOGGER.debug("Script with id {} is running", id);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(entity);
            case CANCELLED:
                LOGGER.debug("Script with id {} has been cancelled", id);
                return ResponseEntity.status(HttpStatus.GONE).body(entity);
            case COMPLETED_EXCEPTIONALLY:
                LOGGER.debug("Script with id {} has been completed exceptionally", id);
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(entity);
            case DONE:
                LOGGER.debug("Script with id {} is done", id);
                return ResponseEntity.ok(entity);
            default:
                LOGGER.error("Unexpected status, exception thrown");
                throw new IllegalStateException();
        }
    }

    /**
     * @param id script id
     * @return 406 - script has already been executed
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
}