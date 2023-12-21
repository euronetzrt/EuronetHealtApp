package hu.aut.android.dm01_v11.ui.fragments.questionnaire.MySeekBar;

/**
 * The type My seek params.
 */
public class MySeekParams {
    /**
     * Instantiates a new My seek params.
     *
     * @param seekBar the seek bar
     */
    MySeekParams(MyTickSeekBar seekBar) {
        this.seekBar = seekBar;
    }

    /**
     * The Seek bar.
     */
//for continuous series seek bar
    // The SeekBar whose progress has changed
    public MyTickSeekBar seekBar;
    /**
     * The Progress.
     */
//The current progress level.The default value for min is 0, max is 100.
    public int progress;
    /**
     * The Progress float.
     */
//The current progress level.The default value for min is 0.0, max is 100.0.
    public float progressFloat;
    /**
     * The From user.
     */
//True if the progress change was initiated by the user, otherwise by setProgress() programmatically.
    public boolean fromUser;

    /**
     * The Thumb position.
     */
//for discrete series seek bar
    //the thumb location on tick when the section changed, continuous series will be zero.
    public int thumbPosition;
    /**
     * The Tick text.
     */
//the text below tick&thumb when the section changed.
    public String tickText;
}
