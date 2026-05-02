package com.pc.mediclaim.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pc.mediclaim.data.DatabaseHelper;
import com.pc.mediclaim.model.Claim;
import com.pc.mediclaim.model.Dependent;
import com.pc.mediclaim.model.Hospital;
import com.pc.mediclaim.model.Policy;
import com.pc.mediclaim.model.User;
import java.util.ArrayList;
import java.util.List;

public class MediclaimRepository {
    private DatabaseHelper dbHelper;

    public MediclaimRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_USERS + 
                " WHERE " + DatabaseHelper.COLUMN_USER_EMAIL + "=?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isMobileExists(String mobile) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_USERS + 
                " WHERE " + DatabaseHelper.COLUMN_USER_MOBILE + "=?", new String[]{mobile});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isMemberIdExists(String memberId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_USERS + 
                " WHERE " + DatabaseHelper.COLUMN_USER_MEMBER_ID + "=?", new String[]{memberId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public User login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + 
                " WHERE " + DatabaseHelper.COLUMN_USER_EMAIL + "=? AND " + 
                DatabaseHelper.COLUMN_USER_PASSWORD + "=?", new String[]{email, password});
        
        User user = null;
        if (cursor.moveToFirst()) {
            int isApproved = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_APPROVED));
            if (isApproved == 1) {
                user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MOBILE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MEMBER_ID))
                );
            } else {
                user = new User(-2, "", "", "", "", "");
            }
        }
        cursor.close();
        return user;
    }

    public long registerUser(String name, String email, String mobile, String password) {
        if (isEmailExists(email) || isMobileExists(mobile)) {
            return -1; // Indicate duplicate
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_MOBILE, mobile);
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_USER_ROLE, "user");
        values.put(DatabaseHelper.COLUMN_USER_IS_APPROVED, 0);
        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USER_IS_APPROVED + "=0", null);
        
        if (cursor.moveToFirst()) {
            do {
                users.add(new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MOBILE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MEMBER_ID))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    public int approveUser(int userId, String memberId, String policyType, double sumInsured) {
        if (isMemberIdExists(memberId)) {
            return -2; // Member ID already exists
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Update User Status
        ContentValues userValues = new ContentValues();
        userValues.put(DatabaseHelper.COLUMN_USER_IS_APPROVED, 1);
        userValues.put(DatabaseHelper.COLUMN_USER_MEMBER_ID, memberId);
        int updated = db.update(DatabaseHelper.TABLE_USERS, userValues, DatabaseHelper.COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        
        if (updated > 0) {
            // Create Policy for user
            ContentValues policyValues = new ContentValues();
            policyValues.put(DatabaseHelper.COLUMN_POLICY_USER_ID, userId);
            policyValues.put(DatabaseHelper.COLUMN_POLICY_COMPANY, "Sinhgad Insurance Company");
            policyValues.put(DatabaseHelper.COLUMN_POLICY_TYPE, policyType);
            policyValues.put(DatabaseHelper.COLUMN_POLICY_NO, "SIC/" + (System.currentTimeMillis() % 100000));
            policyValues.put(DatabaseHelper.COLUMN_POLICY_FROM, "01/01/2024");
            policyValues.put(DatabaseHelper.COLUMN_POLICY_TO, "31/12/2024");
            policyValues.put(DatabaseHelper.COLUMN_POLICY_STATUS, "Active");
            policyValues.put(DatabaseHelper.COLUMN_POLICY_SUM_INSURED, sumInsured);
            policyValues.put(DatabaseHelper.COLUMN_POLICY_UTILIZED, 0);
            db.insert(DatabaseHelper.TABLE_POLICIES, null, policyValues);
        }
        
        return updated;
    }

    public Policy getPolicyByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_POLICIES + " WHERE " + DatabaseHelper.COLUMN_POLICY_USER_ID + "=?", new String[]{String.valueOf(userId)});
        
        Policy policy = null;
        if (cursor.moveToFirst()) {
            policy = new Policy(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_COMPANY)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_NO)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_FROM)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_TO)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_STATUS)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_SUM_INSURED)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POLICY_UTILIZED))
            );
        }
        cursor.close();
        return policy;
    }

    public List<Claim> getClaimsByUserId(int userId) {
        List<Claim> claims = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CLAIMS + " WHERE " + DatabaseHelper.COLUMN_CLAIM_USER_ID + "=?", new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                claims.add(new Claim(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_PATIENT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_DOA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_DOD)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_NO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_STATUS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_DOCUMENT))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return claims;
    }

    public List<Claim> getAllClaims() {
        List<Claim> claims = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CLAIMS, null);
        
        if (cursor.moveToFirst()) {
            do {
                claims.add(new Claim(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_PATIENT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_DOA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_DOD)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_NO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_STATUS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLAIM_DOCUMENT))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return claims;
    }

    public long addClaim(Claim claim) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLAIM_USER_ID, claim.getUserId());
        values.put(DatabaseHelper.COLUMN_CLAIM_PATIENT, claim.getPatientName());
        values.put(DatabaseHelper.COLUMN_CLAIM_DOA, claim.getDoa());
        values.put(DatabaseHelper.COLUMN_CLAIM_DOD, claim.getDod());
        values.put(DatabaseHelper.COLUMN_CLAIM_AMOUNT, claim.getAmount());
        values.put(DatabaseHelper.COLUMN_CLAIM_NO, claim.getClaimNo());
        values.put(DatabaseHelper.COLUMN_CLAIM_STATUS, claim.getStatus());
        values.put(DatabaseHelper.COLUMN_CLAIM_DOCUMENT, claim.getDocumentUri());
        return db.insert(DatabaseHelper.TABLE_CLAIMS, null, values);
    }

    public int updateClaimStatus(int claimId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLAIM_STATUS, status);
        return db.update(DatabaseHelper.TABLE_CLAIMS, values, DatabaseHelper.COLUMN_CLAIM_ID + "=?", new String[]{String.valueOf(claimId)});
    }

    public List<Dependent> getDependentsByUserId(int userId) {
        List<Dependent> dependents = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_DEPENDENTS + " WHERE " + DatabaseHelper.COLUMN_DEP_USER_ID + "=?", new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                dependents.add(new Dependent(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEP_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEP_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEP_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEP_RELATION))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dependents;
    }

    public List<Hospital> getHospitalsByCity(String city) {
        List<Hospital> hospitals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if (city == null || city.isEmpty()) {
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_HOSPITALS, null);
        } else {
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_HOSPITALS + " WHERE " + DatabaseHelper.COLUMN_HOSP_CITY + " LIKE ?", new String[]{"%" + city + "%"});
        }
        
        if (cursor.moveToFirst()) {
            do {
                hospitals.add(new Hospital(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOSP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOSP_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOSP_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOSP_CONTACT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOSP_CITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HOSP_STATE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hospitals;
    }

    public boolean isHospitalExists(String name, String city) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_HOSPITALS + 
                " WHERE " + DatabaseHelper.COLUMN_HOSP_NAME + "=? AND " + 
                DatabaseHelper.COLUMN_HOSP_CITY + "=?", new String[]{name, city});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public long addHospital(Hospital hospital) {
        if (isHospitalExists(hospital.getName(), hospital.getCity())) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_HOSP_NAME, hospital.getName());
        values.put(DatabaseHelper.COLUMN_HOSP_ADDRESS, hospital.getAddress());
        values.put(DatabaseHelper.COLUMN_HOSP_CONTACT, hospital.getContact());
        values.put(DatabaseHelper.COLUMN_HOSP_CITY, hospital.getCity());
        values.put(DatabaseHelper.COLUMN_HOSP_STATE, hospital.getState());
        return db.insert(DatabaseHelper.TABLE_HOSPITALS, null, values);
    }

    public int updateHospital(Hospital hospital) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_HOSP_NAME, hospital.getName());
        values.put(DatabaseHelper.COLUMN_HOSP_ADDRESS, hospital.getAddress());
        values.put(DatabaseHelper.COLUMN_HOSP_CONTACT, hospital.getContact());
        values.put(DatabaseHelper.COLUMN_HOSP_CITY, hospital.getCity());
        values.put(DatabaseHelper.COLUMN_HOSP_STATE, hospital.getState());
        return db.update(DatabaseHelper.TABLE_HOSPITALS, values, DatabaseHelper.COLUMN_HOSP_ID + "=?", new String[]{String.valueOf(hospital.getId())});
    }

    public int updateUser(int userId, String email, String mobile) {
        if (email != null) {
            SQLiteDatabase dbR = dbHelper.getReadableDatabase();
            Cursor c = dbR.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USER_EMAIL + "=? AND " + DatabaseHelper.COLUMN_USER_ID + "!=?", new String[]{email, String.valueOf(userId)});
            if (c.getCount() > 0) {
                c.close();
                return -2;
            }
            c.close();
        }
        if (mobile != null) {
            SQLiteDatabase dbR = dbHelper.getReadableDatabase();
            Cursor c = dbR.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USER_MOBILE + "=? AND " + DatabaseHelper.COLUMN_USER_ID + "!=?", new String[]{mobile, String.valueOf(userId)});
            if (c.getCount() > 0) {
                c.close();
                return -3;
            }
            c.close();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (email != null) values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        if (mobile != null) values.put(DatabaseHelper.COLUMN_USER_MOBILE, mobile);
        return db.update(DatabaseHelper.TABLE_USERS, values, DatabaseHelper.COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }
}
