/*
 * Copyright (c) 2015 Johns Hopkins University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the copyright holder nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.jhu.cs.hinrg.dailyalert.android.utilities;

/**
 * Calculates the direction of a gesture/fling. Used by {@link GestureDetector}
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class GestureDirection {

    public enum UserGesture {
        SWIPE_RIGHT, SWIPE_LEFT, SWIPE_UP, SWIPE_DOWN, SWIPE_UNKNOWN
    }

    private final static int MAX_DISTANCE = 40;

    private float mStartX;
    private float mStartY;

    private float mEndX;
    private float mEndY;


    public GestureDirection(float x, float y) {
        mStartX = x;
        mStartY = y;
        mEndX = x;
        mEndY = y;
    }


    public void updateEndPoint(float x, float y) {
        mEndX = x;
        mEndY = y;
    }


    public float getStartX() {
        return mStartX;
    }


    public float getStartY() {
        return mStartY;
    }


    public UserGesture getDirection() {
        float dx = mEndX - mStartX;
        float dy = mEndY - mStartY;
        double distance = Math.hypot(dx, dy);

        if (distance < MAX_DISTANCE) {
            return UserGesture.SWIPE_UNKNOWN;
        }
        double angle = Math.acos(dx / distance);
        double limit = Math.PI / 6;
        if ((angle < limit || (angle > (Math.PI - limit)))) {
            if (dx > 0)
                return UserGesture.SWIPE_RIGHT;
            else
                return UserGesture.SWIPE_LEFT;

        }
        if ((angle > 2 * limit) && angle < 4 * limit) {
            if (dy > 0)
                return UserGesture.SWIPE_DOWN;
            else
                return UserGesture.SWIPE_UP;
        }
        return UserGesture.SWIPE_UNKNOWN;
    }
}
