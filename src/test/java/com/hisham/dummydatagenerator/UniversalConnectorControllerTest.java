package com.hisham.dummydatagenerator;

import com.hisham.dummydatagenerator.controller.UniversalConnectorController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UniversalConnectorController.class)
public class UniversalConnectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock your service dependencies here using @MockBean

    @Test
    public void test_introspect() throws Exception {
        mockMvc.perform(post("/universal/introspect")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void test_insert() throws Exception {
        mockMvc.perform(post("/universal/insert")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
