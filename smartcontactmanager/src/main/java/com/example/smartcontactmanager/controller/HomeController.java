package com.example.smartcontactmanager.controller;

import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import com.example.smartcontactmanager.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    // home controller
    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    // about controller
    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    // custom login

    @GetMapping("/signin")
    public String customLogin(Model model) {
        model.addAttribute("title", "Login page");
        return "login";
    }

    // signup controller
    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    // handler for register user
    @RequestMapping(value = "/do_register", method = RequestMethod.POST)
    public String registerUser(
            @Valid
            @ModelAttribute("user") User user,BindingResult result1,
            @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
            RedirectAttributes redirectAttributes, ModelMap modelMap) {

        try {
            if (!agreement) {
                throw new Exception("You must accept terms and conditions");
            }

            if (result1.hasErrors()) {
                System.out.println("Error "+result1.toString());
                modelMap.addAttribute("user",user);
                return "signup";
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            User result = this.userRepository.save(user);

            redirectAttributes.addFlashAttribute("message",
                    new Message("Successfully Registered !!", "alert-success"));
            return "redirect:/signup";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message",
                    new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
            return "redirect:/signup";
        }
    }
}
