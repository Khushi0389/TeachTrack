package com.example.teachtrack;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;




public class RecyclerAdapterAttendance extends RecyclerView.Adapter<RecyclerAdapterAttendance.ViewHolder> {
    private String table;
    private Context context;
    private int l;
    private List<Integer> rollList = new ArrayList<>();
    public int[] attendanceArray;

    public RecyclerAdapterAttendance(Context context, String table, int l, int[] attendanceArray) {
        this.table = table;
        this.l = l;
        this.attendanceArray = attendanceArray;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_mark, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        // Load student IDs from the database
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        List<Student> studentList = databaseHandler.getStudentFromDB(table);

        for (int j = 0; j < studentList.size(); j++) {
            Student s = studentList.get(j);
            rollList.add(Integer.parseInt(s.getStudentID()));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String x = Integer.toString(rollList.get(position));
        holder.rollView.setText(x);

        // Set the background of the imageView based on the attendance status
        if (attendanceArray[position] == 1) {
            holder.imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.cross));
        } else if (attendanceArray[position] == 2) {
            holder.imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.late_sign));
        } else {
            holder.imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.check_mark_green));
        }
    }

    @Override
    public int getItemCount() {
        return l;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView rollView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            rollView = itemView.findViewById(R.id.rollView);
            imageView = itemView.findViewById(R.id.imageViewMark);

            // Set click listener on the CardView
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            // Update the attendance status when the CardView is clicked
            if (attendanceArray[position] == 0) {
                attendanceArray[position] = 1;
                imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.cross));
            } else if (attendanceArray[position] == 1) {
                attendanceArray[position] = 2;
                imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.late_sign));
            } else {
                attendanceArray[position] = 0;
                imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.check_mark_green));
            }
        }
    }

    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;

        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();
            int pos = parent.getChildPosition(view);
            if (dataSize > 0 && pos == dataSize - 1) {
                outRect.set(0, 0, 0, 0);
            } else {
                outRect.set(0, 0, 0, 0);
            }
        }
    }
}

