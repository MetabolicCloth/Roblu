package com.cpjd.roblu.ui.forms;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.cpjd.roblu.R;
import com.cpjd.roblu.models.RCheckout;
import com.cpjd.roblu.models.RUI;
import com.cpjd.roblu.models.metrics.RBoolean;
import com.cpjd.roblu.models.metrics.RCheckbox;
import com.cpjd.roblu.models.metrics.RChooser;
import com.cpjd.roblu.models.metrics.RCounter;
import com.cpjd.roblu.models.metrics.RGallery;
import com.cpjd.roblu.models.metrics.RMetric;
import com.cpjd.roblu.models.metrics.RSlider;
import com.cpjd.roblu.models.metrics.RStopwatch;
import com.cpjd.roblu.models.metrics.RTextfield;
import com.cpjd.roblu.utils.Constants;
import com.cpjd.roblu.utils.Utils;
import com.etiennelawlor.imagegallery.library.activities.FullScreenImageGalleryActivity;
import com.etiennelawlor.imagegallery.library.activities.ImageGalleryActivity;
import com.etiennelawlor.imagegallery.library.adapters.FullScreenImageGalleryAdapter;
import com.etiennelawlor.imagegallery.library.adapters.ImageGalleryAdapter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lombok.Setter;

/**
 * RMetricToUI loads a RMetric instance into a UI element (a CardView).
 * Make sure to attach an ElementsListener to listener for updated UI elements
 *
 * @version 2
 * @since 3.2.0
 * @author Will Davies
 */
public class RMetricToUI implements ImageGalleryAdapter.ImageThumbnailLoader, FullScreenImageGalleryAdapter.FullScreenImageLoader {
    /**
     * Activity reference
     */
    private final Activity activity;
    /**
     * True if we're allowed to edit the forms
     */
    private final boolean editable;
    /**
     * UI config
     */
    private final RUI rui;
    private final int width;

    /**
     * Make sure to attach a listener to this!
     */
    @Setter
    private MetricListener listener;

    @Override
    public void loadFullScreenImage(ImageView iv, String imageUrl, int width, LinearLayout bglinearLayout) {

    }

    @Override
    public void loadImageThumbnail(ImageView iv, String imageUrl, int dimension) {

    }

    public interface MetricListener {
        /**
         * Called when a change is made to ANY element, since this class stores all the references, just save everything and you're good to go.
         */
        void changeMade(RMetric metric);
    }

    public RMetricToUI(Activity activity, RUI rui, boolean editable) {
        this.activity = activity;
        this.editable = editable;
        this.rui = rui;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x / 2;
    }

    /**
     * Gets the Boolean UI card from an RBoolean reference
     * @param bool RBoolean reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getBoolean(final RBoolean bool) {
        RadioGroup group = new RadioGroup(activity);
        AppCompatRadioButton yes = new AppCompatRadioButton(activity);
        AppCompatRadioButton no = new AppCompatRadioButton(activity);

        yes.setEnabled(editable);
        no.setEnabled(editable);

        //yes.setSupportButtonTintList(colorStateList);
        //no.setSupportButtonTintList(colorStateList);
        group.setId(Utils.generateViewId());
        yes.setId(Utils.generateViewId());
        no.setId(Utils.generateViewId());
        yes.setText(R.string.yes);
        no.setText(R.string.no);

        // don't check either if the boolean isn't modified
        yes.setChecked(bool.isValue());
        no.setChecked(!bool.isValue());

        group.addView(yes);
        group.addView(no);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                bool.setValue(((RadioButton)radioGroup.getChildAt(0)).isChecked());
                listener.changeMade(bool);
            }
        });

        TextView title = new TextView(activity);
        title.setTextColor(rui.getText());
        title.setText(bool.getTitle());
        title.setMaxWidth(width);
        title.setTextSize(20);
        title.setId(Utils.generateViewId());

        if(!bool.isModified()) title.setText(title.getText()+" (N.O.)");

        RelativeLayout layout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        group.setLayoutParams(params);
        group.setPadding(group.getPaddingLeft(), group.getPaddingTop(), 50, group.getPaddingBottom());
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        title.setPadding(18, title.getPaddingTop(), title.getPaddingRight(), title.getPaddingBottom());
        title.setLayoutParams(params);

        layout.addView(title);
        layout.addView(group);
        return getCard(layout);
    }

    /**
     * Gets the Counter UI card from an RCounter reference
     * @param counter RCounter reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getCounter(final RCounter counter) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        TextView title = new TextView(activity);
        title.setTextColor(rui.getText());
        title.setTextSize(20);
        title.setId(Utils.generateViewId());
        title.setText(counter.getTitle());
        title.setMaxWidth(width);
        title.setPadding(Utils.DPToPX(activity, 8), title.getPaddingTop(), title.getPaddingRight(), title.getPaddingBottom());
        title.setLayoutParams(params);

        Drawable add = ContextCompat.getDrawable(activity, R.drawable.add_small);
        if(add != null) {
            add.mutate();
            add.setColorFilter(rui.getButtons(), PorterDuff.Mode.SRC_IN);
        }
        Drawable minus = ContextCompat.getDrawable(activity,R.drawable.minus_small);
        if(minus != null) {
            minus.mutate();
            minus.setColorFilter(rui.getButtons(), PorterDuff.Mode.SRC_IN);
        }
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        ImageView addButton = new ImageView(activity);
        addButton.setId(Utils.generateViewId());
        addButton.setEnabled(editable);
        addButton.setBackground(add);
        addButton.setPadding(Utils.DPToPX(activity, 4), Utils.DPToPX(activity, 3), Utils.DPToPX(activity, 4), Utils.DPToPX(activity, 3));
        addButton.setScaleX(1.5f);
        addButton.setScaleY(1.5f);
        addButton.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF, addButton.getId());
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        final TextView number = new TextView(activity);
        number.setTextSize(25);
        number.setTextColor(rui.getText());
        number.setId(Utils.generateViewId());
        number.setText(String.valueOf(counter.getTextValue()));
        if(!counter.isModified()) number.setText("N.O.");
        number.setLayoutParams(params);
        number.setPadding(Utils.DPToPX(activity, 20), number.getPaddingTop(), Utils.DPToPX(activity, 20), number.getPaddingBottom());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter.add();
                number.setText(counter.getTextValue());
                listener.changeMade(counter);
            }
        });

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF, number.getId());
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        ImageView minusButton = new ImageView(activity);
        minusButton.setBackground(minus);
        minusButton.setId(Utils.generateViewId());
        minusButton.setEnabled(editable);
        minusButton.setScaleY(1.5f);
        minusButton.setScaleX(1.5f);
        minusButton.setLayoutParams(params);
        minusButton.setPadding(Utils.DPToPX(activity, 4), Utils.DPToPX(activity, 3), Utils.DPToPX(activity, 4), Utils.DPToPX(activity, 3));
        minusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                counter.minus();
                number.setText(String.valueOf(counter.getTextValue()));
                listener.changeMade(counter);
            }
        });

        RelativeLayout layout = new RelativeLayout(activity);
        layout.addView(title);
        layout.addView(minusButton);
        layout.addView(number);
        layout.addView(addButton);
        return getCard(layout);
    }
    /**
     * Gets the Slider UI card from an RSlider reference
     * @param slider RSlider reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getSlider(final RSlider slider) {
        TextView title = new TextView(activity);
        title.setTextColor(rui.getText());
        title.setText(slider.getTitle());
        title.setTextSize(15);
        title.setMaxWidth(width);
        title.setId(Utils.generateViewId());

        SeekBar sb = new SeekBar(activity);
        sb.getThumb().setColorFilter(rui.getAccent(), PorterDuff.Mode.SRC_IN);
        sb.getProgressDrawable().setColorFilter(rui.getAccent(), PorterDuff.Mode.SRC_IN);
        sb.setMax(slider.getMax() - slider.getMin());
        sb.setEnabled(editable);
        sb.setProgress(slider.getValue() - slider.getMin());
        sb.setId(Utils.generateViewId());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.BELOW, title.getId());
        sb.setLayoutParams(params);

        TextView minv = new TextView(activity);
        minv.setTextColor(rui.getText());
        minv.setId(Utils.generateViewId());
        TextView max = new TextView(activity);
        max.setTextColor(rui.getText());
        max.setId(Utils.generateViewId());
        final TextView current = new TextView(activity);
        current.setTextColor(rui.getText());
        current.setId(Utils.generateViewId());
        current.setText(String.valueOf(slider.getValue()));
        if(!slider.isModified()) current.setText("N.O.");
        current.setTextColor(Color.WHITE);
        minv.setText(String.valueOf(slider.getMin()));
        max.setText(String.valueOf(slider.getMax()));

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                slider.setValue(progress + slider.getMin());
                current.setText(String.valueOf(slider.getValue()));
                seekBar.setProgress(progress);
                listener.changeMade(slider);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        RelativeLayout layout = new RelativeLayout(activity);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.BELOW, sb.getId());
        minv.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.BELOW, sb.getId());
        current.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.BELOW, sb.getId());
        max.setLayoutParams(params);

        layout.addView(title);
        layout.addView(sb);
        layout.addView(minv);
        layout.addView(current);
        layout.addView(max);

        return getCard(layout);
    }

    /**
     * Gets the Chooser UI card from an RChooser reference
     * @param chooser RChooser reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getChooser(final RChooser chooser) {
        Spinner spinner = new Spinner(activity);
        spinner.setId(Utils.generateViewId());
        spinner.setEnabled(editable);
        spinner.setPadding(400, spinner.getPaddingTop(), spinner.getPaddingRight(), spinner.getPaddingBottom());
        if(chooser.getValues() != null) {
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(activity, R.layout.spinner_item, chooser.getValues())
                    {
                        @NonNull
                        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                            View v = super.getView(position, convertView, parent);

                            ((TextView) v).setTextSize(16);
                            ((TextView) v).setTextColor(rui.getText());

                            return v;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView,@NonNull ViewGroup parent) {
                            View v = super.getDropDownView(position, convertView, parent);
                            v.setBackgroundColor(rui.getBackground());

                            ((TextView) v).setTextColor(rui.getText());
                            ((TextView) v).setGravity(Gravity.CENTER);
                            return v;
                        }
                    };
            spinner.setAdapter(adapter);
            spinner.setSelection(chooser.getSelectedIndex());
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first;
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!first) {
                    first = true;
                } else {
                    chooser.setSelectedIndex(i);
                    listener.changeMade(chooser);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        TextView title = new TextView(activity);
        title.setTextColor(rui.getText());
        title.setText(chooser.getTitle());
        title.setTextSize(20);
        title.setMaxWidth(width);
        title.setId(Utils.generateViewId());
        title.setPadding(18, title.getPaddingTop(), title.getPaddingRight(), title.getPaddingBottom());
        RelativeLayout layout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        spinner.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        title.setLayoutParams(params);
        layout.addView(title);
        layout.addView(spinner);

        return getCard(layout);
    }
    /**
     * Gets the Counter UI card from an RCheckbox reference
     * @param checkbox RCheckbox reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getCheckbox(final RCheckbox checkbox) {
        TextView title = new TextView(activity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        title.setId(Utils.generateViewId());
        title.setMaxLines(15);
        title.setMaxWidth(width);
        title.setPadding(18, title.getPaddingTop(), 100, title.getPaddingBottom());
        title.setTextSize(20);
        title.setTextColor(rui.getText());
        title.setText(checkbox.getTitle());
        title.setLayoutParams(params);

        RelativeLayout layout = new RelativeLayout(activity);
        layout.addView(title);

        if(checkbox.getValues() != null) {
            final AppCompatCheckBox[] boxes = new AppCompatCheckBox[checkbox.getValues().size()];
            int i = 0;
            for(Object o : checkbox.getValues().keySet()) {
                params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.RIGHT_OF, title.getId());
                if (i > 0) params.addRule(RelativeLayout.BELOW, boxes[i - 1].getId());
                AppCompatCheckBox box = new AppCompatCheckBox(activity);
                box.setText(o.toString());
                box.setTag(o.toString());
                box.setId(Utils.generateViewId());
                box.setTextColor(rui.getText());
                box.setChecked(checkbox.getValues().get(o));
                box.setEnabled(editable);
                box.setLayoutParams(params);
                ColorStateList colorStateList = new ColorStateList(
                        new int[][] {
                                new int[] { -android.R.attr.state_checked }, // unchecked
                                new int[] {  android.R.attr.state_checked }  // checked
                        },
                        new int[] {
                                rui.getText(),
                                rui.getAccent()
                        }
                );
                //box.setSupportButtonTintList(colorStateList);
                box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        checkbox.getValues().put(compoundButton.getTag().toString(), b);
                        listener.changeMade(checkbox);
                    }
                });
                boxes[i] = box;
                layout.addView(boxes[i]);
                i++;
            }
        }

        return getCard(layout);
    }

    /**
     * Gets the Slider UI card from an RSlider reference
     * @param stopwatch RSlider reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getStopwatch(final RStopwatch stopwatch) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        TextView title = new TextView(activity);
        title.setTextColor(rui.getText());
        title.setText(stopwatch.getTitle());
        title.setTextSize(20);
        title.setMaxWidth((int)(width * 0.8));
        title.setId(Utils.generateViewId());
        title.setPadding(Utils.DPToPX(activity, 8), title.getPaddingTop(), title.getPaddingRight(), title.getPaddingBottom());
        title.setLayoutParams(params);
        if(!stopwatch.isModified()) title.setText(title.getText() + " (N.O.)");

        final Drawable play = ContextCompat.getDrawable(activity, R.drawable.play);
        final Drawable pause = ContextCompat.getDrawable(activity,R.drawable.pause);
        final Drawable reset = ContextCompat.getDrawable(activity,R.drawable.replay);

        if(play != null) {
            play.mutate();
            play.setColorFilter(rui.getButtons(), PorterDuff.Mode.SRC_IN);
        }

        if(pause != null) {
            pause.mutate();
            pause.setColorFilter(rui.getButtons(), PorterDuff.Mode.SRC_IN);
        }

        if(reset != null) {
            reset.mutate();
            reset.setColorFilter(rui.getButtons(), PorterDuff.Mode.SRC_IN);
        }

        final ImageView playButton = new ImageView(activity);
        playButton.setBackground(play);
        playButton.setEnabled(editable);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        playButton.setId(Utils.generateViewId());
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        playButton.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF, playButton.getId());
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        final ImageView button = new ImageView(activity);
        button.setBackground(reset);
        button.setId(Utils.generateViewId());
        button.setEnabled(editable);
        button.setLayoutParams(params);
        final TextView timer = new TextView(activity);
        timer.setTextSize(25);
        timer.setPadding(timer.getPaddingLeft(), timer.getPaddingTop(), Utils.DPToPX(activity, 15), timer.getPaddingBottom());
        timer.setText(stopwatch.getTime()+"s");
        timer.setTextColor(rui.getText());
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF, button.getId());
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        timer.setLayoutParams(params);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer.setText(R.string.no_time);
                    }
                });
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            Timer time;
            TimerTask task;
            int mode = 0;
            double t;
            @Override
            public void onClick(View view) {
                if(mode == 0) {
                    time = new Timer();
                    task = new TimerTask() {
                        public void run() {

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(timer.getText().equals("N.O.")) t = 0;
                                    else t = Double.parseDouble(timer.getText().toString().replace("s", ""));
                                    t+=0.1;
                                    timer.setText(String.valueOf(Utils.round(t, 1))+"s");

                                }
                            });
                        }
                    };
                    time.schedule(task, 0, 100);
                    mode = 1;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setBackground(pause);
                        }
                    });

                } else {
                    task.cancel();
                    task = null;

                    playButton.setBackground(play);
                    mode = 0;
                    stopwatch.setTime(t);
                    listener.changeMade(stopwatch);
                }
            }
        });

        RelativeLayout layout = new RelativeLayout(activity);
        layout.addView(title);
        layout.addView(timer);
        layout.addView(button);
        layout.addView(playButton);
        return getCard(layout);
    }
    /**
     * Gets the Textfield UI card from an RTextfield reference
     * @param textfield RTextfield reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getTextfield(final RTextfield textfield) {
        RelativeLayout layout = new RelativeLayout(activity);
        TextView textView = new TextView(activity);
        textView.setTextColor(rui.getText());
        textView.setText(textfield.getTitle());
        textView.setId(Utils.generateViewId());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, textView.getId());
        AppCompatEditText et = new AppCompatEditText(activity);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                AppCompatEditText et2 = (AppCompatEditText)v;
               // if(hasFocus) et2.setSupportBackgroundTintList(ColorStateList.valueOf(rui.getAccent()));
                //else et2.setSupportBackgroundTintList(ColorStateList.valueOf(rui.getText()));
            }
        });
        Utils.setCursorColor(et, rui.getAccent());
        et.setText(textfield.getText());
        et.setEnabled(editable);
        et.setTextColor(rui.getText());
        if(textfield.isNumericalOnly()) et.setInputType(InputType.TYPE_CLASS_NUMBER);
        if(textfield.isOneLine()) et.setMaxLines(1);
        et.setHighlightColor(rui.getAccent());
        Drawable d = et.getBackground();
        d.setColorFilter(rui.getText(), PorterDuff.Mode.SRC_ATOP);
        et.setBackground(d);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textfield.setText(charSequence.toString());
                listener.changeMade(textfield);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        et.setSingleLine(false);
        et.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        et.setFocusableInTouchMode(true);
        et.setLayoutParams(params);

        layout.addView(textView); layout.addView(et);
        return getCard(layout);
    }

    /**
     * Gets the Gallery UI card from an RGallery reference
     * @param gallery RGallery reference to be set to the UI
     * @return a UI CardView
     */
    public CardView getGallery(final boolean demo, final RCheckout handoff, final RGallery gallery) {
        RelativeLayout layout = new RelativeLayout(activity);
        TextView textView = new TextView(activity);
        textView.setTextColor(rui.getText());
        textView.setText(gallery.getTitle());
        textView.setId(Utils.generateViewId());
        textView.setMaxWidth(width / 3);
        textView.setWidth(width);
        textView.setMaxLines(1);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

        Button open = new Button(activity);
        open.setText(R.string.open);
        open.setTextColor(rui.getText());
        open.setId(Utils.generateViewId());
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(demo) return;
                ImageGalleryActivity.setImageThumbnailLoader(RMetricToUI.this);
                FullScreenImageGalleryActivity.setFullScreenImageLoader(RMetricToUI.this);
                Intent intent = new Intent(activity, ImageGalleryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(ImageGalleryActivity.KEY_TITLE, gallery.getTitle());
                bundle.putInt("ID", gallery.getID());
                bundle.putInt("checkout", handoff.getID());
                //bundle.putInt("tabID", tabID);
                bundle.putBoolean("readOnly", editable);
                intent.putExtras(bundle);
                activity.startActivityForResult(intent, Constants.GENERAL);
            }
        });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        open.setLayoutParams(params);
        open.setPadding(open.getPaddingLeft(), open.getPaddingTop(), Utils.DPToPX(activity, 6), open.getPaddingBottom());
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        textView.setPadding(Utils.DPToPX(activity, 8), textView.getPaddingTop(),textView.getPaddingRight(), textView.getPaddingBottom());
        textView.setLayoutParams(params);
        layout.setTag(gallery.getID());

        layout.addView(textView);
        layout.addView(open);
        return getCard(layout);
    }

    private CardView getCard(View layout) {
        CardView card = new CardView(activity);
        if(editable) {
            Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
            params.rightMargin = 65;
            card.setLayoutParams(params);
            card.setMaxCardElevation(0);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            card.setLayoutParams(params);
            card.setCardElevation(5);
        }
        card.setUseCompatPadding(true);
        card.setRadius(rui.getFormRadius());
        card.setContentPadding(Utils.DPToPX(activity, 8), Utils.DPToPX(activity, 8), Utils.DPToPX(activity, 8), Utils.DPToPX(activity, 8));
        card.setPadding(Utils.DPToPX(activity, 8), Utils.DPToPX(activity, 8), Utils.DPToPX(activity, 8), Utils.DPToPX(activity, 8));
        card.setCardBackgroundColor(rui.getCardColor());
        card.addView(layout);
        return card;
    }

    public CardView getInfoField(final String name, String data, final String website, final int number) {
        RelativeLayout layout = new RelativeLayout(activity);
        TextView textView = new TextView(activity);
        textView.setText(name);
        textView.setTextColor(rui.getText());
        textView.setId(Utils.generateViewId());

        if(number != -1) {
            final Drawable reset = ContextCompat.getDrawable(activity, R.drawable.export);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            final ImageView page = new ImageView(activity);
            page.setBackground(reset);
            page.setLayoutParams(params);
            page.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://www.thebluealliance.com/team/" + number));
                    activity.startActivity(i);
                }
            });
            layout.addView(page);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, textView.getId());
        TextView et = new TextView(activity);
        et.setId(Utils.generateViewId());
        et.setTextColor(rui.getText());
        et.setText(data);
        et.setSingleLine(false);
        et.setEnabled(false);
        et.setFocusableInTouchMode(false);
        et.setLayoutParams(params);

        if(website != null && !website.equals("")) {
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, et.getId());
            Button b = new Button(activity);
            b.setTextColor(rui.getText());
            b.setText(website);
            b.setLayoutParams(params);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(website));
                    activity.startActivity(i);
                }
            });
            layout.addView(b);
        }

        layout.addView(textView); layout.addView(et);
        return getCard(layout);
    }

    public CardView getEditHistory(LinkedHashMap<String, Long> edits) {
        RelativeLayout layout = new RelativeLayout(activity);
        TextView textView = new TextView(activity);
        textView.setText(R.string.edit_history);
        textView.setTextColor(rui.getText());
        textView.setId(Utils.generateViewId());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, textView.getId());
        TextView et = new TextView(activity);
        et.setId(Utils.generateViewId());
        et.setTextColor(rui.getText());
        /*
         * Generate string
         */
        StringBuilder editHistory = new StringBuilder();
        for(Object o : edits.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            editHistory.append(pair.getKey()).append(" on ").append(Utils.convertTime((Long)pair.getValue()));
        }

        et.setText(editHistory.toString());
        et.setSingleLine(false);
        et.setEnabled(false);
        et.setFocusableInTouchMode(false);
        et.setLayoutParams(params);

        layout.addView(textView); layout.addView(et);
        return getCard(layout);
    }

}
