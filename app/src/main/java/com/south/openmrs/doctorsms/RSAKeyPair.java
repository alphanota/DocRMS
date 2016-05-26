package com.south.openmrs.doctorsms;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.codec.binary.Base64;


import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by angel on 5/21/16.
 */
public class RSAKeyPair {

    private static String mAlgorithm = "SHA256withRSA";


    // 2028 bit MODP group. group id 14
    // see RFC 3526
    private static final String DHModulus2048 ="FFFFFFFF"+"FFFFFFFF"+"C90FDAA2"+"2168C234"+"C4C6628B"+"80DC1CD1"
            +"29024E08"+"8A67CC74"+"020BBEA6"+"3B139B22"+"514A0879"+"8E3404DD"
            +"EF9519B3"+"CD3A431B"+"302B0A6D"+"F25F1437"+"4FE1356D"+"6D51C245"
            +"E485B576"+"625E7EC6"+"F44C42E9"+"A637ED6B"+"0BFF5CB6"+"F406B7ED"
            +"EE386BFB"+"5A899FA5"+"AE9F2411"+"7C4B1FE6"+"49286651"+"ECE45B3D"
            +"C2007CB8"+"A163BF05"+"98DA4836"+"1C55D39A"+"69163FA8"+"FD24CF5F"
            +"83655D23"+"DCA3AD96"+"1C62F356"+"208552BB"+"9ED52907"+"7096966D"
            +"670C354E"+"4ABC9804"+"F1746C08"+"CA18217C"+"32905E46"+"2E36CE3B"
            +"E39E772C"+"180E8603"+"9B2783A2"+"EC07A28F"+"B5C55DF0"+"6F4C52C9"
            +"DE2BCBF6"+"95581718"+"3995497C"+"EA956AE5"+"15D22618"+"98FA0510"
            +"15728E5A"+"8AACAA68"+"FFFFFFFF"+"FFFFFFFF";

    private static final String DHGenerator = "2";







    static KeyPair genRSAKeyPair(){
        KeyPairGenerator keyGen = null;
        try{
            keyGen=  KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e){

        }
        keyGen.initialize(2048);
        KeyPair key = keyGen.generateKeyPair();

        return key;

    }







    void foo(){

        KeyPairGenerator keyGen = null;
        try{
            keyGen=  KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e){

        }
        keyGen.initialize(2048);
        KeyPair key = keyGen.generateKeyPair();
        PrivateKey priv = key.getPrivate();
        PublicKey pub = key.getPublic();






        String privateKey = new String(Base64.encodeBase64URLSafeString(priv.getEncoded() ));
        String publicKey = new String(Base64.encodeBase64URLSafeString(priv.getEncoded() ));

        Signature rsa;
        try {
            rsa = Signature.getInstance(mAlgorithm,"SUN");
            rsa.initSign(priv);

            //rsa.initVerify();

            /*
            FileInputStream fis = new FileInputStream(args[0]);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            };
            bufin.close();

                            // sign
            byte[] realSig = dsa.sign();


            PublicKey publicKey =
            KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));

            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            */




        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


    }





    public static KeyPair generateDHKey(){


        BigInteger DHModulus = new BigInteger(DHModulus2048,16);
        BigInteger DHBase  = new BigInteger(DHGenerator,16);


        DHParameterSpec dhKeySpec=new DHParameterSpec(DHModulus,DHBase);
        try {

            // create and init DH keypair
            KeyPairGenerator DHKeyPairGenerator = KeyPairGenerator.getInstance("DH");
            DHKeyPairGenerator.initialize(dhKeySpec);
            KeyPair myKeyPair = DHKeyPairGenerator.generateKeyPair();

            /*
            // create and init DH key agreement
            KeyAgreement myKeyAgree = KeyAgreement.getInstance("DH");
            myKeyAgree.init(myKeyPair.getPrivate());


            // encode public key and send
            byte[] myDHPubkey = myKeyPair.getPublic().getEncoded();
            // send myDHPPubKey
            */

            return myKeyPair;





        }catch (NoSuchAlgorithmException e){


        } catch (InvalidAlgorithmParameterException f){

        }


        return null;

    }





    void otherGenerate(byte[] DH_PUB_KEY_Initiator_Bytes){


        try {
            KeyFactory myKeyFactory = KeyFactory.getInstance("DH");
            X509EncodedKeySpec X509KeySpec = new X509EncodedKeySpec(DH_PUB_KEY_Initiator_Bytes);
            PublicKey Initiator_DH_PUB_KEY = myKeyFactory.generatePublic(X509KeySpec);


            DHParameterSpec dhParamSpec = ((DHPublicKey) Initiator_DH_PUB_KEY ).getParams();

            // Create my own DH Keypair

            KeyPairGenerator myKeyPairGen = KeyPairGenerator.getInstance("DH");
            myKeyPairGen.initialize(dhParamSpec);
            KeyPair myKeyPair = myKeyPairGen.generateKeyPair();



            // generate key agreement

            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(myKeyPair.getPrivate());

            byte[] my_pub_key_bytes = myKeyPair.getPublic().getEncoded();

            // send my_pub_key_bytes




        } catch (NoSuchAlgorithmException e){

        } catch (InvalidKeySpecException iks){

        } catch (InvalidAlgorithmParameterException iap){

        } catch(InvalidKeyException ikeys){

        }



    }




    public static long saveKeyPair(Context context, long localUserId, long remoteUserId, String pub, String priv){

        RSAKeyStore  rsaKeyStore = new RSAKeyStore(context);

        //get the data repository in write mode
        SQLiteDatabase db = rsaKeyStore.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(RSAKeyStore.RSAKeyEntry.COLUMN_NAME_LOCALID,localUserId);
        values.put(RSAKeyStore.RSAKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId);
        values.put(RSAKeyStore.RSAKeyEntry.COLUMN_NAME_PUB_KEY,pub);
        values.put(RSAKeyStore.RSAKeyEntry.COLUMN_NAME_PRIV_KEY,priv);


        long newRowId;

        newRowId = db.insert(RSAKeyStore.RSAKeyEntry.TABLE_NAME, null,
                values);



        //db.close();

        return newRowId;


    }



    public static long saveDHKeyPair(Context context, long localUserId, long remoteUserId, String pub, String priv){

        DHKeyStore  dhKeyStore = new DHKeyStore(context);

        //get the data repository in write mode
        SQLiteDatabase db = dhKeyStore.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DHKeyStore.DHKeyEntry.COLUMN_NAME_LOCALID,localUserId);
        values.put(DHKeyStore.DHKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId);
        values.put(DHKeyStore.DHKeyEntry.COLUMN_NAME_PUB_KEY,pub);
        values.put(DHKeyStore.DHKeyEntry.COLUMN_NAME_PRIV_KEY,priv);


        long newRowId;

        newRowId = db.insert(DHKeyStore.DHKeyEntry.TABLE_NAME, null,
                values);



        //db.close();

        return newRowId;


    }


    public static long savePublicKey(Context context, long localUserId, long remoteUserId, String pub, String priv){

        RSAPublicKeyStore  rsaPublicKeyStore = new RSAPublicKeyStore(context);

        //get the data repository in write mode
        SQLiteDatabase db = rsaPublicKeyStore.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(RSAPublicKeyStore.RSAPublicKeyEntry.COLUMN_NAME_LOCALID,localUserId);
        values.put(RSAPublicKeyStore.RSAPublicKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId);
        values.put(RSAPublicKeyStore.RSAPublicKeyEntry.COLUMN_NAME_PUB_KEY,pub);

        long newRowId;

        newRowId = db.insert(RSAPublicKeyStore.RSAPublicKeyEntry.TABLE_NAME, null,
                values);



        //db.close();

        return newRowId;


    }



    public static long saveDHPublicKey(Context context, long localUserId, long remoteUserId, String pub, String priv){

        DHPublicKeyStore  rsaPublicKeyStore = new DHPublicKeyStore(context);

        //get the data repository in write mode
        SQLiteDatabase db = rsaPublicKeyStore.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DHPublicKeyStore.DHPublicKeyEntry.COLUMN_NAME_LOCALID,localUserId);
        values.put(DHPublicKeyStore.DHPublicKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId);
        values.put(DHPublicKeyStore.DHPublicKeyEntry.COLUMN_NAME_PUB_KEY,pub);

        long newRowId;

        newRowId = db.insert(DHPublicKeyStore.DHPublicKeyEntry.TABLE_NAME, null,
                values);



        //db.close();

        return newRowId;


    }


    public static byte[] sign(PrivateKey priv, String data){


        Signature rsa;
        byte[] stream = data.getBytes();
        try {
            rsa = Signature.getInstance(mAlgorithm);
            rsa.initSign(priv);

            rsa.update(stream);

            // sign
            byte[] realSig = rsa.sign();

            return realSig;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }  catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }



    public static byte[] AESencrypt(String plainText, byte[] enc_key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(enc_key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
        byte[] enc =  cipher.doFinal(plainText.getBytes());
        return enc;

    }

    public static byte[] AESdecrypt(byte[] cipherText, byte[] enk_key, byte[] iv) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(enk_key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }

    public static long saveAESKey(Context context,long localUserId, long remoteUserId, String secure_message_key){

        AesKeyStore  aesks = new AesKeyStore(context);

        //get the data repository in write mode
        SQLiteDatabase db = aesks.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(AesKeyStore.AesKeyEntry.COLUMN_NAME_LOCALID,localUserId);
        values.put(AesKeyStore.AesKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId);
        values.put(AesKeyStore.AesKeyEntry.COLUMN_NAME_AES_KEY,secure_message_key);

        long newRowId;

        newRowId = db.insert(AesKeyStore.AesKeyEntry.TABLE_NAME, null,
                values);



        //db.close();

        return newRowId;

    }


    public static String getAESKey(Context context,long localUserId, long remoteUserId){

        AesKeyStore  aesks = new AesKeyStore(context);
        SQLiteDatabase db = aesks.getReadableDatabase();

        String[] projection = {
                AesKeyStore.AesKeyEntry.COLUMN_NAME_LOCALID,
                AesKeyStore.AesKeyEntry.COLUMN_NAME_REMOTEID,
                AesKeyStore.AesKeyEntry.COLUMN_NAME_AES_KEY

        };

        // this is just simple
        // placeholder replacement
        // not switching cases or anything
        @SuppressLint("DefaultLocale")
        String selection = String.format("(%s = %d AND %s = %d)",
                AesKeyStore.AesKeyEntry.COLUMN_NAME_LOCALID,localUserId,
                AesKeyStore.AesKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId
        );




        Cursor c =db.query(AesKeyStore.AesKeyEntry.TABLE_NAME, // the table name
                projection,
                selection,
                null,
                null,
                null,
                null
        );

        c.moveToFirst();
        int keypos = c.getColumnIndexOrThrow(AesKeyStore.AesKeyEntry.COLUMN_NAME_AES_KEY);
        String key = c.getString(keypos);

        return key;

    }


    public static String getRSAPrivateKey(Context context,long localUserId,long remoteUserId){

        RSAKeyStore  rsaks = new RSAKeyStore(context);
        SQLiteDatabase db = rsaks.getReadableDatabase();

        String[] projection = {
                RSAKeyStore.RSAKeyEntry.COLUMN_NAME_LOCALID,
                RSAKeyStore.RSAKeyEntry.COLUMN_NAME_REMOTEID,
                RSAKeyStore.RSAKeyEntry.COLUMN_NAME_PRIV_KEY

        };

        // this is just simple
        // placeholder replacement
        // not switching cases or anything
        @SuppressLint("DefaultLocale")
        String selection = String.format("(%s = %d AND %s = %d)",
                AesKeyStore.AesKeyEntry.COLUMN_NAME_LOCALID,localUserId,
                AesKeyStore.AesKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId
        );




        Cursor c =db.query(RSAKeyStore.RSAKeyEntry.TABLE_NAME, // the table name
                projection,
                selection,
                null,
                null,
                null,
                null
        );

        c.moveToFirst();
        int keypos = c.getColumnIndexOrThrow(RSAKeyStore.RSAKeyEntry.COLUMN_NAME_PRIV_KEY);
        String key = c.getString(keypos);

        return key;

    }


    public static String[] getDHKey(Context context,long localUserId,long remoteUserId){

        DHKeyStore  dhks = new DHKeyStore(context);
        SQLiteDatabase db = dhks.getReadableDatabase();

        String[] projection = {
                DHKeyStore.DHKeyEntry.COLUMN_NAME_LOCALID,
                DHKeyStore.DHKeyEntry.COLUMN_NAME_REMOTEID,
                DHKeyStore.DHKeyEntry.COLUMN_NAME_PRIV_KEY,
                DHKeyStore.DHKeyEntry.COLUMN_NAME_PUB_KEY

        };

        // this is just simple
        // placeholder replacement
        // not switching cases or anything
        @SuppressLint("DefaultLocale")
        String selection = String.format("(%s = %d AND %s = %d)",
                DHKeyStore.DHKeyEntry.COLUMN_NAME_LOCALID,localUserId,
                DHKeyStore.DHKeyEntry.COLUMN_NAME_REMOTEID,remoteUserId
        );




        Cursor c =db.query(DHKeyStore.DHKeyEntry.TABLE_NAME, // the table name
                projection,
                selection,
                null,
                null,
                null,
                null
        );

        c.moveToFirst();
        int priv_keypos = c.getColumnIndexOrThrow(DHKeyStore.DHKeyEntry.COLUMN_NAME_PRIV_KEY);
        String priv_key = c.getString(priv_keypos);

        int pub_keypos = c.getColumnIndexOrThrow(DHKeyStore.DHKeyEntry.COLUMN_NAME_PUB_KEY);
        String pub_key = c.getString(pub_keypos);

        return new String[]{pub_key,priv_key};

    }





}
