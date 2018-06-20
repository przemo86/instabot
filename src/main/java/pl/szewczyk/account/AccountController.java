package pl.szewczyk.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.projects.Project;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
public class AccountController {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private InstaUserRepository instaUserRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private InstaConstants instaConstants = new InstaConstants();

    @ModelAttribute("module")
    String module() {
        return "users";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("userList", accountRepository.findAll().stream().filter(s -> !s.isDeleted()).collect(Collectors.toList()));
        return "home/users";
    }


    @GetMapping("/user")
    public String addUser(Model model, HttpServletRequest request) {
        log.info("GET USER NO ID");
        request.getSession(false).removeAttribute("account");
        UserForm userForm = new UserForm();
        log.info(" " + userForm);
        model.addAttribute("userForm", userForm);
        request.getSession(false).setAttribute("userForm", userForm);
        return "home/userForm";
    }

    @PostMapping(path = "/users/delete", params = "email")
    public String deleteUser(@RequestParam(name = "email") String email) {

        Account account = accountRepository.findOneByEmail(email);
        System.out.println(account);
        account.setDeleted(true);

        accountRepository.save(account);

        return "redirect:/users";
    }

    @GetMapping(value = "/addInstaUser")
    public String addInstaUser(Model model,
                               HttpServletRequest request, @RequestParam(required = false, value = "id") Long id) {


        return "home/instaResponse";
    }

    @GetMapping(value = "/user", params = {"id"})
//    @PreAuthorize("hasRole('ROLE_ADMIN') or @accountRepository.findOneByEmail(#principal.name).id == #id")
    public String user(Model model, @P("id") @RequestParam(value = "id") Long id,
                       HttpServletRequest request, @P("principal") Principal principal) {
        Account account = em.createQuery("select a from Account a left outer join fetch a.instaUsers where a.id = :id", Account.class).setParameter("id", id).getSingleResult();
        request.getSession().setAttribute("account", account);
        UserForm userForm = new UserForm(account);
        model.addAttribute("id", account.getId());
        model.addAttribute("userForm", userForm);

        request.getSession(false).setAttribute("userForm", userForm);
        return "home/userForm";
    }

    @PostMapping(path = "user", params = {"save"})
    public String save(@Valid @ModelAttribute UserForm userForm, Errors errors, HttpServletRequest request) {
        Account account = (Account) request.getSession(false).getAttribute("account");
        if (account != null) {
            if (!account.getEmail().equals(userForm.getEmail())) {
                log.severe("NIBY ZE ERROR?");
                return "home/userForm";
            }
        } else {
            account = userForm.createAccount();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (Objects.nonNull(userForm.getExpires()) && !userForm.getExpires().equals(""))
            try {
                account.setExpires(sdf.parse(userForm.getExpires()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        UserForm u2 = (UserForm) request.getSession(false).getAttribute("userForm");

        account.setLocked(userForm.isLocked());
        account.setInstaUsers(userForm.getInstaUsers());
        account.getInstaUsers().addAll(u2.getInstaUsers());

        if (userForm.getPassword() != null && !"".equals(userForm.getPassword()))
            if (userForm.getRepeatPassword() != null && !"".equals(userForm.getRepeatPassword()))
                if (userForm.getPassword().equals(userForm.getRepeatPassword()))
                    account.setPassword(passwordEncoder.encode(userForm.getPassword()));
        log.severe("tostring = " + userForm.toString());

        if (account.getInstaUsers().size() > 0) {
            for (InstaUser iu : account.getInstaUsers())
                instaUserRepository.save(iu);
        }

        accountRepository.save(account);


        return "redirect:users";


    }



    @RequestMapping(value = "/addnewinstauser", method = RequestMethod.POST)
    public String addInstaUser(Model model, @RequestParam(value = "insta_username", required = false) String username,
                               @RequestParam(value = "insta_password", required = false) String pass, HttpServletRequest request,
                               HttpServletResponse response) {
        me.postaddict.instagram.scraper.domain.Account acc;

        try {
//            log.info(username);
//            log.info(pass);
            acc = instaConstants.instaLogin(username, pass);
        } catch (Exception e) {
            log.severe("e MESSAGE " + e.getMessage());
            response.setStatus(500);
            response.setHeader("error", "Wystąpił błąd");
            return null;
        }

        if (acc != null) {

            InstaUser user = new InstaUser();
            user.setBusiness(false);
            user.setBio(acc.biography);
            user.setFullName(acc.fullName);
            user.setProfilePictureURL(acc.profilePicUrl);
            user.setInstaUserName(acc.username);
            user.setId(acc.id);
            try {
                user.setPassword(instaConstants.encrypt(pass.getBytes("UTF-8")));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            UserForm userForm = (UserForm) request.getSession(false).getAttribute("userForm");
            userForm.getInstaUsers().add(user);

            model.addAttribute("userForm", userForm);
            request.setAttribute("userForm", userForm);
        }

        return "fragments/components :: instaUserTable";
    }

    @GetMapping("/listusers")
    public String instaUsersTable(Model model, HttpServletRequest request) {
        model.addAttribute("userForm", request.getSession(false).getAttribute("userForm"));

        return "fragments/components :: instaUserTable";
    }

    @GetMapping(value = "user", params = {"id", "email"})
    public String deleteInstaUser(Model model, @RequestParam("id") Long id, @RequestParam("email") String email, HttpServletRequest request) {

        UserForm form = (UserForm) request.getSession(false).getAttribute("userForm");
        Iterator<InstaUser> instaUserIterator = form.getInstaUsers().iterator();
        while (instaUserIterator.hasNext()) {
            InstaUser user = instaUserIterator.next();
            if (user.getId().equals(id)) {
                instaUserIterator.remove();
            }
        }

        model.addAttribute("userForm", form);


        return "home/userForm";
    }
}
