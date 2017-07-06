package com.cpjd.roblu.forms;

import com.cpjd.roblu.forms.elements.EBoolean;
import com.cpjd.roblu.forms.elements.ECheckbox;
import com.cpjd.roblu.forms.elements.EChooser;
import com.cpjd.roblu.forms.elements.ECounter;
import com.cpjd.roblu.forms.elements.EGallery;
import com.cpjd.roblu.forms.elements.ESlider;
import com.cpjd.roblu.forms.elements.EStopwatch;
import com.cpjd.roblu.forms.elements.ETextfield;
import com.cpjd.roblu.forms.elements.Element;
import com.cpjd.roblu.models.RTab;
import com.cpjd.roblu.models.RTeam;
import com.cpjd.roblu.teams.TeamsView;
import com.cpjd.roblu.utils.Text;

import java.util.ArrayList;

/**
 * ElementsProcessor generates a string that represents the data
 * for one element in a tabs.size() amount of matches.
 *
 * Some statistics will be generated for some elements
 *
 * @since 3.5.9
 * @author Will Davies
 */
public class ElementsProcessor {

    public static final int PIT = 0;
    public static final int PREDICTIONS = 1;
    public static final int MATCHES = 2;
    public static final int OTHER = 3;

    /**
     * Processes data for an element and provides a small string that represents its info.
     *
     * Team MUST be verified with team.verify(form) before being processed.
     *
     * @param team the team to be analyzed
     * @param set specific tabs to be processored, either ElementsProcessor.PIT, ElementsProcessor.PREDICTIONS, ElementsProcessor.MATCH, ElementsProcessor.OTHER
     * @param ID the ID of the element to analyze
     * @return relevance:text, relevance tells how high the team should be placed near the top, text is the subtitle text
     */
    public RTeam process(RTeam team, int set, int ID) {
        team.setSortRelevance(0);
        team.setFilter(TeamsView.SORT);

        String text = "";
        int occurrences = 0;
        double relevance = 0;
        double average = 0.0, min = 0.0, max = 0.0;

        /* PROCESS FOR SET == OTHER */
        if(set == OTHER) {
            for(int i = 2;  i < team.getTabs().size(); i++) {
                if(ID == 0) {
                    if(team.getTabs().get(i).isWon()) {
                        occurrences++;
                        relevance++;
                        text += "W"+ending(i, team.getTabs());
                    } else text += "L"+ending(i, team.getTabs());
                    if(i == team.getTabs().size() - 1) {
                        if(occurrences == 1) text = "1 match win\n"+text;
                        else text = occurrences+" match wins\n"+text;
                    }
                }
                else if(ID == 1) { // user is reseting sorting from within custom sorting
                    relevance++;
                }
            }
            team.setSortRelevance(relevance);
            team.setSortTip(text);
        }

        /* PROCESS FOR SET == PREDICTIONS || SET ++ PIT */
        else if(set == PIT || set == PREDICTIONS) {
            for(Element element : team.getTabs().get(set).getElements()) {
                if(element.getID() != ID) continue;

                if(element instanceof EBoolean) {
                    text = "Boolean: "+element.getTitle()+" is "+friendlyBoolean((EBoolean)element);
                    if(((EBoolean)element).getValue() == 1) relevance++;
                }
                else if(element instanceof ECounter) {
                    text = "Counter: "+element.getTitle()+" is "+friendlyCounter((ECounter)element);
                    relevance += ((ECounter)element).getCurrent();
                }
                else if(element instanceof ESlider) {
                    text = "Slider: "+element.getTitle()+" is "+ friendlySlider((ESlider) element);
                    relevance += ((ESlider)element).getCurrent();
                }
                else if(element instanceof EStopwatch) {
                    text = "Stopwatch: "+element.getTitle()+" is "+friendlyStopwatch((EStopwatch)element);
                    relevance += ((EStopwatch)element).getTime();
                }
                else if(element instanceof ETextfield) {
                    text = "Textfield: "+element.getTitle()+" has "+((ETextfield) element).getText().length()+" characters";
                    relevance += ((ETextfield)element).getText().length();
                }
                else if(element instanceof EGallery) {
                    text = "Gallery: "+element.getTitle()+" contains "+((EGallery)element).getImageIDs().size()+" images";
                    relevance += ((EGallery)element).getImageIDs().size();
                }
                else if(element instanceof ECheckbox) {
                    text = "Checkbox: "+element.getTitle()+ " values: "+friendlyCheckbox((ECheckbox)element);
                    relevance += getCheckedAmount((ECheckbox)element);
                }
                else if(element instanceof EChooser) text = "Chooser: "+element.getTitle()+" has value "+((EChooser)element).getValues().get(((EChooser)element).getSelected());

                text += " in "+friendlyMode(set);
                team.setSortRelevance(relevance);
                team.setSortTip(text);
            }
        }


        /* PROCESS FOR SET == MATCHES */
        else if(set == MATCHES) {
            for(int i = set; i < team.getTabs().size(); i++) {
                for(Element element : team.getTabs().get(i).getElements()) {
                    if(element.getID() != ID) continue;

                    if(element instanceof EBoolean) {
                        if(((EBoolean) element).getValue() == 1) {
                            occurrences++;
                            relevance++;
                        }
                        if(i == set) text = "Boolean: "+element.getTitle()+" is true in "+occurrences+" / "+(team.getTabs().size() - 2)+" matches\nRaw data: ";
                        text += friendlyBoolean((EBoolean)element)+ending(i, team.getTabs());
                    }
                    else if(element instanceof ECounter) {
                        int value = ((ECounter) element).getCurrent();
                        if(i == set) {
                            min = value;
                            max = value;
                        }
                        if(element.isModified()) { // Only add this value to stats if it's modified
                            if(value < min) min = value;
                            if(value > max) max = value;
                            average += (double)value / (double)numModified(team.getTabs(), ID);
                            relevance = average;
                        }
                        text += friendlyCounter((ECounter)element)+ending(i, team.getTabs());
                        if(i == team.getTabs().size() - 1) text = "Counter: "+element.getTitle()+" Average: "+ Text.round(average, 2) +" Min: "+(int)min+" Max: "+(int)max+"\nRaw data: "+text;
                    }
                    else if(element instanceof ESlider) {
                        int value = ((ESlider) element).getCurrent();
                        if(i == set) {
                            min = value;
                            max = value;
                        }
                        if(element.isModified()) { // Only add this value to stats if it's modified
                            if(value < min) min = value;
                            if(value > max) max = value;
                            average += (double)value / (double)numModified(team.getTabs(), ID);
                            relevance = average;
                        }
                        text += friendlySlider((ESlider)element)+ending(i, team.getTabs());
                        if(i == team.getTabs().size() - 1) text = "Slider: "+element.getTitle()+" Average: "+ Text.round(average, 2)+" Min: "+(int)min+" Max: "+(int)max+"\nRaw data: "+text;
                    }
                    else if(element instanceof EStopwatch) {
                        double value = ((EStopwatch) element).getTime();
                        if(i == set) {
                            min = value;
                            max = value;
                        }
                        if(element.isModified()) { // Only add this value to stats if it's modified
                            if(value < min) min = value;
                            if(value > max) max = value;
                            average += value / (double)numModified(team.getTabs(), ID);
                            relevance = average;
                        }
                        text += friendlyStopwatch((EStopwatch)element)+ending(i, team.getTabs());
                        if(i == team.getTabs().size() - 1) text = "Stopwatch: "+element.getTitle()+" Average: "+ Text.round(average, 2)+" Min: "+min+" Max: "+max+"\nRaw data: "+text;
                    }
                    else if(element instanceof ETextfield) {
                        int value = ((ETextfield) element).getText().length();
                        if(i == set) {
                            min = value;
                            max = value;
                        }
                        if(element.isModified()) { // Only add this value to stats if it's modified
                            if(value < min) min = value;
                            if(value > max) max = value;
                            average += (double)value / (double)numModified(team.getTabs(), ID);
                            relevance = average;
                        }
                        text += value+" chars"+ending(i, team.getTabs());
                        if(i == team.getTabs().size() - 1) text = "Textfield: "+element.getTitle()+" Average chars: "+ Text.round(average, 2)+" Min: "+min+" Max: "+max+"\nRaw data: "+text;
                    }
                    else if(element instanceof EGallery) {
                        int value = ((EGallery)element).getImageIDs().size();
                        if(i == set) {
                            min = value;
                            max = value;
                        }
                        if(element.isModified()) { // Only add this value to stats if it's modified
                            if(value < min) min = value;
                            if(value > max) max = value;
                            average += (double)value / (double)numModified(team.getTabs(), ID);
                            relevance = average;
                        }
                        text += value+" images"+ending(i, team.getTabs());
                        if(i == team.getTabs().size() - 1) text = "Gallery: "+element.getTitle()+" Average images: "+ Text.round(average, 2)+" Min: "+min+" Max: "+max+"\nRaw data: "+text;
                    }
                    else if(element instanceof ECheckbox) {
                        if(i == set) text = "Raw data: ";
                        text += friendlyCheckbox((ECheckbox)element)+ending(i, team.getTabs());
                        relevance+=getCheckedAmount((ECheckbox)element);
                    }
                    else if(element instanceof EChooser) {
                        if(element.isModified()) relevance++;
                        if(i == set) text = "Raw data: ";
                        text += ((EChooser)element).getValues().get(((EChooser)element).getSelected())+ending(i, team.getTabs());
                    }
                    break;
                }
            }
            team.setSortRelevance(relevance);
            team.setSortTip(text);
        }

        return team;
    }

    private String friendlyMode(int set) {
        switch(set) {
            case 0:
                return "PIT.";
            case 1:
                return "PREDICTIONS.";
            default:
                return "Matches.";
        }
    }

    private String friendlyBoolean(EBoolean element) {
        switch(element.getValue()) {
            case -1:
                return "N.O.";
            case 0:
                return "F";
            default:
                return "T";
        }
    }

    private String friendlyCounter(ECounter element) {
        if(!element.isModified()) return "N.O.";
        else return String.valueOf(element.getCurrent());
    }

    private String friendlySlider(ESlider element) {
        if(!element.isModified()) return "N.O.";
        else return String.valueOf(element.getCurrent());
    }

    private String friendlyStopwatch(EStopwatch element) {
        if(!element.isModified()) return "N.O.";
        else return String.valueOf(element.getTime()+"s");
    }
    private String friendlyCheckbox(ECheckbox element) {
        String temp = "(";
        for(int i = 0; i < element.getChecked().size(); i++) {
            if(i != element.getChecked().size() - 1) {
                if(element.getChecked().get(i)) temp +="T,";
                else temp+="F,";
            } else {
                if(element.getChecked().get(i)) temp+="T)";
                else temp +="F)";
            }
        }
        return temp;
    }

    private int getCheckedAmount(ECheckbox checkbox) {
        int num = 0;
        for(boolean b : checkbox.getChecked()) if(b) num++;
        return num;
    }

    private int numModified(ArrayList<RTab> tabs, int ID) {
        int num = 0;
        for(RTab tab : tabs) for(Element e : tab.getElements()) if(e.getID() == ID && e.isModified()) num++;
        return num;
    }


    private String ending(int i, ArrayList<RTab> tabs) {
        if(i != tabs.size() - 1) return ", ";
        else return " ";
    }

}