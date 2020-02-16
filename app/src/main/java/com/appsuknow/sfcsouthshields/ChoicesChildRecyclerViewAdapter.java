package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ChoicesChildRecyclerViewAdapter extends RecyclerView.Adapter<ChoicesChildRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<Choices> choices;
    private Context context;

    public ChoicesChildRecyclerViewAdapter(ArrayList<Choices> choices, Context context) {
        this.choices = choices;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View choiceChildView = LayoutInflater.from(parent.getContext()).inflate(R.layout.choice_child_listitem, parent, false);
        ViewHolder choiceChildViewHolder = new ViewHolder(choiceChildView);
        return choiceChildViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.choiceChildTextView.setText(choices.get(position).item);

        holder.choiceChildRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + choices.get(position));
                Intent intent = new Intent(context, ChoicesParentActivity.class);
                intent.putExtra("choice", choices.get(position).item);
                intent.putExtra("section", choices.get(position).section);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView choiceChildTextView;
        RelativeLayout choiceChildRelativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            choiceChildTextView = itemView.findViewById(R.id.text_view_choice_child);
            choiceChildRelativeLayout = itemView.findViewById(R.id.relative_layout_choice_child_cell);
        }

    }
}
