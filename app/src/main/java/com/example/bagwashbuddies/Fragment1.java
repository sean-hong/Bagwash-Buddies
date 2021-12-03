package com.example.bagwashbuddies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Fragment1 extends Fragment {
    private final ArrayList<String> dorm1MachineIDs = new ArrayList<>();
    private final ArrayList<String> dorm1MachineClaimants = new ArrayList<>();
    private final ArrayList<Boolean> dorm1MachineIsClaimed = new ArrayList<>();
    private final ArrayList<Timestamp> dorm1MachineExpirationTimes = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final int claimLength = 60; // in minutes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment1_layout, container, false);

        ListView lv = (ListView) v.findViewById(R.id.dorm1_listview);
        dorm1MachineIDs.clear();
        generateListContent(lv);

        return v;
    }

    private void generateListContent(ListView lv) {
        // Query firestore db to get dorm1machines
        db.collection("laundry")
                .whereEqualTo("dormID", 1)
                .orderBy("machineID")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // DO Listview population in here

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            dorm1MachineIDs.add((String) document.getData().get("machineID"));
                            dorm1MachineClaimants.add((String) document.getData().get("claimant"));
                            dorm1MachineIsClaimed.add((boolean) document.getData().get("isClaimed"));
                            dorm1MachineExpirationTimes.add((Timestamp) document.getData().get("expirationTime"));
                        }

                        lv.setAdapter(new myListAdapter(getContext(), R.layout.list_item, dorm1MachineIDs));
                    } else {
                        Log.w("LOADMACHINES", "Error getting documents.", task.getException());
                    }
                });
    }

    public static class ViewHolder {
        ImageView thumbnail;
        TextView title;
        Button button;
        TextView claimant;
        TextView expiration;
        // List more views here as you add more to list_item.xml
    }

    private class myListAdapter extends ArrayAdapter<String> {
        private final int layout;

        public myListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewHolder;

            if (convertView == null) {
                // create viewholder
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_thumbnail);
                viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text);
                viewHolder.button = (Button) convertView.findViewById(R.id.list_item_button);
                viewHolder.claimant = (TextView) convertView.findViewById(R.id.list_item_claimant);
                viewHolder.expiration = (TextView) convertView.findViewById(R.id.list_item_expiration);

                viewHolder.title.setText("Machine ID: " + dorm1MachineIDs.get(position));
                viewHolder.claimant.setText("Claimed by: " + dorm1MachineClaimants.get(position));
                viewHolder.button.setBackgroundColor(Color.parseColor("#05a33c"));

                if (dorm1MachineIsClaimed.get(position)) {
                    // https://stackoverflow.com/questions/10032003/how-to-make-a-countdown-timer-in-android
                    long timeToExpire = (dorm1MachineExpirationTimes.get(position).getSeconds() - (Timestamp.now().getSeconds())) * 1000;

                    new CountDownTimer(timeToExpire, 1000) {
                        public void onTick(long duration) {
                            long Mmin = (duration / 1000) / 60;
                            long Ssec = (duration / 1000) % 60;

                            viewHolder.button.setBackgroundColor(Color.RED);
                            viewHolder.button.setClickable(false);

                            if (Ssec < 10) {
                                viewHolder.expiration.setText("" + Mmin + ":0" + Ssec);
                            } else {
                                viewHolder.expiration.setText("" + Mmin + ":" + Ssec);
                            }
                        }

                        public void onFinish() {
                            viewHolder.button.setBackgroundColor(Color.parseColor("#05a33c"));
                            viewHolder.button.setClickable(true);
                            viewHolder.expiration.setText("Available");
                            viewHolder.claimant.setText("Claimed by:");
                            resetMachine(position);
                        }
                    }.start();
                } else {
                    viewHolder.expiration.setText("Available");
                }

                // Set onclick behavior for button
                viewHolder.button.setOnClickListener(view -> {
                    // Get expiration time
                    Timestamp currTime = Timestamp.now();
                    Timestamp expirationTime = new Timestamp(currTime.getSeconds() + (claimLength * 60L), currTime.getNanoseconds());

                    DocumentReference machineRef = db.collection("laundry").document(dorm1MachineIDs.get(position));
                    machineRef
                            .update("isClaimed", true,
                                    "claimant", auth.getCurrentUser().getEmail(),
                                    "claimantEmail", auth.getCurrentUser().getEmail(),
                                    "expirationTime", expirationTime
                            )
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    dorm1MachineExpirationTimes.set(position, expirationTime);

                                    // Relaunch activity to load new data
                                    Intent intent = new Intent(getContext(), LaundryActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                } else {
                                    Log.w("Update Machine", "Error Updating Machine", task.getException());
                                }
                            });
                });

                convertView.setTag(viewHolder);
            } else {
                // retrieve viewholder
                mainViewHolder = (ViewHolder) convertView.getTag();
                mainViewHolder.title.setText("Machine " + position);
            }

            return convertView;
        }

        public void resetMachine(int position) {
            // First things first send notification email
            BackgroundMail.newBuilder(getContext())
                    .withUsername("username") // use your own email name
                    .withPassword("password") // use your own email password
                    .withMailto(dorm1MachineClaimants.get(position))
                    .withType(BackgroundMail.TYPE_PLAIN)
                    .withSubject("Washing Machine Claim Expired")
                    .withBody("Your laundry is done! Come claim your laundry from machine " + dorm1MachineIDs.get(position) + " in dorm 1!")
                    .withProcessVisibility(false)
                    .withOnSuccessCallback(() -> Log.d("Email", "Email Successfully Sent"))
                    .withOnFailCallback(() -> Log.d("Email", "Email Failed to Send"))
                    .send();

            DocumentReference machineRef = db.collection("laundry").document(dorm1MachineIDs.get(position));
            machineRef
                    .update("isClaimed", false,
                            "claimant", "",
                            "claimantEmail", ""
                    )
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Reset Machine", "Successfully reset machine");
                        } else {
                            Log.w("Reset Machine", "Error resetting Machine", task.getException());
                        }
                    });

            // Maybe restart activity here
        }
    }
}
