package com.fundakuyben.instagramclone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.fundakuyben.instagramclone.R;
import com.fundakuyben.instagramclone.adapter.PostAdapter;
import com.fundakuyben.instagramclone.databinding.ActivityFeedBinding;
import com.fundakuyben.instagramclone.model.Post;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    private ActivityFeedBinding binding;
    PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);//Uygulamanın kenarlara doğru genişlemesini sağlar, sistem çubukları (status bar, navigation bar) gizlenerek tüm ekran kullanılır.
        binding = ActivityFeedBinding.inflate(getLayoutInflater());//viewBinding ile XML'deki ActivityFeedBinding objesini oluşturur. Bu, findViewById kullanımını ortadan kaldırır.
        View view = binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        /*ViewCompat.setOnApplyWindowInsetsListener: Uygulama kenarlıklarını sisteme uygun şekilde ayarlar, çerçeveler yok edilir ve tüm ekran kullanılır.*/
        postArrayList=new ArrayList<>();//Postların tutulacağı bir liste oluşturulmuştur.
        auth=FirebaseAuth.getInstance(); //initialize ettim
        firebaseFirestore=FirebaseFirestore.getInstance();

        getData();// Post verilerini alır

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Postları listelemek için RecyclerView
        postAdapter=new PostAdapter(postArrayList);// Adapter ayarlandı
        binding.recyclerView.setAdapter(postAdapter);// Adapter RecyclerView'ye bağlandı

    }

    private void getData(){ //Post Verilerini Almak için
        firebaseFirestore.collection("Posts")// Firestore'da "Posts" koleksiyonunu seç
                .orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(FeedActivity.this,error.getLocalizedMessage(),Toast.LENGTH_LONG);
                }
                if(value != null){
                    for(DocumentSnapshot snapshot : value.getDocuments()){ //getiDocuments listesindeki elemanlar siralanir
                        Map<String, Object> data = snapshot.getData();
                        //casting-emin oldugumuzda parantez icinde turunu yazarak degistirmek
                        String userEmail=(String) data.get("useremail");
                        String comment=(String) data.get("comment");
                        String downloadUrl=(String) data.get("downloadurl");
                        Post post = new Post(userEmail,comment,downloadUrl);
                        postArrayList.add(post);
                    }

                    postAdapter.notifyDataSetChanged(); //haber ver recycler view'a yeni veri geldigini

                }
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {      //option_menu'yu feedActivity'e baglamak icin yaptim
        MenuInflater menuInflater=getMenuInflater(); //xml ile buradaki kodu birbirine baglar
        menuInflater.inflate(R.menu.option_menu,menu); //option_menu'yu bagladiks
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {      //secilince ne olacagini yazdim
        if(item.getItemId()==R.id.add_post) { //yani kullanici add_post yazan yere tikladiysa
            //upload activity'e gidilecek
            Intent intentToUpload = new Intent(FeedActivity.this, UploadActivity.class); //***************bura onemli, feedactivity'den Uploadactivity'e gecmeyi saglar*****************************
            startActivity(intentToUpload);
            //finish demiyoruz cunku kullanici signout yapmaktan vazgecip geri donebilir
        } else if (item.getItemId()==R.id.signout) {
            //signout islemleri
            auth.signOut(); //database'in de signout yaptigimizi bilmesi gerekiyor..

            Intent intentToMain = new Intent(FeedActivity.this, MainActivity.class); //***************************aynı islem****************************
            startActivity(intentToMain);
            finish(); //kullanici cikis yaptigi icin finish de yazdik, geri donememesi lazim
        }
        return super.onOptionsItemSelected(item);
    }
}
/*Kullanıcı giriş yaptıktan sonra, postları listelemek ve giriş/çıkış işlemleri için Firebase Auth ve Firestore'u kullanır.
Ayrıca, RecyclerView ile postları listeleyen bir adaptör kullanılır.
Kullanıcı menüden post ekleyebilir ve çıkış yapabilir.*/



