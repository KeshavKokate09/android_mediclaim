package com.pc.mediclaim.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sinhgad_mediclaim.db";
    private static final int DATABASE_VERSION = 6; // Incremented for document scanning support

    public static final String TABLE_USERS = "users";
    public static final String TABLE_POLICIES = "policies";
    public static final String TABLE_CLAIMS = "claims";
    public static final String TABLE_DEPENDENTS = "dependents";
    public static final String TABLE_HOSPITALS = "hospitals";

    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_MOBILE = "mobile";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_MEMBER_ID = "member_id";
    public static final String COLUMN_USER_IS_APPROVED = "is_approved";

    public static final String COLUMN_POLICY_ID = "id";
    public static final String COLUMN_POLICY_USER_ID = "user_id";
    public static final String COLUMN_POLICY_COMPANY = "company";
    public static final String COLUMN_POLICY_TYPE = "type";
    public static final String COLUMN_POLICY_NO = "policy_no";
    public static final String COLUMN_POLICY_FROM = "valid_from";
    public static final String COLUMN_POLICY_TO = "valid_to";
    public static final String COLUMN_POLICY_STATUS = "status";
    public static final String COLUMN_POLICY_SUM_INSURED = "sum_insured";
    public static final String COLUMN_POLICY_UTILIZED = "utilized";

    public static final String COLUMN_CLAIM_ID = "id";
    public static final String COLUMN_CLAIM_USER_ID = "user_id";
    public static final String COLUMN_CLAIM_PATIENT = "patient_name";
    public static final String COLUMN_CLAIM_DOA = "doa";
    public static final String COLUMN_CLAIM_DOD = "dod";
    public static final String COLUMN_CLAIM_AMOUNT = "amount";
    public static final String COLUMN_CLAIM_NO = "claim_no";
    public static final String COLUMN_CLAIM_STATUS = "status";
    public static final String COLUMN_CLAIM_DOCUMENT = "document_uri";

    public static final String COLUMN_DEP_ID = "id";
    public static final String COLUMN_DEP_USER_ID = "user_id";
    public static final String COLUMN_DEP_NAME = "name";
    public static final String COLUMN_DEP_RELATION = "relation";

    public static final String COLUMN_HOSP_ID = "id";
    public static final String COLUMN_HOSP_NAME = "name";
    public static final String COLUMN_HOSP_ADDRESS = "address";
    public static final String COLUMN_HOSP_CONTACT = "contact";
    public static final String COLUMN_HOSP_CITY = "city";
    public static final String COLUMN_HOSP_STATE = "state";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + "(" 
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COLUMN_USER_NAME + " TEXT," 
                + COLUMN_USER_EMAIL + " TEXT," 
                + COLUMN_USER_MOBILE + " TEXT," 
                + COLUMN_USER_PASSWORD + " TEXT," 
                + COLUMN_USER_ROLE + " TEXT," 
                + COLUMN_USER_MEMBER_ID + " TEXT,"
                + COLUMN_USER_IS_APPROVED + " INTEGER DEFAULT 0)");
        
        db.execSQL("CREATE TABLE " + TABLE_POLICIES + "(" 
                + COLUMN_POLICY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COLUMN_POLICY_USER_ID + " INTEGER," 
                + COLUMN_POLICY_COMPANY + " TEXT," 
                + COLUMN_POLICY_TYPE + " TEXT," 
                + COLUMN_POLICY_NO + " TEXT," 
                + COLUMN_POLICY_FROM + " TEXT," 
                + COLUMN_POLICY_TO + " TEXT," 
                + COLUMN_POLICY_STATUS + " TEXT," 
                + COLUMN_POLICY_SUM_INSURED + " REAL," 
                + COLUMN_POLICY_UTILIZED + " REAL)");
        
        db.execSQL("CREATE TABLE " + TABLE_CLAIMS + "(" 
                + COLUMN_CLAIM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COLUMN_CLAIM_USER_ID + " INTEGER," 
                + COLUMN_CLAIM_PATIENT + " TEXT," 
                + COLUMN_CLAIM_DOA + " TEXT," 
                + COLUMN_CLAIM_DOD + " TEXT," 
                + COLUMN_CLAIM_AMOUNT + " REAL," 
                + COLUMN_CLAIM_NO + " TEXT," 
                + COLUMN_CLAIM_STATUS + " TEXT,"
                + COLUMN_CLAIM_DOCUMENT + " TEXT)");
        
        db.execSQL("CREATE TABLE " + TABLE_DEPENDENTS + "(" 
                + COLUMN_DEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COLUMN_DEP_USER_ID + " INTEGER," 
                + COLUMN_DEP_NAME + " TEXT," 
                + COLUMN_DEP_RELATION + " TEXT)");
        
        db.execSQL("CREATE TABLE " + TABLE_HOSPITALS + "(" 
                + COLUMN_HOSP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COLUMN_HOSP_NAME + " TEXT," 
                + COLUMN_HOSP_ADDRESS + " TEXT," 
                + COLUMN_HOSP_CONTACT + " TEXT," 
                + COLUMN_HOSP_CITY + " TEXT," 
                + COLUMN_HOSP_STATE + " TEXT)");
        
        seedData(db);
        seedDummyHospitals(db);
    }

    private void seedData(SQLiteDatabase db) {
        ContentValues user = new ContentValues();
        user.put(COLUMN_USER_NAME, "Pratiksha Chaudhari");
        user.put(COLUMN_USER_EMAIL, "pratiksha");
        user.put(COLUMN_USER_MOBILE, "9876543210");
        user.put(COLUMN_USER_PASSWORD, "password");
        user.put(COLUMN_USER_ROLE, "user");
        user.put(COLUMN_USER_MEMBER_ID, "SINH-0012345");
        user.put(COLUMN_USER_IS_APPROVED, 1);
        long userId = db.insert(TABLE_USERS, null, user);

        ContentValues admin = new ContentValues();
        admin.put(COLUMN_USER_NAME, "Admin Sinhgad");
        admin.put(COLUMN_USER_EMAIL, "admin");
        admin.put(COLUMN_USER_MOBILE, "9999999999");
        admin.put(COLUMN_USER_PASSWORD, "admin");
        admin.put(COLUMN_USER_ROLE, "admin");
        admin.put(COLUMN_USER_IS_APPROVED, 1);
        db.insert(TABLE_USERS, null, admin);

        ContentValues policy = new ContentValues();
        policy.put(COLUMN_POLICY_USER_ID, userId);
        policy.put(COLUMN_POLICY_COMPANY, "Sinhgad Insurance Company");
        policy.put(COLUMN_POLICY_TYPE, "Gold Policy");
        policy.put(COLUMN_POLICY_NO, "SIC/2024/00876");
        policy.put(COLUMN_POLICY_FROM, "01/01/2024");
        policy.put(COLUMN_POLICY_TO, "31/12/2024");
        policy.put(COLUMN_POLICY_STATUS, "Active");
        policy.put(COLUMN_POLICY_SUM_INSURED, 500000);
        policy.put(COLUMN_POLICY_UTILIZED, 45000);
        db.insert(TABLE_POLICIES, null, policy);

        ContentValues dep1 = new ContentValues();
        dep1.put(COLUMN_DEP_USER_ID, userId);
        dep1.put(COLUMN_DEP_NAME, "Arjun Chaudhari");
        dep1.put(COLUMN_DEP_RELATION, "Father");
        db.insert(TABLE_DEPENDENTS, null, dep1);
    }

    private void seedDummyHospitals(SQLiteDatabase db) {
        String[][] data = {
            {"Deenanath Mangeshkar Hospital", "Erandwane", "020-40151000", "Pune", "Maharashtra"},
            {"Sahyadri Super Speciality Hospital", "Deccan Gymkhana", "020-67213000", "Pune", "Maharashtra"},
            {"Ruby Hall Clinic", "Sassoon Road", "020-66455100", "Pune", "Maharashtra"},
            {"Jehangir Hospital", "Sassoon Road", "020-66050000", "Pune", "Maharashtra"},
            {"Noble Hospital", "Hadapsar", "020-66285000", "Pune", "Maharashtra"},
            {"Inamdar Multispeciality Hospital", "Fatima Nagar", "020-66812222", "Pune", "Maharashtra"},
            {"Columbia Asia Hospital", "Kharadi", "020-71290000", "Pune", "Maharashtra"},
            {"Jupiter Hospital", "Baner", "020-27219000", "Pune", "Maharashtra"},
            {"Sancheti Hospital", "Shivajinagar", "020-25533333", "Pune", "Maharashtra"},
            {"Hardikar Hospital", "Shivajinagar", "020-25535353", "Pune", "Maharashtra"},
            {"KEM Hospital", "Rasta Peth", "020-26217300", "Pune", "Maharashtra"},
            {"Poona Hospital", "Sadashiv Peth", "020-66096000", "Pune", "Maharashtra"},
            {"Sassoon General Hospital", "Station Road", "020-26128000", "Pune", "Maharashtra"},
            {"Aditya Birla Memorial Hospital", "Chinchwad", "020-30717500", "Pune", "Maharashtra"},
            {"Lokmanya Hospital", "Nigdi", "020-30612000", "Pune", "Maharashtra"},
            {"Surya Mother and Child Super Specialty Hospital", "Wakad", "020-67911911", "Pune", "Maharashtra"},
            {"Lifepoint Multispecialty Hospital", "Wakad", "020-67123333", "Pune", "Maharashtra"},
            {"Global Multispeciality Hospital", "Sinhgad Road", "020-24350000", "Pune", "Maharashtra"},
            {"Navale Medical College and General Hospital", "Narhe", "020-24106215", "Pune", "Maharashtra"},
            {"Bharati Hospital", "Katraj-Dhankawadi", "020-40586000", "Pune", "Maharashtra"},
            {"Poona Medical Foundation", "Bibwewadi", "020-24214567", "Pune", "Maharashtra"},
            {"Chaitanya Hospital", "Chinchwad", "020-27471234", "Pune", "Maharashtra"},
            {"YCM Hospital", "Pimpri", "020-27422222", "Pune", "Maharashtra"},
            {"Star Hospital", "Akurdi", "020-27650000", "Pune", "Maharashtra"},
            {"Oyster & Pearl Hospital", "Shivajinagar", "020-67216600", "Pune", "Maharashtra"},
            {"Ratna Memorial Hospital", "Senapati Bapat Road", "020-41097777", "Pune", "Maharashtra"},
            {"Prayag Hospital", "Deccan Gymkhana", "020-25654567", "Pune", "Maharashtra"},
            {"Lokanath Hospital", "Kothrud", "020-25430000", "Pune", "Maharashtra"},
            {"Shashwat Hospital", "Kothrud", "020-25441234", "Pune", "Maharashtra"},
            {"Medipoint Hospital", "Aundh", "020-67258000", "Pune", "Maharashtra"},
            {"Vitalife Clinic", "Baner", "020-66859000", "Pune", "Maharashtra"},
            {"Apollo Clinic", "Viman Nagar", "020-49012345", "Pune", "Maharashtra"},
            {"Cloudnine Hospital", "Kalyani Nagar", "020-67911911", "Pune", "Maharashtra"},
            {"Motherhood Hospital", "Kharadi", "020-67210000", "Pune", "Maharashtra"},
            {"Rising Medicare Hospital", "Kharadi", "020-67240000", "Pune", "Maharashtra"},
            {"Sushrut Medical Care and Research", "Erandwane", "020-25434567", "Pune", "Maharashtra"},
            {"Lodha Multispeciality Hospital", "Pimpri", "020-27420000", "Pune", "Maharashtra"},
            {"Sant Dnyaneshwar Hospital", "Bhosari", "020-27122222", "Pune", "Maharashtra"},
            {"Sterling Multispeciality Hospital", "Nigdi", "020-67332222", "Pune", "Maharashtra"},
            {"Gunjal Hospital", "Wagholi", "020-67234567", "Pune", "Maharashtra"},
            {"Care Multispeciality Hospital", "Wagholi", "020-27050000", "Pune", "Maharashtra"},
            {"Fortis Hiranandani Hospital", "Vashi", "022-39199222", "Navi Mumbai", "Maharashtra"},
            {"MGM New Bombay Hospital", "Vashi", "022-61526666", "Navi Mumbai", "Maharashtra"},
            {"Lilavati Hospital", "Bandra West", "022-26751000", "Mumbai", "Maharashtra"},
            {"Nanavati Super Speciality Hospital", "Vile Parle West", "022-26267500", "Mumbai", "Maharashtra"},
            {"Kokilaben Dhirubhai Ambani Hospital", "Andheri West", "022-30666666", "Mumbai", "Maharashtra"},
            {"Breach Candy Hospital", "Bhulabhai Desai Road", "022-23667129", "Mumbai", "Maharashtra"},
            {"Godrej Memorial Hospital", "Vikhroli", "022-67964444", "Mumbai", "Maharashtra"},
            {"Bombay Hospital", "Marine Lines", "022-22067676", "Mumbai", "Maharashtra"},
            {"Wockhardt Hospital", "Nashik", "0253-6624444", "Nashik", "Maharashtra"}
        };

        for (String[] h : data) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HOSP_NAME, h[0]);
            values.put(COLUMN_HOSP_ADDRESS, h[1]);
            values.put(COLUMN_HOSP_CONTACT, h[2]);
            values.put(COLUMN_HOSP_CITY, h[3]);
            values.put(COLUMN_HOSP_STATE, h[4]);
            db.insert(TABLE_HOSPITALS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_CLAIMS + " ADD COLUMN " + COLUMN_CLAIM_DOCUMENT + " TEXT");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POLICIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLAIMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPENDENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOSPITALS);
            onCreate(db);
        }
    }
}
