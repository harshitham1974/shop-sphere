package com.ecom.shopsphere.mapper.impl;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateAddressRequestDTO;
import com.ecom.shopsphere.dto.response.AddressResponseDTO;
import com.ecom.shopsphere.entity.Address;
import com.ecom.shopsphere.mapper.AddressMapper;
import org.springframework.stereotype.Component;

@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toEntity(
            AddAddressRequestDTO request) {

        return Address.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .defaultAddress(request.getDefaultAddress())
                .build();
    }

    @Override
    public AddressResponseDTO toResponse(
            Address address) {

        return AddressResponseDTO.builder()
                .addressId(address.getAddressId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .defaultAddress(address.getDefaultAddress())
                .build();
    }

    @Override
    public void updateAddressFromRequest(
            UpdateAddressRequestDTO request,
            Address address) {

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setDefaultAddress(request.getDefaultAddress());
    }
}