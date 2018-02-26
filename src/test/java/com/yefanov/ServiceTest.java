package com.yefanov;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.entities.ScriptStatus;
import com.yefanov.service.ScriptService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.yefanov.ControllerTest.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ServiceTest {

    @Autowired
    private ScriptService scriptService;

    @Test
    public void executeScriptCorrect() {
        ScriptEntity entity = new ScriptEntity(CORRECT_SCRIPT);
        String res = scriptService.executeScript(entity);
        assertEquals(CORRECT_SCRIPT_RESULT, res.trim());
        assertEquals(ScriptStatus.DONE, entity.getStatus());
        assertNotNull(entity.getResult());
    }

    @Test
    public void executeScriptWithException() {
        ScriptEntity entity = new ScriptEntity(ERROR_SCRIPT);
        scriptService.executeScript(entity);
        assertEquals(ScriptStatus.COMPLETED_EXCEPTIONALLY, entity.getStatus());
        assertNotNull(entity.getThrownException());
    }

    @Test
    public void addScript() {
        ScriptEntity entity1 = new ScriptEntity("Test 1");
        ScriptEntity entity2 = new ScriptEntity("Test 2");
        ScriptEntity res1 = scriptService.addScriptToStorage(entity1.getScript());
        ScriptEntity res2 = scriptService.addScriptToStorage(entity2.getScript());
        assertEquals(0, res1.getId());
        assertEquals(1, res2.getId());
        assertEquals(entity1.getScript(), res1.getScript());
        assertEquals(entity2.getScript(), res2.getScript());
    }

    @Test
    public void executeScriptAsyncCorrect() throws ExecutionException, InterruptedException {
        ScriptEntity entity = new ScriptEntity(CORRECT_SCRIPT);
        CompletableFuture<String> future = scriptService.executeScriptAsync(entity);
        String res = future.get();
        assertNotNull(entity.getFuture());
        assertNotNull(entity.getResult());
        assertEquals(ScriptStatus.DONE, entity.getStatus());
        assertEquals(CORRECT_SCRIPT_RESULT, res.trim());
    }

    @Test
    public void executeScriptAsyncWithError() throws ExecutionException, InterruptedException {
        ScriptEntity entity = new ScriptEntity(ERROR_SCRIPT);
        CompletableFuture<String> future = scriptService.executeScriptAsync(entity);
        String res = future.get();
        assertNotNull(entity.getFuture());
        assertNull(entity.getResult());
        assertEquals(ScriptStatus.COMPLETED_EXCEPTIONALLY, entity.getStatus());
        assertNotNull(entity.getThrownException());
        assertEquals(res, entity.getThrownException().getMessage());
    }

    @Test(expected = TimeoutException.class)
    public void executeScriptAsyncEndless() throws ExecutionException, InterruptedException, TimeoutException {
        ScriptEntity entity = new ScriptEntity(ENDLESS_SCRIPT);
        CompletableFuture<String> future = scriptService.executeScriptAsync(entity);
        future.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void cancelScriptFalse() {
        ScriptEntity entity = scriptService.addScriptToStorage(CORRECT_SCRIPT);
        scriptService.executeScript(entity);
        assertFalse(scriptService.cancelScript(entity.getId()));
        assertNotEquals(ScriptStatus.CANCELLED, entity.getStatus());
    }

    @Test
    public void cancelScriptTrue() throws InterruptedException {
        ScriptEntity entity = scriptService.addScriptToStorage(ENDLESS_SCRIPT);
        scriptService.executeScriptAsync(entity);
        Thread.sleep(1);
        assertTrue(scriptService.cancelScript(entity.getId()));
        assertEquals(ScriptStatus.CANCELLED, entity.getStatus());
    }
}