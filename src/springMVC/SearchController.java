package springMVC;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

import Invert.Util;

@Controller
public class SearchController {

  private static String UPLOAD_DIRECTORY = System.getProperty("rootpath")+"sources\\";
  private static String result = UPLOAD_DIRECTORY+"IIndex\\Result";

  @RequestMapping(value = "uploadWord", method = RequestMethod.POST)
  @ResponseBody
  public ModelAndView uploadSearch(@RequestParam("word") String s) throws IOException {

    long t1 = 0l;
    long t2 = 0l;

    t1 = System.currentTimeMillis();
    String res = Util.searchWord(s.trim().toLowerCase(),result);
    t2 = System.currentTimeMillis();

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("say");
    modelAndView.addObject("word",s.trim());
    modelAndView.addObject("res",res);
    modelAndView.addObject("time",t2-t1+"ms");
    modelAndView.addObject("size",Util.getFileSize(result)/1024l+"KB");

    return modelAndView;
  }
}