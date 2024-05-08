package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.logging.Logger;

@Controller
public class AdminController {
    private final UserService userService;


    private final Logger logger = Logger.getLogger(AdminController.class.getName());

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    public ModelAndView adminPage() {
        ModelAndView mav = new ModelAndView("admin");
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<User> users = userService.allUsers();
            mav.addObject("listUser", users);
            mav.addObject("curUser", user);
            mav.addObject("newUser", new User());
            logger.info("Загружена страница администратора");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return mav;
    }

    @PostMapping("/admin/{id}")
    public String saveEdit(@PathVariable(name = "id") Long id, @ModelAttribute("user") @Valid User user, String role, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return "redirect:/admin";
            }
            if (role.equals("user")) {
                user.setRoles(Collections.singleton(new Role(1L, "ROLE_USER")));
            } else if (role.equals("admin")) {
                Set<Role> roleSet = new HashSet<>();
                roleSet.add(new Role(1L, "ROLE_USER"));
                roleSet.add(new Role(0L, "ROLE_ADMIN"));
                user.setRoles(roleSet);
            }
            userService.updateUser(user);
            logger.info("Пользователь обновлен");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return "redirect:/admin";
    }
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") Long id, @ModelAttribute("user") @Valid User user, String role, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return "redirect:/admin";
            }
            userService.deleteUser(user.getId());
            logger.info("Пользователь удален");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return "redirect:/admin";
    }

    @PostMapping("/new")
    public String saveUser(@ModelAttribute("user") @Valid User user, @RequestParam("role") String role) {
        try {
            if (role.equals("user")) {
                userService.saveUser(user);
                logger.info("Пользователь " + user.getUsername() + " добавлен");
            } else if (role.equals("admin")) {
                userService.saveAdminUser(user);
                logger.info("Пользователь с правами админа " + user.getUsername() + " добавлен");
            }
        } catch (Exception e) {
            logger.warning("Ошибка: " + Arrays.toString(e.getStackTrace()));
        }
        return "redirect:/admin";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                request.getSession().invalidate();
            }
            logger.info("Пользователь вышел из системы");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return "redirect:/";
    }

}
