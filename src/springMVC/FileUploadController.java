package springMVC;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Invert.Util;

@Controller
public class FileUploadController {

  private static String UPLOAD_DIRECTORY = System.getProperty("rootpath")+"sources\\";
  private static String stopWord = UPLOAD_DIRECTORY+"stopWords.txt";
  private static String tempStore = UPLOAD_DIRECTORY+"src\\";
  private static String tempStoreInvertIndex = UPLOAD_DIRECTORY+"IIndex\\";
  private static String tempStoreListAndMap = UPLOAD_DIRECTORY+"Rank\\";

  @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
  public ModelAndView uploadFile(@RequestParam("fileList") MultipartFile[] files) {

    List<String> filePaths = new ArrayList<>();

    File directory = new File(UPLOAD_DIRECTORY);
    if (!directory.exists()) {
      directory.mkdirs();
    }

    if (files != null && files.length > 0) {
      for (int i = 0; i < files.length; i++) {
        MultipartFile file = files[i];

        String filePath = UPLOAD_DIRECTORY + File.separator + file.getOriginalFilename();
        filePaths.add(filePath);

        try {
          file.transferTo(new File(filePath));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    //build Inverted list and store to the result
    Util.Process(filePaths,stopWord,tempStoreInvertIndex,tempStore,tempStoreListAndMap);

    //redirect to the result page
    return new ModelAndView("showResults");
  }

}
