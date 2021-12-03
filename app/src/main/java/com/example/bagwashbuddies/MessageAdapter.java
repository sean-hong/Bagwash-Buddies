package com.example.bagwashbuddies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<MessageHandler> MESSAGES;

    public MessageAdapter(ArrayList<MessageHandler> messages) {
        this.MESSAGES = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0: return new UserVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_msg, parent, false));
            case 1: return new BotVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_msg, parent, false));
            default: throw new IllegalArgumentException("Unexpected viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageHandler handler = MESSAGES.get(position);

        switch (handler.getSender()) {
            case "user":
                ((UserVH) holder).userTV.setText(handler.getMessage());
                break;
            case "bot":
                ((BotVH) holder).botTV.setText(handler.getMessage());
                break;
            default: break;
        }
    }

    @Override
    public int getItemCount() {
        return MESSAGES.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (MESSAGES.get(position).getSender()) {
            case "user": return 0;
            case "bot": return 1;
            default: return -1;
        }
    }

    public static class UserVH extends RecyclerView.ViewHolder {
        TextView userTV;

        public UserVH(@NonNull View itemView) {
            super(itemView);
            userTV = itemView.findViewById(R.id.userTV);
        }
    }

    public static class BotVH extends RecyclerView.ViewHolder {
        TextView botTV;

        public BotVH(@NonNull View itemView) {
            super(itemView);
            botTV = itemView.findViewById(R.id.botTV);
        }
    }
}
