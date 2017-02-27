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

package edu.jhu.cs.hinrg.dailyalert.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import edu.jhu.cs.hinrg.dailyalert.android.activities.FormEntryActivity;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Widget that allows user to scan barcodes and add them to the form.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class BarcodeWidget extends QuestionWidget implements IBinaryWidget {
    private Button mActionButton;
    private TextView mStringAnswer;
    private boolean mWaitingForData;


    public BarcodeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        mWaitingForData = false;
        setOrientation(LinearLayout.VERTICAL);

        // set button formatting
        mActionButton = new Button(getContext());
        mActionButton.setText(getContext().getString(R.string.get_barcode));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalApp.APPLICATION_FONT_SIZE);
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setEnabled(!prompt.isReadOnly());

        // launch barcode capture intent on click
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent("com.google.zxing.client.android.SCAN");
                mWaitingForData = true;
                try {
                    ((Activity) getContext()).startActivityForResult(i,
                        FormEntryActivity.BARCODE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(),
                        getContext().getString(R.string.barcode_scanner_error), Toast.LENGTH_SHORT)
                            .show();
                    mWaitingForData = false;
                }
            }
        });

        // set text formatting
        mStringAnswer = new TextView(getContext());
        mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalApp.APPLICATION_FONT_SIZE);
        mStringAnswer.setGravity(Gravity.CENTER);

        String s = prompt.getAnswerText();
        if (s != null) {
            mActionButton.setText(getContext().getString(R.string.replace_barcode));
            mStringAnswer.setText(s);
        }
        // finish complex layout
        addView(mActionButton);
        addView(mStringAnswer);
    }


    @Override
    public void clearAnswer() {
        mStringAnswer.setText(null);
        mActionButton.setText(getContext().getString(R.string.get_barcode));
    }


    @Override
    public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    /**
     * Allows answer to be set externally in {@Link FormEntryActivity}.
     */
    @Override
    public void setBinaryData(Object answer) {
        mStringAnswer.setText((String) answer);
        mWaitingForData = false;
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    @Override
    public boolean isWaitingForBinaryData() {
        return mWaitingForData;
    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        super.setOnLongClickListener(l);
        mActionButton.setOnLongClickListener(l);
    }

}
