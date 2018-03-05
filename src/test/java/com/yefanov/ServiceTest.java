package com.yefanov;

import com.yefanov.entities.ScriptEntity;
import com.yefanov.entities.ScriptStatus;
import com.yefanov.service.ScriptService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ServiceTest {

    @Value("${empty_script}")
    public String emptyScript;
    @Value("${error_script}")
    public String errorScript;
    @Value("${endless_script}")
    public String endlessScript;
    @Value("${correct_script}")
    public String correctScript;
    @Value("${correct_script_result}")
    public String correctScriptResult;

    @Autowired
    private ScriptService scriptService;

    @Test
    public void executeScriptCorrect() {
        ScriptEntity entity = scriptService.create(correctScript);
        String res = scriptService.executeScript(entity);
        assertEquals(correctScriptResult, res.trim());
        assertEquals(ScriptStatus.DONE, entity.getStatus());
        assertNotNull(entity.getResult());
    }

    @Test
    public void executeScriptWithException() {
        ScriptEntity entity = new ScriptEntity(errorScript);
        scriptService.executeScript(entity);
        assertEquals(ScriptStatus.COMPLETED_EXCEPTIONALLY, entity.getStatus());
        assertNotNull(entity.getThrownException());
    }

    @Test
    public void addScript() {
        ScriptEntity entity1 = new ScriptEntity(correctScript);
        ScriptEntity entity2 = new ScriptEntity(correctScript);
        ScriptEntity res1 = scriptService.create(entity1.getScript());
        ScriptEntity res2 = scriptService.create(entity2.getScript());
        assertEquals(0, res1.getId());
        assertEquals(1, res2.getId());
        assertEquals(entity1.getScript(), res1.getScript());
        assertEquals(entity2.getScript(), res2.getScript());
    }

    @Test
    public void executeScriptAsyncCorrect() throws ExecutionException, InterruptedException {
        ScriptEntity entity = scriptService.create(correctScript);
        CompletableFuture<String> future = scriptService.executeScriptAsync(entity);
        String res = future.get();
        assertNotNull(entity.getFuture());
        assertEquals(ScriptStatus.DONE, entity.getStatus());
        assertEquals(correctScriptResult, res.trim());
    }

    @Test
    public void executeScriptAsyncWithError() throws ExecutionException, InterruptedException {
        ScriptEntity entity = new ScriptEntity(errorScript);
        CompletableFuture<String> future = scriptService.executeScriptAsync(entity);
        String res = future.get();
        assertNotNull(entity.getFuture());
        assertEquals(ScriptStatus.COMPLETED_EXCEPTIONALLY, entity.getStatus());
        assertNotNull(entity.getThrownException());
        assertEquals(res, entity.getThrownException().getMessage());
    }

    @Test(expected = TimeoutException.class)
    public void executeScriptAsyncEndless() throws ExecutionException, InterruptedException, TimeoutException {
        ScriptEntity entity = scriptService.create(endlessScript);
        CompletableFuture<String> future = scriptService.executeScriptAsync(entity);
        future.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void cancelScriptFalse() {
        ScriptEntity entity = scriptService.create(correctScript);
        scriptService.executeScript(entity);
        assertFalse(scriptService.cancelScript(entity.getId()));
        assertNotEquals(ScriptStatus.CANCELLED, entity.getStatus());
    }

    @Test
    public void cancelScriptTrue() throws InterruptedException {
        ScriptEntity entity = scriptService.create(endlessScript);
        scriptService.executeScriptAsync(entity);
        Thread.sleep(10);
        assertTrue(scriptService.cancelScript(entity.getId()));
        assertEquals(ScriptStatus.CANCELLED, entity.getStatus());
    }

    @Test
    public void threadState() throws InterruptedException {
        ScriptEntity entity = scriptService.create(endlessScript);
        scriptService.executeScriptAsync(entity);
        Thread.sleep(100);
        Thread.State state = entity.getThread().getState();
        assertEquals(Thread.State.RUNNABLE, state);
        scriptService.cancelScript(entity.getId());
        Thread.sleep(100);
        assertEquals(true, entity.getThread().isAlive());
    }

    @Test
    public void threadQueue() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            ScriptEntity entity = scriptService.create(endlessScript);
            scriptService.executeScriptAsync(entity);
        }
        Thread.sleep(100);
        ScriptEntity entity = scriptService.getScriptEntityById(5);
        assertNull(entity.getThread());
        scriptService.cancelScript(0);
        Thread.sleep(100);
        assertEquals(true, entity.getThread().isAlive());
    }
}