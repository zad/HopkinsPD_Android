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
package edu.jhu.cs.hinrg.dailyalert.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import edu.jhu.cs.hinrg.dailyalert.android.views.MediaLayout;
import edu.jhu.hopkinspd.GlobalApp;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class QuestionWidget extends LinearLayout {
    
    @SuppressWarnings("unused")
    private final static String t = "QuestionWidget";

    private LinearLayout.LayoutParams mLayout;
    protected FormEntryPrompt mPrompt;
    
    //TODO:  These should probably be some kind of global preference?
//    private final static int TEXTSIZE = 14;
//    public final static int APPLICATION_FONTSIZE = 12;


    public QuestionWidget(Context context, FormEntryPrompt p) {
        super(context);

        mPrompt = p;
       
 
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.TOP);
//        setPadding(0, 7, 0, 0);

        mLayout =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
//        mLayout.setMargins(10, 0, 10, 0);
        
        addQuestionText(p);
        addHelpText(p);
    }
    
    public FormEntryPrompt getPrompt() {
        return mPrompt;
    }


    public abstract IAnswerData getAnswer();

    public abstract void clearAnswer();
    
    public abstract void setFocus(Context context);
     

    /**
     * Add a Views containing the question text, audio (if applicable), and image (if applicable).
     * To satisfy the RelativeLayout constraints, we add the audio first if it exists, then the
     * TextView to fit the rest of the space, then the image if applicable.
     */
    private void addQuestionText(FormEntryPrompt p) {
        String imageURI = p.getImageText();
        String audioURI = p.getAudioText();
        String videoURI = p.getSpecialFormQuestionText("video");

        // shown when image is clicked
        String bigImageURI = p.getSpecialFormQuestionText("big-image");

        // Add the text view. Textview always exists, regardless of whether there's text.
        TextView questionText = new TextView(getContext());
        questionText.setText(p.getLongText());
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalApp.QUESTION_FONT_SIZE);
        questionText.setTypeface(null, Typeface.BOLD);
//        questionText.setPadding(0, 0, 0, 7);
        questionText.setId(38475483); // assign random id
        
        // Wrap to the size of the parent view
        questionText.setHorizontallyScrolling(false);

        if (p.getLongText() == null) {
            questionText.setVisibility(GONE);
        }
            
        // Create the layout for audio, image, text
        MediaLayout mediaLayout = new MediaLayout(getContext());
        mediaLayout.setAVT(questionText, audioURI, imageURI, videoURI, bigImageURI);

        addView(mediaLayout, mLayout);
    }


    /**
     * Add a TextView containing the help text.
     */
    private void addHelpText(FormEntryPrompt p) {

        String s = p.getHelpText();

        if (s != null && !s.equals("")) {
            TextView tv = new TextView(getContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalApp.QUESTION_FONT_SIZE - 5);
            tv.setPadding(0, -5, 0, 7);
            // wrap to the widget of view
            tv.setHorizontallyScrolling(false);
            tv.setText(s);
            tv.setTypeface(null, Typeface.ITALIC);
            
            addView(tv, mLayout);
        }
    }
    
    
    

}
