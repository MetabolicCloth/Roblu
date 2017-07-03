package com.cpjd.roblu.models;

import com.cpjd.roblu.forms.elements.Element;
import com.cpjd.roblu.utils.Text;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Data;

/**
 * This class stores information about either PIT, Prediction, or Match data.
 *
 * @since 3.5.0
 * @author Will Davies
 */
@Data
public class RTab implements Serializable, Comparable<RTab> {

    /**
     * RTabs are identified by their title, no duplicate titles!
     */
    private String title;
    private ArrayList<Element> elements;
    private ArrayList<String> edits;
    private final boolean redAlliance;
    private boolean won;
    private long time;

    private transient ArrayList<RTeam> teammates;
    private transient ArrayList<RTeam> opponents;

    public RTab(ArrayList<Element> elements, String title, boolean redAlliance, boolean won, long time) {
        this.elements = elements;
        this.title = title;
        this.redAlliance = redAlliance;
        this.won = won;
        this.time = time;
    }

    @Override
    public int compareTo(RTab tab) {
        return ((Long)Text.getMatchScore(title)).compareTo(Text.getMatchScore(tab.getTitle()));
    }
}
