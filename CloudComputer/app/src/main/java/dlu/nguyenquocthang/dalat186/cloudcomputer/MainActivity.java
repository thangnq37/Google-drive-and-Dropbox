package dlu.nguyenquocthang.dalat186.cloudcomputer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import dlu.nguyenquocthang.dalat186.cloudcomputer.DropboxAPI.ListMainDropbox;
import dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI.ListFilesInFolderActivity;


public class MainActivity extends Activity {


//    private SignInButton btnSignIn;
    private Button imgBtnDrive,imgBtnDropbox;


//    private static final int REQUEST_CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgBtnDrive = (Button)findViewById(R.id.imageButtonDrive);
        imgBtnDropbox = (Button)findViewById(R.id.imageButtonDropbox);


        Animation animation = AnimationUtils.loadAnimation(this,R.anim.translate_btn_drive);
        imgBtnDrive.startAnimation(animation);
        imgBtnDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // signOutGoogle();
                Intent intent = new Intent(MainActivity.this, ListFilesInFolderActivity.class);

                startActivity(intent);
                overridePendingTransition(R.anim.translate_layout_main,R.anim.translate_layout_file_main);

            }
        });
        imgBtnDropbox.startAnimation(animation);
        imgBtnDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListMainDropbox.class);

                startActivity(intent);
                overridePendingTransition(R.anim.translate_layout_main,R.anim.translate_layout_file_main);

            }
        });
    }



}
