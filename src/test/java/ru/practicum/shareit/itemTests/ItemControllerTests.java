package ru.practicum.shareit.itemTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comments.dto.CommentInputDto;
import ru.practicum.shareit.comments.dto.CommentOutputDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemPaginationParams;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    public void testAddItem() throws Exception {
        User user = new User(1L, "userName", "user@email.com");

        ItemInputDto inputDto = new ItemInputDto();
        inputDto.setName("name");
        inputDto.setDescription("description");
        inputDto.setUser(user);
        inputDto.setAvailable(true);
        inputDto.setRequestId(1L);

        ItemOutputDto outputDto = new ItemOutputDto();
        outputDto.setName("name");
        outputDto.setDescription("description");
        outputDto.setUser(user);
        outputDto.setAvailable(true);
        outputDto.setRequestId(1L);

        when(itemService.addItem(eq(inputDto), eq(1L))).thenReturn(outputDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(outputDto.getName()))
                .andExpect(jsonPath("$.description").value(outputDto.getDescription()))
                .andExpect(jsonPath("$.user.id").value(outputDto.getUser().getId()))
                .andExpect(jsonPath("$.user.name").value(outputDto.getUser().getName()))
                .andExpect(jsonPath("$.user.email").value(outputDto.getUser().getEmail()));

        verify(itemService, times(1)).addItem(eq(inputDto), eq(1L));
    }

    @Test
    public void testUpdateItem() throws Exception {
        User user = new User(1L, "userName", "user@email.com");

        ItemInputDto inputDto = new ItemInputDto();
        inputDto.setId(1L);
        inputDto.setName("newName");
        inputDto.setDescription("newDescription");
        inputDto.setAvailable(true);
        inputDto.setUser(user);
        inputDto.setRequestId(1L);

        ItemOutputDto outputDto = new ItemOutputDto();
        outputDto.setId(1L);
        outputDto.setName("newName");
        outputDto.setDescription("newDescription");
        outputDto.setAvailable(true);
        outputDto.setUser(user);
        outputDto.setRequestId(1L);

        when(itemService.updateItem(eq(inputDto), eq(1L), eq(1L))).thenReturn(outputDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(outputDto.getId()))
                .andExpect(jsonPath("$.name").value(outputDto.getName()));

        verify(itemService, times(1)).updateItem(eq(inputDto), eq(1L), eq(1L));
    }

    @Test
    public void testGetItem() throws Exception {
        Long ownerId = 1L;
        Long itemId = 1L;

        ItemOutputDto outputDto = new ItemOutputDto();
        outputDto.setId(itemId);
        outputDto.setName("name");
        outputDto.setDescription("description");
        outputDto.setAvailable(true);
        outputDto.setUser(new User());
        outputDto.setRequestId(2L);

        when(itemService.getItem(eq(ownerId), eq(itemId))).thenReturn(outputDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value(outputDto.getName()))
                .andExpect(jsonPath("$.description").value(outputDto.getDescription()))
                .andExpect(jsonPath("$.available").value(outputDto.getAvailable()))
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.requestId").value(outputDto.getRequestId()));

        verify(itemService, times(1)).getItem(eq(ownerId), eq(itemId));
    }

    @Test
    public void testGetItems() throws Exception {
        User user = new User(1L, "testUser", "testuser@example.com");
        ItemOutputDto item1 = new ItemOutputDto();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setUser(user);

        ItemOutputDto item2 = new ItemOutputDto();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(false);
        item2.setUser(user);

        List<ItemOutputDto> outputDtoList = Arrays.asList(item1, item2);

        when(itemService.getItems(eq(1L), any(ItemPaginationParams.class)))
                .thenReturn(outputDtoList);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(outputDtoList.get(0).getId().intValue()))
                .andExpect(jsonPath("$[0].name").value(outputDtoList.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(outputDtoList.get(0).getDescription()))
                .andExpect(jsonPath("$[0].available").value(outputDtoList.get(0).getAvailable()))
                .andExpect(jsonPath("$[0].user.id").value(outputDtoList.get(0).getUser().getId().intValue()))
                .andExpect(jsonPath("$[0].user.name").value(outputDtoList.get(0).getUser().getName()))
                .andExpect(jsonPath("$[0].user.email").value(outputDtoList.get(0).getUser().getEmail()))
                .andExpect(jsonPath("$[1].id").value(outputDtoList.get(1).getId().intValue()))
                .andExpect(jsonPath("$[1].name").value(outputDtoList.get(1).getName()))
                .andExpect(jsonPath("$[1].description").value(outputDtoList.get(1).getDescription()))
                .andExpect(jsonPath("$[1].available").value(outputDtoList.get(1).getAvailable()))
                .andExpect(jsonPath("$[1].user.id").value(outputDtoList.get(1).getUser().getId().intValue()))
                .andExpect(jsonPath("$[1].user.name").value(outputDtoList.get(1).getUser().getName()))
                .andExpect(jsonPath("$[1].user.email").value(outputDtoList.get(1).getUser().getEmail()));

        verify(itemService, times(1)).getItems(eq(1L), any(ItemPaginationParams.class));
    }

    @Test
    public void testSearchItem() throws Exception {
        User user = new User(1L, "testUser", "testuser@example.com");
        ItemOutputDto item1 = new ItemOutputDto();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setUser(user);

        ItemOutputDto item2 = new ItemOutputDto();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(false);
        item2.setUser(user);

        List<ItemOutputDto> outputDtoList = Arrays.asList(item1, item2);

        when(itemService.searchItem(eq("text"), any(ItemPaginationParams.class))).thenReturn(outputDtoList);

        mockMvc.perform(get("/items/search")
                        .param("text", "text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Item 2"))
                .andExpect(jsonPath("$[1].description").value("Description 2"))
                .andExpect(jsonPath("$[1].available").value(false));

        verify(itemService).searchItem(eq("text"), any(ItemPaginationParams.class));
    }

    @Test
    public void testDeleteItem() throws Exception {
        Long itemId = 1L;

        ItemOutputDto outputDto = new ItemOutputDto();
        outputDto.setId(itemId);
        outputDto.setName("Item 1");
        outputDto.setDescription("Description 1");
        outputDto.setAvailable(true);

        when(itemService.deleteItem(itemId)).thenReturn(outputDto);

        mockMvc.perform(delete("/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Item 1"))
                .andExpect(jsonPath("$.description").value("Description 1"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService, times(1)).deleteItem(itemId);
    }

    @Test
    void postComment_shouldReturnCommentOutputDto() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;

        CommentInputDto inputDto = new CommentInputDto();
        inputDto.setText("This is a comment");

        CommentOutputDto outputDto = new CommentOutputDto();
        outputDto.setId(1L);
        outputDto.setText("This is a comment");
        outputDto.setAuthorName("User");
        outputDto.setCreated(LocalDateTime.now());

        when(itemService.addComment(eq(inputDto), eq(itemId), eq(userId))).thenReturn(outputDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(new ObjectMapper().writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(outputDto.getId()))
                .andExpect(jsonPath("$.text").value(outputDto.getText()))
                .andExpect(jsonPath("$.authorName").value(outputDto.getAuthorName()));
    }
}
