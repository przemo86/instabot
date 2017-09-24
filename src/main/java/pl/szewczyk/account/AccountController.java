package pl.szewczyk.account;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
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
    @Secured("ROLE_ADMIN")
    public String listUsers(Model model) {
        model.addAttribute("userList", accountRepository.findAll());
        return "home/users";
    }

    private Logger logger = Logger.getLogger(Account.class.getName());

    @GetMapping("/user")
    @Secured("ROLE_ADMIN")
    public String user(Model model, @RequestParam(value = "id") Long id,
                       @RequestParam(value = "code", required = false) String code,
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "error_reason", required = false) String error_reason,
                       @RequestParam(value = "error_description", required = false) String error_description,
                       HttpServletRequest request) {
        logger.severe("aaaaaaaaaaaaaaaaaaa");
        Account account = em.createQuery("select a from Account a left join fetch a.instaUsers where a.id = :id", Account.class).setParameter("id", id).getSingleResult();
        request.getSession().setAttribute("account", account);
        logger.severe("11");
        UserForm userForm = new UserForm(account);
        logger.severe("222");
        model.addAttribute("userForm", userForm);


        logger.severe("1 " + code);
        logger.severe("errorrrs");
        model.addAttribute("error", error);
        model.addAttribute("error_reason", error_reason);
        model.addAttribute("error_description", error_description);


        logger.severe("2");
        if (Objects.nonNull(code) && !code.equals("")) {
            logger.severe("CALLING INSTA");


            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, String> map = new HashMap<>();
            map.put("code", code);
            map.put("client_id", "67a5ccf8ac4045a98906e37679caa4e2");
            map.put("client_secret", "ebd82636cefa449c88eabc1db3cb83d0");
            map.put("grant_type", "authorization_code");
            map.put("redirect_uri", request.getRequestURL().toString() + "?id=" + account.getId());

            try {
                String uri = "https://api.instagram.com/oauth/access_token";
                HttpURLConnection conn = (HttpURLConnection) new java.net.URL(uri).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                StringBuffer buffer = new StringBuffer();
                for (String key : map.keySet()) {
                    buffer.append(key);
                    buffer.append("=");
                    buffer.append(URLEncoder.encode(map.get(key), "UTF-8"));
                    buffer.append("&");
                }
                logger.severe("5555555");
                buffer.deleteCharAt(buffer.length() - 1);
                conn.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
                outputStreamWriter.write(buffer.toString());
                outputStreamWriter.flush();
                logger.severe("aaaaaaaa");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                logger.severe("gggg");
                conn.disconnect();
                logger.severe("CLOSE = ");
                String responseString = response.toString();
                JSONObject jsonObject = new JSONObject(responseString);
                logger.severe("CLOSE 2");
                InstaUser instaUser = new InstaUser();
                instaUser.setAccessToken(jsonObject.getString("access_token"));
                logger.severe("ACCESS TOKEN NOWY = " + jsonObject.getString("access_token"));
                instaUser.setId(jsonObject.getJSONObject("user").getLong("id"));
                instaUser.setInstaUserName(jsonObject.getJSONObject("user").getString("username"));
                instaUser.setProfilePictureURL(jsonObject.getJSONObject("user").getString("profile_picture"));
                instaUser.setFullName(jsonObject.getJSONObject("user").getString("full_name"));
                instaUser.setBio(jsonObject.getJSONObject("user").getString("bio"));
                instaUser.setWebsite(jsonObject.getJSONObject("user").getString("website"));
                instaUser.setBusiness(jsonObject.getJSONObject("user").getBoolean("is_business"));
                logger.severe("CLOSE 2");
                if (userForm.getInstaUsers().contains(instaUser)){
                    userForm.getInstaUsers().remove(instaUser);
                }
                userForm.getInstaUsers().add(instaUser);

                if (account.getInstaUsers().contains(instaUser)){
                    account.getInstaUsers().remove(instaUser);
                }
                account.getInstaUsers().add(instaUser);

                logger.severe("SAVING");
//                em.createNativeQuery("INSERT into log VALUES (CURRENT_TIMESTAMP , 'merging')").executeUpdate();

                instaUserRepository.save(instaUser);
                logger.severe("SAVED");
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("HAUA BAUA " + e.getMessage());
            }


            return "home/userForm";
        } else {
            model.addAttribute("error", "Nieznany błąd");
            logger.severe("2");

            return "home/userForm";
        }

    }

    @PostMapping(path = "user", params = "save")
    @Secured("ROLE_ADMIN")
    public String save(@Valid @ModelAttribute UserForm userForm, Errors errors, HttpServletRequest request) {
        logger.severe("SAVE");
        Account account = (Account) request.getSession().getAttribute("account");
        if (account != null) {
            if (!account.getEmail().equals(userForm.getEmail())) {
                logger.severe("NIBY ZE ERROR?");

                return "home/userForm";
            }
        }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                logger.severe("errors count " + errors.getErrorCount() + "   " + errors.hasErrors());
                if (Objects.nonNull(userForm.getExpires()) && !userForm.getExpires().equals(""))
                    try {
                        account.setExpires(sdf.parse(userForm.getExpires()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                account.setLocked(userForm.isLocked());

                account.setPassword(passwordEncoder.encode(userForm.getPassword()));
                logger.severe("tostring = " + userForm.toString());

                accountRepository.save(account);

                return "redirect:users";


    }

    @PostMapping(path = "user", params = "insta")
    @Secured("ROLE_ADMIN")
    public String insta(//@RequestParam(required=false, value="save") String save,
//                        @RequestParam(required=false, value="insta") String insta,

                        @ModelAttribute UserForm userForm, Errors errors, HttpServletRequest request) {

        Account account = (Account) request.getSession().getAttribute("account");


        return "redirect:https://www.instagram.com/oauth/authorize/?client_id="+ InstaConstants.ClientID+"&redirect_uri=" +
                request.getRequestURL().toString() + "?id=" + account.getId()
                + "&response_type=code&scope=public_content+likes+comments";
    }
}
