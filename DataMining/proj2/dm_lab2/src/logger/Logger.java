package logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

  private static Logger instance = new Logger();
  FileWriter logWriter;
  boolean toConsole = true;

  private Logger() {

  }

  public static Logger getInstance() {
    return instance;
  }

  public void setFile(String filename) {
    if (logWriter != null) {
      try {
        logWriter.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    File file = new File(filename);
    if (file.exists()) file.delete();
    try {
      file.createNewFile();
      logWriter = new FileWriter(new File(filename));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  public void log(String str) {
    try {
      logWriter.append(str + "\n");
      logWriter.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (toConsole) {
      System.out.println(str);
    }
  }
}
