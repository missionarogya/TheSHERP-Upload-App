package android.sherp.missionarogya.sherp_upload;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText txtName = (EditText)findViewById(R.id.txtName);
        final EditText txtPassword = (EditText)findViewById(R.id.txtPassword);
        final Button btnLogin = (Button)findViewById(R.id.btnLogin);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String username = txtName.getText().toString();
                    String password = txtPassword.getText().toString();
                    if(username.equals("admin") && password.equals("admin")){
                        showToast("Welcome admin!");
                        Intent intent = new Intent(LoginActivity.this, UploadActivity.class);
                        LoginActivity.this.startActivity(intent);
                    }else if(username.equals("") || password.equals("")){
                        showToast("Please fill in the username and password before you try to login.");
                    }else{
                        showToast("Permission denied !");
                    }
                }catch(Exception e){
                    Toast.makeText(LoginActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showToast(String message){
        Toast toast;
        toast = Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 545);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
