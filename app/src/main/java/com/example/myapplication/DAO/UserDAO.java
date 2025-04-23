package com.example.myapplication.DAO;

import com.example.myapplication.Model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserDAO {
    private final FirebaseFirestore db;

    public UserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // Đăng ký người dùng mới với id tự tăng (lấy max id hiện tại + 1)
    public void registerUser(String name, String email, String password, String img, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(result -> {
                    if (!result.isEmpty()) {
                        onSuccess.onSuccess(false); // Email đã tồn tại
                    } else {
                        // Lấy ID lớn nhất hiện có
                        db.collection("users")
                                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(maxQuery -> {
                                    int newId = 1;
                                    if (!maxQuery.isEmpty()) {
                                        Integer currentMaxId = maxQuery.getDocuments().get(0).getLong("id").intValue();
                                        newId = currentMaxId + 1;
                                    }

                                    UserModel user = new UserModel(newId, name, email, password, img);
                                    db.collection("users")
                                            .add(user)
                                            .addOnSuccessListener(docRef -> onSuccess.onSuccess(true))
                                            .addOnFailureListener(onFailure);

                                }).addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // Kiểm tra đăng nhập
    public void checkUser(String email, String password, OnSuccessListener<UserModel> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        UserModel user = querySnapshot.getDocuments().get(0).toObject(UserModel.class);
                        onSuccess.onSuccess(user);
                    } else {
                        onSuccess.onSuccess(null); // Không đúng
                    }
                })
                .addOnFailureListener(onFailure);
    }
}
