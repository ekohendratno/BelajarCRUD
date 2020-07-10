package id.kopas.firebase.belajarcrud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements RecyclerViewAdapter.dataListener {

    AppCompatEditText editKey, editNama, editAlamat;

    private ArrayList<Siswa> siswaList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    DatabaseReference myRef;
    FirebaseUser user;
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("siswa");


        final String getUserID = user.getUid();

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

        findViewById(R.id.actBaru).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editKey.setText("");
                editNama.setText("");
                editAlamat.setText("");
            }
        });

        findViewById(R.id.actSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = editKey.getText().toString();
                String nama = editNama.getText().toString();
                String alamat = editAlamat.getText().toString();

                if(!TextUtils.isEmpty(key)){

                    myRef.child(key).setValue( new Siswa(nama,alamat) )
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    editKey.setText("");
                                    editNama.setText("");
                                    editAlamat.setText("");

                                    Toast.makeText(MainActivity.this, "Data Berhasil diubah", Toast.LENGTH_SHORT).show();
                                }
                            });

                }else{

                    myRef.push().setValue( new Siswa(nama,alamat) )
                            .addOnSuccessListener(MainActivity.this, new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {

                                    editKey.setText("");
                                    editNama.setText("");
                                    editAlamat.setText("");

                                    Toast.makeText(MainActivity.this, "Data Tersimpan", Toast.LENGTH_SHORT).show();

                                }
                            });

                }
            }
        });

        /**
         String userID = auth.getUid();
         String getKey = getIntent().getExtras().getString("getPrimaryKey");
         database.child("Admin")
         .child(userID)
         .child("Mahasiswa")
         .child(getKey)
         .setValue(mahasiswa)
         .addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
        nimBaru.setText("");
        namaBaru.setText("");
        jurusanBaru.setText("");
        Toast.makeText(updateData.this, "Data Berhasil diubah", Toast.LENGTH_SHORT).show();
        finish();
        }
        });
         */
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
    }
}