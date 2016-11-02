package com.peerstars.android.awscognito;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.peerstars.android.pststorage.PSTStorageHandler;

import java.io.File;

/**
 * Created by bmiller on 9/11/2015.
 */
public class AWSTransferObserverFactory {

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client = null;
    private static CognitoCachingCredentialsProvider credentialsProvider = null;
    private static TransferUtility sTransferUtility = null;

    // Cognito identity pool id
    protected static String cognitoIdentityPoolId = "us-east-1:f7aa75ea-335b-46dd-aac1-9097a8ffc064";
    protected static Regions region = Regions.US_EAST_1;

    protected static CognitoCachingCredentialsProvider getCognitoCashingCredentialsProvider(Context context) {
        if (credentialsProvider == null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    cognitoIdentityPoolId,
                    region
            );
        }

        return credentialsProvider;
    }

    public static AmazonS3Client getsS3Client() {
        if (sS3Client == null) {
            // create an Amazon client
            sS3Client = new AmazonS3Client(getCognitoCashingCredentialsProvider(PSTStorageHandler.getCurrentContext()));
        }
        return sS3Client;
    }

    public static TransferUtility getsTransferUtility() {
        if (sS3Client == null) {
            // create an Amazon client
            sS3Client = new AmazonS3Client(getCognitoCashingCredentialsProvider(PSTStorageHandler.getCurrentContext()));
        }
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(sS3Client, PSTStorageHandler.getCurrentContext());
        }
        return sTransferUtility;
    }

    public static TransferObserver getUploadObserver(String bucket, String key, File file) {

        // make sure the S3Client and the TransferUtility are initialized
        getsTransferUtility();

        // instantiate the observers
        TransferObserver uploadObserver = sTransferUtility.upload(
                bucket,     /* The bucket to upload to */
                key,    /* The key for the uploaded object */
                file        /* The file where the data to upload exists */
        );

        return uploadObserver;
    }

    public static TransferObserver getDownloadObserver(String bucket, String key, File file) {

        // make sure the S3Client and the TransferUtility are initialized
        getsTransferUtility();

        // instantiate the observers
        TransferObserver downloadObserver = sTransferUtility.download(
                bucket,     /* The bucket to upload to */
                key,    /* The key for the uploaded object */
                file        /* The file where the data to upload exists */
        );

        return downloadObserver;
    }

    public static void cancelAllTransfers(Context context) {

        // make sure the S3Client and the TransferUtility are initialized
        getsTransferUtility();

        sTransferUtility.cancelAllWithType(TransferType.ANY);

    }

    public static void cancelTransfer(int id, Context context) {

        // make sure the S3Client and the TransferUtility are initialized
        getsTransferUtility();

        sTransferUtility.cancel(id);

    }
}
