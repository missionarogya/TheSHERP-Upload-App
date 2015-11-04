package android.sherp.missionarogya.sherp_upload;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Created by Sonali Sinha on 11/4/2015.
 */
public class Logging {

    private static Logging ourInstance = new Logging();
    private String logMessage = "";
    private String jsonString;

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    private Logging(){

    }

    public static Logging getInstance() {
        return ourInstance;
    }

    public static void setInstance(Logging interviewDetails) {
        Logging.ourInstance = interviewDetails;
    }

    public static boolean writeToLogFile(String message){
        boolean success ;
        File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"SherpLog.txt");
        try {
            if (logFile.exists() && logFile.isFile()) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
                pw.println(message);
                pw.flush();
                pw.close();
                success = true;
            } else {
                logFile.createNewFile();
                if(logFile.isFile() && logFile.exists()) {
                    FileOutputStream f = new FileOutputStream(logFile);
                    PrintWriter pw = new PrintWriter(f);
                    pw.println(message);
                    pw.flush();
                    pw.close();
                    f.close();
                    success = true;
                }
                else{
                    success = false;
                }
            }
        }
        catch(Exception e){
            success = false;
        }
        return success;
    }

}
