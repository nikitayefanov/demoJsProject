package com.yefanov.controller;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.service.ScriptService;
import com.yefanov.storage.ScriptStorage;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class MainController {

    @Autowired
    private ScriptService scriptService;


    /**
     * @param body
     * @param async
     * @return 202 - script is accepted and will be executed asynchronously
     *         201 - script was executed and result would be returned
     * @throws URISyntaxException
     */
    @RequestMapping(
            value = "/scripts",
            method = RequestMethod.POST,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> addScript(@RequestBody String body,
                                            @RequestParam(value = "async", defaultValue = "false") boolean async

    ) throws URISyntaxException {
        ScriptEntity entity = new ScriptEntity(body);
        Link link;
        if (async) {
            scriptService.executeScriptAsync(entity);
            link = ControllerLinkBuilder.linkTo(methodOn(MainController.class).addScript(body, async)).slash(entity.getId()).withSelfRel();
            return ResponseEntity.accepted().location(new URI(link.getHref())).build();
        } else {
            String result = scriptService.executeScript(entity);
            link = ControllerLinkBuilder.linkTo(methodOn(MainController.class).addScript(body, async)).slash(entity.getId()).withSelfRel();
            return ResponseEntity.created(new URI(link.getHref())).body(result);
        }
    }

    /**
     * @param id
     * @return 200 - script output
     * 410 - script has been cancelled
     * 406 - script has been completed exceptionally
     * 204 - script is still evaluating
     * 500 - server error
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.GET
    )
    public ResponseEntity getStatus(@PathVariable("id") int id) throws ExecutionException, InterruptedException {
        ScriptEntity entity = scriptService.getScriptEntityById(id);
        switch (entity.getStatus()) {
            case RUNNING:
                return ResponseEntity.noContent().build();
            case CANCELLED:
                return ResponseEntity.status(410).build();
            case COMPLETED_EXCEPTIONALLY:
                return ResponseEntity.status(406).build();
            case DONE:
                return ResponseEntity.ok(entity.getResult());
        }
        return ResponseEntity.status(500).build();
    }

    /**
     * @param id
     * @return 406 - script has already been executed
     * 200 - script has been cancelled successfully
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity cancelScript(@PathVariable("id") int id) {
        boolean deleted = scriptService.cancelScript(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(406).build();
        }
    }

    @RequestMapping(
            value = "/test",
            method = RequestMethod.GET
    )
    public ResponseEntity test(Writer writer) throws IOException, InterruptedException {
        for (int i = 0; i < 20; i++) {
            writer.write("This is example");
            Thread.sleep(200);
        }
        return ResponseEntity.ok().build();
    }
}
