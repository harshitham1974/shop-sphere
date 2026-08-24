package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateAddressRequestDTO;
import com.ecom.shopsphere.dto.response.AddressResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteAddressResponseDTO;

import java.util.List;

public interface AddressService {

    AddressResponseDTO addAddress(
            AddAddressRequestDTO request);

    List<AddressResponseDTO> getAllAddresses();

    AddressResponseDTO getAddressById(
            Long addressId);

    AddressResponseDTO updateAddress(
            Long addressId,
            UpdateAddressRequestDTO request);

    DeleteAddressResponseDTO deleteAddress(
            Long addressId);

    AddressResponseDTO setDefaultAddress(
            Long addressId);
}