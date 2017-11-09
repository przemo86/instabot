package pl.szewczyk.account;

import org.jinstagram.Instagram;
import org.jinstagram.InstagramClient;
import org.jinstagram.auth.InstagramAuthService;
import org.jinstagram.auth.model.Token;
import org.jinstagram.auth.model.Verifier;
import org.jinstagram.auth.oauth.InstagramService;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.exceptions.InstagramException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Logger;

@Controller
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private InstaUserRepository instaUserRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute("module")
    String module() {
        return "users";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("userList", accountRepository.findAll());
        return "home/users";
    }

    private Logger logger = Logger.getLogger(Account.class.getName());

    @GetMapping("/user")
    public String addUser(Model model, HttpServletRequest request) {
        System.out.println("GET USER NO ID");
        request.getSession(false).removeAttribute("account");
        UserForm userForm = new UserForm();
        System.out.println(" " + userForm);
        model.addAttribute("userForm", userForm);
        request.getSession(false).setAttribute("userForm", userForm);
        return "home/userForm";
    }

    @GetMapping(value = "/userAdded")
    public String addInstaUser(Model model, @RequestParam(required = false, value = "code") String code,
                               HttpServletRequest request, @RequestParam(required = false, value = "id") Long id) {
        System.out.println("ADD INSTA USER " + code);
        System.out.println("CODE " + request.getRequestURL().toString());

        if (Objects.nonNull(code) && !code.equals("")) {
            InstagramService service = new InstagramAuthService()
                    .apiKey(InstaConstants.ClientID)
                    .apiSecret(InstaConstants.ClientSecret)
                    .callback(request.getRequestURL().toString() + (id == null ? "" : "?id=" + id))
                    .build();

            Verifier verifier = new Verifier(code);
            Token accessToken = service.getAccessToken(verifier);

            InstagramClient instagram = new Instagram(accessToken);

            try {
                UserInfo userInfo = instagram.getCurrentUserInfo();
                request.getSession(false).setAttribute("userInfo", userInfo.getData());
                request.getSession(false).setAttribute("accessToken", accessToken.getToken());
            } catch (InstagramException e) {
                e.printStackTrace();
            }

//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//            Map<String, String> map = new HashMap<>();
//            map.put("code", code);
//            map.put("client_id", InstaConstants.ClientID);
//            map.put("client_secret", InstaConstants.ClientSecret);
//            map.put("grant_type", "authorization_code");
//            map.put("redirect_uri", request.getRequestURL().toString() +
//                    (userForm.getEmail() != null ?
//                            "?id=" + accountRepository.findOneByEmail(userForm.getEmail()).getId() :
//                            ""));
//
//            try {
//                String uri = "https://api.instagram.com/oauth/access_token";
//                HttpURLConnection conn = (HttpURLConnection) new java.net.URL(uri).openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//
//                StringBuffer buffer = new StringBuffer();
//                for (String key : map.keySet()) {
//                    buffer.append(key);
//                    buffer.append("=");
//                    buffer.append(URLEncoder.encode(map.get(key), "UTF-8"));
//                    buffer.append("&");
//                }
//                buffer.deleteCharAt(buffer.length() - 1);
//                conn.setDoOutput(true);
//                System.out.println("PARAMS " + buffer.toString());
//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
//                outputStreamWriter.write(buffer.toString());
//                outputStreamWriter.flush();
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String inputLine;
//                StringBuffer response = new StringBuffer();
//
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                logger.severe("gggg");
//                conn.disconnect();
//                logger.severe("CLOSE = ");
//                String responseString = response.toString();
//                JSONObject jsonObject = new JSONObject(responseString);
//                logger.severe("CLOSE 2");
//                InstaUser instaUser = new InstaUser();
//                instaUser.setAccessToken(jsonObject.getString("access_token"));
//                logger.severe("ACCESS TOKEN NOWY = " + jsonObject.getString("access_token"));
//                instaUser.setId(jsonObject.getJSONObject("user").getLong("id"));
//                instaUser.setInstaUserName(jsonObject.getJSONObject("user").getString("username"));
//                instaUser.setProfilePictureURL(jsonObject.getJSONObject("user").getString("profile_picture"));
//                instaUser.setFullName(jsonObject.getJSONObject("user").getString("full_name"));
//                instaUser.setBio(jsonObject.getJSONObject("user").getString("bio"));
//                instaUser.setWebsite(jsonObject.getJSONObject("user").getString("website"));
//                instaUser.setBusiness(jsonObject.getJSONObject("user").getBoolean("is_business"));
//                logger.severe("CLOSE 2");
//                if (userForm.getInstaUsers().contains(instaUser)) {
//                    userForm.getInstaUsers().remove(instaUser);
//                }
//
//                if (userForm.getInstaUsers().contains(instaUser)) {
//                    userForm.getInstaUsers().remove(instaUser);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.severe("HAUA BAUA " + e.getMessage());
//            }


        } else {
            model.addAttribute("error", "Nieznany błąd");
            logger.severe("2");

        }
        return "home/instaResponse";
    }

    @GetMapping(value = "/user", params = {"id"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @accountRepository.findOneByEmail(#principal.name).id == #id")
    public String user(Model model, @P("id") @RequestParam(value = "id") Long id,
//                       @RequestParam(value = "code", required = false) String code,
//                       @RequestParam(value = "error", required = false) String error,
//                       @RequestParam(value = "error_reason", required = false) String error_reason,
//                       @RequestParam(value = "error_description", required = false) String error_description,
                       HttpServletRequest request, @P("principal") Principal principal) {
        System.out.println("GET USER ID " + id);
        Account account = em.createQuery("select a from Account a left join fetch a.instaUsers where a.id = :id", Account.class).setParameter("id", id).getSingleResult();
        request.getSession().setAttribute("account", account);
        UserForm userForm = new UserForm(account);
        System.out.println(" " + userForm);
        model.addAttribute("id", account.getId());
        model.addAttribute("userForm", userForm);

//        model.addAttribute("error", error);
//        model.addAttribute("error_reason", error_reason);
//        model.addAttribute("error_description", error_description);

        request.getSession(false).setAttribute("userForm", userForm);
        System.out.println("get ID " + userForm);

        return "home/userForm";

    }

    @PostMapping(path = "user", params = {"save"})
    public String save(@Valid @ModelAttribute UserForm userForm, Errors errors, HttpServletRequest request) {
        System.out.println("save " + errors);
        System.out.println("save " + userForm);
        Account account = (Account) request.getSession().getAttribute("account");
        if (account != null) {
            System.out.println(account.getEmail());
            System.out.println(userForm.getEmail());
            if (!account.getEmail().equals(userForm.getEmail())) {
                logger.severe("NIBY ZE ERROR?");
                return "home/userForm";
            }
        } else {
            account = userForm.createAccount();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        logger.severe("errors countMediaId " + errors.getErrorCount() + "   " + errors.hasErrors());
        if (Objects.nonNull(userForm.getExpires()) && !userForm.getExpires().equals(""))
            try {
                account.setExpires(sdf.parse(userForm.getExpires()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        account.setLocked(userForm.isLocked());

        if (userForm.getPassword() != null && !"".equals(userForm.getPassword()))
            if (userForm.getRepeatPassword() != null && !"".equals(userForm.getRepeatPassword()))
                if (userForm.getPassword().equals(userForm.getRepeatPassword()))
                    account.setPassword(passwordEncoder.encode(userForm.getPassword()));
        logger.severe("tostring = " + userForm.toString());

        accountRepository.save(account);

        return "redirect:users";


    }

    @GetMapping(path = "adduser")
    public String addInstaUser(Model model, HttpServletRequest request) {
        System.out.println("POST USER NO ID");
        System.out.println("MODEL " + model.asMap().keySet());
        UserForm sessionForm = (UserForm) request.getSession(false).getAttribute("userForm");
        System.out.println("SESION " + sessionForm);
        UserInfoData data = (UserInfoData) request.getSession(false).getAttribute("userInfo");
        if (data != null) {
            System.out.println("DATA " + data);
            InstaUser user = new InstaUser();
            user.setBusiness(false);
            user.setWebsite(data.getWebsite());
            user.setBio(data.getBio());
            user.setFullName(data.getFullName());
            user.setProfilePictureURL(data.getProfilePicture());
            user.setAccessToken((String) request.getSession(false).getAttribute("accessToken"));
            user.setInstaUserName(data.getUsername());
            user.setId(Long.valueOf(data.getId()));

            user = instaUserRepository.save(user);

            sessionForm.getInstaUsers().add(user);
            System.out.println("SESION " + sessionForm);
//        userForm.getInstaUsers().addAll(sessionForm.getInstaUsers());

            request.getSession(false).removeAttribute("userInfo");
        }
        model.addAttribute("userForm", sessionForm);
        return "fragments/components :: instaUserTable";
    }

    @GetMapping(path = "user", params = {"id", "email"})
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
