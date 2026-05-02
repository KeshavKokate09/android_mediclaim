package com.pc.mediclaim.viewmodel;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pc.mediclaim.data.DatabaseHelper;
import com.pc.mediclaim.model.User;
import com.pc.mediclaim.repository.MediclaimRepository;

public class ProfileViewModel extends AndroidViewModel {
    private MediclaimRepository repository;
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new MediclaimRepository(application);
    }

    public LiveData<User> getUserLiveData() { return userLiveData; }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public void loadProfile(int userId) {
        User user = getUserById(userId);
        userLiveData.setValue(user);
    }

    public void updateProfile(int userId, String email, String mobile) {
        int result = repository.updateUser(userId, email, mobile);
        if (result == -2) {
            statusMessage.setValue("Email already in use by another account");
        } else if (result == -3) {
            statusMessage.setValue("Mobile number already in use by another account");
        } else if (result > 0) {
            statusMessage.setValue("Profile updated successfully");
            loadProfile(userId);
        } else {
            statusMessage.setValue("Update failed");
        }
    }

    private User getUserById(int userId) {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplication());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MOBILE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MEMBER_ID))
            );
        }
        cursor.close();
        return user;
    }
}
