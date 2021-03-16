package com.zero.shareby.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zero.shareby.R;
import com.zero.shareby.models.UserDetails;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;


public class UserSettings extends AppCompatActivity implements Button.OnClickListener{

    private static final String TAG=EditProfile.class.getSimpleName();
    private static final int RC_PICK=567;

    Uri photoUri;
    Uri downloadedUri;
    ImageView editImageProfile;
    EditText editNameText,editPhoneText,editAgeText,editAboutText;
//    EditText editEmailText;
    ProgressBar progressBar;
    Button saveButton;
    TextView editAddress;
    FirebaseUser user;
    String oldEmail;
    boolean isName,isEmail,isImage,isAbout,isAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        FirebaseAuth auth=FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        Log.d(TAG,user.getProviderId());
        ActionBar actionBar=getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        photoUri=null;
        downloadedUri=null;
        progressBar=findViewById(R.id.edit_layout_progress_bar);
        editImageProfile=findViewById(R.id.edit_profile_image);
        editNameText=findViewById(R.id.edit_profile_name);
        //editEmailText=findViewById(R.id.edit_profile_email);
        editPhoneText=findViewById(R.id.edit_profile_phone);
        editAgeText=findViewById(R.id.edit_profile_age);
        editAboutText=findViewById(R.id.edit_profile_about);
        saveButton=findViewById(R.id.edit_profile_save_button);
        editAddress=findViewById(R.id.edit_address_text_view);
        final Button changeLocationButton=findViewById(R.id.change_location_button);
        saveButton.setOnClickListener(this);
        updateTextFields();
        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserSettings.this,AddressActivity.class).putExtra("uname",editNameText.getText().toString().trim()));
            }
        });

        editImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto=new Intent(Intent.ACTION_PICK);
                pickPhoto.setType("image/*");
                startActivityForResult(Intent.createChooser(pickPhoto,"Complete action using"),RC_PICK);
            }
        });

        oldEmail = user.getEmail();

        if(auth.getCurrentUser()!=null){
            editNameText.setText(auth.getCurrentUser().getDisplayName());
            if (auth.getCurrentUser().getPhotoUrl()==null) {
                editImageProfile.setImageResource(R.drawable.sign);
                progressBar.setVisibility(View.GONE);
            }
            else{
                Log.d(TAG,"PHOTO"+auth.getCurrentUser().getPhotoUrl().toString());
                Glide.with(UserSettings.this)
                        .load(auth.getCurrentUser().getPhotoUrl())
                        .into(editImageProfile);
                progressBar.setVisibility(View.GONE);
            }
        }
        if (editNameText.getText().toString().trim().length()>0) {
            changeLocationButton.setEnabled(true);
            changeLocationButton.setTextColor(getResources().getColor(android.R.color.white));
            changeLocationButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
        }
        else {
            changeLocationButton.setEnabled(false);
            changeLocationButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
            changeLocationButton.setTextColor(getResources().getColor(android.R.color.white));
        }
        editNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()>0) {
                    changeLocationButton.setEnabled(true);
                    changeLocationButton.setTextColor(getResources().getColor(android.R.color.white));
                    changeLocationButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                }
                else {
                    changeLocationButton.setEnabled(false);
                    changeLocationButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
                    changeLocationButton.setTextColor(getResources().getColor(android.R.color.white));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void updateTextFields(){
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("UserDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double lat,lng;
                UserDetails userDetails=dataSnapshot.getValue(UserDetails.class);
                Log.d(TAG,String.valueOf(userDetails.getLatitude()));
                lat=userDetails.getLatitude();
                lng=userDetails.getLongitude();
                if (userDetails.getLongitude()!=0){
                    Geocoder geocoder = new Geocoder(UserSettings.this, Locale.getDefault());
                    List<Address> addressList;
                    try {
                        addressList=geocoder.getFromLocation(lat,lng,3);
                        Log.d(TAG,addressList.toString());
                        editAddress.setText(addressList.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                if(userDetails.getEmail()!=null)
//                    editEmailText.setText(userDetails.getEmail());
                if(userDetails.getPhone()!=null)
                    editPhoneText.setText(userDetails.getPhone());
                if(userDetails.getAge()>10 && userDetails.getAge()<110)
                    editAgeText.setText(String.valueOf(userDetails.getAge()));
                if(userDetails.getAbout()!=null)
                    editAboutText.setText(userDetails.getAbout());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==RC_PICK && resultCode==RESULT_OK && data!=null){
            photoUri=data.getData();
            try {
                editImageProfile.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),photoUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, photoUri.toString());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"onBack pressed");
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isImage=isAge=isAbout=isEmail=isName=false;
        updateTextFields();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_profile_menu:
                saveChangesToProfile();
                break;

            case R.id.discard_changes_menu:

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChangesToProfile(){
        String name=editNameText.getText().toString().trim();
       // String email=editEmailText.getText().toString().trim();
        String phone=editPhoneText.getText().toString().trim();
        String about=editAboutText.getText().toString().trim();
        String ageString=editAgeText.getText().toString();
        Log.d(TAG,"lol"+editAddress.getText().toString()+"lol");

        if (name.isEmpty()){
            Toast.makeText(this,"Name Field is Required",Toast.LENGTH_LONG).show();
            return;
        }

        if (phone.isEmpty()){
            Toast.makeText(this,"Phone Field is Required",Toast.LENGTH_LONG).show();
            return;
        }
        if (ageString.isEmpty()){
            Toast.makeText(this,"Age Field is Required",Toast.LENGTH_LONG).show();
            return;
        }

        int age = Integer.parseInt(ageString);
        if (!(age>10 && age<=100)){
            Toast.makeText(this,"Invalid Age Entered",Toast.LENGTH_LONG).show();
            return;
        }

        if (editAddress.getText().toString().trim().equals("No Address Set Yet")){
            Toast.makeText(this,"Address Field is Required",Toast.LENGTH_LONG).show();
        }

        else {

            if (photoUri != null) {
                progressBar.setVisibility(View.VISIBLE);

                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(photoUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byteArray = stream.toByteArray();
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long time=System.currentTimeMillis();
                uploadOnlyTextData(name,"",phone,about,age);
                progressBar.setVisibility(View.VISIBLE);
                final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(String.valueOf(time));
                storageRef.putBytes(byteArray).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (task.isSuccessful()) {
                            String oldFileURL;
                            Uri oldPhotoUrl=null;
                            try{
                            oldPhotoUrl=FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();}
                            catch (NullPointerException e){e.printStackTrace();}
                            if (oldPhotoUrl!=null) {
                                Log.d(TAG,oldPhotoUrl.toString());
                                if (!oldPhotoUrl.toString().contains("googleusercontent.com")) {
                                    oldFileURL = oldPhotoUrl.getLastPathSegment();
                                    StorageReference oldRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(oldFileURL.substring(oldFileURL.indexOf("/")));
                                    Log.d(TAG, oldFileURL.substring(oldFileURL.indexOf("/")));
                                    oldRef.delete();
                                }
                            }
                            return storageRef.getDownloadUrl();
                        }
                        else
                            throw task.getException();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        progressBar.setVisibility(View.VISIBLE);
                        downloadedUri = task.getResult();
                        UserProfileChangeRequest profileUpdates;
                        profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(editNameText.getText().toString().trim())
                                .setPhotoUri(downloadedUri)
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference()
                                                    .child("UserDetails").child(user.getUid()).child("photoUrl");
                                            ref.setValue(downloadedUri.toString());
                                            isName=isImage=true;
                                            Log.d(TAG, "User profile updated with profile image.");
                                            Toast.makeText(UserSettings.this, "Updated", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                            if (getIntent().getStringExtra("newUser")!=null && getIntent().getStringExtra("newUser").equals("true")){
                                                startActivity(new Intent(UserSettings.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            }
                                            toFinish();
                                        }
                                    }
                                });
                    }
                });
            }
            else {
                uploadOnlyTextData(name,"",phone,about,age);
                isImage=true;
                toFinish();
            }
        }
    }

    private void uploadOnlyTextData(String name,final String email,String phone,String about,int age){

        progressBar.setVisibility(View.VISIBLE);
        final UserProfileChangeRequest profileUpdates;
        profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        final DatabaseReference userNameref=FirebaseDatabase.getInstance().getReference().child("UserDetails").child(user.getUid());

//        Log.d(TAG,oldEmail+email);
//        if (!email.isEmpty() && !oldEmail.equals(email)) {
//            user.updateEmail(email)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                Log.d(TAG, "User email address updated.");
//                                userNameref.child("email").setValue(email);
//                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        isEmail=true;
//                                    }
//                                });
//                            }else Log.d(TAG,task.toString());
//                        }
//                    });
//
//        }else isEmail=true;
        userNameref.child("name").setValue(name);
        userNameref.child("age").setValue(age).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isAge = true;
            }
        });
        userNameref.child("phone").setValue(phone);

        if (!about.isEmpty())
            userNameref.child("about").setValue(about).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    isAbout=true;
                }
            });
        else isAbout=true;

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            isName=true;
                            Log.d(TAG, "User profile updated w/o image.");
                            Toast.makeText(UserSettings.this, "Updated", Toast.LENGTH_LONG).show();
                            if (getIntent().getStringExtra("newUser")!=null && getIntent().getStringExtra("newUser").equals("true")){
//                                while (!(isAbout && isEmail && isAge && isName && isImage));
                                startActivity(new Intent(UserSettings.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }

                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        saveChangesToProfile();
    }




    private void toFinish(){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                while (!(isAbout && isAge && isName && isImage)) {
                    try {
                        Log.d(TAG, "inside Run"+isAbout+isAge+isImage+isName);
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                UserSettings.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        UserSettings.this.finish();
                    }
                });
            }
        });

    }
}
