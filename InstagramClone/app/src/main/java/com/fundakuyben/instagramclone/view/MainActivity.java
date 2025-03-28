package com.fundakuyben.instagramclone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.fundakuyben.instagramclone.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); //findViewById kullanmamak icin
        View view = binding.getRoot(); //build.gradle.kts'de viewBinding = true yazmamizin sebebi bu
        setContentView(view);

        auth = FirebaseAuth.getInstance(); //firebase documantation'dan bakara yap
        FirebaseUser user=auth.getCurrentUser(); //uygulamadan cikilinca hesap kapanmasin diye

        if(user != null){
            Intent intent = new Intent(MainActivity.this,FeedActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void signInClicked(View view) {
        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();

        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Please enter your e-mail and password", Toast.LENGTH_LONG).show();
        } else {
            // Kullanıcı giriş işlemi
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    // Başarılı giriş, FeedActivity'e geçiş
                    Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Başarısız giriş
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void signUpClicked(View view){
        String email=binding.emailText.getText().toString();
        String password=binding.passwordText.getText().toString();

        if(email.equals("") || password.equals("")){
            Toast.makeText(this,"Please enter your e-mail and password",Toast.LENGTH_LONG).show();
        }else {
            //eger basarili bir sekilde email ve password alindiysa:
            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Intent intent = new Intent(MainActivity.this,FeedActivity.class); //mainactivity'den feedActivity'e gideceğiz
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() { //eger basarili degilse
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show(); //getLocalizedMessage kullanicinin anlayacagi dilden bir hata mesaji yazdirir
                }
            });
        }


    }
}
/*Bu kod bloğu, Giriş ve Kayıt işlemleri için kullanılan bir MainActivity sınıfıdır.
 Kullanıcı Firebase Auth aracılığıyla sisteme giriş yapabilir veya yeni bir hesap oluşturabilir.
 Eğer giriş başarılı olursa, kullanıcı FeedActivity’ye yönlendirilir.
 Aksi takdirde, hatalar kullanıcıya bildirilir.*/