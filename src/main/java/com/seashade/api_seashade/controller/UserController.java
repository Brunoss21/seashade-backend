package com.seashade.api_seashade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.seashade.api_seashade.controller.dto.CreateUserDto;
import com.seashade.api_seashade.controller.dto.UserResponseDto;
import com.seashade.api_seashade.model.User;

import com.seashade.api_seashade.service.UserService;




@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> newUser(@RequestBody CreateUserDto dto) {
        try {
            User createdUser = userService.registerUser(dto);
            // Converta a entidade para o DTO de resposta
            UserResponseDto responseDto = new UserResponseDto(createdUser); 
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (RuntimeException e) {
            // ... tratamento de erro
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
    }
}
/* 
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponseDto> newUser(@RequestBody CreateUserDto dto) {
        try {
            User createdUser = userService.registerUser(dto);
            UserResponseDto responseDto = new UserResponseDto(createdUser); 
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (RuntimeException e) {
            // Exemplo de tratamento de exceção
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
    }
}

*/

/* 
@RestController
public class UserController {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController(UserRepository userRepository, RoleRepository roleRepository){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    @PostMapping("/users")
    public ResponseEntity<Void> newUser(@RequestBody CreateUserDto dto){
        
        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());

        var userFromDb = userRepository.findByEmail(dto.email());
        if (userFromDb.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(bCryptPasswordEncoder.encode(dto.password()));
        user.setRoles(Set.of(basicRole));


        userRepository.save(user);

        return ResponseEntity.ok().build();

    }

}

*/
    

