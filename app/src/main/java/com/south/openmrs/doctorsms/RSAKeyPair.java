package com.south.openmrs.doctorsms;


import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Created by angel on 5/21/16.
 */
public class RSAKeyPair {

    String mAlgorithm = "SHA256withRSA";


    void foo(){

        KeyPairGenerator keyGen = null;
        try{
            keyGen=  KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e){

        }
        keyGen.initialize(1024);
        KeyPair key = keyGen.generateKeyPair();
        PrivateKey priv = key.getPrivate();
        PublicKey pub = key.getPublic();

        String privateKey = new String(Base64.encode(priv.getEncoded(), 0,priv.getEncoded().length, Base64.NO_WRAP));
        String publicKey1 = new String(Base64.encode(pub.getEncoded(), 0,pub.getEncoded().length, Base64.NO_WRAP));
        String publicKey = new String(Base64.encode(publicKey1.getBytes(),0, publicKey1.getBytes().length, Base64.NO_WRAP));
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




}
