package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateAddressRequestDTO;
import com.ecom.shopsphere.dto.response.AddressResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteAddressResponseDTO;
import com.ecom.shopsphere.exception.AddressNotFoundException;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void addAddress_Success() throws Exception {

        AddAddressRequestDTO request =
                AddAddressRequestDTO.builder()
                        .fullName("John Doe")
                        .phoneNumber("9876543210")
                        .addressLine1("123 Main Street")
                        .addressLine2("Apt 4B")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .postalCode("400001")
                        .defaultAddress(true)
                        .build();

        AddressResponseDTO response =
                AddressResponseDTO.builder()
                        .addressId(1L)
                        .fullName("John Doe")
                        .phoneNumber("9876543210")
                        .addressLine1("123 Main Street")
                        .addressLine2("Apt 4B")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .postalCode("400001")
                        .defaultAddress(true)
                        .build();

        when(addressService.addAddress(any(AddAddressRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/addresses")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Address added successfully."))
                .andExpect(jsonPath("$.data.addressId")
                        .value(1))
                .andExpect(jsonPath("$.data.fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.data.phoneNumber")
                        .value("9876543210"))
                .andExpect(jsonPath("$.data.addressLine1")
                        .value("123 Main Street"))
                .andExpect(jsonPath("$.data.addressLine2")
                        .value("Apt 4B"))
                .andExpect(jsonPath("$.data.city")
                        .value("Mumbai"))
                .andExpect(jsonPath("$.data.state")
                        .value("Maharashtra"))
                .andExpect(jsonPath("$.data.country")
                        .value("India"))
                .andExpect(jsonPath("$.data.postalCode")
                        .value("400001"))
                .andExpect(jsonPath("$.data.defaultAddress")
                        .value(true));

        verify(addressService)
                .addAddress(any(AddAddressRequestDTO.class));
    }

    @Test
    void getAllAddresses_Success() throws Exception {

        AddressResponseDTO address1 =
                AddressResponseDTO.builder()
                        .addressId(1L)
                        .fullName("John Doe")
                        .phoneNumber("9876543210")
                        .addressLine1("123 Main Street")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .postalCode("400001")
                        .defaultAddress(true)
                        .build();

        AddressResponseDTO address2 =
                AddressResponseDTO.builder()
                        .addressId(2L)
                        .fullName("Jane Doe")
                        .phoneNumber("9876543211")
                        .addressLine1("456 Oak Avenue")
                        .city("Pune")
                        .state("Maharashtra")
                        .country("India")
                        .postalCode("411001")
                        .defaultAddress(false)
                        .build();

        List<AddressResponseDTO> response = List.of(address1, address2);

        when(addressService.getAllAddresses())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/addresses")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Addresses fetched successfully."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].addressId")
                        .value(1))
                .andExpect(jsonPath("$.data[0].fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.data[1].addressId")
                        .value(2))
                .andExpect(jsonPath("$.data[1].fullName")
                        .value("Jane Doe"));

        verify(addressService)
                .getAllAddresses();
    }

    @Test
    void getAddressById_Success() throws Exception {

        AddressResponseDTO response =
                AddressResponseDTO.builder()
                        .addressId(1L)
                        .fullName("John Doe")
                        .phoneNumber("9876543210")
                        .addressLine1("123 Main Street")
                        .addressLine2("Apt 4B")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .postalCode("400001")
                        .defaultAddress(true)
                        .build();

        when(addressService.getAddressById(eq(1L)))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/addresses/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Address fetched successfully."))
                .andExpect(jsonPath("$.data.addressId")
                        .value(1))
                .andExpect(jsonPath("$.data.fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.data.phoneNumber")
                        .value("9876543210"))
                .andExpect(jsonPath("$.data.addressLine1")
                        .value("123 Main Street"))
                .andExpect(jsonPath("$.data.city")
                        .value("Mumbai"))
                .andExpect(jsonPath("$.data.state")
                        .value("Maharashtra"))
                .andExpect(jsonPath("$.data.country")
                        .value("India"))
                .andExpect(jsonPath("$.data.postalCode")
                        .value("400001"))
                .andExpect(jsonPath("$.data.defaultAddress")
                        .value(true));

        verify(addressService)
                .getAddressById(eq(1L));
    }

    @Test
    void updateAddress_Success() throws Exception {

        UpdateAddressRequestDTO request =
                UpdateAddressRequestDTO.builder()
                        .fullName("John Doe Updated")
                        .phoneNumber("9876543210")
                        .addressLine1("789 Pine Road")
                        .addressLine2("Floor 2")
                        .city("Delhi")
                        .state("Delhi")
                        .country("India")
                        .postalCode("110001")
                        .defaultAddress(false)
                        .build();

        AddressResponseDTO response =
                AddressResponseDTO.builder()
                        .addressId(1L)
                        .fullName("John Doe Updated")
                        .phoneNumber("9876543210")
                        .addressLine1("789 Pine Road")
                        .addressLine2("Floor 2")
                        .city("Delhi")
                        .state("Delhi")
                        .country("India")
                        .postalCode("110001")
                        .defaultAddress(false)
                        .build();

        when(addressService.updateAddress(eq(1L), any(UpdateAddressRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/addresses/1")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Address updated successfully."))
                .andExpect(jsonPath("$.data.addressId")
                        .value(1))
                .andExpect(jsonPath("$.data.fullName")
                        .value("John Doe Updated"))
                .andExpect(jsonPath("$.data.addressLine1")
                        .value("789 Pine Road"))
                .andExpect(jsonPath("$.data.city")
                        .value("Delhi"))
                .andExpect(jsonPath("$.data.state")
                        .value("Delhi"))
                .andExpect(jsonPath("$.data.postalCode")
                        .value("110001"))
                .andExpect(jsonPath("$.data.defaultAddress")
                        .value(false));

        verify(addressService)
                .updateAddress(eq(1L), any(UpdateAddressRequestDTO.class));
    }

    @Test
    void deleteAddress_Success() throws Exception {

        DeleteAddressResponseDTO response =
                DeleteAddressResponseDTO.builder()
                        .state("DELETED")
                        .build();

        when(addressService.deleteAddress(eq(1L)))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/api/v1/addresses/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Address deleted successfully."))
                .andExpect(jsonPath("$.data.state")
                        .value("DELETED"));

        verify(addressService)
                .deleteAddress(eq(1L));
    }

    @Test
    void setDefaultAddress_Success() throws Exception {

        AddressResponseDTO response =
                AddressResponseDTO.builder()
                        .addressId(1L)
                        .fullName("John Doe")
                        .phoneNumber("9876543210")
                        .addressLine1("123 Main Street")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .postalCode("400001")
                        .defaultAddress(true)
                        .build();

        when(addressService.setDefaultAddress(eq(1L)))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/addresses/1/default")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Default address updated successfully."))
                .andExpect(jsonPath("$.data.addressId")
                        .value(1))
                .andExpect(jsonPath("$.data.defaultAddress")
                        .value(true));

        verify(addressService)
                .setDefaultAddress(eq(1L));
    }

    @Test
    void getAddressById_AddressNotFound() throws Exception {

        when(addressService.getAddressById(eq(99L)))
                .thenThrow(
                        new AddressNotFoundException(
                                "Address not found with id: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/addresses/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Address Not Found"));

        verify(addressService)
                .getAddressById(eq(99L));
    }

    @Test
    void updateAddress_AddressNotFound() throws Exception {

        UpdateAddressRequestDTO request =
                UpdateAddressRequestDTO.builder()
                        .fullName("John Doe")
                        .phoneNumber("9876543210")
                        .addressLine1("123 Main Street")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .postalCode("400001")
                        .build();

        when(addressService.updateAddress(eq(99L), any(UpdateAddressRequestDTO.class)))
                .thenThrow(
                        new AddressNotFoundException(
                                "Address not found with id: 99"
                        )
                );

        mockMvc.perform(
                        put("/api/v1/addresses/99")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Address Not Found"));

        verify(addressService)
                .updateAddress(eq(99L), any(UpdateAddressRequestDTO.class));
    }

    @Test
    void deleteAddress_AddressNotFound() throws Exception {

        when(addressService.deleteAddress(eq(99L)))
                .thenThrow(
                        new AddressNotFoundException(
                                "Address not found with id: 99"
                        )
                );

        mockMvc.perform(
                        delete("/api/v1/addresses/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Address Not Found"));

        verify(addressService)
                .deleteAddress(eq(99L));
    }

    @Test
    void setDefaultAddress_AddressNotFound() throws Exception {

        when(addressService.setDefaultAddress(eq(99L)))
                .thenThrow(
                        new AddressNotFoundException(
                                "Address not found with id: 99"
                        )
                );

        mockMvc.perform(
                        patch("/api/v1/addresses/99/default")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Address Not Found"));

        verify(addressService)
                .setDefaultAddress(eq(99L));
    }
}
