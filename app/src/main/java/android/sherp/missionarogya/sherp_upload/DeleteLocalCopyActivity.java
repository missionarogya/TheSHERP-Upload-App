package android.sherp.missionarogya.sherp_upload;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class DeleteLocalCopyActivity extends AppCompatActivity {

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
                    Toast.makeText(DeleteLocalCopyActivity.this, "Successfully deleted the local copy of the interview JSON.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DeleteLocalCopyActivity.this, "Delete unsuccessful. Please delete the file manually!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final Button exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //roll back changes
                DeleteLocalCopyActivity.this.finish();
            }
        });
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
