package com.example.bagwashbuddies;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Stack;

public class Fragment4 extends Fragment {
    private final ArrayList<MessageHandler> MESSAGES = new ArrayList<>();
    private final MessageAdapter ADAPTER = new MessageAdapter(MESSAGES);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText usrMsg;
    private RecyclerView chatView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment4_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatView = view.findViewById(R.id.chatMsgs);
        ImageButton sendMsgBtn = view.findViewById(R.id.sendMsgBtn);
        usrMsg = view.findViewById(R.id.msgTxt);

        RequestQueue reqQueue = Volley.newRequestQueue(requireContext());
        reqQueue.getCache().clear();

        sendMsgBtn.setOnClickListener(v -> {
            if (usrMsg.getText().toString().isEmpty()) {
                return;
            }

            sendMessage(usrMsg.getText().toString());

            usrMsg.setText("");
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        chatView.setLayoutManager(linearLayoutManager);
        chatView.setAdapter(ADAPTER);
    }

    private void sendMessage(String userMsg) {
        MESSAGES.add(new MessageHandler(userMsg, "user"));
        ADAPTER.notifyItemInserted(ADAPTER.getItemCount() - 1);
        chatView.scrollToPosition(ADAPTER.getItemCount() - 1);

        String url = null;

        try {
            url = "https://api.wit.ai/message?q=" + URLEncoder.encode(userMsg, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        ResponseHandler jsonObjectRequest = new ResponseHandler(Request.Method.GET, url, null, response -> {
            try {
                JSONObject entities = new JSONObject(response.getString("entities"));

                if (entities.length() == 0) {
                    throw new Exception("I couldn't find a response for that!");
                }

                JSONArray issues = new JSONArray(entities.getString("issues:issues"));

                if (issues.length() == 0) {
                    throw new Exception("I couldn't find a response for that!");
                }

                Stack<String> responses = new Stack<>();

                for (int i = 0; i < issues.length(); i++) {
                    JSONObject resp = new JSONObject(issues.get(i).toString());
                    String value = resp.getString("value");

                    if (responses.contains(value)) {
                        continue;
                    } else {
                        responses.push(value);

                        db.collection("issues")
                                .document(value)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String answer = (String) task.getResult().get("response");

                                        MESSAGES.add(new MessageHandler(answer.replace("\\n", "\n"), "bot"));

                                        ADAPTER.notifyItemInserted(ADAPTER.getItemCount() - 1);
                                        chatView.scrollToPosition(ADAPTER.getItemCount() - 1);
                                    } else {
                                        Log.d("ERROR IN ACCESSING FIREBASE", String.valueOf(task.getException()));
                                    }
                                });
                    }
                }
            } catch (Exception e) {
                MESSAGES.add(new MessageHandler(e.getMessage(), "bot"));
                ADAPTER.notifyItemInserted(ADAPTER.getItemCount() - 1);
                chatView.scrollToPosition(ADAPTER.getItemCount() - 1);
            }
        }, error -> {
            MESSAGES.add(new MessageHandler("Sorry, I don't have a response for that!", "bot"));
            ADAPTER.notifyItemInserted(ADAPTER.getItemCount() - 1);
            chatView.scrollToPosition(ADAPTER.getItemCount() - 1);
        });

        queue.add(jsonObjectRequest);
    }
}
