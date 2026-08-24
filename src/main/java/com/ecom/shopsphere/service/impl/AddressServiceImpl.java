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
import com.ecom.shopsphere.service.AddressService;
import com.ecom.shopsphere.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    private final AddressMapper addressMapper;

    private final CurrentUserService currentUserService;

    @Override
    public AddressResponseDTO addAddress(AddAddressRequestDTO request) {

        log.info("Adding new address.");

        User user = currentUserService.getCurrentUser();

        if (Boolean.TRUE.equals(request.getDefaultAddress())) {

            addressRepository.findByUserUserIdAndDefaultAddressTrue(user.getUserId()).ifPresent(address -> {

                address.setDefaultAddress(false);

                addressRepository.save(address);

                log.info("Previous default address removed. Address ID: {}", address.getAddressId());
            });
        }

        Address address = addressMapper.toEntity(request);

        address.setUser(user);

        if (address.getDefaultAddress() == null) {
            address.setDefaultAddress(false);
        }

        Address savedAddress = addressRepository.save(address);

        log.info("Address added successfully. Address ID: {}", savedAddress.getAddressId());

        return addressMapper.toResponse(savedAddress);
    }

    @Override
    public List<AddressResponseDTO> getAllAddresses() {

        log.info("Fetching all addresses for logged-in user.");

        User user = currentUserService.getCurrentUser();

        List<Address> addresses = addressRepository.findByUserUserId(user.getUserId());

        log.info("{} address(es) found for user: {}", addresses.size(), user.getEmail());

        return addresses.stream().map(addressMapper::toResponse).toList();
    }

    @Override
    public AddressResponseDTO getAddressById(Long addressId) {

        log.info("Fetching address with ID: {}", addressId);

        User user = currentUserService.getCurrentUser();

        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId()).orElseThrow(() -> {

            log.error("Address not found. Address ID: {}", addressId);

            return new AddressNotFoundException("Address not found.");
        });

        log.info("Address fetched successfully. Address ID: {}", address.getAddressId());

        return addressMapper.toResponse(address);
    }

    @Override
    public AddressResponseDTO updateAddress(Long addressId, UpdateAddressRequestDTO request) {

        log.info("Updating address. Address ID: {}", addressId);

        User user = currentUserService.getCurrentUser();

        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId()).orElseThrow(() -> {

            log.error("Address not found. Address ID: {}", addressId);

            return new AddressNotFoundException("Address not found.");
        });


        if (Boolean.TRUE.equals(request.getDefaultAddress())) {

            addressRepository.findByUserUserIdAndDefaultAddressTrue(user.getUserId()).ifPresent(existingDefault -> {

                if (!existingDefault.getAddressId().equals(addressId)) {

                    existingDefault.setDefaultAddress(false);

                    addressRepository.save(existingDefault);

                    log.info("Previous default address removed. ID: {}", existingDefault.getAddressId());
                }
            });
        }


        addressMapper.updateAddressFromRequest(request, address);


        Address updatedAddress = addressRepository.save(address);


        log.info("Address updated successfully. Address ID: {}", updatedAddress.getAddressId());


        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    public DeleteAddressResponseDTO deleteAddress(Long addressId) {

        log.info("Deleting address. Address ID: {}", addressId);


        User user = currentUserService.getCurrentUser();


        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId()).orElseThrow(() -> {

            log.error("Address not found. Address ID: {}", addressId);

            return new AddressNotFoundException("Address not found.");
        });


        addressRepository.delete(address);


        log.info("Address deleted successfully. Address ID: {}", addressId);

        return DeleteAddressResponseDTO.builder().state("DELETED").build();
    }

    @Override
    public AddressResponseDTO setDefaultAddress(Long addressId) {

        log.info("Setting default address. Address ID: {}", addressId);


        User user = currentUserService.getCurrentUser();


        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId()).orElseThrow(() -> {

            log.error("Address not found. Address ID: {}", addressId);

            return new AddressNotFoundException("Address not found.");
        });


        addressRepository.findByUserUserIdAndDefaultAddressTrue(user.getUserId()).ifPresent(existingDefault -> {

            if (!existingDefault.getAddressId().equals(addressId)) {

                existingDefault.setDefaultAddress(false);

                addressRepository.save(existingDefault);

                log.info("Removed previous default address. ID: {}", existingDefault.getAddressId());
            }
        });


        address.setDefaultAddress(true);


        Address savedAddress = addressRepository.save(address);


        log.info("Default address updated successfully. ID: {}", savedAddress.getAddressId());


        return addressMapper.toResponse(savedAddress);
    }
}