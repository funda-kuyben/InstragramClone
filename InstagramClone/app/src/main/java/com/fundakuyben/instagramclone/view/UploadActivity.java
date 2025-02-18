package com.fundakuyben.instagramclone.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.fundakuyben.instagramclone.databinding.ActivityUploadBinding;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    Uri imageData; //firebase
    ActivityResultLauncher<Intent> activityResultLauncher; //galeriye gitme intenti
    ActivityResultLauncher<String> permissionLauncher;
    private ActivityUploadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_upload); //bu satiri sildik
        binding = ActivityUploadBinding.inflate(getLayoutInflater()); //XML'de tanimlanan gorsel bilesenlere dogrudan erisim icin
        View view = binding.getRoot();
        setContentView(view); //aldigimiz gorunumu verebildik boylece.  Activity için bağlanan görünümü set ediyoruz.
        registerLauncher(); // Activity başlarken izin ve galeri seçimi işlemleri için launcher kayıt ediyoruz.
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference=firebaseStorage.getReference();
    }

    public void uploadButtonClicked(View view){
        /*Bu metod, seçilen resmin Firebase Storage'a yüklenmesini ve
        ardından Firestore'a kaydedilmesini gerçekleştiriyor.
        Resim yükleme işlemi başarılı olursa, resmin URL'si alınıp Firestore'a post bilgileri (email, yorum, tarih, URL) gönderiliyor.
Başarısız olursa, kullanıcıya hata mesajı gösteriliyor.*/
        // upload işlemleri burada yapılacak
        //universal uniqee id
        UUID uuid = UUID.randomUUID(); //Benzersiz bir ad oluşturmak için UUID.randomUUID() metodu.Bu yöntem, her çağrıldığında farklı bir kimlik üretir.
        String imageName="images/"+uuid+".jpg"; //her seferinde yeni gorsel ismi olusturmazsak tek bir resim yukleyebiliriz, isimleri ayni olacagindan dolayi
        if(imageData!=null){
            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                //imageDatayı firebase'e yuklemek icin
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //Resim veya dosya, Firebase Storage’a yüklenirken.putFile(imageData) metodu, dosyayı belirlenen referansa yükler.
                    // download url'yi alacagiz, veri tabanına(firestore) kaydedecegiz
                    StorageReference newReference = firebaseStorage.getReference(imageName); //kaydedilen gorsel icin referans
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl=uri.toString(); //gorsel
                            String comment=binding.commentText.getText().toString(); //yorum
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            String email=user.getEmail();

                            HashMap<String, Object> postData = new HashMap<>();
                            postData.put("useremail",email);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent=new Intent(UploadActivity.this, FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    public void selectImageClicked(View view){
        /*Eğer izin verildiyse, galeri seçmek için bir Intent başlatılıyor ve
         resim seçildikten sonra bu Intent'in sonucu işleniyor.*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 ve sonrası
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){ // Kullanıcının "READ_MEDIA_IMAGES" iznine sahip olup olmadığını kontrol eder.
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view, "Permission needed for gallery.",Snackbar.LENGTH_INDEFINITE)
                            .setAction("Give permission.", v -> permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)) // Lambda
                            .show();
                } else {
                    //ask permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES); // Kullanıcıdan "READ_MEDIA_IMAGES" iznini istemek için permissionLauncher kullanılır. Kullanıcı izin verirse, bir sonraki adıma geçeriz.
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //bu intent için result launcher tanımlıyoruz başa
                activityResultLauncher.launch(intentToGallery);  //galeriye gitme intenti
            }
        } else { // Android 12 ve altı için eski izin kontrolü
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "Permission needed for gallery.",Snackbar.LENGTH_INDEFINITE)
                            .setAction("Give permission.", v -> permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)) // Lambda
                            .show();
                } else {
                    //ask permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> { // Lambda
            if (result.getResultCode() == RESULT_OK){
                Intent intentFromResult = result.getData();
                if(intentFromResult != null) { //eğer bu intentten bir sonuç geri döndüyse
                    imageData = intentFromResult.getData(); //bunu yapabilmek için yukarıda Uri imageData degiskenini olustururuz. firebase bizden URI isteyecek.
                    //firebase'e koymak için bu yeterli ama kullaniciya da gostermemiz gerek.
                    binding.imageView.setImageURI(imageData); // bu satır sayesinde gosterebildik gorseli
                }
            }
        });
        /*activityResultLauncher: Seçilen resmin veri döndüğü sonuçları işleyen bir launcher tanımlıyor.*/

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), (Boolean result) -> { // Lambda
            if(result){ //eger result dogruysa yani izin verildiyse
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //bu intent için result launcher tanımlıyoruz
                activityResultLauncher.launch(intentToGallery); //galeriye gitme intenti
            }else{ //izin verilmediyse
                Toast.makeText(UploadActivity.this, "Permission Needed!", Toast.LENGTH_LONG).show();
            }
        });
        /*permissionLauncher: Kullanıcının izin isteğini kontrol ediyor ve
        izin verildiğinde galeriye gitmek için activityResultLauncher'ı başlatıyor.*/
    }
}
/*Bu kod, kullanıcıdan resim seçmesini ve o resmin yorumla birlikte Firebase Storage'a yüklenmesini sağlar.
Yüklenen resimler, Firestore'da "Posts" adında bir koleksiyon içinde saklanır.
Kullanıcı bu işlemi FeedActivity'den görsel olarak takip edebilir ve yüklediği içerikleri görüntüleyebilir.*/
