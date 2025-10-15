package com.example.smartcontactmanager.controller;

import com.example.smartcontactmanager.entities.Contact;
import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import com.example.smartcontactmanager.repository.ContactRepository;
import com.example.smartcontactmanager.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    // method for adding common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {

        String userName = principal.getName();
        System.out.println("USERNAME:"+userName);

        // get the user using username(Email)
        User user = userRepository.getUserByUserName(userName);

        System.out.println("USER:"+user);

        model.addAttribute("user", user);


    }

    // dashboard home

    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal, HttpSession session) {

        model.addAttribute("title", "User Dashboard");

        // ✅ Remove any leftover message when visiting home
        session.removeAttribute("message");

        return "normal/user_dashboard";
    }


    // open add form handler

    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){

        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());

        return "normal/add_contact_form";
    }

    // processing add contact form

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,
                                 HttpSession session,
                                 Model model) {

        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);


            if (!file.isEmpty()) {
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/image").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            user.getContacts().add(contact);
            contact.setUser(user);
            this.userRepository.save(user);

            // ✅ store message temporarily
            session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !! Try again..", "danger"));
        }

        // ✅ show add contact form again (message will appear there)
        return "normal/add_contact_form";
    }

    // show contacts handler
    //per page = 5[n]
    //current page = 0[page]

    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
        m.addAttribute("title", "Show User Contacts");

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        // currentPage = page, contacts per page = 5
        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/show_contacts";
    }


}
