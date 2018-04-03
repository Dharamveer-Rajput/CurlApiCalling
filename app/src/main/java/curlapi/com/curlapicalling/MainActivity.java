package curlapi.com.curlapicalling;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

import curlapi.com.curlapicalling.http.Api;
import curlapi.com.curlapicalling.http.AuthenticationParameters;
import curlapi.com.curlapicalling.util.IOUtil;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Api exampleApi;

    TextView mainTextView,txtResCode;

    ScrollView mainTextScroller;

   // @InjectResource(R.string.server_cert_asset_name) .pem file
    String caCertificateName;

    //@InjectResource(R.string.client_cert_file_name .p12 file)
    String clientCertificateName;

   // @InjectResource(R.string.client_cert_password)
    String clientCertificatePassword;

   // @InjectResource(R.string.example_url)
    String exampleUrl;
    private static final int PERMISSION_REQUEST_CODE = 1;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainTextView = findViewById(R.id.mainTextView);
        txtResCode = findViewById(R.id.txtResCode);
        mainTextScroller = findViewById(R.id.mainTextScroller);

        caCertificateName = getResources().getString(R.string.server_cert_asset_name);
        clientCertificateName = getResources().getString(R.string.client_cert_file_name);;
        clientCertificatePassword = getResources().getString(R.string.client_cert_password);
        exampleUrl = getResources().getString(R.string.example_url);



    }

    @Override
    protected void onResume() {
        super.onResume();


        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
                doRequest();


            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {

            // Code for Below 23 API Oriented Device
            // Do next code
            doRequest();


        }


    }

    private void updateOutput(String text) {


        mainTextView.setText(text);

    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }



    private void doRequest() {

        try {
            AuthenticationParameters authParams = new AuthenticationParameters();
            authParams.setClientCertificate(getClientCertFile());
            authParams.setClientCertificatePassword(clientCertificatePassword);
            authParams.setCaCertificate(readCaCert());

            exampleApi = new Api(authParams);
            updateOutput("Connecting to " + exampleUrl);

            new AsyncTask() {
                @Override
                protected Object doInBackground(Object... objects) {

                    try {
                        String result = exampleApi.doGet(exampleUrl);
                        int responseCode = exampleApi.getLastResponseCode();

                        txtResCode.setText(String.valueOf(responseCode));

                        if (responseCode == 200) {
                            publishProgress(result);
                        } else {
                            publishProgress("HTTP Response Code: " + responseCode);
                        }

                    } catch (Throwable ex) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintWriter writer = new PrintWriter(baos);
                        ex.printStackTrace(writer);
                        writer.flush();
                        writer.close();
                        publishProgress(ex.toString() + " : " + baos.toString());
                    }

                    return null;
                }

                @Override
                protected void onProgressUpdate(final Object... values) {
                    StringBuilder buf = new StringBuilder();
                    for (final Object value : values) {
                        buf.append(value.toString());
                    }
                    updateOutput(buf.toString());
                }

                @Override
                protected void onPostExecute(final Object result) {
                    updateOutput("Done!");
                }
            }.execute();

        } catch (Exception ex) {
            Log.e(TAG, "failed to create timeApi", ex);
            updateOutput(ex.toString());
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }





    private File getClientCertFile() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        return new File(externalStorageDir, clientCertificateName);
    }

    private String readCaCert() throws Exception {
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open(caCertificateName);
        return IOUtil.readFully(inputStream);
    }
}