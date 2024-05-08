package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.kata.spring.boot_security.demo.entity.User;

import java.util.Arrays;
import java.util.logging.Logger;

@Controller
public class UserContoller {

    private final Logger logger = Logger.getLogger(UserContoller.class.getName());

    @GetMapping("/user")
    public ModelAndView showUser() {
        ModelAndView modelAndView = new ModelAndView();
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            modelAndView.setViewName("user");
            modelAndView.addObject("curUser", user);
            logger.info("Загружена страница пользователя");
        } catch (Exception e) {
            logger.warning("Ошбибка" + Arrays.toString(e.getStackTrace()));
        }
        return modelAndView;
    }
}
