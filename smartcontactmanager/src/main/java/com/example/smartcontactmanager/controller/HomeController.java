package com.example.smartcontactmanager.controller;

import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import com.example.smartcontactmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

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
            @ModelAttribute("user") User user,
            @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
            RedirectAttributes redirectAttributes) {

        try {
            if (!agreement) {
                throw new Exception("You must accept terms and conditions");
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");

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
