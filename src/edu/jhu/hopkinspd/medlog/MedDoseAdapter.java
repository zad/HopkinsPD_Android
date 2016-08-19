package edu.jhu.hopkinspd.medlog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;

public class MedDoseAdapter extends ArrayAdapter<ArrayList<String>>{
    protected static final String TAG = "MedDoseAdapter";
    private final Context context;
    private final ArrayList<ArrayList<String>> medDose;
    private HashMap<String, String[]> medDoseSelected;
    public static final String MedDoseSelectedPref = "MedDoseSelectedPref";

    public MedDoseAdapter(Context context, int res, 
            ArrayList<ArrayList<String>> medDose) 
    {
      super(context, res, medDose);
      this.context = context;
      this.medDose = medDose;
      loadMedDoseSelected();
      
              
    }
    
    
    
    private void loadMedDoseSelected() {
        GlobalApp app = GlobalApp.getApp();
        String selected = app.getStringPref(MedDoseSelectedPref, null);
        if(selected == null){
            medDoseSelected = new HashMap<String, String[]>();
            for(int i=0;i<medDose.size();i++){
                String[] item = new String[2];
                String med = medDose.get(i).get(0);
                item[0] = "unchecked";
                item[1] = "unknown";
                medDoseSelected.put(med, item);
            }    
        }
        else{
            medDoseSelected = new HashMap<String, String[]>();
            String[] splitted = selected.split("\\|");
            for (String medDose : splitted){
                String[] splitted2 = medDose.split(";");
                String[] item = new String[2];
                String med = splitted2[0];
                item[0] = splitted2[1];
                item[1] = splitted2[2];
                medDoseSelected.put(med, item);
            }
        }
        
    }
    
    private void saveMedDoseSelected(){
        StringBuilder selected = new StringBuilder("");
        for(String med: medDoseSelected.keySet()){
            if(selected.length()>0)
                selected.append("|");
            String[] result = medDoseSelected.get(med);
            selected.append(med).append(";").append(result[0]).append(";")
                .append(result[1]);
        }
        GlobalApp app = GlobalApp.getApp();
        Log.d(TAG, "saveMedDoseSelected:" + selected.toString());
        app.setStringPref(MedDoseSelectedPref, selected.toString());
        app.setDatePref(MedLogActivity.LastMedUpdateDatePref, new Date());
    }

    private boolean updateSelected(boolean checked, String med, String dose){
        String[] result = this.medDoseSelected.get(med);
        String pre_checked = result[0];
        result[0] = checked? "checked":"unchecked";
        String pre_dose = result[1];
        if(dose != null)
            // dose changed
            result[1] = dose;
        Log.d(TAG, "update: " + med + " " + Arrays.toString(result));
        if(pre_checked.compareTo(result[0])!=0 
                || pre_dose.compareTo(result[1])!=0)
            saveMedDoseSelected();
        return pre_checked.compareTo(result[0])!=0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final String med = medDose.get(position).get(0);
      final View rowView = inflater.inflate(R.layout.med_row, parent, false);
      
      final CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.medCheckBox);
      final Spinner spinner = (Spinner) rowView.findViewById(R.id.doseSpinner);
      final TextView text = (TextView) rowView.findViewById(R.id.textView_dose);
      checkbox.setText(medDose.get(position).get(0));
      
      checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(CompoundButton cb, boolean checked) {
            
            Log.d(TAG, "checked:" + checked +  " dose:" + spinner.getSelectedItem());
            CheckBox checkbox = (CheckBox) cb;
            
            
            boolean checkChanged = 
                    updateSelected(checked, checkbox.getText().toString(), null); 
                            
            updateView(med, checkbox, text, spinner);
            if(checkChanged){
                if(checkbox.isChecked()){
                    Toast.makeText(context, checkbox.getText() + " selected", 
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, checkbox.getText() + " canceled", 
                            Toast.LENGTH_SHORT).show();
                    
                }
            }
        }});
      
      List<String> doses = medDose.get(position)
              .subList(1, medDose.get(position).size());
      ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
              android.R.layout.simple_spinner_item, doses);
      spinnerAdapter.setDropDownViewResource(
              android.R.layout.simple_spinner_dropdown_item);
      
      // change the icon for Windows and iPhone
      spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

          @Override
          public void onItemSelected(AdapterView<?> parentView, 
                  View selectedItemView, int pos, long id) {
              
              String dose = parentView.getItemAtPosition(pos).toString();
              Log.d(TAG, "selected dose: " + med + " " + dose);
              updateSelected(checkbox.isChecked(), med, dose);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parentView) {
              // your code here
          }

      });
      spinner.setAdapter(spinnerAdapter);
      updateView(med, checkbox, text, spinner);
      return rowView;
    }

    private void updateView(String med, CheckBox checkbox, TextView text, 
            Spinner spinner) {
        String[] result = this.medDoseSelected.get(med);
        if(result[0].compareTo("checked")==0){
            if(!checkbox.isChecked())
                checkbox.setChecked(true);
            checkbox.setTextColor(Color.WHITE);
            text.setEnabled(true);
            spinner.setEnabled(true);
            
        }else{
            if(checkbox.isChecked())
                checkbox.setChecked(false);
            checkbox.setTextColor(Color.GRAY);
            text.setEnabled(false);
            spinner.setEnabled(false);
        }
        
        for(int i=0; i< spinner.getCount(); i++){
            String dose = (String) spinner.getItemAtPosition(i);
            if(dose.compareTo(result[1])==0)
            {
                if(spinner.getSelectedItemPosition() != i)
                    spinner.setSelection(i);
                break;
            }
        }
    }
    
    
}
