package pl.szewczyk.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
class SettingsController {

	@ModelAttribute("module")
	String module() {
		return "settings";
	}

	@GetMapping("settings")
	String about() {
		return "home/settings";
	}
}
