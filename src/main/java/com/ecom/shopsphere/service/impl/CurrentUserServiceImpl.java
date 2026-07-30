package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.UserNotFoundException;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        log.info("Logged-in user email: {}", email);

//        User user = userRepository.findByEmail("dummy@gmail.com")
//                .orElseThrow(() ->
//                        new UserNotFoundException("User not found."));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {

                    log.error("User not found: {}", email);

                    return new UserNotFoundException("User not found.");
                });
    }

}
