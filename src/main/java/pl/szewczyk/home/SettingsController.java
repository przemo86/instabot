package pl.szewczyk.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.szewczyk.account.InstaUserRepository;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.instagram.InstaUserListWrapper;

import java.util.logging.Logger;

import static pl.szewczyk.support.web.Message.MESSAGE_ATTRIBUTE;

@Controller
class SettingsController {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    private InstaUserRepository instaUserRepository;

    @ModelAttribute("module")
    String module() {
        return "settings";
    }

    @GetMapping("settings")
    String about(Model model) {
        model.addAttribute("instaUsers", new InstaUserListWrapper(instaUserRepository.findAll()));

        return "home/settings";
    }



    @PostMapping("setproxy")
    public String setUserProxy(@ModelAttribute("instaUsers") InstaUserListWrapper wrapper, RedirectAttributes ra) {

        log.info("hehe " + wrapper.getInstaUser().size());
        for (InstaUser user : wrapper.getInstaUser()) {
            InstaUser instaUser = instaUserRepository.findByUserName(user.getInstaUserName());
            instaUser.setProxyHost(user.getProxyHost());
            instaUser.setProxyPort(user.getProxyPort());
            instaUserRepository.save(instaUser);
        }
        //MessageHelper.addSuccessAttribute(ra, "set.proxy");
        ra.addFlashAttribute(MESSAGE_ATTRIBUTE, "Ustawiono proxy");
        return "redirect:/settings";
    }
}
