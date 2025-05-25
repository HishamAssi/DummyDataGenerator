package com.hisham.dummydatagenerator;

import com.hisham.dummydatagenerator.controller.SchemaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchemaController.class)
public class SchemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock your service dependencies here using @MockBean

    @Test
    public void test_getTableMetadata() throws Exception {
        mockMvc.perform(get("/schema/{schema}/{table}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
