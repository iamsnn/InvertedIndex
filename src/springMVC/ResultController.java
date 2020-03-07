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
public class ResultController {

  private static String UPLOAD_DIRECTORY = System.getProperty("rootpath")+"sources\\";
  private static String result = UPLOAD_DIRECTORY+"IIndex\\Result";
  private static String stopWord = UPLOAD_DIRECTORY+"stopWords.txt";
  private static String tempStoreListAndMap = UPLOAD_DIRECTORY+"Rank\\";

  @RequestMapping(value = "uploadWord", method = RequestMethod.POST)
  @ResponseBody
  public ModelAndView uploadSearch(@RequestParam("word") String s) throws IOException {

    String res = Util.searchWord(s,result,stopWord,tempStoreListAndMap+"list",tempStoreListAndMap+
            "map");

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("result");
    modelAndView.addObject("sentence",s);
    modelAndView.addObject("res",res);

    return modelAndView;
  }
}
