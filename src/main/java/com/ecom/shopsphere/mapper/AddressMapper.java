package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateAddressRequestDTO;
import com.ecom.shopsphere.dto.response.AddressResponseDTO;
import com.ecom.shopsphere.entity.Address;

public interface AddressMapper {

    Address toEntity(
            AddAddressRequestDTO request);

    AddressResponseDTO toResponse(
            Address address);

    void updateAddressFromRequest(
            UpdateAddressRequestDTO request,
            Address address);
}