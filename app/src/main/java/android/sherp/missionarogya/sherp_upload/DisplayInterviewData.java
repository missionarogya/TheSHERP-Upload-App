package android.sherp.missionarogya.sherp_upload;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

public class DisplayInterviewData extends AppCompatActivity {
    SherpData sherpData = SherpData.getInstance();
    JSONArray interviewJSON;
    Logging logfile = Logging.getInstance();
    String logmessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_interview_data);

        Toast.makeText(getApplicationContext(), sherpData.getHttpStatusDownload(), Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),sherpData.getMessageDownload(),Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),sherpData.getInterviewData(),Toast.LENGTH_LONG).show();

        try{
            validateJSON(sherpData.getInterviewData());
        }catch(Exception e){
            Toast.makeText(DisplayInterviewData.this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }

        final Button exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //roll back changes
                Intent intent = new Intent(DisplayInterviewData.this, LoginActivity.class);
                DisplayInterviewData.this.startActivity(intent);
                DisplayInterviewData.this.finish();
            }
        });

    }

    private boolean validateJSON(String jsonContent) throws Exception{
        boolean success;
        try{
            interviewJSON = new JSONArray(jsonContent);
            logfile.setJsonString(interviewJSON.toString());
            logfile.setInstance(logfile);
            success = true;
        }catch(JSONException e){
            Toast.makeText(DisplayInterviewData.this, "Invalid JSON: "+e.getMessage(), Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Invalid JSON: :"+e.getMessage()+"\n";
            success = false;
        }catch(Exception e){
            Toast.makeText(DisplayInterviewData.this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
            logmessage = logmessage + "Invalid JSON: :"+e.getMessage()+"\n";
            success = false;
        }
        return success;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_interview_data, menu);
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
