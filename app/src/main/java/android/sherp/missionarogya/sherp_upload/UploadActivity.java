package android.sherp.missionarogya.sherp_upload;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import org.json.JSONArray;
import org.json.JSONException;


public class UploadActivity extends AppCompatActivity {
    JSONArray interviewJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        final Button upload = (Button) findViewById(R.id.buttonUpload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload.setClickable(false);
                if(buildJSON()){
                    final ImageButton go = (ImageButton) findViewById(R.id.go);
                    go.setVisibility(View.VISIBLE);
                    go.setClickable(true);
                    go.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(UploadActivity.this, DeleteLocalCopyActivity.class);
                            UploadActivity.this.startActivity(intent);
                            UploadActivity.this.finish();
                        }
                    });
                }
            }
        });
        final Button exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interviewJSON = null;
                UploadActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(UploadActivity.this, "You cannot go back from here. Press EXIT!", Toast.LENGTH_SHORT).show();
    }

    private boolean uploadToServer(){
        return true;
    }

    private boolean createJSONbackup(){
        boolean success ;
        String source = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Sherp" + File.separator + "InterviewData" + File.separator + "interviewData.json";
        String dest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Sherp";
        File dir = new File(dest);
        File backupDir = new File(dir, "Backup");
        if (backupDir.exists() && backupDir.isDirectory()){
            Toast.makeText(UploadActivity.this, "Backup folder exists.", Toast.LENGTH_SHORT).show();
        }
        else{
            backupDir.mkdir();
            Toast.makeText(UploadActivity.this, "Created new backup folder.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(UploadActivity.this, "Successfully created a JSON backup in /Documents/Sherp/Backup.", Toast.LENGTH_SHORT).show();
            success = uploadToServer();
            if (success) {
                Toast.makeText(UploadActivity.this, "Successfully uploaded the interview JSON to server.", Toast.LENGTH_SHORT).show();
            }
        }catch(IOException e){
            Toast.makeText(UploadActivity.this, "Error in creating JSON backup: "+e.getMessage()+" Please do it manually!", Toast.LENGTH_SHORT).show();
            success = false;
        }
        return success;
    }

    private boolean validateJSON(String jsonContent){
        boolean success = false;
        try{
            interviewJSON = new JSONArray(jsonContent);
            success = true;
            }catch(JSONException e){
                //Toast.makeText(UploadActivity.this, "Invalid JSON: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                success = false;
            }catch(Exception e){
                Toast.makeText(UploadActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                success = false;
            }
        return success;
    }

    private boolean buildJSON(){
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
                                    Toast.makeText(UploadActivity.this, "JSON is valid.", Toast.LENGTH_SHORT).show();
                                    success = createJSONbackup();
                                }
                            }
                            else{
                                Toast.makeText(UploadActivity.this, "JSON is valid.", Toast.LENGTH_SHORT).show();
                                success = createJSONbackup();
                            }
                        }catch(FileNotFoundException e){
                            Toast.makeText(UploadActivity.this, "File not found: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            success = false;
                        } catch (IOException e) {
                            Toast.makeText(UploadActivity.this, "Error(I/O) writing file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            success = false;
                        } catch (Exception e) {
                            Toast.makeText(UploadActivity.this, "Error writing file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            success = false;
                        }
                    } else {
                        Toast.makeText(UploadActivity.this, "The interviewData JSON does not exist. ", Toast.LENGTH_LONG).show();
                        success = false;
                    }
                } else {
                    Toast.makeText(UploadActivity.this, "The folder InterviewData does not exist. ", Toast.LENGTH_LONG).show();
                    success = false;
                }
            } else {
                Toast.makeText(UploadActivity.this, "The folder Sherp does not exist. ", Toast.LENGTH_LONG).show();
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
