package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProductRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteProductResponseDTO;
import com.ecom.shopsphere.dto.response.ProductResponseDTO;
import com.ecom.shopsphere.exception.CategoryNotFoundException;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import com.ecom.shopsphere.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void createProduct_Success() throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName("Smart Phone")
                        .description("Latest model smartphone with 128GB storage")
                        .brand("TechBrand")
                        .categoryId(1L)
                        .price(new BigDecimal("699.99"))
                        .stockQuantity(50)
                        .imageUrl("https://example.com/phone.jpg")
                        .build();

        ProductResponseDTO response =
                ProductResponseDTO.builder()
                        .productId(1L)
                        .productName("Smart Phone")
                        .description("Latest model smartphone with 128GB storage")
                        .brand("TechBrand")
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .price(new BigDecimal("699.99"))
                        .stockQuantity(50)
                        .imageUrl("https://example.com/phone.jpg")
                        .build();

        when(productService.createProduct(any(CreateProductRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Product created successfully"))
                .andExpect(jsonPath("$.data.productId")
                        .value(1))
                .andExpect(jsonPath("$.data.productName")
                        .value("Smart Phone"))
                .andExpect(jsonPath("$.data.brand")
                        .value("TechBrand"))
                .andExpect(jsonPath("$.data.categoryName")
                        .value("Electronics"))
                .andExpect(jsonPath("$.data.price")
                        .value(699.99))
                .andExpect(jsonPath("$.data.stockQuantity")
                        .value(50));

        verify(productService)
                .createProduct(any(CreateProductRequestDTO.class));
    }

    @Test
    void getAllProducts_Success() throws Exception {

        ProductResponseDTO product1 =
                ProductResponseDTO.builder()
                        .productId(1L)
                        .productName("Smart Phone")
                        .description("Latest model smartphone")
                        .brand("TechBrand")
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .price(new BigDecimal("699.99"))
                        .stockQuantity(50)
                        .imageUrl("https://example.com/phone.jpg")
                        .build();

        ProductResponseDTO product2 =
                ProductResponseDTO.builder()
                        .productId(2L)
                        .productName("Wireless Headphones")
                        .description("Noise-cancelling wireless headphones")
                        .brand("AudioBrand")
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .price(new BigDecimal("199.99"))
                        .stockQuantity(100)
                        .imageUrl("https://example.com/headphones.jpg")
                        .build();

        List<ProductResponseDTO> response = List.of(product1, product2);

        when(productService.getAllProducts())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/products")
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Products fetched successfully"))
                .andExpect(jsonPath("$.data[0].productId")
                        .value(1))
                .andExpect(jsonPath("$.data[0].productName")
                        .value("Smart Phone"))
                .andExpect(jsonPath("$.data[1].productId")
                        .value(2))
                .andExpect(jsonPath("$.data[1].productName")
                        .value("Wireless Headphones"));

        verify(productService)
                .getAllProducts();
    }

    @Test
    void getProductById_Success() throws Exception {

        Long productId = 1L;

        ProductResponseDTO response =
                ProductResponseDTO.builder()
                        .productId(productId)
                        .productName("Smart Phone")
                        .description("Latest model smartphone with 128GB storage")
                        .brand("TechBrand")
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .price(new BigDecimal("699.99"))
                        .stockQuantity(50)
                        .imageUrl("https://example.com/phone.jpg")
                        .build();

        when(productService.getProductById(eq(productId)))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/products/{productId}", productId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Product fetched successfully"))
                .andExpect(jsonPath("$.data.productId")
                        .value(1))
                .andExpect(jsonPath("$.data.productName")
                        .value("Smart Phone"))
                .andExpect(jsonPath("$.data.categoryName")
                        .value("Electronics"))
                .andExpect(jsonPath("$.data.price")
                        .value(699.99));

        verify(productService)
                .getProductById(eq(productId));
    }

    @Test
    void updateProduct_Success() throws Exception {

        Long productId = 1L;

        UpdateProductRequestDTO request =
                UpdateProductRequestDTO.builder()
                        .productName("Updated Smart Phone")
                        .description("Updated description with 256GB storage")
                        .brand("TechBrand")
                        .categoryId(1L)
                        .price(new BigDecimal("799.99"))
                        .stockQuantity(30)
                        .imageUrl("https://example.com/phone-updated.jpg")
                        .build();

        ProductResponseDTO response =
                ProductResponseDTO.builder()
                        .productId(productId)
                        .productName("Updated Smart Phone")
                        .description("Updated description with 256GB storage")
                        .brand("TechBrand")
                        .categoryId(1L)
                        .categoryName("Electronics")
                        .price(new BigDecimal("799.99"))
                        .stockQuantity(30)
                        .imageUrl("https://example.com/phone-updated.jpg")
                        .build();

        when(productService.updateProduct(eq(productId), any(UpdateProductRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/products/{productId}", productId)
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Product updated successfully"))
                .andExpect(jsonPath("$.data.productId")
                        .value(1))
                .andExpect(jsonPath("$.data.productName")
                        .value("Updated Smart Phone"))
                .andExpect(jsonPath("$.data.price")
                        .value(799.99))
                .andExpect(jsonPath("$.data.stockQuantity")
                        .value(30));

        verify(productService)
                .updateProduct(eq(productId), any(UpdateProductRequestDTO.class));
    }

    @Test
    void deleteProduct_Success() throws Exception {

        Long productId = 1L;

        DeleteProductResponseDTO response =
                DeleteProductResponseDTO.builder()
                        .productId(productId)
                        .productName("Smart Phone")
                        .state("DELETED")
                        .build();

        when(productService.deleteProduct(eq(productId)))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/api/v1/products/{productId}", productId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Product deleted successfully"))
                .andExpect(jsonPath("$.data.productId")
                        .value(1))
                .andExpect(jsonPath("$.data.productName")
                        .value("Smart Phone"))
                .andExpect(jsonPath("$.data.state")
                        .value("DELETED"));

        verify(productService)
                .deleteProduct(eq(productId));
    }

    @Test
    void getProductById_ProductNotFoundException() throws Exception {

        Long productId = 999L;

        when(productService.getProductById(eq(productId)))
                .thenThrow(
                        new ProductNotFoundException(
                                "Product Not Found"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/products/{productId}", productId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product Not Found"));

        verify(productService)
                .getProductById(eq(productId));
    }

    @Test
    void createProduct_CategoryNotFoundException() throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName("Smart Phone")
                        .description("Latest model smartphone with 128GB storage")
                        .brand("TechBrand")
                        .categoryId(999L)
                        .price(new BigDecimal("699.99"))
                        .stockQuantity(50)
                        .imageUrl("https://example.com/phone.jpg")
                        .build();

        when(productService.createProduct(any(CreateProductRequestDTO.class)))
                .thenThrow(
                        new CategoryNotFoundException(
                                "Category Not Found"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Category Not Found"));

        verify(productService)
                .createProduct(any(CreateProductRequestDTO.class));
    }
}
