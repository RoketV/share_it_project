package ru.practicum.shareit.itemRequestTests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestPaginationParams;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestOutputDto;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    public void postRequest_shouldReturnCreated() throws Exception {
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        inputDto.setDescription("description");
        Long userId = 1L;
        ItemRequestOutputDto outputDto = new ItemRequestOutputDto();

        when(itemRequestService.addRequest(inputDto, userId)).thenReturn(outputDto);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(inputDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(outputDto.getId()));

        verify(itemRequestService, times(1)).addRequest(inputDto, userId);
    }

    @Test
    public void getRequestsByOwner_shouldReturnOk() throws Exception {
        Long userId = 1L;
        List<ItemRequestOutputDto> outputDtos = new ArrayList<>();

        when(itemRequestService.getItemRequestWithItemsResponse(userId)).thenReturn(outputDtos);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(outputDtos.size())));

        verify(itemRequestService, times(1)).getItemRequestWithItemsResponse(userId);
    }

    @Test
    public void getRequestsById_shouldReturnOk() throws Exception {
        Long userId = 1L;
        Long requestId = 2L;
        ItemRequestOutputDto outputDto = new ItemRequestOutputDto();

        when(itemRequestService.getItemRequest(requestId, userId)).thenReturn(outputDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(outputDto.getId()));

        verify(itemRequestService, times(1)).getItemRequest(requestId, userId);
    }

    @Test
    public void getRequestsWithPagination_shouldReturnOk() throws Exception {
        Long userId = 1L;
        List<ItemRequestOutputDto> outputDtos = new ArrayList<>();
        ItemRequestPaginationParams params = new ItemRequestPaginationParams(0, 10);

        when(itemRequestService.getItemRequestWithPagination(userId, params)).thenReturn(outputDtos);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(params.getFrom()))
                        .param("size", String.valueOf(params.getSize())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(outputDtos.size())));

        verify(itemRequestService, times(1)).getItemRequestWithPagination(userId, params);
    }

    private static String asJsonString(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }
}
