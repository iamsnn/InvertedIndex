package springMVC;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class MyController {

  @RequestMapping("/say")
  public String say(Model model) {
    return "say";

  }

  @RequestMapping("/upload")
  public String upload(Model model) {
    return "upload";
  }

  @RequestMapping("/search")
  public String search(Model model) {
    return "search";
  }

  @RequestMapping("/result")
  public String result(Model model) {
    return "showResults";
  }

}
