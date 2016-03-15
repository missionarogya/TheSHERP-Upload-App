package android.sherp.missionarogya.sherp_upload;

import android.app.ProgressDialog;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeleteLocalCopyActivity extends AppCompatActivity {
    Logging logfile = Logging.getInstance();
    SherpData sherpData = SherpData.getInstance();
    String logmessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_local_copy);

        final Button deleteLocalCopy = (Button) findViewById(R.id.buttonDeleteLocalCopy);
        deleteLocalCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLocalCopy.setClickable(false);
                if (deleteLocalCopy()) {
                    logmessage = "Successfully deleted the local copy of the interview JSON.";
                } else {
                    logmessage = "Delete unsuccessful. Please delete the file manually!";
                }
                Toast.makeText(DeleteLocalCopyActivity.this, logmessage, Toast.LENGTH_SHORT).show();
                logmessage = logmessage + "\nExiting from app.\n-------------------------------------------------------------------------------------------------------------------------------------------------\n";
                logfile.setLogMessage(logmessage);
                Logging.setInstance(logfile);
            }
        });

        final Button downloadDataFromServer = (Button) findViewById(R.id.buttonDownloadData);
        downloadDataFromServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadDataFromServer.setClickable(false);
                try {
                    if(downloadFromServer()){
                        final ImageButton go = (ImageButton) findViewById(R.id.go);
                        go.setVisibility(View.VISIBLE);
                        go.setClickable(true);
                        go.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                logfile.setLogMessage("");
                                Logging.setInstance(logfile);
                                Intent intent = new Intent(DeleteLocalCopyActivity.this, DisplayInterviewData.class);
                                DeleteLocalCopyActivity.this.startActivity(intent);
                                DeleteLocalCopyActivity.this.finish();
                            }
                        });
                        logmessage = logmessage + "Interview Data successfully downloaded from server." +"\n";
                        Toast.makeText(getApplicationContext(),"Interview Data successfully downloaded from server.",Toast.LENGTH_SHORT).show();
                    }else{
                        logmessage = logmessage + "There was an error downloading Interview Data from the server." +"\n";
                        Toast.makeText(getApplicationContext(),"There was an error downloading Interview Data from the server.",Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    logmessage = logmessage + e.toString()+"\n";
                    Toast.makeText(getApplicationContext(),"Error:"+e.toString(),Toast.LENGTH_SHORT).show();
                }
                logfile.setLogMessage(logmessage);
                Logging.setInstance(logfile);
            }
        });
        final Button exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //roll back changes
                Logging.writeToLogFile(logfile.getLogMessage());
                Logging.setInstance(null);
                Intent intent = new Intent(DeleteLocalCopyActivity.this, LoginActivity.class);
                DeleteLocalCopyActivity.this.startActivity(intent);
                DeleteLocalCopyActivity.this.finish();
            }
        });
    }

    private boolean downloadFromServer() throws Exception{
        boolean success;
        final JSONDownloader mJSONDownloader = new JSONDownloader(logfile, logmessage, DeleteLocalCopyActivity.this, sherpData);
        try {
            String status = mJSONDownloader.execute("").getStatus().name();
            Toast.makeText(DeleteLocalCopyActivity.this, "Status of download process: " + status + "\n", Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Status of download process: " + status + "\n";
            success = true;
        }catch(Exception ex){
            Toast.makeText(DeleteLocalCopyActivity.this, "Error occured while downloading to server:"+ex.getMessage(), Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Error occured while downloading to server:" + ex.getMessage() + "\n";
            success = false;
        }
        return success;
    }

    private boolean deleteLocalCopy(){
        boolean success ;
        File interviewDataDir_ = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Sherp");
        if(interviewDataDir_.exists() && interviewDataDir_.isDirectory()){
            File interviewDataDir = new File(interviewDataDir_, "InterviewData");
            if(interviewDataDir.exists() && interviewDataDir.isDirectory()){
                File interviewDataFile = new File(interviewDataDir, "interviewData.json");
                if(interviewDataFile.exists() && interviewDataFile.isFile()){
                    try {
                        success = interviewDataFile.delete();
                    }
                    catch (Exception e){
                        Toast.makeText(DeleteLocalCopyActivity.this, "Error writing file: "+e.getMessage(), Toast.LENGTH_LONG).show();
                        success = false;
                    }
                }
                else{
                    Toast.makeText(DeleteLocalCopyActivity.this,"The file interviewData.json does not exist. ", Toast.LENGTH_LONG).show();
                    success = false;
                }
            }else{
                Toast.makeText(DeleteLocalCopyActivity.this,"The folder InterviewData does not exist. ", Toast.LENGTH_LONG).show();
                success = false;
            }
        }
        else{
            Toast.makeText(DeleteLocalCopyActivity.this,"The folder Sherp does not exist. ", Toast.LENGTH_LONG).show();
            success = false;
        }
        return success;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_delete_local_copy, menu);
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

    @Override
    public void onBackPressed() {
        DeleteLocalCopyActivity.this.finish();
    }
}


class JSONDownloader extends AsyncTask<String, Void, String> {
    SherpData sherpData;
    String output="";
    String logmessage;
    Logging logfile;
    DeleteLocalCopyActivity activity;
    ProgressDialog progressDialog;

    public JSONDownloader(Logging logfile, String logmessage, DeleteLocalCopyActivity activity, SherpData sherpData) {
        this.logfile =  logfile ;
        this.logmessage = logmessage;
        this.activity = activity;
        this.sherpData = sherpData;
    }

    @Override
    public String doInBackground(String... params) {
        try {
            logmessage = logmessage + "\nDownloading from server....\n";
            URL url = new URL("http://springdemo11-sampledemosite.rhcloud.com/profile/GetInterviewDataToServer");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("Content-Type", "application/json");
            //logmessage = logmessage + logfile.getJsonString() + "\n";
            //String input = "{\"answers\":"+logfile.getJsonString()+"}";
            //OutputStream os = conn.getOutputStream();
            //os.flush();
            //os.write(input.getBytes());
            //os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            logmessage = logmessage + "Response from server: "+"\n";
            while ((output = br.readLine()) != null) {
                logmessage = logmessage + output +"\n";
                if(output.length() > 0){
                    output = output.substring(1,output.length()-1);
                    sherpData.setInterviewData(output.substring(75,output.length()));
                    SherpData.setInstance(sherpData);
                    String[] arr = output.split(",");
                    for (String s:arr) {
                        String[] arr1 = s.split(":");
                        if(arr1[0].substring(1,arr1[0].length()-1).equals("httpStatus")){
                            sherpData.setHttpStatusDownload(arr1[1].substring(1, arr1[1].length() - 1));
                            SherpData.setInstance(sherpData);
                        }
                        if(arr1[0].substring(1,arr1[0].length()-1).equals("message")){
                            sherpData.setMessageDownload(arr1[1].substring(1, arr1[1].length() - 1));
                            SherpData.setInstance(sherpData);
                        }
                    }
                }
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
        logmessage = logmessage + "Will download data from server.\n";
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading data from server...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressPercentFormat(null);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        logmessage = logmessage + "Download from server complete.\n";
        logfile.setLogMessage(logmessage);
        Logging.setInstance(logfile);
        Logging.writeToLogFile(logfile.getLogMessage());
        logfile.setLogMessage("");
        Logging.setInstance(logfile);
        progressDialog.dismiss();
        progressDialog = null;
    }

}