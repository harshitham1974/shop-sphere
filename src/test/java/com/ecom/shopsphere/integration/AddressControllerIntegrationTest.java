package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateAddressRequestDTO;
import com.ecom.shopsphere.repository.AddressRepository;
import com.ecom.shopsphere.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private String userToken;

    @BeforeEach
    void setup() throws Exception {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .fullName("Address User")
                .email("addressuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        userToken = extractToken("addressuser@gmail.com", "Password@123");
    }

    private String extractToken(String email, String password) throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .stringValue();
    }

    private Long createAddress(String fullName, boolean defaultAddress) throws Exception {
        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
                .fullName(fullName)
                .phoneNumber("9876543210")
                .addressLine1("123 Main Street")
                .addressLine2("Near Park")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .postalCode("400001")
                .defaultAddress(defaultAddress)
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/addresses")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("addressId")
                .asLong();
    }

    @Test
    void addAddress_Success() throws Exception {
        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
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

        mockMvc.perform(
                        post("/api/v1/addresses")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Address added successfully."))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.city").value("Mumbai"))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));

        assertEquals(1, addressRepository.count());
    }

    @Test
    void addAddress_InvalidPhoneNumber_BadRequest() throws Exception {
        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
                .fullName("John Doe")
                .phoneNumber("12345")
                .addressLine1("123 Main Street")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .postalCode("400001")
                .defaultAddress(false)
                .build();

        mockMvc.perform(
                        post("/api/v1/addresses")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.data.phoneNumber")
                        .value("Phone number must be a valid 10-digit Indian mobile number."));
    }

    @Test
    void getAllAddresses_WithTwoAddresses_ReturnsBoth() throws Exception {
        createAddress("John Doe", true);
        createAddress("Jane Doe", false);

        mockMvc.perform(
                        get("/api/v1/addresses")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Addresses fetched successfully."))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.data[1].fullName").value("Jane Doe"));
    }

    @Test
    void getAddressById_NotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/addresses/99999")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Address Not Found"));
    }

    @Test
    void updateAddress_Success() throws Exception {
        Long addressId = createAddress("John Doe", false);

        UpdateAddressRequestDTO request = UpdateAddressRequestDTO.builder()
                .fullName("John Updated")
                .phoneNumber("9876543211")
                .addressLine1("789 Pine Road")
                .addressLine2("Floor 2")
                .city("Delhi")
                .state("Delhi")
                .country("India")
                .postalCode("110001")
                .defaultAddress(true)
                .build();

        mockMvc.perform(
                        put("/api/v1/addresses/{addressId}", addressId)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Address updated successfully."))
                .andExpect(jsonPath("$.data.fullName").value("John Updated"))
                .andExpect(jsonPath("$.data.city").value("Delhi"))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));
    }

    @Test
    void setDefaultAddress_Success() throws Exception {
        Long oldDefaultAddressId = createAddress("John Doe", true);
        Long newDefaultAddressId = createAddress("Jane Doe", false);

        mockMvc.perform(
                        patch("/api/v1/addresses/{addressId}/default", newDefaultAddressId)
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Default address updated successfully."))
                .andExpect(jsonPath("$.data.addressId").value(newDefaultAddressId))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));

        mockMvc.perform(
                        get("/api/v1/addresses")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].addressId").exists())
                .andExpect(jsonPath("$.data[1].addressId").exists());

        assertTrue(addressRepository.findById(oldDefaultAddressId).orElseThrow().getDefaultAddress().equals(false));
        assertTrue(addressRepository.findById(newDefaultAddressId).orElseThrow().getDefaultAddress());
    }

    @Test
    void deleteAddress_Success() throws Exception {
        Long addressId = createAddress("John Doe", false);

        mockMvc.perform(
                        delete("/api/v1/addresses/{addressId}", addressId)
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Address deleted successfully."))
                .andExpect(jsonPath("$.data.state").value("DELETED"));

        assertEquals(0, addressRepository.count());
    }

    @Test
    void getAllAddresses_WithoutToken_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/addresses"))
                .andExpect(status().isForbidden());
    }
}
