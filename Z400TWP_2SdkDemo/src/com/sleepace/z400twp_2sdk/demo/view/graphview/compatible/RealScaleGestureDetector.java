/**
 * This file is part of GraphView.
 * <p>
 * GraphView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * GraphView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with GraphView.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * <p>
 * Copyright Jonas Gehring
 */

package com.sleepace.z400twp_2sdk.demo.view.graphview.compatible;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ScaleGestureDetector;

@SuppressLint("NewApi")
public class RealScaleGestureDetector extends ScaleGestureDetector {
    public RealScaleGestureDetector(
            Context context,
            final com.sleepace.z400twp_2sdk.demo.view.graphview.compatible.ScaleGestureDetector fakeScaleGestureDetector,
            final com.sleepace.z400twp_2sdk.demo.view.graphview.compatible.ScaleGestureDetector.SimpleOnScaleGestureListener fakeListener) {
        super(
                context,
                new android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        return fakeListener.onScale(fakeScaleGestureDetector);
                    }
                });
    }
}
