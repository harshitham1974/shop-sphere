package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
import com.ecom.shopsphere.dto.response.CategoryResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCategoryResponseDTO;
import com.ecom.shopsphere.exception.CategoryAlreadyExistsException;
import com.ecom.shopsphere.exception.CategoryNotFoundException;
import com.ecom.shopsphere.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import com.ecom.shopsphere.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void createCategory_Success() throws Exception {

        CreateCategoryRequestDTO request =
                CreateCategoryRequestDTO.builder()
                        .categoryName("Electronics")
                        .description("Electronic devices and gadgets")
                        .build();

        CategoryResponseDTO response =
                CategoryResponseDTO.builder()
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .description("Electronic devices and gadgets")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(categoryService.createCategory(any(CreateCategoryRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Category created successfully."))
                .andExpect(jsonPath("$.data.categoryId")
                        .value(1))
                .andExpect(jsonPath("$.data.categoryName")
                        .value("Electronics"))
                .andExpect(jsonPath("$.data.description")
                        .value("Electronic devices and gadgets"));

        verify(categoryService)
                .createCategory(any(CreateCategoryRequestDTO.class));
    }

    @Test
    void getAllCategories_Success() throws Exception {

        CategoryResponseDTO category1 =
                CategoryResponseDTO.builder()
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .description("Electronic devices and gadgets")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        CategoryResponseDTO category2 =
                CategoryResponseDTO.builder()
                        .categoryId(2L)
                        .categoryName("Books")
                        .description("Books and publications")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        List<CategoryResponseDTO> response = List.of(category1, category2);

        when(categoryService.getAllCategories())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/categories")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Categories fetched successfully."))
                .andExpect(jsonPath("$.data[0].categoryId")
                        .value(1))
                .andExpect(jsonPath("$.data[0].categoryName")
                        .value("Electronics"))
                .andExpect(jsonPath("$.data[1].categoryId")
                        .value(2))
                .andExpect(jsonPath("$.data[1].categoryName")
                        .value("Books"));

        verify(categoryService)
                .getAllCategories();
    }

    @Test
    void getCategoryById_Success() throws Exception {

        CategoryResponseDTO response =
                CategoryResponseDTO.builder()
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .description("Electronic devices and gadgets")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(categoryService.getCategoryById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/categories/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Category fetched successfully."))
                .andExpect(jsonPath("$.data.categoryId")
                        .value(1))
                .andExpect(jsonPath("$.data.categoryName")
                        .value("Electronics"))
                .andExpect(jsonPath("$.data.description")
                        .value("Electronic devices and gadgets"));

        verify(categoryService)
                .getCategoryById(1L);
    }

    @Test
    void updateCategory_Success() throws Exception {

        UpdateCategoryRequestDTO request =
                UpdateCategoryRequestDTO.builder()
                        .categoryName("Updated Electronics")
                        .description("Updated description")
                        .build();

        CategoryResponseDTO response =
                CategoryResponseDTO.builder()
                        .categoryId(1L)
                        .categoryName("Updated Electronics")
                        .description("Updated description")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(categoryService.updateCategory(eq(1L), any(UpdateCategoryRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/categories/1")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Category updated successfully."))
                .andExpect(jsonPath("$.data.categoryId")
                        .value(1))
                .andExpect(jsonPath("$.data.categoryName")
                        .value("Updated Electronics"))
                .andExpect(jsonPath("$.data.description")
                        .value("Updated description"));

        verify(categoryService)
                .updateCategory(eq(1L), any(UpdateCategoryRequestDTO.class));
    }

    @Test
    void deleteCategory_Success() throws Exception {

        DeleteCategoryResponseDTO response =
                DeleteCategoryResponseDTO.builder()
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .state("DELETED")
                        .build();

        when(categoryService.deleteCategory(1L))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/api/v1/categories/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Category deleted successfully"))
                .andExpect(jsonPath("$.data.categoryId")
                        .value(1))
                .andExpect(jsonPath("$.data.categoryName")
                        .value("Electronics"))
                .andExpect(jsonPath("$.data.state")
                        .value("DELETED"));

        verify(categoryService)
                .deleteCategory(1L);
    }

    @Test
    void getCategoryById_CategoryNotFoundException() throws Exception {

        when(categoryService.getCategoryById(999L))
                .thenThrow(
                        new CategoryNotFoundException(
                                "Category not found"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/categories/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Category Not Found"));

        verify(categoryService)
                .getCategoryById(999L);
    }

    @Test
    void updateCategory_CategoryNotFoundException() throws Exception {

        UpdateCategoryRequestDTO request =
                UpdateCategoryRequestDTO.builder()
                        .categoryName("Test")
                        .description("Test description")
                        .build();

        when(categoryService.updateCategory(eq(999L), any(UpdateCategoryRequestDTO.class)))
                .thenThrow(
                        new CategoryNotFoundException(
                                "Category not found"
                        )
                );

        mockMvc.perform(
                        put("/api/v1/categories/999")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Category Not Found"));

        verify(categoryService)
                .updateCategory(eq(999L), any(UpdateCategoryRequestDTO.class));
    }

    @Test
    void deleteCategory_CategoryNotFoundException() throws Exception {

        when(categoryService.deleteCategory(999L))
                .thenThrow(
                        new CategoryNotFoundException(
                                "Category not found"
                        )
                );

        mockMvc.perform(
                        delete("/api/v1/categories/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Category Not Found"));

        verify(categoryService)
                .deleteCategory(999L);
    }

    @Test
    void createCategory_CategoryAlreadyExistsException() throws Exception {

        CreateCategoryRequestDTO request =
                CreateCategoryRequestDTO.builder()
                        .categoryName("Electronics")
                        .description("Electronic devices and gadgets")
                        .build();

        when(categoryService.createCategory(any(CreateCategoryRequestDTO.class)))
                .thenThrow(
                        new CategoryAlreadyExistsException(
                                "Category already exists"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Category Creation Failed"));

        verify(categoryService)
                .createCategory(any(CreateCategoryRequestDTO.class));
    }
}
