package com.example.lab_6;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        pb = findViewById(R.id.progressBar);

        CatImages catImages = new CatImages();
        catImages.execute();
    }

    public class CatImages extends AsyncTask<Void, Bitmap, Void> {
        private String id;

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {
                    String baseUrl = "https://cataas.com/cat?json=true";
                    JSONObject jsonObject = getJsonObject(baseUrl);
                    id = jsonObject.getString("id");
                    String url = jsonObject.getString("url");

                    File file = new File(getFilesDir(), id + ".jpg");
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        publishProgress(bitmap);
                    } else {
                        Bitmap bitmap = downloadImage(url);
                        publishProgress(bitmap);
                        saveImage(bitmap);
                    }

                    Thread.sleep(5000); // wait 5 seconds before getting the next cat picture

                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            Bitmap bitmap = values[0];
            imageView.setImageBitmap(bitmap);
            pb.setVisibility(ProgressBar.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(ProgressBar.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        private JSONObject getJsonObject(String urlString) throws IOException, JSONException {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();
                byte[] responseBytes = in.readAllBytes();
                String response = new String(responseBytes);
                return new JSONObject(response);
            } finally {
                urlConnection.disconnect();
            }
        }

        private Bitmap downloadImage(String imageUrl) throws IOException {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }

        private void saveImage(Bitmap bitmap) {
            try {
                File file = new File(getFilesDir(), id + ".jpg");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
