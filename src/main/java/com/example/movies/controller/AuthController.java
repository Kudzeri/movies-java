/*
 * MIT License
 *
 * Copyright (c) 2025 Kudzeri
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.movies.controller;

import com.example.movies.model.Role;
import com.example.movies.model.User;
import com.example.movies.repository.RoleRepository;
import com.example.movies.repository.UserRepository;
import com.example.movies.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    // Пример регистрации пользователя
    @Operation(summary = "Регистрация пользователя", description = "Регистрирует нового пользователя с ролью ROLE_USER")
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password) {
        try {
            // Проверяем, нет ли уже такого пользователя
            if (userRepository.findByUsername(username) != null) {
                return "Пользователь с таким именем уже существует!";
            }

            // Допустим, всем новым пользователям даём роль ROLE_USER
            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = new Role("ROLE_USER");
                roleRepository.save(userRole);
            }

            User newUser = new User(username, passwordEncoder.encode(password));
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            newUser.setRoles(roles);

            userRepository.save(newUser);

            logger.info("Пользователь {} успешно зарегистрирован", username);
            return jwtUtils.generateToken(username);
        } catch(Exception ex) {
            logger.error("Ошибка регистрации пользователя: {}", username, ex);
            return "Ошибка регистрации";
        }
    }

    // Пример регистрации администратора
    @Operation(summary = "Регистрация администратора", description = "Регистрирует нового администратора с ролью ROLE_ADMIN")
    @PostMapping("/register-admin")
    public String registerAdmin(@RequestParam String username,
                                @RequestParam String password) {
        try {
            // Проверяем, нет ли уже такого пользователя
            if (userRepository.findByUsername(username) != null) {
                return "Пользователь с таким именем уже существует!";
            }

            // Допустим, всем новым администраторам даём роль ROLE_ADMIN
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Role("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }

            User newAdmin = new User(username, passwordEncoder.encode(password));
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            newAdmin.setRoles(roles);

            userRepository.save(newAdmin);

            logger.info("Администратор {} успешно зарегистрирован", username);
            return jwtUtils.generateToken(username);
        } catch(Exception ex) {
            logger.error("Ошибка регистрации администратора: {}", username, ex);
            return "Ошибка регистрации администратора";
        }
    }

    // Пример логина, возвращаем JWT-токен
    @Operation(summary = "Аутентификация пользователя", description = "Аутентифицирует пользователя и возвращает JWT токен")
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return "Неверные данные (user not found)";
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return "Неверный пароль";
            }

            // Генерируем JWT
            logger.info("Пользователь {} авторизован", username);
            return jwtUtils.generateToken(user.getUsername());
        } catch(Exception ex) {
            logger.error("Ошибка авторизации пользователя: {}", username, ex);
            return "Ошибка логина";
        }
    }
}
