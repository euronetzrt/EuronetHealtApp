package hu.aut.android.dm01_v11.ui.fragments.questionnaire.MySeekBar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.warkiz.tickseekbar.SizeUtils;
import com.warkiz.tickseekbar.TextPosition;
import com.warkiz.tickseekbar.TickMarkType;

/**
 * The type My builder.
 */
public class MyBuilder {
    /**
     * The Context.
     */
    final Context context;
    /**
     * The Max.
     */
//seek bar
    float max = 100;
    /**
     * The Min.
     */
    float min = 0;
    /**
     * The Progress.
     */
    float progress = 0;
    /**
     * The Progress value float.
     */
    boolean progressValueFloat = false;
    /**
     * The Seek smoothly.
     */
    boolean seekSmoothly = false;
    /**
     * The R 2 l.
     */
    boolean r2l = false;
    /**
     * The User seekable.
     */
    boolean userSeekable = true;
    /**
     * The Only thumb draggable.
     */
    boolean onlyThumbDraggable = false;
    /**
     * The Track background size.
     */
//track
    int trackBackgroundSize;
    /**
     * The Track background color.
     */
    int trackBackgroundColor = Color.parseColor("#D7D7D7");
    /**
     * The Track progress size.
     */
    int trackProgressSize;
    /**
     * The Track progress color.
     */
    int trackProgressColor = Color.parseColor("#FF4081");
    /**
     * The Track rounded corners.
     */
    boolean trackRoundedCorners = false;
    /**
     * The Thumb text color.
     */
//thumbText
    int thumbTextColor = Color.parseColor("#FF4081");
    /**
     * The Thumb text show.
     */
    int thumbTextShow = TextPosition.NONE;
    /**
     * The Thumb size.
     */
//thumb
    int thumbSize;
    /**
     * The Thumb color.
     */
    int thumbColor = Color.parseColor("#FF4081");
    /**
     * The Thumb auto adjust.
     */
    boolean thumbAutoAdjust = true;
    /**
     * The Thumb color state list.
     */
    ColorStateList thumbColorStateList = null;
    /**
     * The Thumb drawable.
     */
    Drawable thumbDrawable = null;
    /**
     * The Tick texts show.
     */
//tickTexts
    int tickTextsShow = TextPosition.NONE;
    /**
     * The Tick texts color.
     */
    int tickTextsColor = Color.parseColor("#FF4081");
    /**
     * The Tick texts size.
     */
    int tickTextsSize;
    /**
     * The Tick texts custom array.
     */
    String[] tickTextsCustomArray = null;
    /**
     * The Tick texts type face.
     */
    Typeface tickTextsTypeFace = Typeface.DEFAULT;
    /**
     * The Tick texts color state list.
     */
    ColorStateList tickTextsColorStateList = null;
    /**
     * The Tick count.
     */
//tickMarks
    int tickCount = 0;
    /**
     * The Show tick marks type.
     */
    int showTickMarksType = TickMarkType.NONE;
    /**
     * The Tick marks color.
     */
    int tickMarksColor = Color.parseColor("#FF4081");
    /**
     * The Tick marks size.
     */
    int tickMarksSize;
    /**
     * The Tick marks drawable.
     */
    Drawable tickMarksDrawable = null;
    /**
     * The Tick marks ends hide.
     */
    boolean tickMarksEndsHide = false;
    /**
     * The Tick marks swept hide.
     */
    boolean tickMarksSweptHide = false;
    /**
     * The Tick marks color state list.
     */
    ColorStateList tickMarksColorStateList = null;
    /**
     * The Clear padding.
     */
    public boolean clearPadding = false;

    /**
     * Instantiates a new My builder.
     *
     * @param context the context
     */
    MyBuilder(Context context) {
        this.context = context;
        this.trackBackgroundSize = SizeUtils.dp2px(context, 2);
        this.trackProgressSize = SizeUtils.dp2px(context, 2);
        this.tickMarksSize = SizeUtils.dp2px(context, 10);
        this.tickTextsSize = SizeUtils.sp2px(context, 13);
        this.thumbSize = SizeUtils.dp2px(context, 14);
    }

    /**
     * call this to new an MyTickSeekBar
     *
     * @return MyTickSeekBar my tick seek bar
     */
    public MyTickSeekBar build() {
        return new MyTickSeekBar(this);
    }

    /**
     * Set the upper limit of this seek bar's range.
     *
     * @param max the max
     * @return the my builder
     */
    public MyBuilder max(float max) {
        this.max = max;
        return this;
    }

    /**
     * Set the  lower limit of this seek bar's range.
     *
     * @param min the min
     * @return the my builder
     */
    public MyBuilder min(float min) {
        this.min = min;
        return this;
    }

    /**
     * Sets the current progress to the specified value.
     *
     * @param progress the progress
     * @return the my builder
     */
    public MyBuilder progress(float progress) {
        this.progress = progress;
        return this;
    }

    /**
     * make the progress in float type. default in int type.
     *
     * @param isFloatProgress true for float progress,default false.
     * @return the my builder
     */
    public MyBuilder progressValueFloat(boolean isFloatProgress) {
        this.progressValueFloat = isFloatProgress;
        return this;
    }

    /**
     * seek continuously or discrete
     *
     * @param seekSmoothly true for seek continuously ignore having tickMarks.
     * @return the my builder
     */
    public MyBuilder seekSmoothly(boolean seekSmoothly) {
        this.seekSmoothly = seekSmoothly;
        return this;
    }

    /**
     * right to left,compat local problem.
     *
     * @param r2l true for local which read text from right to left
     * @return the my builder
     */
    public MyBuilder r2l(boolean r2l) {
        this.r2l = r2l;
        return this;
    }

    /**
     * seek bar has a default padding left and right(16 dp) , call this method to set both to zero.
     *
     * @param clearPadding true to clear the default padding, false to keep.
     * @return MyBuilder my builder
     */
    public MyBuilder clearPadding(boolean clearPadding) {
        this.clearPadding = clearPadding;
        return this;
    }

    /**
     * prevent user from touching to seek or not
     *
     * @param userSeekable true user can seek.
     * @return my builder
     */
    public MyBuilder userSeekable(boolean userSeekable) {
        this.userSeekable = userSeekable;
        return this;
    }

    /**
     * user change the thumb's location by touching thumb,touching track will not worked.
     *
     * @param onlyThumbDraggable true for seeking only by drag thumb. default false;
     * @return my builder
     */
    public MyBuilder onlyThumbDraggable(boolean onlyThumbDraggable) {
        this.onlyThumbDraggable = onlyThumbDraggable;
        return this;
    }

    /**
     * set the seek bar's background track's Stroke Width
     *
     * @param trackBackgroundSize The dp size.
     * @return the my builder
     */
    public MyBuilder trackBackgroundSize(int trackBackgroundSize) {
        this.trackBackgroundSize = SizeUtils.dp2px(context, trackBackgroundSize);
        return this;
    }

    /**
     * set the seek bar's background track's color.
     *
     * @param trackBackgroundColor colorInt
     * @return the my builder
     */
    public MyBuilder trackBackgroundColor(@ColorInt int trackBackgroundColor) {
        this.trackBackgroundColor = trackBackgroundColor;
        return this;
    }

    /**
     * set the seek bar's progress track's Stroke Width
     *
     * @param trackProgressSize The dp size.
     * @return the my builder
     */
    public MyBuilder trackProgressSize(int trackProgressSize) {
        this.trackProgressSize = SizeUtils.dp2px(context, trackProgressSize);
        return this;
    }

    /**
     * set the seek bar's progress track's color.
     *
     * @param trackProgressColor colorInt
     * @return the my builder
     */
    public MyBuilder trackProgressColor(@ColorInt int trackProgressColor) {
        this.trackProgressColor = trackProgressColor;
        return this;
    }

    /**
     * call this method to show the seek bar's ends to square corners.default rounded corners.
     *
     * @param trackRoundedCorners false to show square corners.
     * @return the my builder
     */
    public MyBuilder trackRoundedCorners(boolean trackRoundedCorners) {
        this.trackRoundedCorners = trackRoundedCorners;
        return this;
    }

    /**
     * set the seek bar's thumb's text color.
     *
     * @param thumbTextColor colorInt
     * @return the my builder
     */
    public MyBuilder thumbTextColor(@ColorInt int thumbTextColor) {
        this.thumbTextColor = thumbTextColor;
        return this;
    }

    /**
     * call this method to show the text below thumb in one place,
     * the text will slide with the thumb.
     *
     * @param thumbTextPosition see{@link TextPosition}
     * @return the my builder
     */
    public MyBuilder thumbTextPosition(int thumbTextPosition) {
        this.thumbTextShow = thumbTextPosition;
        return this;
    }

    /**
     * set the seek bar's thumb's color.
     *
     * @param thumbColor colorInt
     * @return the my builder
     */
    public MyBuilder thumbColor(@ColorInt int thumbColor) {
        this.thumbColor = thumbColor;
        return this;
    }

    /**
     * adjust thumb to tick position auto after touch up
     *
     * @param autoAdjust true to adjust thumb to tick position auto after touch up
     * @return the my builder
     */
    public MyBuilder thumbAutoAdjust(boolean autoAdjust) {
        this.thumbAutoAdjust = autoAdjust;
        return this;
    }

    /**
     * set the seek bar's thumb's selector color.
     *
     * @param thumbColorStateList color selector
     * @return MyBuilder  selector format like:
     */
//<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_pressed="true" />  <!--this color is for thumb which is at pressing status-->
    //<item android:color="@color/color_blue" />                                <!--for thumb which is at normal status-->
    //</selector>
    public MyBuilder thumbColorStateList(@NonNull ColorStateList thumbColorStateList) {
        this.thumbColorStateList = thumbColorStateList;
        return this;
    }

    /**
     * set the seek bar's thumb's Width.will be limited in 30dp.
     *
     * @param thumbSize The dp size.
     * @return the my builder
     */
    public MyBuilder thumbSize(int thumbSize) {
        this.thumbSize = SizeUtils.dp2px(context, thumbSize);
        return this;
    }

    /**
     * call this method to custom the thumb showing drawable.
     *
     * @param thumbDrawable the drawable show as Thumb.
     * @return the my builder
     */
    public MyBuilder thumbDrawable(@NonNull Drawable thumbDrawable) {
        this.thumbDrawable = thumbDrawable;
        return this;
    }

    /**
     * call this method to custom the thumb showing drawable by selector Drawable.
     *
     * @param thumbStateListDrawable the drawable show as Thumb.
     * @return MyBuilder  <p> selector format:
     */
//<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:drawable="Your drawableA" android:state_pressed="true" />  <!--this drawable is for thumb when pressing-->
    //<item android:drawable="Your drawableB" />  < !--for thumb when normal-->
    //</selector>
    public MyBuilder thumbDrawable(@NonNull StateListDrawable thumbStateListDrawable) {
        this.thumbDrawable = thumbStateListDrawable;
        return this;
    }

    /**
     * call this method to custom the thumb showing drawable.
     *
     * @param thumbDrawableId the drawableId for thumb drawable.
     * @return the my builder
     */
    public MyBuilder thumbDrawable(@DrawableRes int thumbDrawableId) {
        this.thumbDrawable = ContextCompat.getDrawable(context,thumbDrawableId);
        return this;
    }

    /**
     * show the tick texts in one place
     *
     * @param textPosition the position for texts to show,                     see{@link TextPosition}                     TextPosition.NONE;                     TextPosition.BELOW;                     TextPosition.ABOVE;
     * @return the my builder
     */
    public MyBuilder showTickTextsPosition(int textPosition) {
        this.tickTextsShow = textPosition;
        return this;
    }

    /**
     * set the color for text below/above seek bar's tickText.
     *
     * @param tickTextsColor ColorInt
     * @return the my builder
     */
    public MyBuilder tickTextsColor(@ColorInt int tickTextsColor) {
        this.tickTextsColor = tickTextsColor;
        return this;
    }

    /**
     * set the selector color for text below/above seek bar's tickText.
     *
     * @param tickTextsColorStateList ColorInt
     * @return MyBuilder  selector format like:
     */
//<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_selected="true" />  <!--this color is for texts those are at left side of thumb-->
    //<item android:color="@color/color_blue" android:state_hovered="true" />     <!--for thumb below text-->
    //<item android:color="@color/color_gray" />                                 <!--for texts those are at right side of thumb-->
    //</selector>
    public MyBuilder tickTextsColorStateList(@NonNull ColorStateList tickTextsColorStateList) {
        this.tickTextsColorStateList = tickTextsColorStateList;
        return this;
    }

    /**
     * set the size for tickText which below/above seek bar's tick .
     *
     * @param tickTextsSize The scaled pixel size.
     * @return the my builder
     */
    public MyBuilder tickTextsSize(int tickTextsSize) {
        this.tickTextsSize = SizeUtils.sp2px(context, tickTextsSize);
        return this;
    }

    /**
     * call this method to replace the seek bar's tickMarks' below/above tick texts.
     *
     * @param tickTextsArray the length should same as tickCount.
     * @return the my builder
     */
    public MyBuilder tickTextsArray(String[] tickTextsArray) {
        this.tickTextsCustomArray = tickTextsArray;
        return this;
    }


    /**
     * call this method to replace the seek bar's tickMarks' below/above tick texts.
     *
     * @param tickTextsArray the length should same as tickNum.
     * @return the my builder
     */
    public MyBuilder tickTextsArray(@ArrayRes int tickTextsArray) {
        this.tickTextsCustomArray = context.getResources().getStringArray(tickTextsArray);
        return this;
    }

    /**
     * set the tick text's / thumb text textTypeface .
     *
     * @param tickTextsTypeFace The text textTypeface.
     * @return the my builder
     */
    public MyBuilder tickTextsTypeFace(Typeface tickTextsTypeFace) {
        this.tickTextsTypeFace = tickTextsTypeFace;
        return this;
    }

    /**
     * set the tickMarks number.
     *
     * @param tickCount the tickMarks count show on seek bar.                  if you want the seek bar's block size is N , this tickCount should be N+1.
     * @return the my builder
     */
    public MyBuilder tickCount(int tickCount) {
        this.tickCount = tickCount;
        return this;
    }

    /**
     * call this method to show different tickMark shape.
     *
     * @param tickMarksType see{@link TickMarkType}                      TickMarkType.NONE;                      TickMarkType.OVAL;                      TickMarkType.SQUARE;                      TickMarkType.DIVIDER;                      TickMarkType.CUSTOM;
     * @return the my builder
     */
    public MyBuilder showTickMarksType(int tickMarksType) {
        this.showTickMarksType = tickMarksType;
        return this;
    }

    /**
     * set the seek bar's tick's color.
     *
     * @param tickMarksColor colorInt
     * @return the my builder
     */
    public MyBuilder tickMarksColor(@ColorInt int tickMarksColor) {
        this.tickMarksColor = tickMarksColor;
        return this;
    }

    /**
     * set the seek bar's tick's color.
     *
     * @param tickMarksColorStateList colorInt
     * @return MyBuilder  selector format like:
     */
//<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_selected="true" />  <!--this color is for marks those are at left side of thumb-->
    //<item android:color="@color/color_gray" />                                 <!--for marks those are at right side of thumb-->
    //</selector>
    public MyBuilder tickMarksColor(@NonNull ColorStateList tickMarksColorStateList) {
        this.tickMarksColorStateList = tickMarksColorStateList;
        return this;
    }

    /**
     * set the seek bar's tick width , if tick type is divider, call this method will be not worked(tick type is divider,has a regular value 2dp).
     *
     * @param tickMarksSize the dp size.
     * @return the my builder
     */
    public MyBuilder tickMarksSize(int tickMarksSize) {
        this.tickMarksSize = SizeUtils.dp2px(context, tickMarksSize);
        return this;
    }

    /**
     * call this method to custom the tick showing drawable.
     *
     * @param tickMarksDrawable the drawable show as tickMark.
     * @return the my builder
     */
    public MyBuilder tickMarksDrawable(@NonNull Drawable tickMarksDrawable) {
        this.tickMarksDrawable = tickMarksDrawable;
        return this;
    }

    /**
     * call this method to custom the tick showing drawable by selector.
     *
     * @param tickMarksStateListDrawable the StateListDrawable show as tickMark.
     * @return MyBuilder  selector format like :
     */
//<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:drawable="@mipmap/ic_launcher_round" android:state_pressed="true" />  <!--this drawable is for thumb when pressing-->
    //<item android:drawable="@mipmap/ic_launcher" />  <!--for thumb when normal-->
    //</selector>
    public MyBuilder tickMarksDrawable(@NonNull StateListDrawable tickMarksStateListDrawable) {
        this.tickMarksDrawable = tickMarksStateListDrawable;
        return this;
    }

    /**
     * call this method to hide the tickMarks which show in the both ends sides of seek bar.
     *
     * @param tickMarksEndsHide true for hide.
     * @return the my builder
     */
    public MyBuilder tickMarksEndsHide(boolean tickMarksEndsHide) {
        this.tickMarksEndsHide = tickMarksEndsHide;
        return this;
    }

    /**
     * call this method to hide the tickMarks on seekBar's thumb left;
     *
     * @param tickMarksSweptHide true for hide.
     * @return the my builder
     */
    public MyBuilder tickMarksSweptHide(boolean tickMarksSweptHide) {
        this.tickMarksSweptHide = tickMarksSweptHide;
        return this;
    }

}