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

package edu.jhu.cs.hinrg.dailyalert.android.logic;

import org.javarosa.core.model.FormIndex;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class HierarchyElement {
    private String mPrimaryText = "";
    private String mSecondaryText = "";
    private Drawable mIcon;
    private int mColor;
    int mType;
    FormIndex mFormIndex;
    ArrayList<HierarchyElement> mChildren;


    public HierarchyElement(String text1, String text2, Drawable bullet, int color, int type,
            FormIndex f) {
        mIcon = bullet;
        mPrimaryText = text1;
        mSecondaryText = text2;
        mColor = color;
        mFormIndex = f;
        mType = type;
        mChildren = new ArrayList<HierarchyElement>();
    }


    public String getPrimaryText() {
        return mPrimaryText;
    }


    public String getSecondaryText() {
        return mSecondaryText;
    }


    public void setPrimaryText(String text) {
        mPrimaryText = text;
    }


    public void setSecondaryText(String text) {
        mSecondaryText = text;
    }


    public void setIcon(Drawable icon) {
        mIcon = icon;
    }


    public Drawable getIcon() {
        return mIcon;
    }


    public FormIndex getFormIndex() {
        return mFormIndex;
    }


    public int getType() {
        return mType;
    }


    public void setType(int newType) {
        mType = newType;
    }


    public ArrayList<HierarchyElement> getChildren() {
        return mChildren;
    }


    public void addChild(HierarchyElement h) {
        mChildren.add(h);
    }


    public void setChildren(ArrayList<HierarchyElement> children) {
        mChildren = children;
    }


    public void setColor(int color) {
        mColor = color;
    }


    public int getColor() {
        return mColor;
    }

}
