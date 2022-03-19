package com.telltim.startup;

import java.util.Comparator;
import java.util.List;


/**
 * @author Tim.WJ
 */
public class Utils {

    private static final Comparator<AppBootUpTask> TASK_COMPARATOR = new Comparator<AppBootUpTask>() {
        @Override
        public int compare(AppBootUpTask lhs, AppBootUpTask rhs) {
            return lhs.getPriority() - rhs.getPriority();
        }
    };

    public static void sort(List<AppBootUpTask> tasks) {
        if (tasks.size() <= 1) {
            return;
        }
        tasks.sort(TASK_COMPARATOR);
    }
}
