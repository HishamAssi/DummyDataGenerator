package com.hisham.dummydatagenerator;

import com.hisham.dummydatagenerator.controller.DummyDataController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DummyDataController.class)
public class DummyDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock your service dependencies here using @MockBean

    @Test
    public void test_generateData() throws Exception {
        mockMvc.perform(post("/data/{schema}/{table}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
