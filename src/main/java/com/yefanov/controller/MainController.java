package com.yefanov.controller;

import com.yefanov.service.ScriptService;
import com.yefanov.storage.ScriptStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController(value = "/api")
public class MainController {

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ScriptStorage storage;

    /**
     *
     * @param body
     * @param async
     * @param request
     * @return 202 - script accepted and will be executed asynchronously
     *         201 - script was executed and result would be returned
     * @throws URISyntaxException
     */
    @RequestMapping(
            value = "/scripts",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addScript(@RequestBody String body,
                                            @RequestParam(value = "async", defaultValue = "false") boolean async,
                                            HttpServletRequest request
    ) throws URISyntaxException {
        if (async) {
            CompletableFuture<String> result = scriptService.executeScriptAsync(body);
            long id = storage.addScript(result);
            StringBuffer uri = request.getRequestURL().append("/").append(id);
            return ResponseEntity.status(202).location(new URI(uri.toString())).build();
        } else {
            String result;
            try {
                result = scriptService.executeScript(body);
            } catch (Exception e) {
                result = e.getMessage();
            }
            long id = storage.addScript(result);
            StringBuffer uri = request.getRequestURL().append("/").append(id);
            return ResponseEntity.status(201).location(new URI(uri.toString())).body(result);
        }

    }

    /**
     *
     * @param id
     * @return 200 - script output
     *         404 - no script with such id
     *         410 - script has been cancelled
     *         400 - script has been completed exceptionally
     *         202 - script is still evaluating
     *         500 - server error
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.GET
    )
    public ResponseEntity getStatus(@PathVariable("id") long id) throws ExecutionException, InterruptedException {
        Object script;
        try {
            script = storage.getScript(id);
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.status(404).build();
        }
        if (script instanceof String) {
            return ResponseEntity.ok((String) script);
        } else if (script instanceof CompletableFuture){
            CompletableFuture<String> future = (CompletableFuture<String>) script;
            if (future.isCancelled()) {
                return ResponseEntity.status(410).build();
            } else if (future.isCompletedExceptionally()) {
                return ResponseEntity.status(400).build();
            } else if (future.isDone()) {
                return ResponseEntity.ok(future.get());
            } else {
                return ResponseEntity.status(202).build();
            }
        }
        return ResponseEntity.status(500).build();
    }

    /**
     *
     * @param id
     * @return 404 - no script with such id
     *         406 - script has been executed
     *         200 - script has been cancelled successfully
     *         500 - server error
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity cancelScript(@PathVariable("id") long id) {
        HttpStatus httpStatus = scriptService.cancelScript(id);
        return ResponseEntity.status(httpStatus).build();
    }
}
