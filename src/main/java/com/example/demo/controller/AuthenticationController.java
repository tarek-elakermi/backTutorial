package com.example.demo.controller;


import com.example.demo.model.Role;
import com.example.demo.model.RoleName;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserREpository;
import com.example.demo.request.Login;
import com.example.demo.request.Register;
import com.example.demo.response.JwtResponse;
import com.example.demo.response.ResponseMessage;
import com.example.demo.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserREpository userREpository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody Register registerRequest) {
        if (userREpository.existsByUsername(registerRequest.getUsername())) {
            return new ResponseEntity(new ResponseMessage("Username is already taken !"), HttpStatus.BAD_REQUEST);
        }
        if (userREpository.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity(new ResponseMessage("Email is already taken !"), HttpStatus.BAD_REQUEST);
        }
        //create user account
        User user = new User(registerRequest.getName(), registerRequest.getUsername(), registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        Set<String> rolesInRequest = registerRequest.getRole();
        Set<Role> roles = new HashSet<>();
        rolesInRequest.forEach(
                role -> {
                    switch (role) {
                        case "SUPER_ADMIN" :
                            Role auperAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("role not found"));
                            roles.add(auperAdminRole);
                            break;
                        /*case "ENTREPRISE_MANAGER" :
                            Role Entreprise_managerRole = roleRepository.findByName(RoleName.ENTREPRISE_MANAGER)
                                    .orElseThrow(() -> new RuntimeException("role not found"));
                            roles.add(Entreprise_managerRole);
                            break;*/
                        default:
                            Role candidatRole = roleRepository.findByName(RoleName.CANDIDAT)
                                    .orElseThrow(() -> new RuntimeException("role not found"));
                            roles.add(candidatRole);
                    }
                });
        user.setRole(roles);
        userREpository.save(user);
        return new ResponseEntity<>(new ResponseMessage("UserRegistred successfuly"), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody Login loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername()
                ,loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(jwt,userDetails.getUsername(), userDetails.getAuthorities()));
    }





}
