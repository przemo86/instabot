package pl.szewczyk.instagram;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.Logger;

/**
 * Created by przem on 15.09.2017.
 */
@Controller
public class InstaService {


    @GetMapping("/rest/auth")
    public String authorize(@RequestParam(name = "code") String code) {
        Logger.getGlobal().severe("aaaaaaaaaaaaa");
        Logger.getGlobal().severe("CODE " + code);
        return "redirect:/users";
    }
//
//    @GetMapping(path = "/auth")
//    public String test() {
//        Logger.getGlobal().severe("ffffff");
//        return "home/users";
//
//    }


}
