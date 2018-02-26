package com.yefanov.controller;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class ScriptController {

    public static final String CHARSET = "UTF-8";

    @Autowired
    private ScriptService scriptService;

    /**
     * @param body
     * @param async
     * @return 202 - script is accepted and will be executed asynchronously
     * 201 - script was executed and result would be returned
     * 400 - script is empty
     * @throws URISyntaxException
     */
    @RequestMapping(
            value = "/scripts",
            method = RequestMethod.POST,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<StreamingResponseBody> addScript(@RequestBody String body,
                                                           @RequestParam(value = "async", defaultValue = "false") boolean async

    ) throws URISyntaxException {
        if (body.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        ScriptEntity entity = scriptService.addScriptToStorage(body);
        Link link = ControllerLinkBuilder.linkTo(methodOn(ScriptController.class).addScript(body, async)).slash(entity.getId()).withSelfRel();
        if (async) {
//            CompletableFuture<String> future = scriptService.executeScriptAsync(entity);
//            return ResponseEntity.accepted().location(new URI(link.getHref())).build();
            StreamingResponseBody respBody = outputStream -> scriptService.executeScriptAsync(entity);
            return ResponseEntity.accepted().location(new URI(link.getHref())).body(respBody);
        } else {
//            String result = scriptService.executeScript(entity);
//            return ResponseEntity.created(new URI(link.getHref())).body(result);
            StreamingResponseBody respBody = outputStream -> {
                entity.setOutputStream(outputStream);
                scriptService.executeScript(entity);
            };
            return ResponseEntity.created(new URI(link.getHref())).body(respBody);
        }
    }

    /**
     * @param id
     * @return 200 - script output
     * 410(GONE) - script has been cancelled
     * 406(NOT_ACCEPTABLE) - script has been completed exceptionally
     * 204 - script is still evaluating
     * 500(INTERNAL_SERVER_ERROR - server error
     */
    @RequestMapping(
            value = "/scripts/{id}",
            method = RequestMethod.GET
    )
    public ResponseEntity getStatus(@PathVariable("id") int id) {
        ScriptEntity entity = scriptService.getScriptEntityById(id);
        switch (entity.getStatus()) {
            case RUNNING:
                return ResponseEntity.noContent().build();
            case CANCELLED:
                return ResponseEntity.status(HttpStatus.GONE).build();
            case COMPLETED_EXCEPTIONALLY:
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(entity.getThrownException().getCause().toString());
            case DONE:
                return ResponseEntity.ok(entity.getResult());
        }
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
        boolean deleted = scriptService.cancelScript(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
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