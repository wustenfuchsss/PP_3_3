package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
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
            List<User> users = userService.allUsers();
            mav.addObject("listUser", users);
            mav.addObject(new User());
            logger.info("Загружена страница администратора");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return mav;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id, Model model) {
        try {
            userService.deleteUser(id);
            logger.info("Пользователь удален");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return "redirect:/admin";
    }

    @GetMapping("/edit")
    public String editUserFrom(@RequestParam("id") Long id, Model model) {
        try {
            User user = userService.findUserById(id);
            model.addAttribute("user", user);
            logger.info("Страница изменения пользователя");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return "edit";
    }

    @PostMapping("/edit")
    public String edit(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return "redirect:/edit";
            }
            userService.updateUser(user);
            logger.info("Пользователь обновлен");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return "redirect:/admin";
    }

    @PostMapping("/new")
    public String saveUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        try {
            if (!bindingResult.hasErrors()) {
                userService.saveUser(user);
            }
            logger.info("Пользователь " + user.getUsername() + " добавлен");
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
