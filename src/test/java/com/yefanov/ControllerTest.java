package com.yefanov;

import com.yefanov.controller.ScriptController;
import com.yefanov.entities.ScriptEntity;
import com.yefanov.entities.ScriptStatus;
import com.yefanov.service.ScriptService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ScriptController.class)
public class ControllerTest {

    public static final String EMPTY_SCRIPT = "";
    public static final String ERROR_SCRIPT = "print('Hello)";
    public static final String ENDLESS_SCRIPT = "print('Hello'); for(;;);";
    public static final String CORRECT_SCRIPT = "print('Hello');";
    public static final String CORRECT_SCRIPT_RESULT = "Hello";


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScriptService scriptService;

    @Test
    public void correctNonAsyncScript() throws Exception {
        ScriptEntity entity = new ScriptEntity(CORRECT_SCRIPT);
        when(scriptService.addScriptToStorage(entity.getScript())).thenReturn(entity);
        MvcResult result = mockMvc.perform(post("/scripts?async=false").content(CORRECT_SCRIPT)).andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    @Test
    public void emptyScript() throws Exception {
        MvcResult result = mockMvc.perform(post("/scripts?async=false").content(EMPTY_SCRIPT)).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    public void errorNonAsyncScript() throws Exception {
        ScriptEntity entity = new ScriptEntity(ERROR_SCRIPT);
        when(scriptService.addScriptToStorage(anyString())).thenReturn(entity);
        MvcResult result = mockMvc.perform(post("/scripts?async=false").content(ERROR_SCRIPT)).andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    @Test
    public void correctAsyncScript() throws Exception {
        ScriptEntity entity = new ScriptEntity(CORRECT_SCRIPT);
        when(scriptService.addScriptToStorage(anyString())).thenReturn(entity);
        MvcResult result = mockMvc.perform(post("/scripts?async=true").content(CORRECT_SCRIPT)).andReturn();
        assertEquals(HttpStatus.ACCEPTED.value(), result.getResponse().getStatus());
    }

    @Test
    public void errorAsyncScript() throws Exception {
        ScriptEntity entity = new ScriptEntity(ERROR_SCRIPT);
        when(scriptService.addScriptToStorage(anyString())).thenReturn(entity);
        MvcResult result = mockMvc.perform(post("/scripts?async=true").content(ERROR_SCRIPT)).andReturn();
        assertEquals(HttpStatus.ACCEPTED.value(), result.getResponse().getStatus());
    }

    @Test
    public void endlessAsyncScript() throws Exception {
        ScriptEntity entity = new ScriptEntity(ENDLESS_SCRIPT);
        when(scriptService.addScriptToStorage(anyString())).thenReturn(entity);
        MvcResult result = mockMvc.perform(post("/scripts?async=true").content(ENDLESS_SCRIPT)).andReturn();
        assertEquals(HttpStatus.ACCEPTED.value(), result.getResponse().getStatus());
    }

    @Test
    public void getRunningById() throws Exception {
        ScriptEntity entity = new ScriptEntity();
        entity.setId(0);
        entity.setStatus(ScriptStatus.RUNNING);
        when(scriptService.getScriptEntityById(entity.getId())).thenReturn(entity);
        MvcResult result = mockMvc.perform(get("/scripts/{id}", entity.getId())).andReturn();
        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
    }

    @Test
    public void getCancelledById() throws Exception {
        ScriptEntity entity = new ScriptEntity();
        entity.setId(0);
        entity.setStatus(ScriptStatus.CANCELLED);
        when(scriptService.getScriptEntityById(entity.getId())).thenReturn(entity);
        MvcResult result = mockMvc.perform(get("/scripts/{id}", entity.getId())).andReturn();
        assertEquals(HttpStatus.GONE.value(), result.getResponse().getStatus());
    }

    @Test
    public void getCompletedExceptionallyById() throws Exception {
        ScriptEntity entity = new ScriptEntity();
        entity.setId(0);
        entity.setThrownException(new RuntimeException(new RuntimeException()));
        entity.setStatus(ScriptStatus.COMPLETED_EXCEPTIONALLY);
        when(scriptService.getScriptEntityById(entity.getId())).thenReturn(entity);
        MvcResult result = mockMvc.perform(get("/scripts/{id}", entity.getId())).andReturn();
        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), result.getResponse().getStatus());
    }

    @Test
    public void getDoneById() throws Exception {
        ScriptEntity entity = new ScriptEntity();
        entity.setId(0);
        entity.setStatus(ScriptStatus.DONE);
        when(scriptService.getScriptEntityById(entity.getId())).thenReturn(entity);
        MvcResult result = mockMvc.perform(get("/scripts/{id}", entity.getId())).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void cancelScriptTrue() throws Exception {
        when(scriptService.cancelScript(anyInt())).thenReturn(true);
        MvcResult result = mockMvc.perform(delete("/scripts/{id}", anyInt())).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void cancelScriptFalse() throws Exception {
        when(scriptService.cancelScript(anyInt())).thenReturn(false);
        MvcResult result = mockMvc.perform(delete("/scripts/{id}", anyInt())).andReturn();
        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), result.getResponse().getStatus());
    }
}
