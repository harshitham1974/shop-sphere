package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateAddressRequestDTO;
import com.ecom.shopsphere.dto.response.AddressResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteAddressResponseDTO;
import com.ecom.shopsphere.entity.Address;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.AddressNotFoundException;
import com.ecom.shopsphere.mapper.AddressMapper;
import com.ecom.shopsphere.repository.AddressRepository;
import com.ecom.shopsphere.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @InjectMocks
    private AddressServiceImpl addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Test
    void addAddress_Success() {
        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(false)
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address = Address.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(false)
                .build();
        Address savedAddress = Address.builder()
                .addressId(1L)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(false)
                .user(user)
                .build();
        AddressResponseDTO response = AddressResponseDTO.builder()
                .addressId(1L)
                .fullName("John Doe")
                .defaultAddress(false)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressMapper.toEntity(request)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(savedAddress);
        when(addressMapper.toResponse(savedAddress)).thenReturn(response);

        AddressResponseDTO result = addressService.addAddress(request);

        assertNotNull(result);
        assertEquals(1L, result.getAddressId());
        assertEquals("John Doe", result.getFullName());
        assertFalse(result.getDefaultAddress());

        verify(currentUserService).getCurrentUser();
        verify(addressMapper).toEntity(request);
        verify(addressRepository).save(address);
        verify(addressMapper).toResponse(savedAddress);
    }

    @Test
    void addAddress_NullDefaultDefaultsToFalse() {
        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(null)
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address = Address.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(null)
                .build();
        Address savedAddress = Address.builder()
                .addressId(1L)
                .fullName("John Doe")
                .defaultAddress(false)
                .user(user)
                .build();
        AddressResponseDTO response = AddressResponseDTO.builder()
                .addressId(1L)
                .fullName("John Doe")
                .defaultAddress(false)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressMapper.toEntity(request)).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
        when(addressMapper.toResponse(savedAddress)).thenReturn(response);

        AddressResponseDTO result = addressService.addAddress(request);

        assertNotNull(result);
        assertFalse(result.getDefaultAddress());

        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void addAddress_WithDefaultAddress_UnsetsExistingDefault() {

        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("456 New St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560002")
                .defaultAddress(true)
                .build();

        User user = User.builder()
                .userId(1L)
                .email("test@gmail.com")
                .build();

        Address existingDefault = Address.builder()
                .addressId(1L)
                .fullName("John Doe")
                .addressLine1("123 Old St")
                .defaultAddress(true)
                .user(user)
                .build();

        Address newAddress = Address.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("456 New St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560002")
                .defaultAddress(true)
                .build();

        AddressResponseDTO response = AddressResponseDTO.builder()
                .addressId(2L)
                .fullName("John Doe")
                .defaultAddress(true)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(addressRepository.findByUserUserIdAndDefaultAddressTrue(1L))
                .thenReturn(Optional.of(existingDefault));

        when(addressMapper.toEntity(request))
                .thenReturn(newAddress);

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // IMPORTANT: service passes newAddress here
        when(addressMapper.toResponse(newAddress))
                .thenReturn(response);

        AddressResponseDTO result = addressService.addAddress(request);

        assertNotNull(result);
        assertTrue(result.getDefaultAddress());

        verify(addressRepository)
                .findByUserUserIdAndDefaultAddressTrue(1L);

        verify(addressRepository)
                .save(existingDefault);

        assertFalse(existingDefault.getDefaultAddress());

        verify(addressMapper)
                .toEntity(request);

        verify(addressRepository)
                .save(newAddress);

        verify(addressMapper)
                .toResponse(newAddress);
    }

    @Test
    void addAddress_WithDefaultAddress_NoExistingDefault() {
        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(true)
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address = Address.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(true)
                .build();
        Address savedAddress = Address.builder()
                .addressId(1L)
                .fullName("John Doe")
                .defaultAddress(true)
                .user(user)
                .build();
        AddressResponseDTO response = AddressResponseDTO.builder()
                .addressId(1L)
                .fullName("John Doe")
                .defaultAddress(true)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByUserUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.empty());
        when(addressMapper.toEntity(request)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(savedAddress);
        when(addressMapper.toResponse(savedAddress)).thenReturn(response);

        AddressResponseDTO result = addressService.addAddress(request);

        assertNotNull(result);
        assertTrue(result.getDefaultAddress());

        verify(addressRepository).findByUserUserIdAndDefaultAddressTrue(1L);
        verify(addressMapper).toEntity(request);
        verify(addressRepository).save(address);
    }

    @Test
    void getAllAddresses_Success() {
        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address1 = Address.builder().addressId(1L).fullName("John Doe").city("Bangalore").build();
        Address address2 = Address.builder().addressId(2L).fullName("Jane Doe").city("Mumbai").build();
        AddressResponseDTO response1 = AddressResponseDTO.builder().addressId(1L).fullName("John Doe").city("Bangalore").build();
        AddressResponseDTO response2 = AddressResponseDTO.builder().addressId(2L).fullName("Jane Doe").city("Mumbai").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByUserUserId(1L)).thenReturn(List.of(address1, address2));
        when(addressMapper.toResponse(address1)).thenReturn(response1);
        when(addressMapper.toResponse(address2)).thenReturn(response2);

        List<AddressResponseDTO> result = addressService.getAllAddresses();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals("Jane Doe", result.get(1).getFullName());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByUserUserId(1L);
    }

    @Test
    void getAllAddresses_Empty() {
        User user = User.builder().userId(1L).email("test@gmail.com").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByUserUserId(1L)).thenReturn(List.of());

        List<AddressResponseDTO> result = addressService.getAllAddresses();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByUserUserId(1L);
    }

    @Test
    void getAddressById_Success() {
        Long addressId = 1L;
        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address = Address.builder().addressId(addressId).fullName("John Doe").city("Bangalore").build();
        AddressResponseDTO response = AddressResponseDTO.builder().addressId(addressId).fullName("John Doe").city("Bangalore").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.of(address));
        when(addressMapper.toResponse(address)).thenReturn(response);

        AddressResponseDTO result = addressService.getAddressById(addressId);

        assertNotNull(result);
        assertEquals(addressId, result.getAddressId());
        assertEquals("John Doe", result.getFullName());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressMapper).toResponse(address);
    }

    @Test
    void getAddressById_NotFound() {
        Long addressId = 99L;
        User user = User.builder().userId(1L).email("test@gmail.com").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.empty());

        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class,
                () -> addressService.getAddressById(addressId));

        assertEquals("Address not found.", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressMapper, never()).toResponse(any(Address.class));
    }

    @Test
    void updateAddress_Success() {
        Long addressId = 1L;
        UpdateAddressRequestDTO request = UpdateAddressRequestDTO.builder()
                .fullName("John Updated")
                .phoneNumber("9876543210")
                .addressLine1("456 Updated St")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .postalCode("400001")
                .defaultAddress(false)
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address = Address.builder().addressId(addressId).fullName("John Doe").city("Bangalore").defaultAddress(false).user(user).build();
        Address updatedAddress = Address.builder().addressId(addressId).fullName("John Updated").city("Mumbai").defaultAddress(false).user(user).build();
        AddressResponseDTO response = AddressResponseDTO.builder().addressId(addressId).fullName("John Updated").city("Mumbai").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(address)).thenReturn(updatedAddress);
        when(addressMapper.toResponse(updatedAddress)).thenReturn(response);

        AddressResponseDTO result = addressService.updateAddress(addressId, request);

        assertNotNull(result);
        assertEquals("John Updated", result.getFullName());
        assertEquals("Mumbai", result.getCity());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressMapper).updateAddressFromRequest(request, address);
        verify(addressRepository).save(address);
        verify(addressMapper).toResponse(updatedAddress);
    }

    @Test
    void updateAddress_NotFound() {
        Long addressId = 99L;
        UpdateAddressRequestDTO request = UpdateAddressRequestDTO.builder()
                .fullName("John Updated")
                .phoneNumber("9876543210")
                .addressLine1("456 Updated St")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .postalCode("400001")
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.empty());

        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class,
                () -> addressService.updateAddress(addressId, request));

        assertEquals("Address not found.", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressMapper, never()).updateAddressFromRequest(any(), any());
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void updateAddress_WithDefaultAddress_UnsetsExistingDefault() {
        Long addressId = 2L;
        UpdateAddressRequestDTO request = UpdateAddressRequestDTO.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("456 New St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560002")
                .defaultAddress(true)
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address existingDefault = Address.builder().addressId(1L).fullName("John Doe").defaultAddress(true).user(user).build();
        Address address = Address.builder().addressId(addressId).fullName("John Doe").defaultAddress(false).user(user).build();
        Address updatedAddress = Address.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).user(user).build();
        AddressResponseDTO response = AddressResponseDTO.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.of(address));
        when(addressRepository.findByUserUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(addressMapper.toResponse(address))
                .thenReturn(response);

        AddressResponseDTO result = addressService.updateAddress(addressId, request);

        assertNotNull(result);
        assertTrue(result.getDefaultAddress());

        verify(addressRepository).findByUserUserIdAndDefaultAddressTrue(1L);
        verify(addressRepository).save(existingDefault);
        assertFalse(existingDefault.getDefaultAddress());
        verify(addressMapper).updateAddressFromRequest(request, address);
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_WithDefaultAddress_SameAddressAlreadyDefault() {
        Long addressId = 1L;
        UpdateAddressRequestDTO request = UpdateAddressRequestDTO.builder()
                .fullName("John Updated")
                .phoneNumber("9876543210")
                .addressLine1("456 Updated St")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .postalCode("560001")
                .defaultAddress(true)
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address existingDefault = Address.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).user(user).build();
        Address updatedAddress = Address.builder().addressId(addressId).fullName("John Updated").defaultAddress(true).user(user).build();
        AddressResponseDTO response = AddressResponseDTO.builder().addressId(addressId).fullName("John Updated").defaultAddress(true).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.findByUserUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(existingDefault)).thenReturn(updatedAddress);
        when(addressMapper.toResponse(updatedAddress)).thenReturn(response);

        AddressResponseDTO result = addressService.updateAddress(addressId, request);

        assertNotNull(result);
        assertTrue(result.getDefaultAddress());

        verify(addressRepository).findByUserUserIdAndDefaultAddressTrue(1L);
        verify(addressRepository, never()).save(argThat(a -> !a.getAddressId().equals(addressId)));
    }

    @Test
    void deleteAddress_Success() {
        Long addressId = 1L;
        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address = Address.builder().addressId(addressId).fullName("John Doe").user(user).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.of(address));

        DeleteAddressResponseDTO result = addressService.deleteAddress(addressId);

        assertNotNull(result);
        assertEquals("DELETED", result.getState());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressRepository).delete(address);
    }

    @Test
    void deleteAddress_NotFound() {
        Long addressId = 99L;
        User user = User.builder().userId(1L).email("test@gmail.com").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.empty());

        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class,
                () -> addressService.deleteAddress(addressId));

        assertEquals("Address not found.", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressRepository, never()).delete(any(Address.class));
    }

    @Test
    void setDefaultAddress_Success() {
        Long addressId = 2L;
        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address existingDefault = Address.builder().addressId(1L).fullName("John Doe").defaultAddress(true).user(user).build();
        Address address = Address.builder().addressId(addressId).fullName("John Doe").defaultAddress(false).user(user).build();
        Address savedAddress = Address.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).user(user).build();
        AddressResponseDTO response = AddressResponseDTO.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.of(address));
        when(addressRepository.findByUserUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation ->
                invocation.getArgument(0));
        when(addressMapper.toResponse(savedAddress)).thenReturn(response);

        AddressResponseDTO result = addressService.setDefaultAddress(addressId);

        assertNotNull(result);
        assertTrue(result.getDefaultAddress());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressRepository).findByUserUserIdAndDefaultAddressTrue(1L);
        verify(addressRepository).save(existingDefault);
        assertFalse(existingDefault.getDefaultAddress());
        verify(addressRepository).save(address);
        assertTrue(address.getDefaultAddress());
        verify(addressMapper).toResponse(savedAddress);
    }

    @Test
    void setDefaultAddress_NotFound() {
        Long addressId = 99L;
        User user = User.builder().userId(1L).email("test@gmail.com").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.empty());

        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class,
                () -> addressService.setDefaultAddress(addressId));

        assertEquals("Address not found.", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(addressRepository).findByAddressIdAndUserUserId(addressId, 1L);
        verify(addressRepository, never()).findByUserUserIdAndDefaultAddressTrue(anyLong());
        verify(addressRepository, never()).save(any(Address.class));
        verify(addressMapper, never()).toResponse(any(Address.class));
    }

    @Test
    void setDefaultAddress_SameAddressAlreadyDefault() {
        Long addressId = 1L;
        User user = User.builder().userId(1L).email("test@gmail.com").build();
        Address address = Address.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).user(user).build();
        Address savedAddress = Address.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).user(user).build();
        AddressResponseDTO response = AddressResponseDTO.builder().addressId(addressId).fullName("John Doe").defaultAddress(true).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByAddressIdAndUserUserId(addressId, 1L)).thenReturn(Optional.of(address));
        when(addressRepository.findByUserUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(address)).thenReturn(savedAddress);
        when(addressMapper.toResponse(savedAddress)).thenReturn(response);

        AddressResponseDTO result = addressService.setDefaultAddress(addressId);

        assertNotNull(result);
        assertTrue(result.getDefaultAddress());

        verify(addressRepository).findByUserUserIdAndDefaultAddressTrue(1L);
        verify(addressRepository, never()).save(argThat(a -> !a.getAddressId().equals(addressId)));
        verify(addressRepository).save(address);
        assertTrue(address.getDefaultAddress());
    }
}
