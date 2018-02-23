package com.yefanov;

import com.yefanov.controller.ScriptController;
import com.yefanov.service.ScriptService;
import org.apache.catalina.filters.CorsFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	public static final String EMPTY_SCRIPT = "";
	public static final String ERROR_SCRIPT = "print('Hello)";
	public static final String ENDLESS_SCRIPT = "print('Hello'); for(;;);";
	public static final String CORRECT_SCRIPT = "print('Hello');";

//	private MockMvc mockMvc;

	@Mock
	private ScriptService scriptService;

	@InjectMocks
	private ScriptController scriptController;

	private MockMvc mockMvc;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(scriptController).addFilter(new CorsFilter()).build();
//		this.mockMvc = standaloneSetup(new ScriptController()).build();
	}
	@Test
	public void contextLoads() {
	}

	@Test
	public void t() throws Exception {
		this.mockMvc.perform(post("/scripts?async=false").contentType(MediaType.TEXT_PLAIN).
				content(CORRECT_SCRIPT)).andExpect(status().isAccepted());
	}

	@Test
	public void addEmptyScript() {
//		ScriptEntity entity = new ScriptEntity();
//		entity.setScript();
//		when(scriptService.getScriptEntityById(0)).thenReturn();
//		mockMvc.perform(get("/scripts/{id}"))
	}
}
