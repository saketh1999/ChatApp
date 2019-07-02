package com.example.kacchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
private Button UpdateAccountSetting;
private EditText userName,userStatus;
private CircleImageView userProfileImage;
private String currrentUserID;
private FirebaseAuth mAuth;
private DatabaseReference RootRef;
private static final int GalleryPick=1;
private StorageReference UserProfileImagesRef;
private ProgressDialog loadingBar;
private Toolbar SettingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth=FirebaseAuth.getInstance();
        currrentUserID=mAuth.getCurrentUser().getUid();
        RootRef=FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        InitializeFields();


        UpdateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();
            }
        });
        RetrieveUserInfo();
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });
        
    }




    private void InitializeFields() {
        UpdateAccountSetting=(Button)findViewById(R.id.update_setting_button);
        userName=(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);

        SettingsToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri= data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)

                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("please wait,Uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
               final Uri resultUri=result.getUri();
               final StorageReference filePath=UserProfileImagesRef.child(currrentUserID+".jpg");
               filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                               final String downloadUri=uri.toString();
                               RootRef.child("Users").child(currrentUserID).child("image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       if(task.isSuccessful()){
                                           Toast.makeText(SettingsActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();

                                           loadingBar.dismiss();


                                       }
                                       else{
                                           String message = task.getException().getMessage();

                                           Toast.makeText(SettingsActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();

                                           loadingBar.dismiss();

                                       }
                                   }
                               });

                           }
                       });

                   }
               });
            }

        }
    }

    private void UpdateSetting(){
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Enter an UserName", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object>profileMap=new HashMap<>();
            profileMap.put("uid",currrentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currrentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile updated ", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String message=task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error :"+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")&&(dataSnapshot.hasChild("image"))))
                {

                    String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                       String retrievesStatus=dataSnapshot.child("status").getValue().toString();

                       String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();
                       userName.setText(retrieveUserName);
                       userStatus.setText(retrievesStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                }
                else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){


                    String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                    String retrievesStatus=dataSnapshot.child("status").getValue().toString();


                    userName.setText(retrieveUserName);
                    userStatus.setText(retrievesStatus);

                }
                else{

                    Toast.makeText(SettingsActivity.this, "Set and Update profile ", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
