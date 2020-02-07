package jp.yaplus.dispenser;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String getUrlString = "http://192.168.0.22/~pi/dispenser.php";
    private static final int POURING_TIME_NORMAL = 7;
    private static final int POURING_TIME_LARGE  = 10;
    private static final int FLAVOR_1_READ_REQUEST_CODE = 12;
    private static final int FLAVOR_2_READ_REQUEST_CODE = 7;
    private static final int FLAVOR_3_READ_REQUEST_CODE = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mysteryEditText).setFocusable(false);

        findViewById(R.id.flavor1Button).setOnClickListener(this);
        findViewById(R.id.flavor2Button).setOnClickListener(this);
        findViewById(R.id.flavor3Button).setOnClickListener(this);

        findViewById(R.id.flavor1ImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, FLAVOR_1_READ_REQUEST_CODE);
            }
        });

        findViewById(R.id.flavor2ImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, FLAVOR_2_READ_REQUEST_CODE);
            }
        });

        findViewById(R.id.flavor3ImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, FLAVOR_3_READ_REQUEST_CODE);
            }
        });

        findViewById(R.id.emergencyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pourDrink(0, 0);
                pourDrink(1, 0);
                pourDrink(2, 0);
            }
        });

        findViewById(R.id.mysteryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flavor = (new Random()).nextInt(3);
                int time = ((Switch) findViewById(R.id.largeSwitch)).isChecked() ? POURING_TIME_LARGE : POURING_TIME_NORMAL;
                pourDrink(flavor, time);
                waitPouring(time);
            }
        });

        findViewById(R.id.mix12Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time = ((Switch) findViewById(R.id.largeSwitch)).isChecked() ? POURING_TIME_LARGE : POURING_TIME_NORMAL;
                pourDrink(0, time);
                pourDrink(1, time);
                waitPouring(time);
            }
        });

        findViewById(R.id.mix23Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time = ((Switch) findViewById(R.id.largeSwitch)).isChecked() ? POURING_TIME_LARGE : POURING_TIME_NORMAL;
                pourDrink(1, time);
                pourDrink(2, time);
                waitPouring(time);
            }
        });

        findViewById(R.id.mix31Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time = ((Switch) findViewById(R.id.largeSwitch)).isChecked() ? POURING_TIME_LARGE : POURING_TIME_NORMAL;
                pourDrink(2, time);
                pourDrink(0, time);
                waitPouring(time);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (   (requestCode == FLAVOR_1_READ_REQUEST_CODE ||
                requestCode == FLAVOR_2_READ_REQUEST_CODE ||
                requestCode == FLAVOR_3_READ_REQUEST_CODE )
             && resultCode == RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                try {
                    Bitmap bmp = getBitmapFromUri(uri);
                    switch (requestCode) {
                        case FLAVOR_1_READ_REQUEST_CODE:
                            ((ImageView)findViewById(R.id.flavor1ImageView)).setImageBitmap(bmp);
                            break;
                        case FLAVOR_2_READ_REQUEST_CODE:
                            ((ImageView)findViewById(R.id.flavor2ImageView)).setImageBitmap(bmp);
                            break;
                        case FLAVOR_3_READ_REQUEST_CODE:
                            ((ImageView)findViewById(R.id.flavor3ImageView)).setImageBitmap(bmp);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =  getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    public void onClick(View v) {
        int flavor;
        switch (v.getId()) {
            case R.id.flavor1Button:
                flavor = 0;
                break;
            case R.id.flavor2Button:
                flavor = 1;
                break;
            case R.id.flavor3Button:
                flavor = 2;
                break;
            default:
                return; // それ以外はリターン
        }

        int time = ((Switch) findViewById(R.id.largeSwitch)).isChecked() ? POURING_TIME_LARGE : POURING_TIME_NORMAL;
        // 注ぐ
        pourDrink(flavor, time);
        waitPouring(time);
    }

    private void pourDrink(int flavor, int time) {
        final String query = "?flavor=" + flavor + "&time=" + time;
        // 注ぐ
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(getUrlString + query);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    connection.getInputStream();
                    Log.d("HTTP", getUrlString + query);
                } catch(Exception ex) {
                    System.out.println(ex);
                }
            }
        }).start();
    }

    private void waitPouring(int time) {
        // 注いでいる間はダイアログを表示
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pour_dialog_time);
        builder.setMessage(R.string.pour_dialog_wait);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        Handler handler = new Handler();
        Runnable dialogDismiss = new Runnable() {
            public void run() {
                dialog.dismiss();
            }
        };
        dialog.show();
        handler.postDelayed(dialogDismiss, (time+2)*1000);
    }
}


