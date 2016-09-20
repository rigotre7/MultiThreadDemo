package com.example.rigot.inclass04;
/*
    Name: Rodrigo Trejo
    Name: Anal Shah
    In Class 04
 */

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Message;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {
    ProgressDialog progDialog;
    ExecutorService threadPool;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final TextView passCount = (TextView) findViewById(R.id.passCountLabel);
        final TextView passLength = (TextView) findViewById(R.id.passLengthLabel);
        final SeekBar passCountSeekbar = (SeekBar) findViewById(R.id.passCountSeekbar);
        final SeekBar passLengthSeekbar = (SeekBar) findViewById(R.id.passLengthSeekbar);
        threadPool = Executors.newFixedThreadPool(2);



        passCountSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                passCount.setText("Select Passwords Count: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        passLengthSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                passLength.setText("Select Password Length: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.threadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(passCountSeekbar.getProgress() == 0){
                    Toast.makeText(MainActivity.this, "Minimum is 1", Toast.LENGTH_SHORT).show();
                }else if(passLengthSeekbar.getProgress() <8)
                    Toast.makeText(MainActivity.this, "Minimum is 8", Toast.LENGTH_SHORT).show();
                else{

                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("Generating Passwords ...");
                    progressDialog.setMax(passCountSeekbar.getProgress());
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();

                    final ArrayList<String> pwdArray = new ArrayList();
                    handler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            final TextView passLabel = (TextView) findViewById(R.id.passwordLabel);

                            pwdArray.add(msg.obj.toString());
                            progressDialog.setProgress(pwdArray.size());
                            if (pwdArray.size() == passCountSeekbar.getProgress()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Passwords").setItems(pwdArray.toArray(new CharSequence[pwdArray.size()]), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        passLabel.setText("Password: " + pwdArray.get(which).toString());
                                    }
                                });

                                AlertDialog alert = builder.create();
                                progressDialog.dismiss();
                                alert.show();
                            }
                            return false;
                        }
                    });
                    for (int i = 0; i < passLengthSeekbar.getProgress(); i++) {
                        threadPool.execute(new GetPwds(passLengthSeekbar.getProgress()));
                    }
                }
            }
        });



        findViewById(R.id.asyncButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passCountSeekbar.getProgress() == 0){
                    Toast.makeText(MainActivity.this, "Minimum is 1", Toast.LENGTH_SHORT).show();
                }else if(passLengthSeekbar.getProgress() <8)
                    Toast.makeText(MainActivity.this, "Minimum is 8", Toast.LENGTH_SHORT).show();
                else{
                    new generatePass().execute(passCountSeekbar.getProgress(), passLengthSeekbar.getProgress());
                }
            }
        });



    }

    class GetPwds implements Runnable {

        int length;

        public GetPwds(int length) {
            this.length = length;
        }

        @Override
        public void run() {
            String pwd = Util.getPassword(length);
            Message msg = new Message();
            msg.obj = pwd;
            handler.sendMessage(msg);
        }
    }

    private class generatePass extends AsyncTask<Integer, Integer, ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(Integer... values) {

            ArrayList<String> passStorage = new ArrayList<>();
            int prog = 100/values[0];
            for(int j = 0; j<values[0]; j++){
                passStorage.add(Util.getPassword(values[1]));
                publishProgress(prog);
            }

            return passStorage;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate(values);
            progDialog.incrementProgressBy(values[0]);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progDialog = new ProgressDialog(MainActivity.this);
            progDialog.setCancelable(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progDialog.setMax(100);
            progDialog.setMessage("Generating Passwords");
            progDialog.show();
        }

        @Override
        protected void onPostExecute(final ArrayList<String> results){
            //super.onPostExecute(results);
            progDialog.dismiss();
            final CharSequence[] items = new CharSequence[results.size()];
            for(int i =0; i<results.size(); i++){
                items[i] = results.get(i);
            }
            final TextView passLabel = (TextView) findViewById(R.id.passwordLabel);
            final AlertDialog.Builder passDisplay = new AlertDialog.Builder(MainActivity.this).setTitle("Passwords")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            passLabel.setText("Password: " + items[i]);
                        }
                    });

            AlertDialog alert = passDisplay.create();
            alert.show();

        }


    }
}
