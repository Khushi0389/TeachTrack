package com.example.teachtrack;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class LabRecordFragment extends Fragment {

    public LabRecordFragment() {
        // Required empty public constructor
    }
    String tableName,credit;
    String[] columnName;
    double[][] d;
    Double[][] marks = new Double[20][1000];
    int rowLength;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checks if the fragment has received any arguments passed to it
        if (getArguments() != null) {
            //If arguments are present, this code retrieves specific values from the arguments bundle.
            tableName = getArguments().getString("table");
            credit = getArguments().getString("credit");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_attendance_record, container, false);
        // Inflate the layout for this fragment
        DatabaseHandler databaseHandler = new DatabaseHandler(getActivity());

        columnName = databaseHandler.columnName(tableName);
        rowLength = databaseHandler.rowNumber(tableName);
        d = databaseHandler.getCtMarks(tableName);
        HorizontalScrollView hsv = (HorizontalScrollView) v.findViewById(R.id.hsv2);

        LinearLayout root = new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.HORIZONTAL);


        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(4, -1));
        root.addView(linearLayout);

        databaseHandler = new DatabaseHandler(getActivity());

        Object[] obj = databaseHandler.getLabMarks(tableName);
        String[][] s = (String[][]) obj[0];
        Double[][] d = (Double[][]) obj[1];


        LinearLayout localLinearLayout;
        View view;
        TextView textView;

        String[] clmnName = {"ID","Name","Lab Performance",/*"Attendance",*/"Quiz","Viva"};

        for(int i=0;i<5;i++){
            localLinearLayout = new LinearLayout(getActivity());
            localLinearLayout.setOrientation(LinearLayout.VERTICAL);
            view = new View(getActivity());
            view.setLayoutParams(new LinearLayout.LayoutParams(-1, 4));
            //view.setBackgroundColor(Color.parseColor("#FFFFFF"));
            localLinearLayout.addView(view);
            textView = new TextView(getActivity());

            textView.setText(clmnName[i]);
            //textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            textView.setPadding(30, 10, 30, 10);
            textView.setBackground(getResources().getDrawable(R.drawable.cell_bg));
            textView.setGravity(17);

            localLinearLayout.addView(textView);

            for(int j=0;j<rowLength;j++){
                view = new View(getActivity());
                view.setLayoutParams(new LinearLayout.LayoutParams(-1, 4));
                //view.setBackgroundColor(Color.parseColor("#F01230"));
                localLinearLayout.addView(view);
                textView = new TextView(getActivity());
                textView.setPadding(30, 10, 30, 10);
                textView.setGravity(17);
                int l = 0;
                if(i==0){
                    textView.setText(s[i+1][j]);
                }else if(i==1){
                    textView.setText(s[i-1][j]);
                } else if(i==2){
                    double sum;
                    sum = 0.0;
                    for(int k=0;k<=12;k++){
                        if(d[k][j]!=-1){
                            System.out.println("sum = "+sum);
                            sum = sum + d[k][j];
                            l++;
                        }
                    }

                    if(Double.toString(sum).equals("NaN"))
                        sum = 0;

                    if(credit.equals("1.5"))
                        textView.setText(Double.toString(sum));
                    else
                        textView.setText(Double.toString(sum));
                }else if(i==3){
                    if(d[14][j]!=-1)
                        textView.setText(Double.toString(d[14][j]));
                }else{
                    if(d[15][j]!=-1)
                        textView.setText(Double.toString(d[15][j]));
                }

                //textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                textView.setBackground(getResources().getDrawable(R.drawable.cell_bg));
                localLinearLayout.addView(textView);
            }
            root.addView(localLinearLayout);
        }

        hsv.addView(root);



        return v;
    }

}
