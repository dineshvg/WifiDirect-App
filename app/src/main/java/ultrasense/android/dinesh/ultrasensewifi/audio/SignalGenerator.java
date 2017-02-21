package ultrasense.android.dinesh.ultrasensewifi.audio;

/**
 * Interface for all Signal generators used in UltraSense
 *
 * Created by Jakob on 27.05.2015.
 */
public interface SignalGenerator {

    /**
     * generates an dinesh.fraunhofer.emk.de.ultrasenseii.audio signal conforming to app mode and parameters
     *
     * @return dinesh.fraunhofer.emk.de.ultrasenseii.audio signal as byte[]
     */
    public byte[] generateAudio();

    /**
     *
     *
     * @return the carrier frequency for the generated signal
     */
    public double getCarrierFrequency();
}
