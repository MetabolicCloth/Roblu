package com.cpjd.roblu.teams.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cpjd.roblu.R;
import com.cpjd.roblu.forms.Elements;
import com.cpjd.roblu.forms.ElementsListener;
import com.cpjd.roblu.forms.SaveThread;
import com.cpjd.roblu.forms.elements.EBoolean;
import com.cpjd.roblu.forms.elements.ECheckbox;
import com.cpjd.roblu.forms.elements.EChooser;
import com.cpjd.roblu.forms.elements.ECounter;
import com.cpjd.roblu.forms.elements.EGallery;
import com.cpjd.roblu.forms.elements.ESTextfield;
import com.cpjd.roblu.forms.elements.ESlider;
import com.cpjd.roblu.forms.elements.EStopwatch;
import com.cpjd.roblu.forms.elements.ETextfield;
import com.cpjd.roblu.forms.elements.Element;
import com.cpjd.roblu.models.Loader;
import com.cpjd.roblu.models.REvent;
import com.cpjd.roblu.models.RForm;
import com.cpjd.roblu.models.RTeam;
import com.cpjd.roblu.teams.TeamViewer;
import com.cpjd.roblu.utils.Text;

import java.util.ArrayList;

public class Match extends Fragment implements ElementsListener {

    private int position;

    private REvent event;
    private RTeam team;
    private RForm form;

    private Elements els;

    private View view;

    private LinearLayoutCompat layout;
    private boolean readOnly;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.match_tab, container, false);

        layout = (LinearLayoutCompat) view.findViewById(R.id.match_layout);

        Bundle bundle = this.getArguments();
        event = (REvent) bundle.getSerializable("event");
        team = new Loader(view.getContext()).loadTeam(event.getID(), bundle.getLong("team"));
        form = (RForm) bundle.getSerializable("form");
        position = bundle.getInt("position") - 1;
        readOnly = bundle.getBoolean("readOnly");

        if(readOnly) position++;

        els = new Elements(getActivity(), new Loader(getActivity()).loadSettings().getRui(), this, false);
        els.setReadOnly(readOnly);

        load();

        return view;
    }

    public void load() {
        if(layout != null && layout.getChildCount() > 0) layout.removeAllViews();

        if(layout != null && layout.getChildCount() > 0) layout.removeAllViews();

        ArrayList<Element> elements;
        if(position == 0) elements = form.getPit();
        else elements = form.getMatch();

        for(Element s : elements) {
            for (Element e : team.getTabs().get(position).getElements()) {
                if (e.getID() == s.getID()) {
                    loadElement(e);
                }
            }
        }
        // Add edits card
        if(event.isCloudEnabled() && team.getTabs().get(position).getEdits() != null && team.getTabs().get(position).getEdits().size() != 0)
            if(team.getTabs().get(position) != null) layout.addView(els.getEditHistory(team.getTabs().get(position).getEdits()));
    }

    private void loadElement(Element e) {
        if (e instanceof ESTextfield) {
            if (e.getID() == 0)
                layout.addView(els.getSTextfield(e.getID(), e.getTitle(), team.getName(), false));
            else
                layout.addView(els.getSTextfield(e.getID(), e.getTitle(), String.valueOf(team.getNumber()), true));
        } else if (e instanceof EBoolean)
            layout.addView(els.getBoolean(e.getID(), e.getTitle(), ((EBoolean) e).getValue(), ((EBoolean) e).isUsingNA()));
        else if (e instanceof ECounter)
            layout.addView(els.getCounter(e.getID(), e.getTitle(), ((ECounter) e).getMin(), ((ECounter) e).getMax(), ((ECounter) e).getIncrement(), ((ECounter) e).getCurrent(), !e.isModified()));
        else if (e instanceof ESlider)
            layout.addView(els.getSlider(e.getID(), e.getTitle(), ((ESlider) e).getMax(), ((ESlider) e).getCurrent(), !e.isModified()));
        else if (e instanceof EChooser)
            layout.addView(els.getChooser(e.getID(), e.getTitle(), ((EChooser) e).getValues(), ((EChooser) e).getSelected()));
        else if (e instanceof ECheckbox)
            layout.addView(els.getCheckbox(e.getID(), e.getTitle(), ((ECheckbox) e).getValues(), ((ECheckbox) e).getChecked()));
        else if (e instanceof EStopwatch) {
            layout.addView(els.getStopwatch(e.getID(), e.getTitle(), Text.round(((EStopwatch) e).getTime(), 1), !e.isModified()));
        } else if (e instanceof ETextfield)
            layout.addView(els.getTextfield(e.getID(), e.getTitle(), ((ETextfield) e).getText()));
        else if(e instanceof EGallery) layout.addView(els.getGallery(e.getID(), e.getTitle(), false, event, team, position));
    }

    public void setTeam(RTeam team) {
        this.team = team;
    }

    @Override
    public void nameInited(String name) {}

    @Override
    public void booleanUpdated(int ID, int value) {
        team.updateBoolean(position, ID, value);
        save();

    }

    @Override
    public void counterUpdated(int ID, int value) {
        team.updateCounter(position, ID, value);
        save();
    }

    @Override
    public void sliderUpdated(int ID, int value) {
        team.updateSlider(position, ID, value);
        save();
    }

    @Override
    public void chooserUpdated(int ID, int selected) {
        team.updateChooser(position, ID, selected);
        save();
    }

    @Override
    public void checkboxUpdated(int ID, ArrayList<Boolean> checked) {
        team.updateCheckbox(position, ID, checked);
        save();
    }

    @Override
    public void stopwatchUpdated(int ID, double time) {
        team.updateStopwatch(position, ID, time);
        save();
    }

    @Override
    public void textfieldUpdated(int ID, String value) {
        if(position == 0) {
            if(ID == 0) {
                ((TeamViewer)getActivity()).setActionBarTitle(value);
            }
            else if(ID == 1) {
                if(value.length() >= 10) value = value.substring(0, 9);
                if(value.equals("")) value = "0";
                ((TeamViewer)getActivity()).setActionBarSubtitle("#"+value);
            }
        }

        team.updateTextfield(position, ID, value);
        save();
    }

    // Start a save thread and save everything to the file system
    private void save() {
        if(!readOnly) new SaveThread(view.getContext(), event.getID(), team);
    }

    public int getPosition() {
        if(readOnly) return position;
        return position + 1;
    }

}
