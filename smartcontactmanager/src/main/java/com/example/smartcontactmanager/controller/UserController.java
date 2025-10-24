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
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    // ✅ Add user details to model for every request
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        model.addAttribute("user", user);
    }

    // ✅ Dashboard home
    @GetMapping("/index")
    public String dashboard(Model model, HttpSession session) {
        model.addAttribute("title", "User Dashboard");
        session.removeAttribute("message"); // Clear old message
        return "normal/user_dashboard";
    }

    // ✅ Open Add Contact form
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    // ✅ Process contact form submission
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,
                                 HttpSession session) {

        try {
            String userName = principal.getName();
            User user = userRepository.getUserByUserName(userName);

            // ✅ Handle image upload
            if (file.isEmpty()) {
                System.out.println("File is empty, setting default image...");
                contact.setImage("contact.png");
            } else {
                contact.setImage(file.getOriginalFilename());
                File saveDir = new ClassPathResource("static/image").getFile();

                Path path = Paths.get(saveDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image uploaded successfully at: " + path);
            }

            // ✅ Set contact’s user and save
            contact.setUser(user);
            user.getContacts().add(contact);
            userRepository.save(user);

            session.setAttribute("message", new Message("Your contact has been added successfully!", "success"));

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong. Please try again!", "danger"));
        }

        return "normal/add_contact_form";
    }

    // ✅ Show contacts with pagination
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page,
                               Model model,
                               Principal principal) {

        model.addAttribute("title", "Show User Contacts");

        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);

        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(), pageable);

        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/show_contacts";
    }

    //showing particular contact details


    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal){
        System.out.println("CID: " + cId);

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if (user.getId() == contact.getUser().getId())
            model.addAttribute("contact", contact);

        return "normal/contact_detail";
    }

    // delete contact handler

    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session, Principal principal) {

        System.out.println("CID: " + cId);

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);

        //check...Assignment
        //delete old photo

        User user = this.userRepository.getUserByUserName(principal.getName());

        user.getContacts().remove(contactOptional.get());

        this.userRepository.save(user);



        System.out.println("Deleted");
        session.setAttribute("message", new Message("Contact deleted successfully!", "success"));

        return "redirect:/user/show-contacts/0";
    }

    //open update form handler

    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model m){

        m.addAttribute("title","Update Contact");

        Contact contact = this.contactRepository.findById(cid).get();

        m.addAttribute("contact", contact);

        return "normal/update_form";
    }

    //update contact handler

    @RequestMapping(value="/process-update",method=RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m, HttpSession session, Principal principal){

        try {

            //old contact details
            Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

            //image..
            if (!file.isEmpty()) {
                //file work
                //rewrite

                //delete old photo

                File deleteFile = new ClassPathResource("static/image").getFile();
                File file1 = new File(deleteFile, oldContactDetail.getImage());
                file1.delete();


                //update new photo

                File saveFile = new ClassPathResource("static/image").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());

            }else
            {
                contact.setImage(oldContactDetail.getImage());
            }

            User user = this.userRepository.getUserByUserName(principal.getName());

            contact.setUser(user);

            this.contactRepository.save(contact);

            session.setAttribute("message", new Message("Your contact is update....", "success"));



        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("CONTACT NAME: " + contact.getName());
        System.out.println("CONTACT ID "+contact.getcId());
        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //your profile handler

    @GetMapping("/profile")
    public String yourProfile(Model model) {

        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

}
