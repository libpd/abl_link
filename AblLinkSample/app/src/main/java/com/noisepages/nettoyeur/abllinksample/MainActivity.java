/*
 *  For information on usage and redistribution, and for a DISCLAIMER OF ALL
 *  WARRANTIES, see the file, LICENSE, in the root of this repository.
 *
 */

package com.noisepages.nettoyeur.abllinksample;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;
import org.puredata.core.utils.PdDispatcher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AblLinkSample";
    static final int PD_LATENCY_MS = 7; // Latency values shouldn't go below this

    private TextView tempoLabel = null;
    private SeekBar tempoBar = null;
    private TextView phase = null;
    private TextView beatTime = null;
    private PdService pdService = null;
    private Toast toast = null;

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.setText(TAG + ": " + msg);
                toast.show();
            }
        });
    }

    private PdDispatcher dispatcher = new PdDispatcher() {
        @Override
        public void print(String s) {
            toast(s);
        }
    };

    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pdService = ((PdService.PdBinder) service).getService();
            initPd();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // this method will never be called
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        AudioParameters.init(this);
        PdPreferences.initPreferences(getApplicationContext());
        initGui();
        bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    private void initGui() {
        setContentView(R.layout.activity_main);
        final CheckBox connectBox = (CheckBox) findViewById(R.id.connectBox);
        connectBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PdBase.sendFloat("connect", connectBox.isChecked() ? 1 : 0);
            }
        });
        tempoLabel = (TextView) findViewById(R.id.tempoLabel);
        tempoBar = (SeekBar) findViewById(R.id.tempoBar);
        tempoBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    PdBase.sendFloat("tempo", (float) progressToTempo(value));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        final TextView quantumLabel = (TextView) findViewById(R.id.quantumLabel);
        final SeekBar quantumBar = (SeekBar) findViewById(R.id.quantumBar);
        quantumBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                int q = value + 1;
                PdBase.sendFloat("quantum", q);
                quantumLabel.setText("Quantum: " + q);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        final TextView latencyLabel = (TextView) findViewById(R.id.offsetLabel);
        final SeekBar latencyBar = (SeekBar) findViewById(R.id.offsetBar);
        latencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    int toSet = PD_LATENCY_MS + value;
                    PdBase.sendFloat("offset", (float) toSet);
                    latencyLabel.setText("Offset: " + toSet + " ms");
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        latencyBar.setProgress(23 - PD_LATENCY_MS);
        phase = (TextView) findViewById(R.id.phase);
        beatTime = (TextView) findViewById(R.id.beatTime);
    }

    private void initPd() {
        Resources res = getResources();
        File patchFile = null;
        try {
            PdBase.setReceiver(dispatcher);
            PdBase.subscribe("android");
            InputStream in = res.openRawResource(R.raw.metronome);
            patchFile = IoUtils.extractResource(in, "metronome.pd", getCacheDir());
            PdBase.openPatch(patchFile);
            dispatcher.addListener("tempoOut", new PdListener.Adapter() {
                @Override public void receiveFloat(final String source, final float x) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tempoLabel.setText("Tempo: " + (int) (x + 0.5) + "bpm");
                            tempoBar.setProgress(tempoToProgress(x));
                        }
                    });
                }
            });
            dispatcher.addListener("phase", new PdListener.Adapter() {
                @Override public void receiveFloat(final String source, final float x) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            phase.setText(String.format(Locale.US, "%,.1f", x));
                        }
                    });
                }
            });
            dispatcher.addListener("beatTime", new PdListener.Adapter() {
                @Override public void receiveFloat(final String source, final float x) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            beatTime.setText(String.format(Locale.US, "%,.1f", x));
                        }
                    });
                }
            });
            startAudio();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            finish();
        } finally {
            if (patchFile != null) patchFile.delete();
        }
    }

    private void startAudio() {
        String name = getResources().getString(R.string.app_name);
        try {
            pdService.initAudio(-1, 0, 2, -1);   // negative values will be replaced with defaults/preferences
            pdService.startAudio(new Intent(this, MainActivity.class), R.drawable.icon, name, "Return to " + name + ".");
        } catch (IOException e) {
            toast(e.toString());
        }
    }

    private void stopAudio() {
        pdService.stopAudio();
    }

    private void cleanup() {
        try {
            stopAudio();
            unbindService(pdConnection);
        } catch (IllegalArgumentException e) {
            // already unbound
            pdService = null;
        }
    }

    private static final double ctemp = Math.sqrt(20.0 * 999.0);
    private static final double qtemp = ctemp / 20.0;

    private double progressToTempo(int progress) {
        return ctemp * Math.pow(qtemp, (progress - 50.0) / 50.0);
    }

    private int tempoToProgress(float bpm) {
        return (int) (50.0 + 50 * Math.log(bpm / ctemp) / Math.log(qtemp));
    }
}