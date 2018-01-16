package com.cpjd.roblu.models.metrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Represents a list of items from which only one item can be selected
 * @see RMetric for more information
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RChooser extends RMetric {

    /**
     * Represents a list of possible items that can be selected
     */
    @NonNull
    private String[] values;
    /**
     * Represents the index of values[] which contains the currently selected item
     */
    private int selectedIndex;

    /**
     * Instantiates a chooser object
     * @param ID the unique identifier for this object
     * @param title object title
     * @param values String[] containing possible item selections
     * @param selectedIndex the index of the currently selected value within values[]
     */
    public RChooser(int ID, String title, String[] values, int selectedIndex) {
        super(ID, title);
        this.values = values;
        this.selectedIndex = selectedIndex;
    }

    @Override
    public String getFormDescriptor() {
        if(values == null) return "Type: Chooser\nItems: ";
        StringBuilder descriptor = new StringBuilder("Type: Chooser\nItems: ");
        for(String s : values) descriptor.append(s).append(", ");
        descriptor.append(" Default value index: ").append(selectedIndex);
        return descriptor.toString().substring(0, descriptor.toString().length() - 2);
    }

    @Override
    public RMetric clone() {
        RChooser chooser = new RChooser(ID, title, values, selectedIndex);
        chooser.setRequired(required);
        return chooser;
    }
}

