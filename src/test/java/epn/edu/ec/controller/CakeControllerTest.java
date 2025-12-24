package epn.edu.ec.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import epn.edu.ec.exception.CakeNotFoundException;
import epn.edu.ec.model.cake.CakeResponse;
import epn.edu.ec.model.cake.CakesResponse;
import epn.edu.ec.model.cake.CreateCakeRequest;
import epn.edu.ec.model.cake.UpdateCakeRequest;
import epn.edu.ec.service.CakeService;

@WebMvcTest(controllers = CakeController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
@ActiveProfiles("test")
public class CakeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("deprecation")
    @MockBean
    private CakeService cakeService;

    private final long cakeId = 1;
    private final CakeResponse mockCakeResponse = new CakeResponse(
            cakeId, "Mock Cake", "Mock Cake Description");
    private final long notExistCakeId = 999;

    @Test
    public void getCakes_shouldReturnListOfCakes() throws Exception {
        // ARRANGE
        // Codigo Similar
        // List<CakeResponse> cakeList = new ArrayList<>();
        // cakeList.add(mockCakeResponse);
        CakesResponse cakesResponse = new CakesResponse(List.of(mockCakeResponse));
        when(cakeService.getCakes()).thenReturn(cakesResponse);

        // ACT
        ResultActions result = mockMvc.perform(get("/cakes")
                .contentType("application/json"));

        // ASSERT
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json"));
        result.andExpect(content().json(objectMapper.writeValueAsString(cakesResponse)));

        System.out.println(result.andReturn().getResponse().getContentAsString());

        verify(cakeService, times(1)).getCakes();
    }

    @Test
    public void getCakes_shouldReturnEmptyList() throws Exception {
        // ARRANGE
        CakesResponse emptyResponse = new CakesResponse(List.of());
        when(cakeService.getCakes()).thenReturn(emptyResponse);
        // ACT
        ResultActions result = mockMvc.perform(get("/cakes")
                .contentType("application/json"));
        // ASSERT
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json"));
        result.andExpect(content().json(objectMapper.writeValueAsString(emptyResponse)));
        verify(cakeService, times(1)).getCakes();
    }

    @Test
    public void getCakeById_shouldReturnCake() throws Exception {
        // ARRANGE
        when(cakeService.getCakeById(cakeId)).thenReturn(mockCakeResponse);

        // ACT
        ResultActions result = mockMvc.perform(get("/cakes/{id}", cakeId)
                .contentType("application/json"));

        // ASSERT
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json"));
        result.andExpect(content().json(objectMapper.writeValueAsString(mockCakeResponse)));
        System.out.println(result.andReturn().getResponse().getContentAsString());
        verify(cakeService, times(1)).getCakeById(cakeId);
    }

    @Test
    public void getCakeById_shouldReturnNotFound() throws Exception {
        // ARRANGE
        when(cakeService.getCakeById(notExistCakeId)).thenThrow(new CakeNotFoundException());

        // ACT
        ResultActions result = mockMvc.perform(get("/cakes/{id}", notExistCakeId)
                .contentType("application/json"));

        // ASSERT
        result.andExpect(status().isNotFound());
        result.andExpect(content().string(""));
        System.out.println(result.andReturn().getResponse().getContentAsString());
        verify(cakeService, times(1)).getCakeById(notExistCakeId);
    }

    @Test
    public void updateCake_shouldUpdateCake() throws Exception {
        // ARRANGE
        //Update Request
        UpdateCakeRequest updateRequest = new UpdateCakeRequest();
        updateRequest.setTitle("Updated cake title");
        updateRequest.setDescription("Updated cake description");
        //Response
        CakeResponse updateCakeResponse = CakeResponse.builder().id(cakeId)
                .title("Updated cake title")
                .description("Updated cake description").build();
        when(cakeService.updateCake(cakeId, updateRequest)).thenReturn(updateCakeResponse);

        // ACT
        ResultActions result = mockMvc.perform(put("/cakes/{id}", cakeId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateRequest)));

        // ASSERT
        result.andExpect(status().isNoContent());
        System.out.println(result.andReturn().getResponse().getContentAsString());
        verify(cakeService, times(1)).updateCake(eq(cakeId), any(UpdateCakeRequest.class));
    }



    @Test
    public void updateCake_shouldReturnNotFound() throws Exception {
        // ARRANGE
        //Update Request
        UpdateCakeRequest updateRequest = new UpdateCakeRequest();
        updateRequest.setTitle("Updated cake title");
        updateRequest.setDescription("Updated cake description");
        when(cakeService.updateCake(notExistCakeId, updateRequest)).thenThrow(new CakeNotFoundException());

        // ACT
        ResultActions result = mockMvc.perform(put("/cakes/{id}", notExistCakeId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateRequest)));

        // ASSERT
        result.andExpect(status().isNotFound());
        System.out.println(result.andReturn().getResponse().getContentAsString());
        verify(cakeService, times(1)).updateCake(eq(notExistCakeId), any(UpdateCakeRequest.class));
    }


    @Test
    public void deleteCake_shouldDeleteCake() throws Exception {
        // ARRANGE
        doNothing().when(cakeService).deleteCake(cakeId);

        // ACT
        ResultActions result = mockMvc.perform(delete("/cakes/{id}", cakeId));

        // ASSERT
        result.andExpect(status().isNoContent());
        System.out.println(result.andReturn().getResponse().getContentAsString());
        verify(cakeService, times(1)).deleteCake(cakeId);
    }

    @Test
    public void createCake_shouldCreateCake() throws Exception {
        // ARRANGE
        // Request
        CreateCakeRequest createCakeRequest = CreateCakeRequest.builder().title("New Cake")
                .description("New Cake Description").build();
        // Response
        CakeResponse cakeResponse = CakeResponse.builder().id(2l)
                .title("New Cake")
                .description("New Cake Description").build();

        when(cakeService.createCake(createCakeRequest)).thenReturn(cakeResponse);

        // ACT
        ResultActions result = mockMvc.perform(post("/cakes")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(createCakeRequest)));

        // ASSERT
        result.andExpect(status().isCreated());

    }



}