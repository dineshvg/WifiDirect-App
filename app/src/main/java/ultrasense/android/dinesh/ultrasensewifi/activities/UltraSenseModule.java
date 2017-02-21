package ultrasense.android.dinesh.ultrasensewifi.activities;

import android.app.Activity;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

import ultrasense.android.dinesh.ultrasensewifi.audio.AudioManager;
import ultrasense.android.dinesh.ultrasensewifi.audio.CWSignalGenerator;
import ultrasense.android.dinesh.ultrasensewifi.audio.SignalGenerator;

/**
 * Factory class for creating different UltraSense scenarios
 *
 * <br><br>
 * Created by Jakob on 10.08.2015.
 */
public class UltraSenseModule {

    public static final double SAMPLE_RATE = 44100.0;
    private static final int fftLength = 4096;
    private static final int hopSize = 2048;
    private static double frequency = 20000;



    private AudioManager audioManager;
/*    private GestureFP gestureFP;
    private ActivityFP activityFP;
    private FeatureDetector featureDetector;*/
    private Activity activity;
    private boolean initialized;


    /**
     * creates a new UltraSenseModule, initializing the AudioManager
     * @param activity the Activity to associate this module with
     */
    public UltraSenseModule(Activity activity){
        this.activity = activity;
        this.audioManager = new AudioManager(activity);
        this.initialized = false;
    }

    /**
     * creates a custom UltraSense scenario given the settings provided by the user.<br>
     * Use when experimenting with CW/FMCW in conjunction with the Recording function
     *
     * //@param settingsParameters the settings defined by the user (settings tab)
     * //@param gestureCallback the GestureCallback to use if specified (pass null if no GE was selected)
     * //@param inferredContextCallback the InferredContextCallback to use if specified (pass null if no AE was selected)
     * @throws IllegalArgumentException
     */
    public void createCustomScenario() throws IllegalArgumentException {
        resetState();
        SignalGenerator signalGen;
        signalGen =  new CWSignalGenerator(frequency, SAMPLE_RATE);
        audioManager.setSignalGenerator(signalGen);
        this.initialized = true;
    }

    /**
     * grants access to the AudioManager<br>
     * E.g. to access record data before saving it to a file
     *
     * @return the AudioManager associated with the current scenario
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * starts a previously created scenario.<br>
     * DOES record to a file! Can be used for later analysis.
     *
     * @throws IllegalStateException if no scenario was created
     */
    public void startRecord() {
        if(!initialized || audioManager == null)
            throw new IllegalStateException("You must call a create method before starting any detection!");

        try {
            audioManager.startRecord();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * stops a previously started scenario. Subsequent calls will do nothing.<br>
     * Use in conjunction with startDetection()
     *
     * @throws IllegalStateException if no scenario was created
     */
    public void stopRecord() {
        if(!initialized || audioManager == null)
            throw new IllegalStateException("You must call a create method before stoping any detection!");

        try {
            audioManager.stopRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

     /**
     * Saves previously recorded files (sent and received data)
     * @param fileName
     * @throws IOException
     */
    public void saveRecordedFiles(String fileName) throws IOException {
        if(!initialized || audioManager == null)
            throw new IllegalStateException("You must call a create method before calling start/stop or save!");

        if(!audioManager.hasRecordData())
            throw new IllegalStateException("You must record something before saving it");

        try {
            audioManager.saveWaveFiles(fileName);
        }catch(OutOfMemoryError e){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Record too large to be saved!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void resetState(){
        /*activityFP = null;
        gestureFP = null;*/
    }
}
