package id.kopas.firebase.belajarcrud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity  implements RecyclerViewAdapter.dataListener {

    Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;

    AppCompatImageView editFoto;
    AppCompatEditText editKey, editNama, editAlamat, editFotoUrl;

    private ArrayList<Siswa> siswaList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    FirebaseStorage storage;
    StorageReference myRefStorage;

    DatabaseReference myRef;
    FirebaseUser user;
    String getUserID;
    String urlStorage = "";

    String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        recyclerView = findViewById(R.id.recyclerView);


        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.line));
        recyclerView.addItemDecoration(itemDecoration);

        getUserID = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("siswa").child(getUserID);

        storage = FirebaseStorage.getInstance();
        myRefStorage = storage.getReference().child(getUserID);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //String value = dataSnapshot.getValue(String.class);

                siswaList = new ArrayList<>();

                for (DataSnapshot a : dataSnapshot.getChildren()){
                    Siswa siswa = a.getValue(Siswa.class);
                    siswa.setKey( a.getKey() );

                    //Log.e("onDataChange",siswa.nama);


                    siswaList.add(siswa);
                }
                adapter = new RecyclerViewAdapter(siswaList, MainActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }


        });


        editKey = findViewById(R.id.editKey);
        editNama = findViewById(R.id.editNama);
        editAlamat = findViewById(R.id.editAlamat);
        editFoto = findViewById(R.id.editFoto);
        editFotoUrl = findViewById(R.id.editFotoUrl);

        findViewById(R.id.actBaru).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editKey.setText("");
                editNama.setText("");
                editAlamat.setText("");
                editFoto.setImageResource(R.drawable.family_avatar);
            }
        });

        findViewById(R.id.actSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String key = editKey.getText().toString();
                String nama = editNama.getText().toString();
                String alamat = editAlamat.getText().toString();
                String fotoLama = editAlamat.getText().toString();

                if(!TextUtils.isEmpty(key)){
                    //update
                    myRef.child(key).setValue( new Siswa(nama,alamat,fotoLama) )
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    editKey.setText("");
                                    editNama.setText("");
                                    editAlamat.setText("");
                                    editFoto.setImageResource(R.drawable.family_avatar);

                                    if(filePath != null) uploadImage(key);

                                    Toast.makeText(MainActivity.this, "Data Berhasil diubah", Toast.LENGTH_SHORT).show();
                                }
                            });


                }else{

                    //add

                    final String key1 = myRef.push().getKey();
                    myRef.child(key1).setValue( new Siswa(nama, alamat, urlStorage) )
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    editKey.setText("");
                                    editNama.setText("");
                                    editAlamat.setText("");
                                    editFoto.setImageResource(R.drawable.family_avatar);

                                    if(filePath != null) uploadImage(key1);

                                    Toast.makeText(MainActivity.this, "Data Tersimpan", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });



        findViewById(R.id.editFoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                editFoto.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeleteData(Siswa data, int position) {
        String userID =  user.getUid();
        if(myRef != null){
            myRef.child(data.key)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onEditData(Siswa data, int position) {
        editKey.setText(data.key);
        editNama.setText(data.nama);
        editAlamat.setText(data.alamat);
        editFotoUrl.setText(data.foto);


        if(!TextUtils.isEmpty(data.foto)){
            Picasso.with(MainActivity.this)
                    .load( data.foto)
                    .transform(new CircleTransform())
                    .into(editFoto);
        }
    }

    boolean uploaded = false;
    private boolean uploadImage(final String key) {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final String namaFile = UUID.randomUUID() + "." + GetFileExtension(filePath);
            myRefStorage.child(namaFile).putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            uploaded = true;

                            myRefStorage.child(namaFile).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    //Menyimpan URL pada Variable String
                                    String url = uri.toString();

                                    myRef.child(key).child("foto").setValue(url);
                                    //Menentukan referensi lokasi data url yang akan disimpan
                                    //databaseReference.child("gambar").push().setValue();

                                }
                            });

                            urlStorage = taskSnapshot.getUploadSessionUri().toString();

                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            uploaded = false;
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }

        return uploaded;
    }

    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

}