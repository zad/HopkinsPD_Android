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

package edu.jhu.cs.hinrg.dailyalert.android.views;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;

import edu.jhu.cs.hinrg.dailyalert.android.widgets.IBinaryWidget;
import edu.jhu.cs.hinrg.dailyalert.android.widgets.QuestionWidget;
import edu.jhu.cs.hinrg.dailyalert.android.widgets.WidgetFactory;
import edu.jhu.hopkinspd.GlobalApp;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is
 * 
 * @author carlhartung
 */
public class ODKView extends ScrollView implements OnLongClickListener {

    // starter random number for view IDs
    private final static int VIEW_ID = 12345;  
    
    private final static String t = "CLASSNAME";
//    private final static int TEXTSIZE = 21;

    private LinearLayout mView;
    private LinearLayout.LayoutParams mLayout;
    private ArrayList<QuestionWidget> widgets;

    public final static String FIELD_LIST = "field-list";


    public ODKView(Context context, FormEntryPrompt questionPrompt,
            FormEntryCaption[] groups) {
        this(context, new FormEntryPrompt[] {
            questionPrompt
        }, groups);
    }


    public ODKView(Context context, FormEntryPrompt[] questionPrompts,
            FormEntryCaption[] groups) {
        super(context);

        widgets = new ArrayList<QuestionWidget>();

        mView = new LinearLayout(getContext());
        mView.setOrientation(LinearLayout.VERTICAL);
        mView.setGravity(Gravity.TOP);
//        mView.setPadding(0, 7, 0, 0);

        mLayout =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        mLayout.setMargins(GlobalApp.MARGIN_SIZE, 0, GlobalApp.MARGIN_SIZE, 0);

        // display which group you are in as well as the question

        addGroupText(groups);
        boolean first = true;
        int id = 0;
        for (FormEntryPrompt p : questionPrompts) {
            if (!first) {
                View divider = new View(getContext());
                divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                divider.setMinimumHeight(3);
                mView.addView(divider);
            } else {
                first = false;
            }

            // if question or answer type is not supported, use text widget
            QuestionWidget qw =
                WidgetFactory.createWidgetFromPrompt(p, getContext());
            qw.setLongClickable(true);
            qw.setOnLongClickListener(this);
            qw.setId(VIEW_ID + id++);

            widgets.add(qw);
            mView.addView((View) qw, mLayout);


        }

        addView(mView);

    }
    
    public void addChildView(View child, LinearLayout.LayoutParams params){
    	mView.addView(child, params);
    	
    }


    /**
     * @return a HashMap of answers entered by the user for this set of widgets
     */
    public HashMap<FormIndex, IAnswerData> getAnswers() {
        HashMap<FormIndex, IAnswerData> answers = new HashMap<FormIndex, IAnswerData>();
        Iterator<QuestionWidget> i = widgets.iterator();
        while (i.hasNext()) {
            /*
             * The FormEntryPrompt has the FormIndex, which is where the answer gets stored. The
             * QuestionWidget has the answer the user has entered.
             */
            QuestionWidget q = i.next();
            FormEntryPrompt p = q.getPrompt();
            answers.put(p.getIndex(), q.getAnswer());
        }

        return answers;
    }


    /**
     * // * Add a TextView containing the hierarchy of groups to which the question belongs. //
     */
    private void addGroupText(FormEntryCaption[] groups) {
        StringBuffer s = new StringBuffer("");
        String t = "";
        int i;
        // list all groups in one string
        for (FormEntryCaption g : groups) {
            i = g.getMultiplicity() + 1;
            t = g.getLongText();
            if (t != null) {
                s.append(t);
                if (g.repeats() && i > 0) {
                    s.append(" (" + i + ")");
                }
                s.append(" > ");
            }
        }

        // build view
        if (s.length() > 0) {
            TextView tv = new TextView(getContext());
            tv.setText(s.substring(0, s.length() - 3));
//            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXTSIZE - 7);
            tv.setPadding(0, 0, 0, 5);
            mView.addView(tv, mLayout);
        }
    }


    /**
     * TODO: Legacy function to bring keyboard up/down. should we still do that?
     * 
     * @param context
     */
    public void setFocus(Context context) {
        // TODO: implement me
    }


    /**
     * Called when another activity returns information to answer this question.
     * 
     * @param answer
     */
    public void setBinaryData(Object answer) {
        boolean set = false;
        for (QuestionWidget q : widgets) {
            if (q instanceof IBinaryWidget) {
                if (((IBinaryWidget) q).isWaitingForBinaryData()) {
                    ((IBinaryWidget) q).setBinaryData(answer);
                    set = true;
                    break;
                }
            }
        }

        if (!set) {
            Log.w(t, "Attempting to return data to a widget or set of widgets no looking for data");
        }
    }


    /**
     * @return true if the answer was cleared, false otherwise.
     */
    public boolean clearAnswer() {
        // If there's only one widget, clear the answer.
        // If there are more, then force a long-press to clear the answer.
        if (widgets.size() == 1 && !widgets.get(0).getPrompt().isReadOnly()) {
            widgets.get(0).clearAnswer();
            return true;
        } else {
            return false;
        }
    }


    public void clearAnswer(FormIndex index) {
        // TODO: implement me?
    }


    public ArrayList<QuestionWidget> getWidgets() {
        return widgets;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.view.ViewGroup#getFocusedChild()
     */
    @Override
    public View getFocusedChild() {
        // TODO Auto-generated method stub
        return super.getFocusedChild();
    }


    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        for (int i = 0; i < widgets.size(); i++) {
            QuestionWidget qw = widgets.get(i);
            qw.setOnFocusChangeListener(l);
        }
    }


    @Override
    public boolean onLongClick(View v) {

        Log.e("Carl", "that's a long click.  woot.");
        return false;
    }

}
