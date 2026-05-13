package com.amrat.JwtAuth.service;

import com.amrat.JwtAuth.dto.UserDetailsDto;
import com.amrat.JwtAuth.repository.UserRepository;
import com.amrat.JwtAuth.dto.RegistrationRequestDto;
import com.amrat.JwtAuth.entity.User;
import com.amrat.JwtAuth.entity.type.Role;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Transactional
    public User registerUser(RegistrationRequestDto registrationRequestDto) {
        if (isUserExists(registrationRequestDto.getUsername())) {
            throw new DuplicateKeyException("Username is already in use.");
        }

        String hashedPassword = passwordEncoder.encode(registrationRequestDto.getPassword());

        String username = registrationRequestDto.getUsername();

        User user = new User(registrationRequestDto.getFullName(), username, hashedPassword, Role.USER);
        return userRepository.save(user);
    }

    public boolean isUserExists(String username) {
        return userRepository.existsByUsername(username);
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found."));
    }

    public UserDetailsDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return modelMapper.map(user, UserDetailsDto.class);
    }

}
