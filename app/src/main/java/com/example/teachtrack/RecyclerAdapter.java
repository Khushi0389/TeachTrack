package com.example.teachtrack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    public LinearLayout linearLayout;
    ViewGroup parent;
    int viewType;

    String table,credit,s1,s2,courseName;
    List<Subject> subList;

    static List<Subject> dbList;//data that will be displayed in the recycler view
    static Context context;//current state of the application to access resources like db or shared preferences
    RecyclerAdapter(Context context, List<Subject> dbList ){
        this.dbList = new ArrayList<Subject>();
        this.context = context;
        this.dbList = dbList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        this.parent = parent; //into which the new View will be added
        this.viewType = viewType;//type of new view

        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_new, null);
        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;//to manage item views
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.SubjectName.setText(dbList.get(position).getSubjectName());
        holder.deptName.setText("Section "+dbList.get(position).getSection()+", "+dbList.get(position).getDeptName());
        holder.semester.setText(dbList.get(position).getSemester()+" Semester");
        holder.type.setText(dbList.get(position).getType());
        holder.SubjectId.setText(dbList.get(position).getSubjectId());
        // Display the correct credit value
        holder.credit.setText("Credit: " + dbList.get(position).getCredit());


        int x[] = {R.color.matColor5,R.color.matColor2,R.color.matColor4};
        int i=0;
        for(int j=0;j<getItemCount();j++){
            if(i==3)
                i=0;
            if(position==j){
                holder.relativeLayoutOption.setBackgroundColor(context.getResources().getColor(x[i]));
                holder.relativeLayout.setBackgroundColor(context.getResources().getColor(x[i]));
                //System.out.println("color2 "+x[j]);
            }
            i++;
        }

    }

    @Override
    public int getItemCount() {
        return dbList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener /*extends RecyclerView.ViewHolder implements View.OnClickListener*/ {

        public TextView SubjectName, deptName, type, semester, SubjectId,credit;
        public ImageButton imageButtonCheck, imageButtonGraph, imageButtonEdit, imageButtonDelete, imageButtonAttendance;
        RelativeLayout relativeLayout, relativeLayoutOption;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            SubjectName = (TextView) itemLayoutView.findViewById(R.id.textViewSubject);
            SubjectId = (TextView) itemLayoutView.findViewById(R.id.textViewSubjectId);
            deptName = (TextView) itemLayoutView.findViewById(R.id.textViewDept);
            type = (TextView) itemLayoutView.findViewById(R.id.textViewType);
            semester = (TextView) itemLayoutView.findViewById(R.id.textViewSemester);
            credit = (TextView) itemLayoutView.findViewById(R.id.textViewCreditValue); // Initialize credit TextView
            relativeLayout = (RelativeLayout) itemLayoutView.findViewById(R.id.relativeLayout);
            relativeLayoutOption = (RelativeLayout) itemLayoutView.findViewById(R.id.optionsLayout);

            imageButtonCheck = (ImageButton) itemLayoutView.findViewById(R.id.imageButtonCheck);
            imageButtonEdit = (ImageButton) itemLayoutView.findViewById(R.id.imageButtonEdit);
            imageButtonDelete = (ImageButton) itemLayoutView.findViewById(R.id.imageButtonDelete);
            imageButtonGraph = (ImageButton) itemLayoutView.findViewById(R.id.imageButtonGraph);
            imageButtonAttendance = (ImageButton) itemLayoutView.findViewById(R.id.imageButtonAttendance);

            imageButtonCheck.setOnClickListener(this);
            imageButtonDelete.setOnClickListener(this);
            imageButtonGraph.setOnClickListener(this);
            imageButtonEdit.setOnClickListener(this);
            imageButtonAttendance.setOnClickListener(this);


//            itemLayoutView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            final int position = getAdapterPosition();
            final DatabaseHandler databaseHandler = new DatabaseHandler(view.getContext());


            final View v = view;
            subList = databaseHandler.getDataFromDB();
            if (position < subList.size()) { // Check if position is within bounds
                Subject s = subList.get(position);
                String type = subList.get(position).getType();
                courseName = subList.get(position).getSubjectName();
                s1 = s.getSubjectId();
                s2 = s.getSection();

                s1 = s1.replaceAll("\\s", "_");
                s1 = s1.replaceAll("-", "_");

                table = s1 + "_" + s2;

                if (view.getId() == R.id.imageButtonCheck) {

                    final ProgressDialog progressDialog = ProgressDialog.show(context, "Please Wait. . .", "Updating UI");

                    new Thread() {
                        public void run() {
                            Intent i = new Intent(context, StudentActivity.class);

                            i.putExtra("name", table);
                            i.putExtra("course", courseName);
                            i.putExtra("credit", subList.get(position).getCredit());
                            i.putExtra("type", subList.get(position).getType());


                            context.startActivity(i);

                            progressDialog.dismiss();
                        }
                    }.start();

                } else if (view.getId() == R.id.imageButtonDelete) {
                    AlertDialog.Builder localBuilder = new AlertDialog.Builder(view.getContext());
                    localBuilder.setTitle("Alert");
                    localBuilder.setMessage("Do you want to delete?");
                    localBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                            //List<Subject> subList =  databaseHandler.getDataFromDB();
                            databaseHandler.deleteDataSubjects(dbList.get(position).getSubjectId(), dbList.get(position).getSection());
                            databaseHandler.dropTable(table);
                            databaseHandler.dropTable(table + "_attendance");
                            notifyItemRemoved(position);

                            // Notify the MainActivity to reload the data
                            ((MainActivity) context).loadDatabase();

                            ((MainActivity) context).onResume(); // Optionally, refresh the UI
                        }
                    });
                    localBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                            paramAnonymousDialogInterface.dismiss();

                        }
                    });
                    AlertDialog alert = localBuilder.create();
                    alert.show();

                    Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                    nbutton.setTextColor(Color.BLUE);
                    Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setTextColor(Color.BLUE);

                } else if (view.getId() == R.id.imageButtonEdit) {
                    //System.out.println(position);
                    Intent i = new Intent(context, EditSubject.class);
                    Bundle extras = new Bundle();
                    extras.putInt("pos", getAdapterPosition());
                    i.putExtras(extras);
                    context.startActivity(i);
                } else if (view.getId() == R.id.imageButtonGraph) {
                    final ProgressDialog progressDialog = ProgressDialog.show(context, "Please Wait. . .", "Updating UI");

                    new Thread() {
                        public void run() {
                            Intent i = new Intent(context, ShowRecord.class);
                            i.putExtra("table", table);
                            i.putExtra("credit", dbList.get(position).getCredit());
                            i.putExtra("type", dbList.get(position).getType());
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            context.startActivity(i);

                            progressDialog.dismiss();
                        }
                    }.start();
                } else if (view.getId() == R.id.imageButtonAttendance) {
                    Intent i = new Intent(context, AttendanceActivity.class);
                    i.putExtra("table", table);
//                i.putExtra("credit",dbList.get(position).getCredit());
//                i.putExtra("type",dbList.get(position).getType());
                    context.startActivity(i);
                }
            }
        }
    }
    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;//used to store the offset value for the bottom decoration

        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();//total number of items in the RecyclerView
            int position = parent.getChildPosition(view);//position of current item relative to its parent
            if (dataSize > 0 && position == dataSize - 1) { //If the current item is the last item in the RecyclerView
                outRect.set(0, 0, 0, mBottomOffset);
            } else {
                outRect.set(0, 0, 0, 0);
            }

        }
    }
}
