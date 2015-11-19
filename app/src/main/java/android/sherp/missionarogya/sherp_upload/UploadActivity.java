package android.sherp.missionarogya.sherp_upload;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.ProgressDialog;

import java.lang.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.channels.FileChannel;

import org.json.JSONArray;
import org.json.JSONException;

public class UploadActivity extends AppCompatActivity {
    JSONArray interviewJSON;
    String source = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Sherp" + File.separator + "InterviewData" + File.separator + "interviewData.json";
    Logging logfile = Logging.getInstance();
    String logmessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        logmessage = "-------------------------------------------------------------------------------------------------------------------------------------------------\nUploading File to server.\n";

        final ImageButton upload = (ImageButton) findViewById(R.id.buttonUpload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload.setClickable(false);
                try {
                    if (buildJSON()) {
                        final ImageButton go = (ImageButton) findViewById(R.id.go);
                        go.setVisibility(View.VISIBLE);
                        go.setClickable(true);
                        go.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                logfile.setLogMessage("");
                                Logging.setInstance(logfile);
                                Intent intent = new Intent(UploadActivity.this, DeleteLocalCopyActivity.class);
                                UploadActivity.this.startActivity(intent);
                                UploadActivity.this.finish();
                            }
                        });
                    }
                } catch (Exception ex) {
                    Toast.makeText(UploadActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        final Button exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logmessage = logmessage + "Exiting from upload screen. \n-------------------------------------------------------------------------------------------------------------------------------------------------\n";
                logfile.setLogMessage(logmessage);
                Logging.setInstance(logfile);
                Logging.writeToLogFile(logfile.getLogMessage());
                logfile.setJsonString(null);
                Logging.setInstance(null);
                Intent intent = new Intent(UploadActivity.this, LoginActivity.class);
                UploadActivity.this.startActivity(intent);
                UploadActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(UploadActivity.this, "You cannot go back from here. Press EXIT!", Toast.LENGTH_SHORT).show();
    }

    private boolean uploadToServer() throws Exception{
        boolean success;
        final JSONParser mJSONParser = new JSONParser(logfile, logmessage, UploadActivity.this);
        try {
            String status = mJSONParser.execute("").getStatus().name();
            Toast.makeText(UploadActivity.this, "Status of upload process: " + status + "\n", Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Status of upload process: " + status + "\n";
            success = true;
        }catch(Exception ex){
            Toast.makeText(UploadActivity.this, "Error occured while uploading to server:"+ex.getMessage(), Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Error occured while uploading to server:" + ex.getMessage() + "\n";
            success = false;
        }
        return success;
    }

    private boolean createJSONbackup() throws Exception{
        boolean success ;
        String dest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Sherp";
        File dir = new File(dest);
        File backupDir = new File(dir, "Backup");
        if (backupDir.exists() && backupDir.isDirectory()){
            Toast.makeText(UploadActivity.this, "Backup folder exists.", Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Backup folder exists.\n";
        }
        else{
            backupDir.mkdir();
            Toast.makeText(UploadActivity.this, "Created new backup folder.", Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Created new backup folder.\n";
        }
        try{
            String filename = "InterviewData_" + Long.toString(System.nanoTime()) + ".json";
            File backupFile = new File(backupDir, filename);
            backupFile.createNewFile();
            dest = dest + File.separator + "Backup" + File.separator + filename ;
            FileChannel inputChannel = new FileInputStream(source).getChannel();
            FileChannel outputChannel =  new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            inputChannel.close();
            outputChannel.close();
            Toast.makeText(UploadActivity.this, "Successfully created a JSON backup in /Documents/Sherp/Backup.", Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Successfully created a JSON backup in /Documents/Sherp/Backup.\n";
            success = uploadToServer();
            if (success) {
               Toast.makeText(UploadActivity.this, "Uploading the interview JSON to server.", Toast.LENGTH_LONG).show();
               logmessage = logmessage + "Successfully uploaded the interview JSON to server.\n";
            }
        }catch(IOException e){
            Toast.makeText(UploadActivity.this, "Error in creating JSON backup: "+e.getMessage()+" Please do it manually!", Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Error in creating JSON backup:"+e.getMessage()+" Please do it manually!.\n";
            success = false;
        } catch(Exception e){
        Toast.makeText(UploadActivity.this, "Error occured: "+e.getMessage()+" Please do it manually!", Toast.LENGTH_LONG).show();
        logmessage = logmessage + "Error occured: :"+e.getMessage()+" Please do it manually!.\n";
        success = false;
    }
        return success;
    }

    private boolean validateJSON(String jsonContent) throws Exception{
        boolean success;
        try{
            interviewJSON = new JSONArray(jsonContent);
            logfile.setJsonString(interviewJSON.toString());
            logfile.setInstance(logfile);
            success = true;
            }catch(JSONException e){
                Toast.makeText(UploadActivity.this, "Invalid JSON: "+e.getMessage(), Toast.LENGTH_LONG).show();
                logmessage = logmessage + "Invalid JSON: :"+e.getMessage()+"\n";
                success = false;
            }catch(Exception e){
                Toast.makeText(UploadActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                logmessage = logmessage + "Invalid JSON: :"+e.getMessage()+"\n";
                success = false;
            }
        return success;
    }

    private boolean buildJSON() throws Exception{
            boolean success;
            File interviewDataDir_ = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Sherp");
            if (interviewDataDir_.exists() && interviewDataDir_.isDirectory()) {
                File interviewDataDir = new File(interviewDataDir_, "InterviewData");
                if (interviewDataDir.exists() && interviewDataDir.isDirectory()) {
                    File interviewDataFile = new File(interviewDataDir, "interviewData.json");
                    if (interviewDataFile.exists() && interviewDataFile.isFile()) {
                        try {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            FileInputStream fis = new FileInputStream(interviewDataFile);
                            int content;
                            while ((content = fis.read()) != -1) {
                                byteArrayOutputStream.write((char) content);
                            }
                            fis.close();
                            String jsonContent = byteArrayOutputStream.toString();
                            success = validateJSON(jsonContent);
                            if(!success)
                            {
                                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(interviewDataFile, true)));
                                pw.println("    }");
                                pw.println("]");
                                pw.flush();
                                pw.close();
                                byteArrayOutputStream = new ByteArrayOutputStream();
                                fis = new FileInputStream(interviewDataFile);
                                while ((content = fis.read()) != -1) {
                                   byteArrayOutputStream.write((char) content);
                                }
                                fis.close();
                                jsonContent = byteArrayOutputStream.toString();
                                success = validateJSON(jsonContent);
                                if(success) {
                                    Toast.makeText(UploadActivity.this, "JSON is valid.", Toast.LENGTH_LONG).show();
                                    logmessage = logmessage + "JSON is valid.\n";
                                    success = createJSONbackup();
                                }
                            }
                            else{
                                Toast.makeText(UploadActivity.this, "JSON is valid.", Toast.LENGTH_LONG).show();
                                logmessage = logmessage + "JSON is valid.\n";
                                success = createJSONbackup();
                            }
                        }catch(FileNotFoundException e){
                            Toast.makeText(UploadActivity.this, "File not found: "+e.getMessage(), Toast.LENGTH_LONG).show();
                            logmessage = logmessage + "File not found: " + e.getMessage() +"\n";
                            success = false;
                        } catch (IOException e) {
                            Toast.makeText(UploadActivity.this, "Error(I/O) writing file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            logmessage = logmessage + "Error(I/O) writing file: " + e.getMessage() +"\n";
                            success = false;
                        } catch (Exception e) {
                            Toast.makeText(UploadActivity.this, "Error writing file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            logmessage = logmessage + "Error writing file: " + e.getMessage() +"\n";
                            success = false;
                        }
                    } else {
                        Toast.makeText(UploadActivity.this, "The interviewData JSON does not exist. ", Toast.LENGTH_LONG).show();
                        logmessage = logmessage + "The interviewData JSON does not exist.\n";
                        success = false;
                    }
                } else {
                    Toast.makeText(UploadActivity.this, "The folder InterviewData does not exist. ", Toast.LENGTH_LONG).show();
                    logmessage = logmessage + "The folder InterviewData does not exist.\n";
                    success = false;
                }
            } else {
                Toast.makeText(UploadActivity.this, "The folder Sherp does not exist. ", Toast.LENGTH_LONG).show();
                logmessage = logmessage + "The folder Sherp does not exist.\n";
                success = false;
            }
        return success;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

class JSONParser extends AsyncTask<String, Void, String> {
    String output="";
    String logmessage;
    Logging logfile;
    UploadActivity activity;
    ProgressDialog progressDialog;

    public JSONParser(Logging logfile, String logmessage, UploadActivity activity) {
        this.logfile =  logfile ;
        this.logmessage = logmessage;
        this.activity = activity;
    }

    @Override
    public String doInBackground(String... params) {
        try {
            logmessage = logmessage + "Uploading to server....\n";
            URL url = new URL("http://springdemo11-sampledemosite.rhcloud.com/profile/PushInterviewDataToServer");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            logmessage = logmessage + logfile.getJsonString() + "\n";
            String input = "{\"answers\":"+logfile.getJsonString()+"}";
            OutputStream os = conn.getOutputStream();
            os.flush();
            os.write(input.getBytes());
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            logmessage = logmessage + "Response from server: ";
            while ((output = br.readLine()) != null) {
                logmessage = logmessage + output +"\n";
            }
            conn.disconnect();
            } catch (Exception e) {
                logmessage = logmessage + "\nFATAL ERROR :: "+ e;
            }
            return output;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        logmessage = logmessage + "Will upload data to server.\n";
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Uploading data to server...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressPercentFormat(null);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        logmessage = logmessage + "Upload to server complete.\n";
        logfile.setLogMessage(logmessage);
        Logging.setInstance(logfile);
        Logging.writeToLogFile(logfile.getLogMessage());
        logfile.setLogMessage("");
        Logging.setInstance(logfile);
        progressDialog.dismiss();
        progressDialog = null;
    }

}