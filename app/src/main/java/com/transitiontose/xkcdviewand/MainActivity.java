package com.transitiontose.xkcdviewand;

import android.Manifest;
import android.app.*;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.*;
import java.io.*;
import java.net.*;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.content.*;
import android.view.*;
import android.net.*;
import org.json.*;

import java.util.*;
import android.util.Log;
import android.graphics.*;
import android.provider.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.BitmapDrawable;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity {

    private RelativeLayout relativeLayout;
    private TextView numberTextView;
    private TextView dateTextView;
    private TextView titleTextView;
    private ImageView comicImageView;
    private EditText comicNumTaker;
    private int maximumComicNumber = 0;
    private int counter = 0;
    private String URLtoRequestDataFrom = "https://xkcd.com/info.0.json";
    private JSONObject json = new JSONObject();
    private Boolean isFirstQuery = true;
    private MediaPlayer player;
    private boolean shouldPlaySound = true;

//    Pentatonicity!
    private final double toneA = 440.;
    private final double toneB = 495.;
    private final double toneCSharp = 556.875;
    private final double toneE = 660.;
    private final double toneFSharp = 742.5;
    private final double toneAyy = 880.;
    private final int duration = 44100 * 3;

    //    Oh baby
    private static final String SOUND_CALLBACKS_KEY = "soundCallbacks";
    private ImageView audioButtonView;
    private ImageView audioButtonViewMuted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        yolo
        audioButtonView = (ImageView) findViewById(R.id.audio_image);
        audioButtonViewMuted = (ImageView) findViewById(R.id.audio_image_muted);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SOUND_CALLBACKS_KEY)) {
                boolean previousSoundCallbacks = savedInstanceState.getBoolean(SOUND_CALLBACKS_KEY);
                shouldPlaySound = previousSoundCallbacks;
            }
            if (shouldPlaySound == true) {
                muteAudioImage();
            } else {
                unMuteAudioImage();
            }

        }

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard();
                }
            }
        });

        comicImageView = (ImageView) findViewById(R.id.comicImageView);
        comicNumTaker = (EditText) findViewById(R.id.comicNumTaker);
        setEditTextOptions();
        numberTextView = (TextView) findViewById(R.id.numberTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        player = new MediaPlayer();


        String initialURL = "https://xkcd.com/info.0.json";
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected() && savedInstanceState != null) {
            maximumComicNumber = savedInstanceState.getInt("oldMaximumComicNumber");
            counter = savedInstanceState.getInt("oldCounter");
            URLtoRequestDataFrom = savedInstanceState.getString("oldURLtoRequestDataFrom");
            isFirstQuery = savedInstanceState.getBoolean("oldisFirstQuery");
            new DownloadWebpageTask().execute(URLtoRequestDataFrom);
        } else if (networkInfo != null && networkInfo.isConnected() && savedInstanceState == null) {
            new DownloadWebpageTask().execute(initialURL);
        } else if (networkInfo == null) {
            System.exit(0);
        }
    }

    private void setEditTextOptions() {
        comicNumTaker.setImeOptions(EditorInfo.IME_ACTION_DONE); // collapse keyboard when done is pressed

        comicNumTaker.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE){
                    comicNumTaker.clearFocus();
                    relativeLayout.requestFocus();
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("oldMaximumComicNumber", maximumComicNumber);
        outState.putInt("oldCounter", counter);
        outState.putString("oldURLtoRequestDataFrom", URLtoRequestDataFrom);
        outState.putBoolean("oldisFirstQuery", isFirstQuery);
        outState.putBoolean(SOUND_CALLBACKS_KEY, shouldPlaySound);
    }

    public void audioPressed(View v) {
        shouldPlaySound = !shouldPlaySound;

//        Logic is as logic does
        if (shouldPlaySound == true) {
            Toast.makeText(this, "Audio enabled!", Toast.LENGTH_SHORT).show();
            muteAudioImage();
        }
        if (shouldPlaySound == false) {
            Toast.makeText(this, "Audio disabled!", Toast.LENGTH_SHORT).show();
            unMuteAudioImage();
        }
    }

    // for shooooooo
    public void muteAudioImage() {
        audioButtonViewMuted.setVisibility(View.INVISIBLE);
        audioButtonView.setVisibility(View.VISIBLE);
    }
    public void unMuteAudioImage() {
        audioButtonView.setVisibility(View.INVISIBLE);
        audioButtonViewMuted.setVisibility(View.VISIBLE);
    }


    public void randomComic(View v) {
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            if (shouldPlaySound) {
                playSound(toneA, duration);
            }
            counter = randomInteger(1, maximumComicNumber);
            URLtoRequestDataFrom = "https://xkcd.com/" + counter + "/info.0.json";
            getData();
        } else {
           networkToast();
        }
    }

    private static int randomInteger(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    private NetworkInfo getNetworkInfo() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }


    public void doubleLeftPressed(View v) {

        if (shouldPlaySound) {
            playSound(toneB, duration);
        }

        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            counter = 1;
            URLtoRequestDataFrom = "https://xkcd.com/" + counter + "/info.0.json";
            getData();
        } else {
            networkToast();
        }
    }

    public void leftPressed(View v) {

        if (shouldPlaySound) {
            playSound(toneCSharp, duration);
        }

        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if(counter >= 2 && counter <= maximumComicNumber) {
                counter--;
                URLtoRequestDataFrom = "https://xkcd.com/" + counter + "/info.0.json";
                getData();
            } else {
                invalidToast();
            }
        } else {
            networkToast();
        }
    }

    public void rightPressed(View v) {

        if (shouldPlaySound) {
            playSound(toneE, duration);
        }

        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if(counter >= 1 && counter <= maximumComicNumber - 1) {
                counter++;
                URLtoRequestDataFrom = "https://xkcd.com/" + counter + "/info.0.json";
                getData();
            } else {
                invalidToast();
            }
        } else {
            networkToast();
        }
    }

    public void doubleRightPressed(View v) {

        if (shouldPlaySound) {
            playSound(toneFSharp, duration);
        }

        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            counter = maximumComicNumber;
            URLtoRequestDataFrom = "https://xkcd.com/" + counter + "/info.0.json";
            getData();
        } else {
            networkToast();
        }
    }


    public void savePressed(View v) {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Bitmap bitmap = ((BitmapDrawable)comicImageView.getDrawable()).getBitmap();
            saveImage(bitmap);
            Toast.makeText(this, "Image saved.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 13);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 13: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Bitmap bitmap = ((BitmapDrawable)comicImageView.getDrawable()).getBitmap();
                    saveImage(bitmap);
                    Toast.makeText(this, "Image saved.", Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo!
                    Toast.makeText(this, "This permission is required in order to save image to your device.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSpecificComic(View v) {

        if (shouldPlaySound) {
            playSound(toneAyy, duration);
        }

        int comicToGet;
        try {
            comicToGet = Integer.parseInt(comicNumTaker.getText().toString());
        } catch (IllegalArgumentException e) {
            relativeLayout.requestFocus();
            invalidToast();
            return;
        }

        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (comicToGet >= 1 && comicToGet <= maximumComicNumber) {
                counter = comicToGet;
                URLtoRequestDataFrom = "https://xkcd.com/" + counter + "/info.0.json"; // update the URL
                getData();
            } else {
                invalidToast();
            }
        } else {
            networkToast();
        }

        relativeLayout.requestFocus();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void getData() {
        new DownloadWebpageTask().execute(URLtoRequestDataFrom);
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            //int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string

            String contentAsString = convertStreamToString(is);

            try {
                json = new JSONObject(contentAsString);
            } catch (org.json.JSONException j) {
                Log.d("downloadUrl", "JSONObject creation failed.");
                j.printStackTrace();
            }

            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private void getComicImage(JSONObject jsonArg) {
        String imageURLtoFetch = "";
        try {
            imageURLtoFetch = jsonArg.getString("img");
        } catch (JSONException j) {
            j.printStackTrace();
        }

        new DownloadImageTask((ImageView) findViewById(R.id.comicImageView)).execute(imageURLtoFetch);
    }

    private void getComicDate(JSONObject jsonArg) {
        String day =""; String month = ""; String year ="";
        try {
            day = jsonArg.getString("day");
            month = jsonArg.getString("month");
            year = jsonArg.getString("year");
        } catch (org.json.JSONException j) {
            j.printStackTrace();
        }

        dateTextView.setText(month + "/" + day + "/" + year);
    }

    private void getComicTitle(JSONObject jsonArg) {
        String title = "";

        try {
            title = jsonArg.getString("title");
        } catch (org.json.JSONException j) {
            Log.d("getComicTitle", "Can't parse title.");
        }

        titleTextView.setText(title);
    }

    private void getComicNumber(JSONObject jsonArg) {
        int num = -1;

        try {
            num = jsonArg.getInt("num");
        } catch (org.json.JSONException j) {
            Log.d("getComicNumber", "Can't parse number.");
        }

        numberTextView.setText("comic #: " + num);
    }

    private void firstQueryWork(JSONObject jsonArg) {
        int max = -1;

        try {
            max = jsonArg.getInt("num");
        } catch (org.json.JSONException j) {
            Log.d("firstQueryWork", "Can't parse number.");
        }

        maximumComicNumber = max;
        counter = maximumComicNumber;
    }

    private void invalidToast() {
        Toast.makeText(this, "Invalid comic number.", Toast.LENGTH_SHORT).show();
    }

    private void networkToast() {
        Toast.makeText(this, "Network unavailable.", Toast.LENGTH_SHORT).show();
    }

    private void playSound(double frequency, int duration) {
        // AudioTrack definition
        int mBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT);

        AudioTrack mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize, AudioTrack.MODE_STREAM);

        // Sine wave
        double[] mSound = new double[4410];
        short[] mBuffer = new short[duration];
        for (int i = 0; i < mSound.length; i++) {
            mSound[i] = Math.sin((2.0*Math.PI * i/(44100/frequency)));
            mBuffer[i] = (short) (mSound[i]*Short.MAX_VALUE);
        }

        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
        mAudioTrack.play();

        mAudioTrack.write(mBuffer, 0, mSound.length);
        mAudioTrack.stop();
        mAudioTrack.release();

    }



    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //textView.setText(result);
            if (isFirstQuery) {
                firstQueryWork(json);
                isFirstQuery = false;
            }

            getComicNumber(json);
            getComicTitle(json);
            getComicImage(json);
            getComicDate(json);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        protected DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            //bmImage.setImageBitmap(result);
            Context c = getApplicationContext();
            ImageViewAnimatedChange(c, bmImage, result);
        }
    }

    public static void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation fadeFirstImageOut = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation fadeSecondImageIn = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        fadeFirstImageOut.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                fadeSecondImageIn.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(fadeSecondImageIn);
            }
        });
        v.startAnimation(fadeFirstImageOut);
    }
}