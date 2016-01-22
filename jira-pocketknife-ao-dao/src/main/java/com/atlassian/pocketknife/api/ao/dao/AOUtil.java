package com.atlassian.pocketknife.api.ao.dao;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AOUtil {

    /**
     * Maps a List of business objects onto a list of ActiveObjects. A AOListMapper is required to calculate the
     * new/to be updated/to be deleted entities.
     */
    public static <T extends Entity, U> List<T> setListValues(ActiveObjects ao, AOListMapper<T, U> listMapper, List<U> newValues) {
        // load existing values
        T[] existingValues = listMapper.getExisting();
        List<T> toRemove = new ArrayList<T>(Arrays.asList(existingValues));

        // Create the new set of T, also setting position along the way
        List<T> resultValues = new ArrayList<T>();

        int index = 0;
        for (U u : newValues) {
            // check whether we got an existing one
            T t = listMapper.findExisting(existingValues, u);

            if (t != null) {
                // Make sure we don't delete that value
                toRemove.remove(t);

                // set the new values
                listMapper.setValues(t, u);

                // set position if we are positionable
                if (t instanceof Positionable) {
                    ((Positionable) t).setPos(index);
                }

                // then save
                t.save();
            } else {
                // create a new ActiveObject
                Map<String, Object> params = new HashMap<String, Object>();
                // get the create values from the mapper
                listMapper.addCreateValues(u, params);
                // add position if we are positionable
                Class<T> aoClass = listMapper.getActiveObjectClass();
                if (Positionable.class.isAssignableFrom(aoClass)) {
                    params.put("POS", index);
                }
                // now create
                t = ao.create(aoClass, params);
            }

            // call post create
            listMapper.postCreateUpdate(t, u);

            // add to result list and increment
            resultValues.add(t);
            index++;
        }

        // remove obsolete values
        for (T t : toRemove) {
            listMapper.preDelete(t);
            ao.delete(t);
        }

        // return updated swimlanes
        return resultValues;
    }

    public static <T extends Positionable> void sortPositionableArray(T[] elements) {
        Arrays.sort(elements, POS_COMPARATOR);
    }

    public static <T extends Positionable> void sortPositionableList(List<T> list) {
        Collections.sort(list, POS_COMPARATOR);
    }

    private static PositionableComparator POS_COMPARATOR = new PositionableComparator();

    static class PositionableComparator implements Comparator<Positionable> {
        @Override
        public int compare(Positionable arg0, Positionable arg1) {
            return arg0.getPos() - arg1.getPos();
        }

    }

}
