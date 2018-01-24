package com.mikechoch.prism.helper;

import android.transition.Fade;
import android.transition.Transition;

/**
 * Created by mikechoch on 1/23/18.
 */

public class TransitionUtils {

    public static Transition makeEnterTransition() {
        Transition fade = new Fade();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        return fade;
    }

}
